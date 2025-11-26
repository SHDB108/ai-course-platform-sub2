package com.example.aicourse.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.aicourse.entity.User;
import com.example.aicourse.repository.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Provides a database-backed UserDetailsService for non-mock profiles.
 * This loads real users from the t_user table in MySQL.
 */
@Configuration
@Profile("!mock")
public class DatabaseUserDetailsConfig {

    @Autowired
    private UserMapper userMapper;

    @Bean
    public UserDetailsService databaseUserDetailsService() {
        return username -> {
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("username", username);
            User user = userMapper.selectOne(queryWrapper);

            if (user == null) {
                throw new UsernameNotFoundException("User not found: " + username);
            }

            return user;
        };
    }
}
