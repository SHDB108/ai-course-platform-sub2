package com.example.aicourse.client.impl;

import com.example.aicourse.client.KnowledgeGraphClient;
import com.example.aicourse.entity.Course;
import com.example.aicourse.entity.CourseStudent;
import com.example.aicourse.entity.KnowledgePoint;
import com.example.aicourse.entity.Teacher;
import com.example.aicourse.vo.KnowledgeGraphVO;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Profile("mock")
public class MockKnowledgeGraphClient implements KnowledgeGraphClient {
    @Override
    public List<CourseStudent> findEnrollmentsByStudent(Long studentId) {
        return Collections.emptyList();
    }

    @Override
    public long countEnrollmentsByStudent(Long studentId) {
        return 0;
    }

    @Override
    public List<Course> findCoursesByIds(Collection<Long> courseIds, String keyword) {
        return Collections.emptyList();
    }

    @Override
    public Optional<Course> getCourse(Long courseId) {
        return Optional.empty();
    }

    @Override
    public Optional<Teacher> getTeacher(Long teacherId) {
        return Optional.empty();
    }

    @Override
    public Optional<KnowledgePoint> getKnowledgePoint(Long knowledgePointId) {
        return Optional.empty();
    }

    @Override
    public KnowledgeGraphVO getCourseGraph(Long courseId) {
        KnowledgeGraphVO graph = new KnowledgeGraphVO();

        graph.getNodes().add(new KnowledgeGraphVO.NodeVO("kp1", "矩阵基础", 0));
        graph.getNodes().add(new KnowledgeGraphVO.NodeVO("kp2", "自注意力机制", 1));
        graph.getNodes().add(new KnowledgeGraphVO.NodeVO("kp3", "Transformer 编码器", 1));
        graph.getNodes().add(new KnowledgeGraphVO.NodeVO("kp4", "GNN 基础", 2));

        graph.getEdges().add(new KnowledgeGraphVO.EdgeVO("kp1", "kp2", "Prerequisite"));
        graph.getEdges().add(new KnowledgeGraphVO.EdgeVO("kp2", "kp3", "Builds On"));
        graph.getEdges().add(new KnowledgeGraphVO.EdgeVO("kp2", "kp4", "Related"));

        return graph;
    }
}
