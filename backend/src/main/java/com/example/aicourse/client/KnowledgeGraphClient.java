package com.example.aicourse.client;

import com.example.aicourse.entity.Course;
import com.example.aicourse.entity.CourseStudent;
import com.example.aicourse.entity.KnowledgePoint;
import com.example.aicourse.entity.Teacher;
import com.example.aicourse.vo.KnowledgeGraphVO;
import com.example.aicourse.vo.resource.ResourceVO;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Facade for interacting with Subsystem 1 (课程大脑、知识图谱等) so that
 * Subsystem 2 no longer touches repositories/services directly.
 *
 * The methods mirror every current coupling point we identified in
 * {@code StudentServiceImpl}, {@code StudyProgressServiceImpl}, and
 * {@code RecommendationServiceImpl}.
 */
public interface KnowledgeGraphClient {

    /**
     * 获取学生的选课记录，用于课程列表/学习驾驶舱。
     */
    List<CourseStudent> findEnrollmentsByStudent(Long studentId);

    /**
     * 统计学生已选课程数量，供仪表板等场景使用。
     */
    long countEnrollmentsByStudent(Long studentId);

    /**
     * 批量查询课程元数据，并可结合关键词过滤。
     *
     * @param courseIds 选课得到的课程ID集合
     * @param keyword   课程名称/编码模糊匹配，可为 null
     */
    List<Course> findCoursesByIds(Collection<Long> courseIds, String keyword);

    /**
     * 查询单个课程详情（学习进度、分析等需要课程名称/元数据）。
     */
    Optional<Course> getCourse(Long courseId);

    /**
     * 查询授课教师信息，用于学生端展示。
     */
    Optional<Teacher> getTeacher(Long teacherId);

    /**
     * 查询知识点节点信息（名称、描述等），供推荐与知识进度展示使用。
     */
    Optional<KnowledgePoint> getKnowledgePoint(Long knowledgePointId);

    /**
     * 获取课程的知识图谱结构（节点+边）。
     */
    KnowledgeGraphVO getCourseGraph(Long courseId);

    /**
     * 获取课程下的教学资源列表，供视频统计等功能使用。
     */
    List<ResourceVO> getCourseResources(Long courseId);
}
