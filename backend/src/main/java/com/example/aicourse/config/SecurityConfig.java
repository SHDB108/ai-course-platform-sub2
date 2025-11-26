package com.example.aicourse.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;

    @Autowired
    public SecurityConfig(JwtRequestFilter jwtRequestFilter) {
        this.jwtRequestFilter = jwtRequestFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // === 公开访问 ===
                        .requestMatchers(
                                "/api/v1/auth/login",
                                "/api/v1/auth/register/**",
                                "/api/v1/auth/forgot-password/**"
                        ).permitAll()

                        // === 管理员权限 ===
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // === 教师权限 ===
                        // 教师可以创建、更新、删除课程/任务/题目/试卷
                        .requestMatchers(HttpMethod.POST, "/courses", "/api/v1/tasks", "/api/v1/questions", "/api/v1/papers/generate").hasRole("TEACHER")
                        .requestMatchers(HttpMethod.PUT, "/courses/**", "/api/v1/tasks/**", "/api/v1/questions/**").hasRole("TEACHER")
                        .requestMatchers(HttpMethod.DELETE, "/courses/**", "/api/v1/tasks/**", "/api/v1/questions/**", "/api/v1/papers/**").hasRole("TEACHER")
                        // 教师可以管理资源和查看分析数据
                        .requestMatchers("/api/v1/resources/**", "/api/v1/analytics/**").hasRole("TEACHER")

                        // === 学生权限 ===
                        // 学生可以选课、退课、提交任务/测验、上报视频进度
                        .requestMatchers(HttpMethod.POST, "/courses/{courseId}/students/{studentId}").hasRole("STUDENT")
                        .requestMatchers(HttpMethod.DELETE, "/courses/{courseId}/students/{studentId}").hasRole("STUDENT")
                        .requestMatchers("/api/v1/task-submissions/**", "/api/v1/quiz-submissions/**").hasRole("STUDENT")
                        .requestMatchers("/api/v1/videos/**").hasRole("STUDENT")
                        .requestMatchers("/api/v1/recommendations/**").hasRole("STUDENT")

                        // === 教师和学生均可访问 ===
                        // 例如，查看课程列表/详情，查看任务列表/详情
                        .requestMatchers(HttpMethod.GET, "/courses/**", "/api/v1/tasks/**", "/api/v1/questions/**", "/api/v1/papers/**").hasAnyRole("TEACHER", "STUDENT")

                        // 其他所有请求都需要认证
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}