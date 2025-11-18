package com.example.aicourse.client.impl;

import com.example.aicourse.client.AssessmentClient;
import com.example.aicourse.vo.analytics.KnowledgePointPerformanceVO;
import com.example.aicourse.vo.analytics.StudentCoursePerformanceVO;
import com.example.aicourse.vo.analytics.TaskCompletionSummaryVO;
import com.example.aicourse.vo.exam.ExamStatisticsVO;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
        return Optional.empty();
    }
}
