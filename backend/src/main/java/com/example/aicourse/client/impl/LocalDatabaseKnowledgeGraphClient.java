package com.example.aicourse.client.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.aicourse.client.KnowledgeGraphClient;
import com.example.aicourse.entity.Course;
import com.example.aicourse.entity.CourseStudent;
import com.example.aicourse.entity.KnowledgePoint;
import com.example.aicourse.entity.Teacher;
import com.example.aicourse.repository.CourseMapper;
import com.example.aicourse.repository.CourseStudentMapper;
import com.example.aicourse.repository.KnowledgePointMapper;
import com.example.aicourse.repository.TeacherMapper;
import com.example.aicourse.vo.KnowledgeGraphVO;
import com.example.aicourse.vo.resource.ResourceVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Local database implementation of KnowledgeGraphClient.
 * Fetches data directly from the local MySQL database instead of making HTTP calls.
 * This implementation is used in non-mock profiles (dev, prod) and is marked as @Primary
 * to override the RestKnowledgeGraphClient.
 */
@Service
@Profile("!mock")
@Primary
@Slf4j
public class LocalDatabaseKnowledgeGraphClient implements KnowledgeGraphClient {

    private final CourseStudentMapper courseStudentMapper;
    private final CourseMapper courseMapper;
    private final TeacherMapper teacherMapper;
    private final KnowledgePointMapper knowledgePointMapper;

    @Autowired
    public LocalDatabaseKnowledgeGraphClient(CourseStudentMapper courseStudentMapper,
                                             CourseMapper courseMapper,
                                             TeacherMapper teacherMapper,
                                             KnowledgePointMapper knowledgePointMapper) {
        this.courseStudentMapper = courseStudentMapper;
        this.courseMapper = courseMapper;
        this.teacherMapper = teacherMapper;
        this.knowledgePointMapper = knowledgePointMapper;
    }

    @Override
    public List<CourseStudent> findEnrollmentsByStudent(Long studentId) {
        log.debug("Fetching enrollments for student: {}", studentId);
        LambdaQueryWrapper<CourseStudent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseStudent::getStudentId, studentId);
        return courseStudentMapper.selectList(wrapper);
    }

    @Override
    public long countEnrollmentsByStudent(Long studentId) {
        log.debug("Counting enrollments for student: {}", studentId);
        LambdaQueryWrapper<CourseStudent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseStudent::getStudentId, studentId);
        return courseStudentMapper.selectCount(wrapper);
    }

    @Override
    public List<Course> findCoursesByIds(Collection<Long> courseIds, String keyword) {
        log.debug("Fetching courses by IDs: {} with keyword: {}", courseIds, keyword);
        if (courseIds == null || courseIds.isEmpty()) {
            return Collections.emptyList();
        }

        // Fetch courses by IDs
        List<Course> courses = courseMapper.selectBatchIds(courseIds);

        // Filter by keyword if provided
        if (keyword != null && !keyword.isBlank()) {
            String lowerKeyword = keyword.toLowerCase();
            courses = courses.stream()
                    .filter(course ->
                        (course.getCourseName() != null && course.getCourseName().toLowerCase().contains(lowerKeyword)) ||
                        (course.getCourseCode() != null && course.getCourseCode().toLowerCase().contains(lowerKeyword))
                    )
                    .collect(Collectors.toList());
        }

        return courses;
    }

    @Override
    public Optional<Course> getCourse(Long courseId) {
        log.debug("Fetching course: {}", courseId);
        if (courseId == null) {
            return Optional.empty();
        }
        Course course = courseMapper.selectById(courseId);
        return Optional.ofNullable(course);
    }

    @Override
    public Optional<Teacher> getTeacher(Long teacherId) {
        log.debug("Fetching teacher: {}", teacherId);
        if (teacherId == null) {
            return Optional.empty();
        }
        Teacher teacher = teacherMapper.selectById(teacherId);
        return Optional.ofNullable(teacher);
    }

    @Override
    public Optional<KnowledgePoint> getKnowledgePoint(Long knowledgePointId) {
        log.debug("Fetching knowledge point: {}", knowledgePointId);
        if (knowledgePointId == null) {
            return Optional.empty();
        }
        KnowledgePoint kp = knowledgePointMapper.selectById(knowledgePointId);
        return Optional.ofNullable(kp);
    }

    @Override
    public KnowledgeGraphVO getCourseGraph(Long courseId) {
        log.debug("Fetching knowledge graph for course: {}", courseId);

        // Query all knowledge points for this course
        LambdaQueryWrapper<KnowledgePoint> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgePoint::getCourseId, courseId);
        List<KnowledgePoint> knowledgePoints = knowledgePointMapper.selectList(wrapper);

        KnowledgeGraphVO graph = new KnowledgeGraphVO();

        // Build nodes from knowledge points
        for (KnowledgePoint kp : knowledgePoints) {
            KnowledgeGraphVO.NodeVO node = new KnowledgeGraphVO.NodeVO(
                String.valueOf(kp.getId()),
                kp.getName(),
                kp.getLevel()
            );
            graph.getNodes().add(node);
        }

        // Build edges from parent-child relationships
        for (KnowledgePoint kp : knowledgePoints) {
            if (kp.getParentId() != null && kp.getParentId() > 0) {
                KnowledgeGraphVO.EdgeVO edge = new KnowledgeGraphVO.EdgeVO(
                    String.valueOf(kp.getParentId()),
                    String.valueOf(kp.getId()),
                    "prerequisite"
                );
                graph.getEdges().add(edge);
            }
        }

        log.debug("Built knowledge graph with {} nodes and {} edges",
                  graph.getNodes().size(), graph.getEdges().size());

        return graph;
    }

    @Override
    public List<ResourceVO> getCourseResources(Long courseId) {
        log.debug("Fetching resources for course: {}", courseId);

        // Since we might not have a Resource entity/table yet in this codebase,
        // return an empty list to avoid crashes
        // TODO: Implement this when Resource entity and mapper are available
        log.warn("getCourseResources not fully implemented - returning empty list. " +
                 "Implement this when Resource entity/mapper are available.");
        return Collections.emptyList();
    }
}
