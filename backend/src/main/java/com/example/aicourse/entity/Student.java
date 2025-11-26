
package com.example.aicourse.entity;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data @TableName("t_student")
public class Student{
 @TableId
 private Long id;
 private String stuNo,name;
 private Integer gender; //0为女，1为男
 private String major,grade,phone,email;

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
