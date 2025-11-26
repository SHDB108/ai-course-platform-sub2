package com.example.aicourse.client.impl;

import com.example.aicourse.client.AssessmentClient;
import com.example.aicourse.vo.PageVO;
import com.example.aicourse.vo.analytics.KnowledgePointPerformanceVO;
import com.example.aicourse.vo.analytics.StudentCoursePerformanceVO;
import com.example.aicourse.vo.analytics.TaskCompletionSummaryVO;
import com.example.aicourse.vo.exam.ExamStatisticsVO;
import com.example.aicourse.vo.task.StudentTaskVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Profile("!mock")
public class RestAssessmentClient implements AssessmentClient {

    private final RestTemplate restTemplate;
    private final String subsystem3BaseUrl;

    @Autowired
    public RestAssessmentClient(RestTemplate restTemplate,
                                @Value("${subsystem3.api.url}") String subsystem3BaseUrl) {
        this.restTemplate = restTemplate;
        this.subsystem3BaseUrl = subsystem3BaseUrl;
    }

    @Override
    public Optional<StudentCoursePerformanceVO> getStudentCoursePerformance(Long studentId, Long courseId) {
        String url = subsystem3BaseUrl + "/api/v1/analytics/student/" + studentId + "/course/" + courseId + "/performance-overview";
        StudentCoursePerformanceVO vo = restTemplate.getForObject(url, StudentCoursePerformanceVO.class);
        return Optional.ofNullable(vo);
    }

    @Override
    public List<TaskCompletionSummaryVO> getTaskCompletionSummary(Long courseId) {
        String url = subsystem3BaseUrl + "/api/v1/analytics/courses/" + courseId + "/task-completion-summary";
        ResponseEntity<List<TaskCompletionSummaryVO>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<TaskCompletionSummaryVO>>() {});
        List<TaskCompletionSummaryVO> body = response.getBody();
        return body == null ? Collections.emptyList() : body;
    }

    @Override
    public List<KnowledgePointPerformanceVO> getKnowledgePointPerformance(Long courseId, Long studentId) {
        StringBuilder url = new StringBuilder(subsystem3BaseUrl)
                .append("/api/v1/analytics/courses/")
                .append(courseId)
                .append("/knowledge-points/performance");
        if (studentId != null) {
            url.append("?studentId=").append(studentId);
        }
        ResponseEntity<List<KnowledgePointPerformanceVO>> response = restTemplate.exchange(
                url.toString(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<KnowledgePointPerformanceVO>>() {});
        List<KnowledgePointPerformanceVO> body = response.getBody();
        return body == null ? Collections.emptyList() : body;
    }

    @Override
    public Optional<ExamStatisticsVO> getStudentExamStatistics(Long studentId, Long courseId) {
        StringBuilder url = new StringBuilder(subsystem3BaseUrl)
                .append("/api/v1/students/")
                .append(studentId)
                .append("/exam-statistics");
        if (courseId != null) {
            url.append("?courseId=").append(courseId);
        }
        ExamStatisticsVO vo = restTemplate.getForObject(url.toString(), ExamStatisticsVO.class);
        return Optional.ofNullable(vo);
    }

    @Override
    public PageVO<StudentTaskVO> findStudentTasks(Long studentId, Long pageNum, Long pageSize, String status) {
        long current = (pageNum == null || pageNum < 1) ? 1 : pageNum;
        long size = (pageSize == null || pageSize < 1) ? 10 : pageSize;

        StringBuilder url = new StringBuilder(subsystem3BaseUrl)
                .append("/api/v1/tasks/student/")
                .append(studentId)
                .append("?page=").append(current)
                .append("&size=").append(size);
        if (status != null && !status.isBlank()) {
            url.append("&status=").append(status);
        }

        ResponseEntity<PageVO<StudentTaskVO>> response = restTemplate.exchange(
                url.toString(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<PageVO<StudentTaskVO>>() {});
        PageVO<StudentTaskVO> body = response.getBody();
        return body == null ? new PageVO<>(Collections.emptyList(), 0, size, current, 0) : body;
    }
}
