package com.example.aicourse.dto.student;

import lombok.Data;
// import jakarta.validation.constraints.NotBlank; // 根据文档示例，这些字段可以是部分更新，所以不强制NotBlank
// import jakarta.validation.constraints.Email;

/**
 * API 3.4 更新学生信息请求
 */
@Data
public class StudentUpdateDTO {
    private String name;
    private Integer gender;
    private String major;
    private String grade;
    private String phone;
    private String email;
}