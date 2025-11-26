package com.example.aicourse.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_course")
@Getter
public class Course{

 @TableId(type = IdType.AUTO)
 private Long id;

 private String courseCode,courseName;
 private String description; // 课程描述 (来自CourseVO/CourseCreateDTO)
 private Integer credits; // 学分 (将 BigDecimal credit 改为 Integer credits，与API和DTO/VO保持一致)
 private Integer hours; // 学时 (已存在)
 private Long teacherId; // 授课教师ID (已存在)
 private String semester; // 新增：开课学期
 private String department; // 新增：开课学院
 private String classroom; // 新增：上课地点 (来自CourseVO/CourseCreateDTO)
 private String scheduleTime; // 新增：上课时间 (来自CourseVO/CourseCreateDTO)
 private Integer capacity; // 新增：课程容量 (来自CourseCreateDTO)
 private Integer enrolledStudents; // 新增：已选学生数 (CourseServiceImpl中用到并初始化为0)
 private Long categoryId; // 新增：课程分类ID

 @TableField(fill = FieldFill.INSERT)
 private LocalDateTime gmtCreate;

 @TableField(fill = FieldFill.INSERT_UPDATE)
 private LocalDateTime gmtModified;
}