package com.example.aicourse.vo.course;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * API 4.1 获取课程列表, API 4.2 获取课程详情响应
 */
@Data
public class CourseVO {
    private Long id;
    private String courseCode; // 课程代码
    private String courseName; // 课程名称
    private String name; // 课程名称别名，匹配前端期望
    private String description; // 课程描述
    private Integer credits; // 学分
    private Integer duration; // 课程时长，匹配前端期望
    private Integer hours; // 课程时长（小时）
    private String semester; // 开课学期
    private String department; // 开课学院
    private Long teacherId; // 授课教师ID
    private String teacherName; // 授课教师姓名 (需要联查或在Service层填充)
    private String classroom; // 上课地点
    private String scheduleTime; // 上课时间
    private Integer capacity; // 课程容量
    private Integer maxStudents; // 最大学生数别名，匹配前端期望
    private Integer enrolledStudents; // 已选学生数
    private String status; // 课程状态，匹配前端期望
    private String categoryName; // 课程分类名称，匹配前端期望
    private String startDate; // 开始日期，匹配前端期望
    private String endDate; // 结束日期，匹配前端期望
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModified;
    private String createdAt; // 创建时间别名，匹配前端期望
    private String updatedAt; // 更新时间别名，匹配前端期望
}