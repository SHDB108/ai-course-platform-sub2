package com.example.aicourse.client.impl;

import com.example.aicourse.client.AssessmentClient;
import com.example.aicourse.entity.StudyProgress;
import com.example.aicourse.repository.StudyProgressMapper;
import com.example.aicourse.vo.PageVO;
import com.example.aicourse.vo.analytics.KnowledgePointPerformanceVO;
import com.example.aicourse.vo.analytics.StudentCoursePerformanceVO;
import com.example.aicourse.vo.analytics.TaskCompletionSummaryVO;
import com.example.aicourse.vo.exam.ExamStatisticsVO;
import com.example.aicourse.vo.task.StudentTaskVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 本地数据库实现的 AssessmentClient - 混合数据源模式
 *
 * 设计理念：
 * 1. 【真实数据】从数据库查询已有表的数据（如 t_study_progress）
 * 2. 【模拟数据】对于缺失的表（如 t_task, t_exam），在内存中构造丰富的模拟数据
 *
 * 这样确保前端仪表盘能够正常显示，不会因为数据库表缺失而显示空白或全零。
 */
@Service
@Profile("!mock")
@Primary
@Slf4j
public class LocalDatabaseAssessmentClient implements AssessmentClient {

    private final StudyProgressMapper studyProgressMapper;

    @Autowired
    public LocalDatabaseAssessmentClient(StudyProgressMapper studyProgressMapper) {
        this.studyProgressMapper = studyProgressMapper;
        log.info("LocalDatabaseAssessmentClient initialized with hybrid data source mode");
    }

    /**
     * 【真实数据】查询学生课程表现 - 从数据库 t_study_progress 表获取
     */
    @Override
    public Optional<StudentCoursePerformanceVO> getStudentCoursePerformance(Long studentId, Long courseId) {
        log.debug("Fetching student course performance from database - studentId: {}, courseId: {}", studentId, courseId);

        // 从数据库查询真实的进度数据
        StudyProgress progress = studyProgressMapper.findByStudentAndCourse(studentId, courseId);

        StudentCoursePerformanceVO vo = new StudentCoursePerformanceVO();
        vo.setCourseId(courseId);
        vo.setCourseName("Course " + courseId);

        if (progress != null) {
            // 使用数据库中的真实进度数据
            Double completionRate = progress.getTotalProgress() != null
                ? progress.getTotalProgress().doubleValue()
                : 0.0;
            vo.setCompletionRate(completionRate);
            log.debug("Found real progress data: {}%", completionRate);
        } else {
            // 如果数据库中没有记录，返回初始值
            vo.setCompletionRate(0.0);
            log.debug("No progress record found, returning 0%");
        }

        // 平均分暂时使用模拟值（因为评分数据可能在其他表）
        vo.setAverageScore(88.5);

        return Optional.of(vo);
    }

    /**
     * 【模拟数据】任务完成统计 - 返回硬编码的模拟数据
     * 因为数据库中没有 t_task 表，这里构造丰富的示例数据
     */
    @Override
    public List<TaskCompletionSummaryVO> getTaskCompletionSummary(Long courseId) {
        log.debug("Generating mock task completion summary for courseId: {}", courseId);

        List<TaskCompletionSummaryVO> summaries = new ArrayList<>();

        // 模拟任务 1: 深度学习环境配置
        summaries.add(new TaskCompletionSummaryVO(
            101L,
            "深度学习环境配置",
            15,  // 总人数
            12,  // 已完成人数
            87.5 // 平均分
        ));

        // 模拟任务 2: 第一章测验
        summaries.add(new TaskCompletionSummaryVO(
            102L,
            "第一章知识测验",
            15,  // 总人数
            15,  // 已完成人数
            92.0 // 平均分
        ));

        // 模拟任务 3: 神经网络实验
        summaries.add(new TaskCompletionSummaryVO(
            103L,
            "神经网络基础实验",
            15,  // 总人数
            10,  // 已完成人数
            85.3 // 平均分
        ));

        // 模拟任务 4: 卷积神经网络项目
        summaries.add(new TaskCompletionSummaryVO(
            104L,
            "CNN图像识别项目",
            15,  // 总人数
            8,   // 已完成人数
            90.5 // 平均分
        ));

        log.debug("Generated {} mock task summaries", summaries.size());
        return summaries;
    }

    /**
     * 【模拟数据】知识点表现 - 当前返回空列表
     * 可以根据需要添加模拟数据
     */
    @Override
    public List<KnowledgePointPerformanceVO> getKnowledgePointPerformance(Long courseId, Long studentId) {
        log.debug("Knowledge point performance query - returning empty list (not implemented yet)");
        return Collections.emptyList();
    }

    /**
     * 【模拟数据】考试统计 - 返回硬编码的模拟统计数据
     * 因为数据库中没有 t_exam 表，这里构造示例数据确保前端显示正常
     */
    @Override
    public Optional<ExamStatisticsVO> getStudentExamStatistics(Long studentId, Long courseId) {
        log.debug("Generating mock exam statistics for studentId: {}, courseId: {}", studentId, courseId);

        ExamStatisticsVO stats = new ExamStatisticsVO();
        stats.setTotalExams(3);        // 总共3场考试
        stats.setCompletedExams(2);    // 已完成2场
        stats.setPendingExams(1);      // 待完成1场
        stats.setExpiredExams(0);      // 没有过期的
        stats.setAverageScore(91.5);   // 平均分91.5
        stats.setPassedExams(2);       // 通过2场
        stats.setFailedExams(0);       // 没有不及格的

        log.debug("Mock exam statistics: {} total, {} completed, avg score: {}",
            stats.getTotalExams(), stats.getCompletedExams(), stats.getAverageScore());

        return Optional.of(stats);
    }

    /**
     * 【模拟数据】学生任务列表 - 返回硬编码的模拟任务列表
     * 因为数据库中没有 t_task 表，这里构造丰富的示例任务
     * 包含不同状态（未提交、已提交、逾期）以便前端展示完整的任务管理界面
     */
    @Override
    public PageVO<StudentTaskVO> findStudentTasks(Long studentId, Long pageNum, Long pageSize, String status) {
        log.debug("Generating mock student tasks - studentId: {}, status: {}, page: {}/{}",
            studentId, status, pageNum, pageSize);

        // 构造丰富的模拟任务列表
        List<StudentTaskVO> allTasks = new ArrayList<>();

        // 任务 1: 深度学习环境配置 - 未提交，即将到期
        StudentTaskVO task1 = new StudentTaskVO();
        task1.setTaskId(101L);
        task1.setTaskTitle("深度学习环境配置");
        task1.setCourseName("深度学习基础与实践");
        task1.setTaskType("ASSIGNMENT");
        task1.setDeadline(LocalDateTime.now().plusDays(3));
        task1.setSubmissionStatus("NOT_SUBMITTED");
        task1.setIsOverdue(false);
        allTasks.add(task1);

        // 任务 2: 第一章知识测验 - 逾期未提交
        StudentTaskVO task2 = new StudentTaskVO();
        task2.setTaskId(102L);
        task2.setTaskTitle("第一章知识测验");
        task2.setCourseName("深度学习基础与实践");
        task2.setTaskType("QUIZ");
        task2.setDeadline(LocalDateTime.now().minusDays(5));
        task2.setSubmissionStatus("NOT_SUBMITTED");
        task2.setIsOverdue(true);
        allTasks.add(task2);

        // 任务 3: 神经网络实验 - 已提交并评分
        StudentTaskVO task3 = new StudentTaskVO();
        task3.setTaskId(103L);
        task3.setTaskTitle("神经网络基础实验");
        task3.setCourseName("深度学习基础与实践");
        task3.setTaskType("LAB");
        task3.setDeadline(LocalDateTime.now().minusDays(2));
        task3.setSubmissionStatus("SUBMITTED");
        task3.setIsOverdue(false);
        task3.setScore(new BigDecimal("92.5"));
        allTasks.add(task3);

        // 任务 4: CNN项目 - 未提交，还有时间
        StudentTaskVO task4 = new StudentTaskVO();
        task4.setTaskId(104L);
        task4.setTaskTitle("CNN图像识别项目");
        task4.setCourseName("深度学习基础与实践");
        task4.setTaskType("PROJECT");
        task4.setDeadline(LocalDateTime.now().plusDays(7));
        task4.setSubmissionStatus("NOT_SUBMITTED");
        task4.setIsOverdue(false);
        allTasks.add(task4);

        // 任务 5: Java多线程编程 - 已提交高分
        StudentTaskVO task5 = new StudentTaskVO();
        task5.setTaskId(105L);
        task5.setTaskTitle("Java多线程编程练习");
        task5.setCourseName("Java高级架构设计");
        task5.setTaskType("ASSIGNMENT");
        task5.setDeadline(LocalDateTime.now().minusDays(1));
        task5.setSubmissionStatus("SUBMITTED");
        task5.setIsOverdue(false);
        task5.setScore(new BigDecimal("95.0"));
        allTasks.add(task5);

        // 任务 6: Spring Boot项目 - 未提交，即将到期
        StudentTaskVO task6 = new StudentTaskVO();
        task6.setTaskId(106L);
        task6.setTaskTitle("Spring Boot微服务开发");
        task6.setCourseName("Java高级架构设计");
        task6.setTaskType("PROJECT");
        task6.setDeadline(LocalDateTime.now().plusDays(2));
        task6.setSubmissionStatus("NOT_SUBMITTED");
        task6.setIsOverdue(false);
        allTasks.add(task6);

        // 根据状态筛选任务
        List<StudentTaskVO> filteredTasks = allTasks;
        if (status != null && !status.isEmpty() && !status.equalsIgnoreCase("ALL")) {
            filteredTasks = allTasks.stream()
                .filter(task -> task.getSubmissionStatus().equalsIgnoreCase(status))
                .collect(Collectors.toList());
            log.debug("Filtered tasks by status '{}': {} tasks", status, filteredTasks.size());
        }

        // 简单的内存分页逻辑
        long total = filteredTasks.size();
        long totalPages = (total + pageSize - 1) / pageSize;

        // 计算分页范围
        int start = (int) ((pageNum - 1) * pageSize);
        int end = (int) Math.min(start + pageSize, total);

        List<StudentTaskVO> pagedTasks = start < total
            ? filteredTasks.subList(start, end)
            : Collections.emptyList();

        log.debug("Returning {} tasks (page {}/{}, total: {})",
            pagedTasks.size(), pageNum, totalPages, total);

        return new PageVO<>(pagedTasks, total, pageSize, pageNum, totalPages);
    }
}
