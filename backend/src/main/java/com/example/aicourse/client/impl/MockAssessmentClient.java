package com.example.aicourse.client.impl;

import com.example.aicourse.client.AssessmentClient;
import com.example.aicourse.vo.PageVO;
import com.example.aicourse.vo.analytics.KnowledgePointPerformanceVO;
import com.example.aicourse.vo.analytics.StudentCoursePerformanceVO;
import com.example.aicourse.vo.analytics.TaskCompletionSummaryVO;
import com.example.aicourse.vo.exam.ExamStatisticsVO;
import com.example.aicourse.vo.task.StudentTaskVO;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Profile("mock")
public class MockAssessmentClient implements AssessmentClient {
    @Override
    public Optional<StudentCoursePerformanceVO> getStudentCoursePerformance(Long studentId, Long courseId) {
        return Optional.empty();
    }

    @Override
    public List<TaskCompletionSummaryVO> getTaskCompletionSummary(Long courseId) {
        return Collections.emptyList();
    }

    @Override
    public List<KnowledgePointPerformanceVO> getKnowledgePointPerformance(Long courseId, Long studentId) {
        return Collections.emptyList();
    }

    @Override
    public Optional<ExamStatisticsVO> getStudentExamStatistics(Long studentId, Long courseId) {
        ExamStatisticsVO stats = new ExamStatisticsVO();
        stats.setTotalExams(5);
        stats.setCompletedExams(2);
        stats.setPendingExams(3);
        stats.setExpiredExams(0);
        stats.setAverageScore(88.5);
        stats.setPassedExams(2);
        stats.setFailedExams(0);
        return Optional.of(stats);
    }

    @Override
    public PageVO<StudentTaskVO> findStudentTasks(Long studentId, Long pageNum, Long pageSize, String status) {
        List<StudentTaskVO> tasks = buildMockTasks(studentId);

        List<StudentTaskVO> filtered = tasks.stream()
                .filter(task -> status == null || status.isBlank()
                        || status.equalsIgnoreCase(task.getSubmissionStatus()))
                .collect(Collectors.toList());

        long current = (pageNum == null || pageNum < 1) ? 1 : pageNum;
        long resolvedSize = (pageSize == null || pageSize < 1) ? filtered.size() : pageSize;
        if (resolvedSize <= 0) {
            resolvedSize = filtered.isEmpty() ? 1 : filtered.size();
        }

        long fromIndexLong = Math.max(0L, (current - 1) * resolvedSize);
        int fromIndex = (int) Math.min(filtered.size(), fromIndexLong);
        int toIndex = (int) Math.min(filtered.size(), fromIndexLong + resolvedSize);
        List<StudentTaskVO> pageRecords = filtered.subList(fromIndex, toIndex);
        long pages = resolvedSize == 0
                ? 0
                : (long) Math.ceil(filtered.isEmpty() ? 0.0 : (double) filtered.size() / resolvedSize);

        return new PageVO<>(pageRecords, filtered.size(), resolvedSize, current, pages);
    }

    private List<StudentTaskVO> buildMockTasks(Long studentId) {
        List<StudentTaskVO> tasks = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        StudentTaskVO submitted = new StudentTaskVO();
        submitted.setTaskId(101L);
        submitted.setTaskTitle("机器学习作业 1");
        submitted.setTaskDescription("完成梯度下降推导并提交报告");
        submitted.setTaskType("ASSIGNMENT");
        submitted.setDeadline(now.plusDays(2));
        submitted.setMaxScore(BigDecimal.valueOf(100));
        submitted.setSubmitType("ONLINE");
        submitted.setCourseId(201L);
        submitted.setCourseName("机器学习导论");
        submitted.setTeacherName("张老师");
        submitted.setSubmissionId(1001L);
        submitted.setSubmissionStatus("SUBMITTED");
        submitted.setSubmittedAt(now.minusHours(3));
        submitted.setScore(BigDecimal.valueOf(92));
        submitted.setFeedback("推导清晰，注意格式。");
        submitted.setIsOverdue(false);
        submitted.setIsSubmitted(true);
        submitted.setIsGraded(true);
        submitted.setDaysUntilDeadline(2);
        submitted.setTaskCreatedAt(now.minusDays(5));
        submitted.setTaskUpdatedAt(now.minusHours(2));
        tasks.add(submitted);

        StudentTaskVO overdue = new StudentTaskVO();
        overdue.setTaskId(102L);
        overdue.setTaskTitle("深度学习项目");
        overdue.setTaskDescription("实现ResNet并在CIFAR-10上训练");
        overdue.setTaskType("PROJECT");
        overdue.setDeadline(now.minusDays(1));
        overdue.setMaxScore(BigDecimal.valueOf(100));
        overdue.setSubmitType("FILE");
        overdue.setCourseId(202L);
        overdue.setCourseName("深度学习实战");
        overdue.setTeacherName("李老师");
        overdue.setSubmissionStatus("LATE");
        overdue.setIsOverdue(true);
        overdue.setIsSubmitted(false);
        overdue.setIsGraded(false);
        overdue.setDaysUntilDeadline(-1);
        overdue.setTaskCreatedAt(now.minusDays(15));
        overdue.setTaskUpdatedAt(now.minusDays(1));
        tasks.add(overdue);

        StudentTaskVO pending = new StudentTaskVO();
        pending.setTaskId(103L);
        pending.setTaskTitle("NLP 小测验");
        pending.setTaskDescription("Transformer 结构与Attention基础");
        pending.setTaskType("QUIZ");
        pending.setDeadline(now.plusDays(5));
        pending.setMaxScore(BigDecimal.valueOf(20));
        pending.setSubmitType("ONLINE");
        pending.setCourseId(203L);
        pending.setCourseName("自然语言处理入门");
        pending.setTeacherName("王老师");
        pending.setSubmissionStatus("NOT_SUBMITTED");
        pending.setIsOverdue(false);
        pending.setIsSubmitted(false);
        pending.setIsGraded(false);
        pending.setDaysUntilDeadline(5);
        pending.setTaskCreatedAt(now.minusDays(1));
        pending.setTaskUpdatedAt(now.minusHours(1));
        tasks.add(pending);

        return tasks;
    }
}
