package com.example.aicourse.client;

import com.example.aicourse.vo.PageVO;
import com.example.aicourse.vo.analytics.KnowledgePointPerformanceVO;
import com.example.aicourse.vo.analytics.StudentCoursePerformanceVO;
import com.example.aicourse.vo.analytics.TaskCompletionSummaryVO;
import com.example.aicourse.vo.exam.ExamStatisticsVO;
import com.example.aicourse.vo.task.StudentTaskVO;

import java.util.List;
import java.util.Optional;

/**
 * Abstraction over Subsystem 3 (评估专家、成绩与学情分析) so that the learning
 * navigator can consume assessment data without直接依赖其 Service/Mapper。
 *
 * The signatures reflect the statistics that Subsystem 2 already exposes or
 * plans to expose (课程表现、任务完成度、知识点掌握、考试统计等) and that were
 * previously obtained through direct repository/service calls.
 */
public interface AssessmentClient {

    /**
     * 获取某学生在某课程的整体表现概览（成绩趋势、任务完成率等）。
     */
    Optional<StudentCoursePerformanceVO> getStudentCoursePerformance(Long studentId, Long courseId);

    /**
     * 查询课程任务完成情况，用于学习驾驶舱/任务统计。
     */
    List<TaskCompletionSummaryVO> getTaskCompletionSummary(Long courseId);

    /**
     * 查询知识点维度的表现数据（薄弱点分析、推荐系统输入）。
     */
    List<KnowledgePointPerformanceVO> getKnowledgePointPerformance(Long courseId, Long studentId);

    /**
     * 获取学生的考试/测验统计数据（考试进度、通过率等）。
     */
    Optional<ExamStatisticsVO> getStudentExamStatistics(Long studentId, Long courseId);

    /**
     * 查询学生的任务列表，支持分页和按状态过滤。
     */
    PageVO<StudentTaskVO> findStudentTasks(Long studentId, Long pageNum, Long pageSize, String status);
}
