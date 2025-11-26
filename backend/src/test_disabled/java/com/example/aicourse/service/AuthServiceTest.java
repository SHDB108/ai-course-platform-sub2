package com.example.aicourse.service;

import com.example.aicourse.dto.auth.*;
import com.example.aicourse.vo.auth.LoginResponseVO;
import com.example.aicourse.vo.user.UserDetailVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * AuthService 测试类
 * 测试用户认证相关的核心功能
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthService authService;

    @Test
    void testLoginSuccess() {
        // 准备测试数据
        String username = "testuser";
        String password = "password123";
        
        LoginResponseVO expectedResponse = new LoginResponseVO();
        expectedResponse.setToken("test-token");
        expectedResponse.setUserId(1L);
        expectedResponse.setUsername(username);

        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setUsername(username);
        dto.setPassword(password);

        when(authService.login(dto)).thenReturn(expectedResponse);
        
        // 执行测试
        LoginResponseVO result = authService.login(dto);
        
        // 验证结果
        assertNotNull(result);
        assertEquals("test-token", result.getToken());
        assertEquals(1L, result.getUserId());
        assertEquals(username, result.getUsername());
        
        // 验证方法被调用
        verify(authService, times(1)).login(dto);
    }

    @Test
    void testLoginFailure() {
        // 准备测试数据
        String username = "invaliduser";
        String password = "wrongpassword";

        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setUsername(username);
        dto.setPassword(password);
        
        when(authService.login(dto)).thenReturn(null);
        
        // 执行测试
        LoginResponseVO result = authService.login(dto);
        
        // 验证结果
        assertNull(result);
        
        // 验证方法被调用
        verify(authService, times(1)).login(dto);
    }

    @Test
    void testLoginWithEmptyUsername() {
        // 测试空用户名
        assertThrows(IllegalArgumentException.class, () -> {
            LoginRequestDTO dto = new LoginRequestDTO();
            dto.setUsername("");
            dto.setPassword("password123");
            authService.login(dto);
        });
    }

    @Test
    void testLoginWithEmptyPassword() {
        // 测试空密码
        assertThrows(IllegalArgumentException.class, () -> {
            LoginRequestDTO dto = new LoginRequestDTO();
            dto.setUsername("testuser");
            dto.setPassword("");
            authService.login(dto);
        });
    }

    @Test
    void testRegisterStudentSuccess() {
        // 准备测试数据
        StudentRegisterDTO dto = new StudentRegisterDTO();
        dto.setUsername("newstudent");
        dto.setPassword("password123");
        dto.setEmail("student@example.com");
        dto.setName("张三");
        dto.setStuNo("2024001");
        
        when(authService.registerStudent(dto)).thenReturn(1L);
        
        // 执行测试
        Long result = authService.registerStudent(dto);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result);
        
        // 验证方法被调用
        verify(authService, times(1)).registerStudent(dto);
    }

    @Test
    void testRegisterStudentWithInvalidEmail() {
        // 准备测试数据
        StudentRegisterDTO dto = new StudentRegisterDTO();
        dto.setUsername("newstudent");
        dto.setPassword("password123");
        dto.setEmail("invalid-email");
        dto.setName("张三");
        dto.setStuNo("2024001");
        
        when(authService.registerStudent(dto)).thenThrow(new IllegalArgumentException("Invalid email format"));
        
        // 执行测试
        assertThrows(IllegalArgumentException.class, () -> {
            authService.registerStudent(dto);
        });
        
        // 验证方法被调用
        verify(authService, times(1)).registerStudent(dto);
    }

    @Test
    void testRegisterTeacherSuccess() {
        // 准备测试数据
        TeacherRegisterDTO dto = new TeacherRegisterDTO();
        dto.setUsername("newteacher");
        dto.setPassword("password123");
        dto.setEmail("teacher@example.com");
        dto.setName("李老师");
        dto.setDepartment("计算机学院");
        
        when(authService.registerTeacher(dto)).thenReturn(2L);
        
        // 执行测试
        Long result = authService.registerTeacher(dto);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(2L, result);
        
        // 验证方法被调用
        verify(authService, times(1)).registerTeacher(dto);
    }

    @Test
    void testLogout() {
        when(authService.logout()).thenReturn(true);
        
        // 执行测试
        boolean result = authService.logout();
        
        // 验证结果
        assertTrue(result);
        
        // 验证方法被调用
        verify(authService, times(1)).logout();
    }

    @Test
    void testGetCurrentUserInfo() {
        // 准备测试数据
        UserDetailVO expectedUser = new UserDetailVO();
        expectedUser.setId(1L);
        expectedUser.setUsername("testuser");
        expectedUser.setName("测试用户");
        expectedUser.setEmail("test@example.com");
        
        when(authService.getCurrentUserInfo()).thenReturn(expectedUser);
        
        // 执行测试
        UserDetailVO result = authService.getCurrentUserInfo();
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("测试用户", result.getName());
        assertEquals("test@example.com", result.getEmail());
        
        // 验证方法被调用
        verify(authService, times(1)).getCurrentUserInfo();
    }

    @Test
    void testUpdatePasswordSuccess() {
        // 准备测试数据
        Long userId = 1L;
        PasswordUpdateDTO dto = new PasswordUpdateDTO();
        dto.setOldPassword("oldpassword");
        dto.setNewPassword("newpassword");
        
        when(authService.updatePassword(userId, dto)).thenReturn(true);
        
        // 执行测试
        boolean result = authService.updatePassword(userId, dto);
        
        // 验证结果
        assertTrue(result);
        
        // 验证方法被调用
        verify(authService, times(1)).updatePassword(userId, dto);
    }

    @Test
    void testUpdatePasswordWithWrongOldPassword() {
        // 准备测试数据
        Long userId = 1L;
        PasswordUpdateDTO dto = new PasswordUpdateDTO();
        dto.setOldPassword("wrongpassword");
        dto.setNewPassword("newpassword");
        
        when(authService.updatePassword(userId, dto)).thenThrow(new IllegalArgumentException("Old password is incorrect"));
        
        // 执行测试
        assertThrows(IllegalArgumentException.class, () -> {
            authService.updatePassword(userId, dto);
        });
        
        // 验证方法被调用
        verify(authService, times(1)).updatePassword(userId, dto);
    }

    @Test
    void testRequestPasswordReset() {
        // 准备测试数据
        ForgotPasswordRequestDTO dto = new ForgotPasswordRequestDTO();
        dto.setIdentifier("test@example.com");
        
        when(authService.requestPasswordReset(dto)).thenReturn("验证码已发送到您的邮箱");
        
        // 执行测试
        String result = authService.requestPasswordReset(dto);
        
        // 验证结果
        assertNotNull(result);
        assertEquals("验证码已发送到您的邮箱", result);
        
        // 验证方法被调用
        verify(authService, times(1)).requestPasswordReset(dto);
    }

    @Test
    void testResetPasswordSuccess() {
        // 准备测试数据
        PasswordResetDTO dto = new PasswordResetDTO();
        dto.setIdentifier("test@example.com");
        dto.setVerificationCode("123456");
        dto.setNewPassword("newpassword");
        
        when(authService.resetPassword(dto)).thenReturn(true);
        
        // 执行测试
        boolean result = authService.resetPassword(dto);
        
        // 验证结果
        assertTrue(result);
        
        // 验证方法被调用
        verify(authService, times(1)).resetPassword(dto);
    }

    @Test
    void testResetPasswordWithInvalidCode() {
        // 准备测试数据
        PasswordResetDTO dto = new PasswordResetDTO();
        dto.setIdentifier("test@example.com");
        dto.setVerificationCode("000000");
        dto.setNewPassword("newpassword");
        
        when(authService.resetPassword(dto)).thenThrow(new IllegalArgumentException("Invalid verification code"));
        
        // 执行测试
        assertThrows(IllegalArgumentException.class, () -> {
            authService.resetPassword(dto);
        });
        
        // 验证方法被调用
        verify(authService, times(1)).resetPassword(dto);
    }
} 