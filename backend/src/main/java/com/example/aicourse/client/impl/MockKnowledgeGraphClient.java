package com.example.aicourse.client.impl;

import com.example.aicourse.client.KnowledgeGraphClient;
import com.example.aicourse.entity.Course;
import com.example.aicourse.entity.CourseStudent;
import com.example.aicourse.entity.KnowledgePoint;
import com.example.aicourse.entity.Teacher;
import com.example.aicourse.vo.KnowledgeGraphVO;
import com.example.aicourse.vo.resource.ResourceVO;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Profile("mock")
public class MockKnowledgeGraphClient implements KnowledgeGraphClient {

    // 1. 模拟数据库中的选课记录 (Student ID = 1)
    @Override
    public List<CourseStudent> findEnrollmentsByStudent(Long studentId) {
        // 对应数据库：t_course_student 表
        List<CourseStudent> list = new ArrayList<>();

        // 课程 101
        CourseStudent cs1 = new CourseStudent();
        cs1.setId(1L);
        cs1.setStudentId(studentId); // 假设当前登录的就是这个学生
        cs1.setCourseId(101L);
        cs1.setEnrollmentDate(LocalDateTime.now());
        list.add(cs1);

        // 课程 102
        CourseStudent cs2 = new CourseStudent();
        cs2.setId(2L);
        cs2.setStudentId(studentId);
        cs2.setCourseId(102L);
        cs2.setEnrollmentDate(LocalDateTime.now());
        list.add(cs2);

        return list;
    }

    @Override
    public long countEnrollmentsByStudent(Long studentId) {
        return 2; // 模拟有2门课
    }

    // 2. 模拟数据库中的课程详情
    @Override
    public List<Course> findCoursesByIds(Collection<Long> courseIds, String keyword) {
        List<Course> allCourses = new ArrayList<>();

        // 课程 101
        Course c1 = new Course();
        c1.setId(101L);
        c1.setCourseName("深度学习基础与实践");
        c1.setCourseCode("AI-2024-01");
        c1.setDescription("本课程涵盖神经网络基础、CNN、RNN及Transformer前沿模型，结合PyTorch实战。");
        c1.setCredits(4);
        c1.setHours(64);
        c1.setTeacherId(2L);
        c1.setCapacity(100);
        c1.setGmtCreate(LocalDateTime.now());
        allCourses.add(c1);

        // 课程 102
        Course c2 = new Course();
        c2.setId(102L);
        c2.setCourseName("Java高级架构设计");
        c2.setCourseCode("CS-2024-05");
        c2.setDescription("深入理解JVM原理、并发编程、微服务架构（Spring Cloud）实战。");
        c2.setCredits(3);
        c2.setHours(48);
        c2.setTeacherId(2L);
        c2.setCapacity(80);
        c2.setGmtCreate(LocalDateTime.now());
        allCourses.add(c2);

        // 简单的过滤逻辑
        return allCourses.stream()
                .filter(c -> courseIds.contains(c.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Course> getCourse(Long courseId) {
        // 复用上面的逻辑
        return findCoursesByIds(Collections.singletonList(courseId), null).stream().findFirst();
    }

    @Override
    public Optional<Teacher> getTeacher(Long teacherId) {
        Teacher t = new Teacher();
        t.setId(teacherId);
        t.setName("李教授"); // 对应数据库 t_teacher
        t.setTitle("教授");
        return Optional.of(t);
    }

    @Override
    public Optional<KnowledgePoint> getKnowledgePoint(Long knowledgePointId) {
        // 简单模拟
        KnowledgePoint kp = new KnowledgePoint();
        kp.setId(knowledgePointId);
        kp.setName("模拟知识点-" + knowledgePointId);
        return Optional.of(kp);
    }

    @Override
    public KnowledgeGraphVO getCourseGraph(Long courseId) {
        // 返回一个简单的图结构，防止前端图表报错
        KnowledgeGraphVO graph = new KnowledgeGraphVO();
        graph.getNodes().add(new KnowledgeGraphVO.NodeVO("root", "课程根节点", 0,null));
        return graph;
    }

    @Override
    public List<ResourceVO> getCourseResources(Long courseId) {
        return Collections.emptyList();
    }
}