package com.example.aicourse.client.impl;

import com.example.aicourse.client.KnowledgeGraphClient;
import com.example.aicourse.entity.Course;
import com.example.aicourse.entity.CourseStudent;
import com.example.aicourse.entity.KnowledgePoint;
import com.example.aicourse.entity.Teacher;
import com.example.aicourse.vo.KnowledgeGraphVO;
import com.example.aicourse.vo.resource.ResourceVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

@Service
@Profile("!mock")
@Slf4j
public class RestKnowledgeGraphClient implements KnowledgeGraphClient {

    private final RestTemplate restTemplate;
    private final String subsystem1BaseUrl;

    @Autowired
    public RestKnowledgeGraphClient(RestTemplate restTemplate,
                                    @Value("${subsystem1.api.url}") String subsystem1BaseUrl) {
        this.restTemplate = restTemplate;
        this.subsystem1BaseUrl = subsystem1BaseUrl;
    }

    @Override
    public List<CourseStudent> findEnrollmentsByStudent(Long studentId) {
        String url = subsystem1BaseUrl + "/api/v1/course-enrollments?studentId=" + studentId;
        try {
            ResponseEntity<List<CourseStudent>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<CourseStudent>>() {});
            List<CourseStudent> body = response.getBody();
            return body == null ? Collections.emptyList() : body;
        } catch (RestClientException e) {
            log.error("Failed to fetch enrollments for student {}", studentId, e);
            return Collections.emptyList();
        }
    }

    @Override
    public long countEnrollmentsByStudent(Long studentId) {
        String url = subsystem1BaseUrl + "/api/v1/course-enrollments/count?studentId=" + studentId;
        try {
            Long result = restTemplate.getForObject(url, Long.class);
            return result == null ? 0L : result;
        } catch (RestClientException e) {
            log.error("Failed to count enrollments for student {}", studentId, e);
            return 0L;
        }
    }

    @Override
    public List<Course> findCoursesByIds(Collection<Long> courseIds, String keyword) {
        if (CollectionUtils.isEmpty(courseIds)) {
            return Collections.emptyList();
        }
        StringJoiner joiner = new StringJoiner(",");
        courseIds.forEach(id -> joiner.add(String.valueOf(id)));
        StringBuilder url = new StringBuilder(subsystem1BaseUrl)
                .append("/api/v1/courses/batch?ids=")
                .append(joiner);
        if (keyword != null && !keyword.isBlank()) {
            url.append("&keyword=").append(keyword);
        }
        try {
            ResponseEntity<List<Course>> response = restTemplate.exchange(
                    url.toString(),
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Course>>() {});
            List<Course> body = response.getBody();
            return body == null ? Collections.emptyList() : body;
        } catch (RestClientException e) {
            log.error("Failed to fetch courses {} with keyword {}", courseIds, keyword, e);
            return Collections.emptyList();
        }
    }

    @Override
    public Optional<Course> getCourse(Long courseId) {
        String url = subsystem1BaseUrl + "/api/v1/courses/" + courseId;
        try {
            Course course = restTemplate.getForObject(url, Course.class);
            return Optional.ofNullable(course);
        } catch (RestClientException e) {
            log.error("Failed to fetch course {}", courseId, e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<Teacher> getTeacher(Long teacherId) {
        String url = subsystem1BaseUrl + "/api/v1/teachers/" + teacherId;
        try {
            Teacher teacher = restTemplate.getForObject(url, Teacher.class);
            return Optional.ofNullable(teacher);
        } catch (RestClientException e) {
            log.error("Failed to fetch teacher {}", teacherId, e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<KnowledgePoint> getKnowledgePoint(Long knowledgePointId) {
        String url = subsystem1BaseUrl + "/api/v1/knowledge-points/" + knowledgePointId;
        try {
            KnowledgePoint kp = restTemplate.getForObject(url, KnowledgePoint.class);
            return Optional.ofNullable(kp);
        } catch (RestClientException e) {
            log.error("Failed to fetch knowledge point {}", knowledgePointId, e);
            return Optional.empty();
        }
    }

    @Override
    public KnowledgeGraphVO getCourseGraph(Long courseId) {
        String url = subsystem1BaseUrl + "/api/v1/knowledge-graphs/course/" + courseId;
        try {
            KnowledgeGraphVO graph = restTemplate.getForObject(url, KnowledgeGraphVO.class);
            return graph == null ? new KnowledgeGraphVO() : graph;
        } catch (RestClientException e) {
            log.error("Failed to fetch knowledge graph for course {}", courseId, e);
            return new KnowledgeGraphVO();
        }
    }

    @Override
    public List<ResourceVO> getCourseResources(Long courseId) {
        String url = subsystem1BaseUrl + "/api/v1/courses/" + courseId + "/resources";
        try {
            ResponseEntity<List<ResourceVO>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<ResourceVO>>() {});
            List<ResourceVO> body = response.getBody();
            return body == null ? Collections.emptyList() : body;
        } catch (RestClientException e) {
            log.error("Failed to fetch resources for course {}", courseId, e);
            return Collections.emptyList();
        }
    }
}
