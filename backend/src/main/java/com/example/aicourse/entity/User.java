package com.example.aicourse.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@TableName("t_user")
@JsonIgnoreProperties(ignoreUnknown = true)
public class User implements UserDetails {
    @TableId
    private Long id;
    private String username;
    private String password;
    private String email;
    private String phone;
    private String role; // e.g., 'STUDENT', 'TEACHER', 'ADMIN'
    private Integer status; // 1=ACTIVE, 0=INACTIVE, -1=SUSPENDED, -2=DELETED

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime gmtCreate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime gmtModified;

    private LocalDateTime lastLoginTime;
    
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

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + this.role));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 账户永不过期
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 账户永不锁定
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 凭证永不过期
    }

    @Override
    public boolean isEnabled() {
        // 账户是否启用，使用我们自己的status字段 (1=ACTIVE)
        return Integer.valueOf(1).equals(this.status);
    }
}