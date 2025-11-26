package com.example.aicourse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.aicourse.client.AssessmentClient;
import com.example.aicourse.entity.CapabilityDimension;
import com.example.aicourse.entity.CapabilityRule;
import com.example.aicourse.entity.StudentCapabilityScore;
import com.example.aicourse.repository.CapabilityDimensionMapper;
import com.example.aicourse.repository.CapabilityRuleMapper;
import com.example.aicourse.repository.StudentCapabilityScoreMapper;
import com.example.aicourse.service.CapabilityService;
import com.example.aicourse.vo.MyDashboardVO;
import com.example.aicourse.vo.PageVO;
import com.example.aicourse.vo.task.StudentTaskVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 能力模型服务实现类
 */
@Service
public class CapabilityServiceImpl implements CapabilityService {

    private final CapabilityRuleMapper capabilityRuleMapper;
    private final CapabilityDimensionMapper capabilityDimensionMapper;
    private final StudentCapabilityScoreMapper studentCapabilityScoreMapper;
    private final AssessmentClient assessmentClient;

    @Autowired
    public CapabilityServiceImpl(
            CapabilityRuleMapper capabilityRuleMapper,
            CapabilityDimensionMapper capabilityDimensionMapper,
            StudentCapabilityScoreMapper studentCapabilityScoreMapper,
            AssessmentClient assessmentClient) {
        this.capabilityRuleMapper = capabilityRuleMapper;
        this.capabilityDimensionMapper = capabilityDimensionMapper;
        this.studentCapabilityScoreMapper = studentCapabilityScoreMapper;
        this.assessmentClient = assessmentClient;
    }

    @Override
    @Transactional
    public List<MyDashboardVO.CapabilityScoreVO> calculateAndGetCapabilityScores(Long studentId) {
        // 1. 获取所有启用的规则 (Map<TaskType, DimensionId>)
        List<CapabilityRule> rules = capabilityRuleMapper.selectList(
                Wrappers.<CapabilityRule>lambdaQuery()
                        .eq(CapabilityRule::getStatus, 1)
        );

        Map<String, Long> taskTypeToDimensionMap = rules.stream()
                .collect(Collectors.toMap(
                        CapabilityRule::getTaskType,
                        CapabilityRule::getDimensionId,
                        (existing, replacement) -> existing // 如果有重复，保留第一个
                ));

        // 2. 获取所有启用的维度信息 (Map<DimensionId, DimensionName>)
        List<CapabilityDimension> dimensions = capabilityDimensionMapper.selectList(
                Wrappers.<CapabilityDimension>lambdaQuery()
                        .eq(CapabilityDimension::getStatus, 1)
        );

        Map<Long, String> dimensionIdToNameMap = dimensions.stream()
                .collect(Collectors.toMap(
                        CapabilityDimension::getId,
                        CapabilityDimension::getDimensionName,
                        (existing, replacement) -> existing
                ));

        // 3. 获取学生的所有已评分任务
        // 由于 AssessmentClient.findStudentTasks 返回分页数据，我们需要获取足够多的数据
        // 这里使用一个较大的 pageSize (如 1000) 来获取所有任务，或者多次调用直到获取完所有数据
        List<StudentTaskVO> allTasks = fetchAllStudentTasks(studentId);

        // 4. 按维度计算分数
        // Map<DimensionId, List<Score>>
        Map<Long, List<Double>> dimensionScoresMap = new HashMap<>();

        for (StudentTaskVO task : allTasks) {
            // 只统计已批改且有分数的任务
            if (task.getScore() == null || !Boolean.TRUE.equals(task.getIsGraded())) {
                continue;
            }

            String taskType = task.getTaskType();
            Long dimensionId = taskTypeToDimensionMap.get(taskType);

            if (dimensionId == null) {
                // 该任务类型没有对应的能力维度规则，跳过
                continue;
            }

            // 将分数添加到对应维度的列表中
            dimensionScoresMap
                    .computeIfAbsent(dimensionId, k -> new ArrayList<>())
                    .add(task.getScore().doubleValue());
        }

        // 5. 计算每个维度的平均分，并保存/更新到数据库
        List<MyDashboardVO.CapabilityScoreVO> result = new ArrayList<>();

        for (CapabilityDimension dimension : dimensions) {
            Long dimensionId = dimension.getId();
            String dimensionName = dimension.getDimensionName();

            List<Double> scores = dimensionScoresMap.get(dimensionId);
            Double avgScore;
            Integer taskCount;

            if (scores != null && !scores.isEmpty()) {
                // 计算平均分
                avgScore = scores.stream()
                        .mapToDouble(Double::doubleValue)
                        .average()
                        .orElse(0.0);
                taskCount = scores.size();
            } else {
                // 没有任务数据，尝试读取旧数据
                StudentCapabilityScore oldScore = studentCapabilityScoreMapper.selectOne(
                        Wrappers.<StudentCapabilityScore>lambdaQuery()
                                .eq(StudentCapabilityScore::getStudentId, studentId)
                                .eq(StudentCapabilityScore::getDimensionId, dimensionId)
                );

                if (oldScore != null) {
                    avgScore = oldScore.getScore();
                    taskCount = oldScore.getTaskCount();
                } else {
                    avgScore = 0.0;
                    taskCount = 0;
                }
            }

            // 6. 保存或更新到数据库
            StudentCapabilityScore scoreEntity = studentCapabilityScoreMapper.selectOne(
                    Wrappers.<StudentCapabilityScore>lambdaQuery()
                            .eq(StudentCapabilityScore::getStudentId, studentId)
                            .eq(StudentCapabilityScore::getDimensionId, dimensionId)
            );

            if (scoreEntity == null) {
                // 新增
                scoreEntity = new StudentCapabilityScore();
                scoreEntity.setStudentId(studentId);
                scoreEntity.setDimensionId(dimensionId);
                scoreEntity.setScore(avgScore);
                scoreEntity.setTaskCount(taskCount);
                scoreEntity.setLastCalculatedAt(LocalDateTime.now());
                studentCapabilityScoreMapper.insert(scoreEntity);
            } else {
                // 更新
                scoreEntity.setScore(avgScore);
                scoreEntity.setTaskCount(taskCount);
                scoreEntity.setLastCalculatedAt(LocalDateTime.now());
                studentCapabilityScoreMapper.updateById(scoreEntity);
            }

            // 7. 添加到返回结果
            MyDashboardVO.CapabilityScoreVO vo = new MyDashboardVO.CapabilityScoreVO(
                    dimensionName,
                    avgScore
            );
            result.add(vo);
        }

        return result;
    }

    /**
     * 获取学生的所有任务（处理分页）
     * 这里使用一个较大的 pageSize 来简化实现
     * 如果任务数量特别多，建议改为循环分页获取
     */
    private List<StudentTaskVO> fetchAllStudentTasks(Long studentId) {
        // 方案1: 使用足够大的 pageSize (适用于任务数量不会特别多的情况)
        Long pageSize = 1000L;
        PageVO<StudentTaskVO> taskPage = assessmentClient.findStudentTasks(
                studentId,
                1L,        // pageNum
                pageSize,  // pageSize
                null       // status: 获取所有状态的任务
        );

        if (taskPage == null || taskPage.getRecords() == null) {
            return new ArrayList<>();
        }

        // 如果实际数据量可能超过 pageSize，可以在这里添加分页循环逻辑
        // 例如：
        // List<StudentTaskVO> allTasks = new ArrayList<>();
        // long totalPages = taskPage.getPages();
        // for (long page = 1; page <= totalPages; page++) {
        //     PageVO<StudentTaskVO> currentPage = assessmentClient.findStudentTasks(studentId, page, pageSize, null);
        //     allTasks.addAll(currentPage.getRecords());
        // }
        // return allTasks;

        return taskPage.getRecords();
    }
}
