package com.example.aicourse.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_teacher")
public class Teacher {
    @TableId
    private Long id; // Corresponds to User ID
    private String teacherNo;
    private String name;
    private String department;
    private String title;
    private String phone;
    private String email;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime gmtCreate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime gmtModified;
    
    // 兼容Service层的方法名
    public LocalDateTime getCreatedAt() {
        return gmtCreate;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.gmtCreate = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return gmtModified;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.gmtModified = updatedAt;
    }
}