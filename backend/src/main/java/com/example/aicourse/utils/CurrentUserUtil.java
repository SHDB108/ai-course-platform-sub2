package com.example.aicourse.utils;

import com.example.aicourse.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class CurrentUserUtil {
   public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalStateException("获取当前用户信息失败，用户未认证");
        }
        return (User) authentication.getPrincipal();
    }
}
