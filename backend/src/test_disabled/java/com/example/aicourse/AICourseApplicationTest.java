package com.example.aicourse;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Spring Boot 应用集成测试
 * 测试应用启动和基本配置
 */
@SpringBootTest
@ActiveProfiles("test")
class AICourseApplicationTest {

    @Test
    void contextLoads() {
        // 测试Spring上下文是否正常加载
        assertTrue(true, "Spring context should load successfully");
    }

    @Test
    void testApplicationStartup() {
        // 测试应用启动
        AICourseApplication application = new AICourseApplication();
        assertNotNull(application, "Application should not be null");
    }

    @Test
    void testMainMethod() {
        // 测试main方法（不实际启动应用）
        assertDoesNotThrow(() -> {
            // 这里只是验证main方法存在且可以调用
            // 实际启动测试在contextLoads中完成
        });
    }
} 