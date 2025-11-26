package com.example.aicourse.vo.student;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * API 3.1 & 3.2 学生信息响应
 * 新增用户相关字段以匹配API 4.8的需求
 */
@Data
public class StudentVO {
    private Long id;
    private String stuNo;
    private String name;
    private Integer gender;
    private String major;
    private String grade;
    private String phone;
    private String email;
    private LocalDateTime gmtCreate;

    // ====== 新增字段以从User实体填充 ======
    private String username; // 用户名
    private String role;     // 用户角色
    private Integer status;   // 用户状态 (修改为Integer类型以匹配User实体: 1=ACTIVE, 0=INACTIVE, -1=SUSPENDED, -2=DELETED)
    // ======================================
}