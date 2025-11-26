package com.example.aicourse.dto.student;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * API 3.3 新增学生请求
 */
@Data
public class StudentCreateDTO {
    @NotBlank
    private String stuNo;
    @NotBlank
    private String name;
    @NotBlank
    private Integer gender;
    @NotBlank
    private String major;
    @NotBlank
    private String grade;
    private String phone;
    @Email
    private String email;
}