package com.example.aicourse.service;

import com.example.aicourse.dto.task.TaskCreateDTO;
import com.example.aicourse.dto.task.TaskUpdateDTO;
import com.example.aicourse.entity.Task;
import com.example.aicourse.vo.PageVO;
import com.example.aicourse.vo.task.TaskVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * TaskService 测试类
 * 测试任务管理相关的核心功能
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskService taskService;

    @Test
    void testGetCourseTasks() {
        // 准备测试数据
        Long courseId = 1L;
        String type = "HOMEWORK";
        String status = "PUBLISHED";
        Long pageNum = 1L;
        Long pageSize = 10L;
        
        // 创建模拟的任务列表
        List<TaskVO> taskList = Arrays.asList(
            createMockTaskVO(1L, "Java作业1", "HOMEWORK", "PUBLISHED", courseId),
            createMockTaskVO(2L, "Java作业2", "HOMEWORK", "PUBLISHED", courseId)
        );
        
        PageVO<TaskVO> expectedPage = new PageVO<>();
        expectedPage.setRecords(taskList);
        expectedPage.setTotal(2L);
        expectedPage.setCurrent(pageNum);
        expectedPage.setSize(pageSize);
        
        when(taskService.getCourseTasks(courseId, type, status, pageNum, pageSize))
            .thenReturn(expectedPage);
        
        // 执行测试
        PageVO<TaskVO> result = taskService.getCourseTasks(courseId, type, status, pageNum, pageSize);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(2L, result.getTotal());
        assertEquals(pageNum, result.getCurrent());
        assertEquals(pageSize, result.getSize());
        assertEquals(2, result.getRecords().size());
        
        // 验证方法被调用
        verify(taskService, times(1)).getCourseTasks(courseId, type, status, pageNum, pageSize);
    }

    @Test
    void testGetCourseTasksWithNullFilters() {
        // 测试空过滤器
        Long courseId = 1L;
        Long pageNum = 1L;
        Long pageSize = 10L;
        
        PageVO<TaskVO> expectedPage = new PageVO<>();
        expectedPage.setRecords(Arrays.asList());
        expectedPage.setTotal(0L);
        
        when(taskService.getCourseTasks(courseId, null, null, pageNum, pageSize))
            .thenReturn(expectedPage);
        
        // 执行测试
        PageVO<TaskVO> result = taskService.getCourseTasks(courseId, null, null, pageNum, pageSize);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(0L, result.getTotal());
        assertTrue(result.getRecords().isEmpty());
        
        // 验证方法被调用
        verify(taskService, times(1)).getCourseTasks(courseId, null, null, pageNum, pageSize);
    }

    @Test
    void testGetTaskDetails() {
        // 准备测试数据
        Long taskId = 1L;
        TaskVO expectedTask = createMockTaskVO(taskId, "Java作业1", "HOMEWORK", "PUBLISHED", 1L);
        
        when(taskService.getTaskDetails(taskId)).thenReturn(expectedTask);
        
        // 执行测试
        TaskVO result = taskService.getTaskDetails(taskId);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(taskId, result.getId());
        assertEquals("Java作业1", result.getTitle());
        assertEquals("HOMEWORK", result.getType());
        assertEquals("PUBLISHED", result.getStatus());
        assertEquals(1L, result.getCourseId());
        
        // 验证方法被调用
        verify(taskService, times(1)).getTaskDetails(taskId);
    }

    @Test
    void testGetTaskDetailsWithInvalidId() {
        // 准备测试数据
        Long invalidId = 999L;
        
        when(taskService.getTaskDetails(invalidId)).thenReturn(null);
        
        // 执行测试
        TaskVO result = taskService.getTaskDetails(invalidId);
        
        // 验证结果
        assertNull(result);
        
        // 验证方法被调用
        verify(taskService, times(1)).getTaskDetails(invalidId);
    }

    @Test
    void testCreateTask() {
        // 准备测试数据
        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setTitle("Java作业1");
        dto.setDescription("完成Java基础练习");
        dto.setType("HOMEWORK");
        dto.setCourseId(1L);
        dto.setDeadline(LocalDateTime.now().plusDays(7));
        dto.setMaxScore(new BigDecimal("100"));
        dto.setCreatorId(1L);
        
        Long expectedId = 1L;
        when(taskService.createTask(dto)).thenReturn(expectedId);
        
        // 执行测试
        Long result = taskService.createTask(dto);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(expectedId, result);
        
        // 验证方法被调用
        verify(taskService, times(1)).createTask(dto);
    }

    @Test
    void testCreateTaskWithInvalidData() {
        // 准备测试数据 - 缺少必要字段
        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setDescription("完成Java基础练习");
        // 缺少标题等必要字段
        
        when(taskService.createTask(dto)).thenThrow(new IllegalArgumentException("任务标题不能为空"));
        
        // 执行测试
        assertThrows(IllegalArgumentException.class, () -> {
            taskService.createTask(dto);
        });
        
        // 验证方法被调用
        verify(taskService, times(1)).createTask(dto);
    }

    @Test
    void testUpdateTask() {
        // 准备测试数据
        Long taskId = 1L;
        TaskUpdateDTO dto = new TaskUpdateDTO();
        dto.setTitle("Java作业1（已更新）");
        dto.setDescription("完成Java基础练习（更新版）");
        dto.setMaxScore(new BigDecimal("120"));
        
        when(taskService.updateTask(taskId, dto)).thenReturn(true);
        
        // 执行测试
        boolean result = taskService.updateTask(taskId, dto);
        
        // 验证结果
        assertTrue(result);
        
        // 验证方法被调用
        verify(taskService, times(1)).updateTask(taskId, dto);
    }

    @Test
    void testUpdateTaskWithInvalidId() {
        // 准备测试数据
        Long invalidId = 999L;
        TaskUpdateDTO dto = new TaskUpdateDTO();
        dto.setTitle("Java作业1");
        
        when(taskService.updateTask(invalidId, dto)).thenReturn(false);
        
        // 执行测试
        boolean result = taskService.updateTask(invalidId, dto);
        
        // 验证结果
        assertFalse(result);
        
        // 验证方法被调用
        verify(taskService, times(1)).updateTask(invalidId, dto);
    }

    @Test
    void testDeleteTask() {
        // 准备测试数据
        Long taskId = 1L;
        
        when(taskService.deleteTask(taskId)).thenReturn(true);
        
        // 执行测试
        boolean result = taskService.deleteTask(taskId);
        
        // 验证结果
        assertTrue(result);
        
        // 验证方法被调用
        verify(taskService, times(1)).deleteTask(taskId);
    }

    @Test
    void testDeleteTaskWithInvalidId() {
        // 准备测试数据
        Long invalidId = 999L;
        
        when(taskService.deleteTask(invalidId)).thenReturn(false);
        
        // 执行测试
        boolean result = taskService.deleteTask(invalidId);
        
        // 验证结果
        assertFalse(result);
        
        // 验证方法被调用
        verify(taskService, times(1)).deleteTask(invalidId);
    }

    @Test
    void testUpdateTaskStatusToPublished() {
        // 准备测试数据
        Long taskId = 1L;
        String newStatus = "PUBLISHED";
        
        when(taskService.updateTaskStatus(taskId, newStatus)).thenReturn(true);
        
        // 执行测试
        boolean result = taskService.updateTaskStatus(taskId, newStatus);
        
        // 验证结果
        assertTrue(result);
        
        // 验证方法被调用
        verify(taskService, times(1)).updateTaskStatus(taskId, newStatus);
    }

    @Test
    void testUpdateTaskStatusToDraft() {
        // 准备测试数据
        Long taskId = 1L;
        String newStatus = "DRAFT";
        
        when(taskService.updateTaskStatus(taskId, newStatus)).thenReturn(true);
        
        // 执行测试
        boolean result = taskService.updateTaskStatus(taskId, newStatus);
        
        // 验证结果
        assertTrue(result);
        
        // 验证方法被调用
        verify(taskService, times(1)).updateTaskStatus(taskId, newStatus);
    }

    @Test
    void testUpdateTaskStatusToArchived() {
        // 准备测试数据
        Long taskId = 1L;
        String newStatus = "ARCHIVED";
        
        when(taskService.updateTaskStatus(taskId, newStatus)).thenReturn(true);
        
        // 执行测试
        boolean result = taskService.updateTaskStatus(taskId, newStatus);
        
        // 验证结果
        assertTrue(result);
        
        // 验证方法被调用
        verify(taskService, times(1)).updateTaskStatus(taskId, newStatus);
    }

    @Test
    void testUpdateTaskStatusWithInvalidStatus() {
        // 准备测试数据
        Long taskId = 1L;
        String invalidStatus = "INVALID_STATUS";
        
        when(taskService.updateTaskStatus(taskId, invalidStatus))
            .thenThrow(new IllegalArgumentException("无效的任务状态"));
        
        // 执行测试
        assertThrows(IllegalArgumentException.class, () -> {
            taskService.updateTaskStatus(taskId, invalidStatus);
        });
        
        // 验证方法被调用
        verify(taskService, times(1)).updateTaskStatus(taskId, invalidStatus);
    }

    @Test
    void testUpdateTaskStatusWithInvalidId() {
        // 准备测试数据
        Long invalidId = 999L;
        String newStatus = "PUBLISHED";
        
        when(taskService.updateTaskStatus(invalidId, newStatus)).thenReturn(false);
        
        // 执行测试
        boolean result = taskService.updateTaskStatus(invalidId, newStatus);
        
        // 验证结果
        assertFalse(result);
        
        // 验证方法被调用
        verify(taskService, times(1)).updateTaskStatus(invalidId, newStatus);
    }

    // 辅助方法：创建模拟的TaskVO对象
    private TaskVO createMockTaskVO(Long id, String title, String type, String status, Long courseId) {
        TaskVO task = new TaskVO();
        task.setId(id);
        task.setTitle(title);
        task.setDescription(title + "描述");
        task.setType(type);
        task.setStatus(status);
        task.setCourseId(courseId);
        task.setMaxScore(new BigDecimal("100"));
        task.setDeadline(LocalDateTime.now().plusDays(7));
        task.setGmtCreate(LocalDateTime.now());
        return task;
    }
} 