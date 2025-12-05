package com.example.aicourse.client.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.aicourse.client.KnowledgeGraphClient;
import com.example.aicourse.entity.*;
import com.example.aicourse.repository.*;
import com.example.aicourse.utils.CurrentUserUtil;
import com.example.aicourse.vo.KnowledgeGraphVO;
import com.example.aicourse.vo.resource.ResourceVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 本地数据库版图谱客户端
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
    private final ResourceMapper resourceMapper;
    // ✅ 1. 新增：进度Mapper字段
    private final KnowledgePointProgressMapper progressMapper;

    @Autowired
    public LocalDatabaseKnowledgeGraphClient(CourseStudentMapper courseStudentMapper,
                                             CourseMapper courseMapper,
                                             TeacherMapper teacherMapper,
                                             KnowledgePointMapper knowledgePointMapper,
                                             ResourceMapper resourceMapper,
                                             // ✅ 2. 新增：构造函数注入
                                             KnowledgePointProgressMapper progressMapper) {
        this.courseStudentMapper = courseStudentMapper;
        this.courseMapper = courseMapper;
        this.teacherMapper = teacherMapper;
        this.knowledgePointMapper = knowledgePointMapper;
        this.resourceMapper = resourceMapper;
        this.progressMapper = progressMapper; // ✅ 3. 新增：赋值
    }

    @Override
    public KnowledgeGraphVO getCourseGraph(Long courseId) {
        log.debug("Fetching knowledge graph for course: {}", courseId);

        KnowledgeGraphVO graph = new KnowledgeGraphVO();

        if (courseId == null) {
            return graph;
        }

        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            log.warn("Course not found: {}", courseId);
            return graph;
        }

        // 获取当前学生的掌握度数据
        Map<Long, String> masteryMap = new HashMap<>();
        try {
            Long studentId = CurrentUserUtil.getCurrentUser().getId();
            // ✅ 现在这里可以正常使用 progressMapper 了
            List<KnowledgePointProgress> progressList = progressMapper.findByStudentAndCourse(studentId, courseId);

            masteryMap = progressList.stream()
                    .collect(Collectors.toMap(
                            KnowledgePointProgress::getKnowledgePointId,
                            KnowledgePointProgress::getMasteryLevel,
                            (existing, replacement) -> existing
                    ));
        } catch (Exception e) {
            log.warn("无法获取当前用户信息或进度，掌握度将显示为默认值: {}", e.getMessage());
        }

        String rootNodeId = "course_" + courseId;
        // 根节点
        graph.getNodes().add(new KnowledgeGraphVO.NodeVO(
                rootNodeId,
                course.getCourseName(),
                0,
                null
        ));

        // 查询知识点
        LambdaQueryWrapper<KnowledgePoint> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgePoint::getCourseId, courseId);
        wrapper.orderByAsc(KnowledgePoint::getLevel, KnowledgePoint::getId);
        List<KnowledgePoint> knowledgePoints = knowledgePointMapper.selectList(wrapper);

        if (knowledgePoints.isEmpty()) {
            return graph;
        }

        Map<Long, KnowledgePoint> kpMap = knowledgePoints.stream()
                .collect(Collectors.toMap(KnowledgePoint::getId, kp -> kp));

        // 构建节点
        for (KnowledgePoint kp : knowledgePoints) {
            String nodeId = "kp_" + kp.getId();
            Integer category = (kp.getParentId() == null) ? 1 : 2;
            String level = masteryMap.getOrDefault(kp.getId(), "UNKNOWN");

            graph.getNodes().add(new KnowledgeGraphVO.NodeVO(
                    nodeId,
                    kp.getName(),
                    category,
                    level
            ));
        }

        // 构建连线
        for (KnowledgePoint kp : knowledgePoints) {
            String targetNodeId = "kp_" + kp.getId();
            if (kp.getParentId() == null) {
                graph.getEdges().add(new KnowledgeGraphVO.EdgeVO(rootNodeId, targetNodeId, "contains"));
            } else {
                String parentNodeId = "kp_" + kp.getParentId();
                if (kpMap.containsKey(kp.getParentId())) {
                    graph.getEdges().add(new KnowledgeGraphVO.EdgeVO(parentNodeId, targetNodeId, "prerequisite"));
                } else {
                    graph.getEdges().add(new KnowledgeGraphVO.EdgeVO(rootNodeId, targetNodeId, "contains"));
                }
            }
        }

        return graph;
    }

    // --- 其他方法保持原样 ---
    @Override
    public List<CourseStudent> findEnrollmentsByStudent(Long studentId) {
        return courseStudentMapper.selectList(new LambdaQueryWrapper<CourseStudent>().eq(CourseStudent::getStudentId, studentId));
    }

    @Override
    public long countEnrollmentsByStudent(Long studentId) {
        return courseStudentMapper.selectCount(new LambdaQueryWrapper<CourseStudent>().eq(CourseStudent::getStudentId, studentId));
    }

    @Override
    public List<Course> findCoursesByIds(Collection<Long> courseIds, String keyword) {
        if (courseIds == null || courseIds.isEmpty()) return Collections.emptyList();
        List<Course> courses = courseMapper.selectBatchIds(courseIds);
        if (keyword != null && !keyword.isBlank()) {
            String lowerKeyword = keyword.toLowerCase();
            return courses.stream().filter(c -> c.getCourseName().toLowerCase().contains(lowerKeyword)).collect(Collectors.toList());
        }
        return courses;
    }

    @Override
    public Optional<Course> getCourse(Long courseId) {
        return Optional.ofNullable(courseMapper.selectById(courseId));
    }

    @Override
    public Optional<Teacher> getTeacher(Long teacherId) {
        return Optional.ofNullable(teacherMapper.selectById(teacherId));
    }

    @Override
    public Optional<KnowledgePoint> getKnowledgePoint(Long knowledgePointId) {
        return Optional.ofNullable(knowledgePointMapper.selectById(knowledgePointId));
    }

    @Override
    public List<ResourceVO> getCourseResources(Long courseId) {
        return resourceMapper.selectList(new LambdaQueryWrapper<com.example.aicourse.entity.Resource>()
                        .eq(com.example.aicourse.entity.Resource::getCourseId, courseId))
                .stream().map(r -> {
                    ResourceVO vo = new ResourceVO();
                    BeanUtils.copyProperties(r, vo);
                    return vo;
                }).collect(Collectors.toList());
    }
}