package com.example.aicourse.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_course_student") // 对应数据库表名
public class CourseStudent {

    @TableId(type = IdType.AUTO)
    private Long id; // 主键ID

    private Long courseId; // 课程ID

    private Long studentId; // 学生ID

    private LocalDateTime enrollmentDate; // 选课日期

    // 您可以根据需要添加其他字段，例如：
    // private String status; // 选课状态，例如 "ENROLLED", "COMPLETED", "DROPPED"
    // private Double grade; // 该学生在该课程的最终成绩
}