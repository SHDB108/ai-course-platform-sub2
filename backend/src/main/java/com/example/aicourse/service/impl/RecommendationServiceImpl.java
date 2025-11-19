package com.example.aicourse.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.aicourse.client.KnowledgeGraphClient;
import com.example.aicourse.dto.knowledge.RecommendationStatusUpdateDTO;
import com.example.aicourse.entity.LearningRecommendation;
import com.example.aicourse.entity.KnowledgePointProgress;
import com.example.aicourse.repository.LearningRecommendationMapper;
import com.example.aicourse.repository.KnowledgePointProgressMapper;
import com.example.aicourse.service.LlmService;
import com.example.aicourse.service.RecommendationService;
import com.example.aicourse.vo.analytics.KnowledgePointPerformanceVO;
import com.example.aicourse.vo.knowledge.LearningRecommendationVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class RecommendationServiceImpl implements RecommendationService {

    private final LearningRecommendationMapper recommendationMapper;
    private final LlmService llmService; // 【优化】依赖抽象的LlmService
    private final KnowledgePointProgressMapper knowledgePointProgressMapper;
    private final KnowledgeGraphClient knowledgeGraphClient;

    @Autowired
    public RecommendationServiceImpl(LearningRecommendationMapper recommendationMapper,
                                   LlmService llmService,
                                   KnowledgePointProgressMapper knowledgePointProgressMapper,
                                   KnowledgeGraphClient knowledgeGraphClient) {
        this.recommendationMapper = recommendationMapper;
        this.llmService = llmService;
        this.knowledgePointProgressMapper = knowledgePointProgressMapper;
        this.knowledgeGraphClient = knowledgeGraphClient;
    }

    @Override
    @Transactional
    public int generateRecommendationsForStudent(Long studentId, Long courseId) {
        log.info("开始为学生ID: {} 在课程ID: {} 中生成学习推荐。", studentId, courseId);

        // 1. 直接从knowledge_point_progress表获取学生的薄弱知识点
        List<KnowledgePointPerformanceVO> weakPoints = getWeakKnowledgePoints(studentId, courseId);

        if (weakPoints.isEmpty()) {
            log.info("学生ID: {} 当前无学习数据或表现良好，生成基础推荐。", studentId);
            // 为新学生或表现良好的学生生成基础学习推荐
            return generateBasicRecommendations(studentId, courseId);
        }

        // 【优化】不再直接删除旧推荐，而是将未处理的旧推荐标记为“过时”状态 (isDismissed = 2)
        // 0=活跃, 1=用户已忽略, 2=系统判定为过时
        LambdaUpdateWrapper<LearningRecommendation> updateWrapper = Wrappers.<LearningRecommendation>lambdaUpdate()
                .eq(LearningRecommendation::getStudentId, studentId)
                .eq(LearningRecommendation::getCourseId, courseId)
                .eq(LearningRecommendation::getIsDismissed, 0) // 只处理活跃的推荐
                .set(LearningRecommendation::getIsDismissed, 2);
        recommendationMapper.update(null, updateWrapper);

        int count = 0;
        for (KnowledgePointPerformanceVO weakPoint : weakPoints) {
            try {
                // 2. 【优化】通过LlmService生成个性化建议
                String prompt = buildRecommendationPrompt(weakPoint.getKnowledgePointName());
                String recommendationText = llmService.generateText(prompt);

                if (recommendationText == null || recommendationText.isBlank()) {
                    // LLM调用失败时的降级策略
                    recommendationText = "在学习“" + weakPoint.getKnowledgePointName() + "”时遇到困难了吗？别担心，回顾一下相关的课程材料，你一定能掌握它！";
                }

                // 3. 创建并存储新的推荐记录
                LearningRecommendation recommendation = new LearningRecommendation();
                recommendation.setStudentId(studentId);
                recommendation.setCourseId(courseId);
                recommendation.setRecommendationType("KNOWLEDGE_POINT");
                recommendation.setTargetId(weakPoint.getKnowledgePointId());
                recommendation.setReason(recommendationText);
                recommendation.setIsDismissed(0); // 新推荐为活跃状态
                recommendationMapper.insert(recommendation);
                count++;
            } catch (Exception e) {
                log.error("为知识点 '{}' 生成推荐时失败", weakPoint.getKnowledgePointName(), e);
            }
        }
        log.info("成功为学生ID: {} 生成了 {} 条新推荐。", studentId, count);
        return count;
    }

    /**
     * 为新学生或表现良好的学生生成基础学习推荐
     */
    private int generateBasicRecommendations(Long studentId, Long courseId) {
        log.info("为学生ID: {} 在课程ID: {} 生成基础推荐", studentId, courseId);
        
        try {
            // 标记旧推荐为过时
            LambdaUpdateWrapper<LearningRecommendation> updateWrapper = Wrappers.<LearningRecommendation>lambdaUpdate()
                    .eq(LearningRecommendation::getStudentId, studentId)
                    .eq(LearningRecommendation::getCourseId, courseId)
                    .eq(LearningRecommendation::getIsDismissed, 0)
                    .set(LearningRecommendation::getIsDismissed, 2);
            recommendationMapper.update(null, updateWrapper);

            // 生成通用的学习建议
            String[] basicRecommendations = {
                "开始学习本课程的基础知识点，建立扎实的理论基础",
                "定期复习已学内容，加深理解和记忆",
                "积极参与课堂讨论，多与同学交流学习心得"
            };

            int count = 0;
            for (String recommendation : basicRecommendations) {
                LearningRecommendation entity = new LearningRecommendation();
                entity.setStudentId(studentId);
                entity.setCourseId(courseId);
                entity.setRecommendationType("GENERAL");
                entity.setTargetId(null);
                entity.setReason(recommendation);
                entity.setIsDismissed(0);
                
                recommendationMapper.insert(entity);
                count++;
                log.info("已生成基础推荐: {}", recommendation);
            }

            return count;
        } catch (Exception e) {
            log.error("生成基础推荐时发生错误", e);
            return 0;
        }
    }

    /**
     * 直接从knowledge_point_progress表获取学生的薄弱知识点
     */
    private List<KnowledgePointPerformanceVO> getWeakKnowledgePoints(Long studentId, Long courseId) {
        log.info("从数据库直接获取学生ID: {} 在课程ID: {} 的薄弱知识点", studentId, courseId);
        
        // 查询学生在该课程下掌握度为"待加强"的知识点
        List<KnowledgePointProgress> progressList = knowledgePointProgressMapper.selectList(
            Wrappers.<KnowledgePointProgress>lambdaQuery()
                .eq(KnowledgePointProgress::getStudentId, studentId)
                .eq(KnowledgePointProgress::getCourseId, courseId)
                .in(KnowledgePointProgress::getMasteryLevel, Arrays.asList("LEARNING", "PARTIAL"))
        );
        
        if (CollectionUtils.isEmpty(progressList)) {
            log.info("学生ID: {} 在课程ID: {} 下无薄弱知识点记录", studentId, courseId);
            return Collections.emptyList();
        }
        
        // 转换为KnowledgePointPerformanceVO
        return progressList.stream().map(progress -> {
            // 获取知识点信息
            KnowledgePointPerformanceVO vo = new KnowledgePointPerformanceVO();
            vo.setKnowledgePointId(progress.getKnowledgePointId());
            knowledgeGraphClient.getKnowledgePoint(progress.getKnowledgePointId())
                    .ifPresentOrElse(kp -> vo.setKnowledgePointName(kp.getName()), () -> vo.setKnowledgePointName("未知知识点"));
            vo.setMasteryLevel(progress.getMasteryLevel());
            if (progress.getMasteryScore() != null) {
                vo.setAverageScore(progress.getMasteryScore().doubleValue());
            }
            
            log.info("找到薄弱知识点: {} (ID: {}), 掌握度: {}", vo.getKnowledgePointName(), vo.getKnowledgePointId(), vo.getMasteryLevel());
            return vo;
        }).toList();
    }

    private String buildRecommendationPrompt(String knowledgePointName) {
        return String.format("""
            你是一个循循善诱的AI助教。请为在知识点 "%s" 上遇到困难的学生，生成一句简短、友好且鼓励性的学习建议。直接返回建议文本即可，不要包含其它任何内容。
            """, knowledgePointName);
    }

    @Override
    public List<LearningRecommendationVO> getRecommendations(Long studentId, Long courseId, String recommendationType, Integer count) {
        log.info("为学生ID: {} 在课程ID: {} 获取最多 {} 条学习推荐。", studentId, courseId, count);

        int limit = (count == null || count <= 0) ? 5 : count;
        List<LearningRecommendationVO> recommendations = recommendationMapper.selectEnrichedRecommendations(studentId, courseId, limit);

        return CollectionUtils.isEmpty(recommendations) ? Collections.emptyList() : recommendations;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRecommendationStatus(Long recommendationId, RecommendationStatusUpdateDTO dto) {
        LearningRecommendation recommendation = recommendationMapper.selectById(recommendationId);
        if (recommendation != null) {
            if ("DISMISSED".equalsIgnoreCase(dto.getStatus())) {
                recommendation.setIsDismissed(1); // 1 = 用户已忽略
                recommendationMapper.updateById(recommendation);
            }
        }
    }
}
