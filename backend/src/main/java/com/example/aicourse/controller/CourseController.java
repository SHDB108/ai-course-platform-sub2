package com.example.aicourse.controller;

import com.example.aicourse.client.KnowledgeGraphClient;
import com.example.aicourse.entity.Course;
import com.example.aicourse.entity.Teacher;
import com.example.aicourse.utils.Result;
import com.example.aicourse.vo.course.CourseVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * API 4.2: 获取课程详情
 */
@RestController
@RequestMapping("/api/v1/courses")
public class CourseController {

    private final KnowledgeGraphClient knowledgeGraphClient;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    public CourseController(KnowledgeGraphClient knowledgeGraphClient) {
        this.knowledgeGraphClient = knowledgeGraphClient;
    }

    /**
     * API 4.2: 获取课程详情
     * @param courseId 课程ID
     * @return 课程详细信息
     */
    @GetMapping("/{courseId}")
    public Result<CourseVO> getCourseDetail(@PathVariable Long courseId) {
        // 1. 获取课程实体
        Optional<Course> courseOpt = knowledgeGraphClient.getCourse(courseId);

        if (courseOpt.isEmpty()) {
            return Result.error("Course not found");
        }

        Course course = courseOpt.get();

        // 2. 转换为 VO
        CourseVO courseVO = convertToVO(course);

        return Result.ok(courseVO);
    }

    /**
     * 将 Course 实体转换为 CourseVO，并填充关联信息（如教师名）
     */
    private CourseVO convertToVO(Course course) {
        CourseVO vo = new CourseVO();

        // 1. 复制基础属性
        BeanUtils.copyProperties(course, vo);

        // 2. 设置别名字段以匹配前端期望
        vo.setName(course.getCourseName());
        vo.setDuration(course.getHours());
        vo.setMaxStudents(course.getCapacity());

        // 3. 补全教师信息 (关键修复点)
        if (course.getTeacherId() != null) {
            knowledgeGraphClient.getTeacher(course.getTeacherId())
                    .map(Teacher::getName)
                    .ifPresent(vo::setTeacherName);
        } else {
            vo.setTeacherName("未知教师");
        }

        // 4. 设置默认状态
        vo.setStatus("ACTIVE");

        // 5. 设置默认分类
        vo.setCategoryName("通用课程");

        // 6. 设置默认日期（防止前端显示空白）
        // 也可以尝试从 semester 字段解析，这里先给个默认范围
        vo.setStartDate("2024-09-01");
        vo.setEndDate("2025-01-15");

        // 7. 设置时间字段格式化
        if (course.getGmtCreate() != null) {
            vo.setCreatedAt(course.getGmtCreate().format(DATE_FORMATTER));
        }
        if (course.getGmtModified() != null) {
            vo.setUpdatedAt(course.getGmtModified().format(DATE_FORMATTER));
        }

        return vo;
    }
}