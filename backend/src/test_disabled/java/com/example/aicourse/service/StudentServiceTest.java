package com.example.aicourse.service;

import com.example.aicourse.dto.student.StudentCreateDTO;
import com.example.aicourse.dto.student.StudentUpdateDTO;
import com.example.aicourse.entity.Student;
import com.example.aicourse.vo.PageVO;
import com.example.aicourse.vo.student.ImportResultVO;
import com.example.aicourse.vo.student.StudentVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * StudentService 测试类
 * 测试学生管理相关的核心功能
 */
@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentService studentService;

    @Test
    void testGetStudentPage() {
        // 准备测试数据
        Long pageNum = 1L;
        Long pageSize = 10L;
        String keyword = "张三";
        String major = "计算机科学";
        String grade = "2024";
        
        // 创建模拟的学生列表
        List<StudentVO> studentList = Arrays.asList(
            createMockStudentVO(1L, "张三", "2024001", "计算机科学", "2024"),
            createMockStudentVO(2L, "李四", "2024002", "计算机科学", "2024")
        );
        
        PageVO<StudentVO> expectedPage = new PageVO<>();
        expectedPage.setRecords(studentList);
        expectedPage.setTotal(2L);
        expectedPage.setCurrent(pageNum);
        expectedPage.setSize(pageSize);
        
        when(studentService.getStudentPage(pageNum, pageSize, keyword, major, grade))
            .thenReturn(expectedPage);
        
        // 执行测试
        PageVO<StudentVO> result = studentService.getStudentPage(pageNum, pageSize, keyword, major, grade);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(2L, result.getTotal());
        assertEquals(pageNum, result.getCurrent());
        assertEquals(pageSize, result.getSize());
        assertEquals(2, result.getRecords().size());
        
        // 验证方法被调用
        verify(studentService, times(1)).getStudentPage(pageNum, pageSize, keyword, major, grade);
    }

    @Test
    void testGetStudentPageWithNullParameters() {
        // 测试空参数
        PageVO<StudentVO> expectedPage = new PageVO<>();
        expectedPage.setRecords(Arrays.asList());
        expectedPage.setTotal(0L);
        
        when(studentService.getStudentPage(null, null, null, null, null))
            .thenReturn(expectedPage);
        
        // 执行测试
        PageVO<StudentVO> result = studentService.getStudentPage(null, null, null, null, null);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(0L, result.getTotal());
        assertTrue(result.getRecords().isEmpty());
        
        // 验证方法被调用
        verify(studentService, times(1)).getStudentPage(null, null, null, null, null);
    }

    @Test
    void testGetStudentDetails() {
        // 准备测试数据
        Long studentId = 1L;
        StudentVO expectedStudent = createMockStudentVO(studentId, "张三", "2024001", "计算机科学", "2024");
        
        when(studentService.getStudentDetails(studentId)).thenReturn(expectedStudent);
        
        // 执行测试
        StudentVO result = studentService.getStudentDetails(studentId);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(studentId, result.getId());
        assertEquals("张三", result.getName());
        assertEquals("2024001", result.getStuNo());
        assertEquals("计算机科学", result.getMajor());
        assertEquals("2024", result.getGrade());
        
        // 验证方法被调用
        verify(studentService, times(1)).getStudentDetails(studentId);
    }

    @Test
    void testGetStudentDetailsWithInvalidId() {
        // 准备测试数据
        Long invalidId = 999L;
        
        when(studentService.getStudentDetails(invalidId)).thenReturn(null);
        
        // 执行测试
        StudentVO result = studentService.getStudentDetails(invalidId);
        
        // 验证结果
        assertNull(result);
        
        // 验证方法被调用
        verify(studentService, times(1)).getStudentDetails(invalidId);
    }

    @Test
    void testCreateStudent() {
        // 准备测试数据
        StudentCreateDTO dto = new StudentCreateDTO();
        dto.setStuNo("2024001");
        dto.setName("张三");
        dto.setGender(1);
        dto.setMajor("计算机科学");
        dto.setGrade("2024");
        dto.setPhone("13800138000");
        dto.setEmail("zhangsan@example.com");
        
        Long expectedId = 1L;
        when(studentService.createStudent(dto)).thenReturn(expectedId);
        
        // 执行测试
        Long result = studentService.createStudent(dto);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(expectedId, result);
        
        // 验证方法被调用
        verify(studentService, times(1)).createStudent(dto);
    }

    @Test
    void testCreateStudentWithInvalidData() {
        // 准备测试数据 - 缺少必要字段
        StudentCreateDTO dto = new StudentCreateDTO();
        dto.setName("张三");
        // 缺少学号等必要字段
        
        when(studentService.createStudent(dto)).thenThrow(new IllegalArgumentException("学号不能为空"));
        
        // 执行测试
        assertThrows(IllegalArgumentException.class, () -> {
            studentService.createStudent(dto);
        });
        
        // 验证方法被调用
        verify(studentService, times(1)).createStudent(dto);
    }

    @Test
    void testUpdateStudent() {
        // 准备测试数据
        Long studentId = 1L;
        StudentUpdateDTO dto = new StudentUpdateDTO();
        dto.setName("张三（已更新）");
        dto.setPhone("13900139000");
        dto.setEmail("zhangsan_updated@example.com");
        
        when(studentService.updateStudent(studentId, dto)).thenReturn(true);
        
        // 执行测试
        boolean result = studentService.updateStudent(studentId, dto);
        
        // 验证结果
        assertTrue(result);
        
        // 验证方法被调用
        verify(studentService, times(1)).updateStudent(studentId, dto);
    }

    @Test
    void testUpdateStudentWithInvalidId() {
        // 准备测试数据
        Long invalidId = 999L;
        StudentUpdateDTO dto = new StudentUpdateDTO();
        dto.setName("张三");
        
        when(studentService.updateStudent(invalidId, dto)).thenReturn(false);
        
        // 执行测试
        boolean result = studentService.updateStudent(invalidId, dto);
        
        // 验证结果
        assertFalse(result);
        
        // 验证方法被调用
        verify(studentService, times(1)).updateStudent(invalidId, dto);
    }

    @Test
    void testDeleteStudent() {
        // 准备测试数据
        Long studentId = 1L;
        
        when(studentService.deleteStudent(studentId)).thenReturn(true);
        
        // 执行测试
        boolean result = studentService.deleteStudent(studentId);
        
        // 验证结果
        assertTrue(result);
        
        // 验证方法被调用
        verify(studentService, times(1)).deleteStudent(studentId);
    }

    @Test
    void testDeleteStudentWithInvalidId() {
        // 准备测试数据
        Long invalidId = 999L;
        
        when(studentService.deleteStudent(invalidId)).thenReturn(false);
        
        // 执行测试
        boolean result = studentService.deleteStudent(invalidId);
        
        // 验证结果
        assertFalse(result);
        
        // 验证方法被调用
        verify(studentService, times(1)).deleteStudent(invalidId);
    }

    @Test
    void testImportStudents() throws IOException {
        // 准备测试数据
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "students.xlsx", 
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "test file content".getBytes()
        );
        
        ImportResultVO expectedResult = new ImportResultVO();
        expectedResult.setSuccessCount(8);
        expectedResult.setFailureCount(2);
        
        ImportResultVO.FailedRecord failedRecord1 = new ImportResultVO.FailedRecord();
        failedRecord1.setRow(3);
        failedRecord1.setReason("学号重复");
        
        ImportResultVO.FailedRecord failedRecord2 = new ImportResultVO.FailedRecord();
        failedRecord2.setRow(7);
        failedRecord2.setReason("邮箱格式错误");
        
        expectedResult.setFailedRecords(Arrays.asList(failedRecord1, failedRecord2));
        
        when(studentService.importStudents(file)).thenReturn(expectedResult);
        
        // 执行测试
        ImportResultVO result = studentService.importStudents(file);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(8, result.getSuccessCount());
        assertEquals(2, result.getFailureCount());
        assertEquals(2, result.getFailedRecords().size());
        
        // 验证方法被调用
        verify(studentService, times(1)).importStudents(file);
    }

    @Test
    void testImportStudentsWithEmptyFile() throws IOException {
        // 准备测试数据 - 空文件
        MockMultipartFile emptyFile = new MockMultipartFile(
            "file", 
            "empty.xlsx", 
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            new byte[0]
        );
        
        when(studentService.importStudents(emptyFile)).thenThrow(new IllegalArgumentException("文件不能为空"));
        
        // 执行测试
        assertThrows(IllegalArgumentException.class, () -> {
            studentService.importStudents(emptyFile);
        });
        
        // 验证方法被调用
        verify(studentService, times(1)).importStudents(emptyFile);
    }

    @Test
    void testExportStudents() throws IOException {
        // 准备测试数据
        String major = "计算机科学";
        String grade = "2024";
        String format = "xlsx";
        
        // 模拟文件资源
        Resource mockResource = mock(Resource.class);
        ResponseEntity<Resource> expectedResponse = ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=students.xlsx")
            .body(mockResource);
        
        when(studentService.exportStudents(major, grade, format)).thenReturn(expectedResponse);
        
        // 执行测试
        ResponseEntity<Resource> result = studentService.exportStudents(major, grade, format);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(200, result.getStatusCodeValue());
        assertNotNull(result.getBody());
        assertTrue(result.getHeaders().containsKey("Content-Disposition"));
        
        // 验证方法被调用
        verify(studentService, times(1)).exportStudents(major, grade, format);
    }

    @Test
    void testExportStudentsWithInvalidFormat() throws IOException {
        // 准备测试数据
        String major = "计算机科学";
        String grade = "2024";
        String invalidFormat = "txt";
        
        when(studentService.exportStudents(major, grade, invalidFormat))
            .thenThrow(new IllegalArgumentException("不支持的导出格式"));
        
        // 执行测试
        assertThrows(IllegalArgumentException.class, () -> {
            studentService.exportStudents(major, grade, invalidFormat);
        });
        
        // 验证方法被调用
        verify(studentService, times(1)).exportStudents(major, grade, invalidFormat);
    }

    // 辅助方法：创建模拟的StudentVO对象
    private StudentVO createMockStudentVO(Long id, String name, String stuNo, String major, String grade) {
        StudentVO student = new StudentVO();
        student.setId(id);
        student.setName(name);
        student.setStuNo(stuNo);
        student.setMajor(major);
        student.setGrade(grade);
        student.setGender(1);
        student.setPhone("13800138000");
        student.setEmail(name.toLowerCase() + "@example.com");
        return student;
    }
} 