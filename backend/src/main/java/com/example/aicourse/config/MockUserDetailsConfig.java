package com.example.aicourse.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

/**
 * Provides a lightweight UserDetailsService when running in mock profile so that
 * JwtRequestFilter can load principals without hitting real auth services.
 */
@Configuration
@Profile("mock")
public class MockUserDetailsConfig {

    @Bean
    public UserDetailsService mockUserDetailsService() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        UserDetails admin = User.withUsername("admin")
                .password(encoder.encode("mock-password"))
                .roles("ADMIN", "TEACHER", "STUDENT")
                .build();
        UserDetails teacher = User.withUsername("teacher")
                .password(encoder.encode("mock-password"))
                .roles("TEACHER")
                .build();
        UserDetails student = User.withUsername("student")
                .password(encoder.encode("mock-password"))
                .roles("STUDENT")
                .build();
        return new InMemoryUserDetailsManager(admin, teacher, student);
    }
}
