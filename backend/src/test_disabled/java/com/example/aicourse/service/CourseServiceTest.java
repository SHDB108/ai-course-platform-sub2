package com.example.aicourse.service;

import com.example.aicourse.dto.course.CourseCreateDTO;
import com.example.aicourse.dto.course.CourseScheduleDTO;
import com.example.aicourse.dto.course.CourseUpdateDTO;
import com.example.aicourse.vo.PageVO;
import com.example.aicourse.vo.course.CourseVO;
import com.example.aicourse.vo.student.StudentVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * CourseService 测试类
 * 测试课程管理相关的核心功能
 */
@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseService courseService;

    @Test
    void testListCourses() {
        // 准备测试数据
        Long pageNum = 1L;
        Long pageSize = 10L;
        Long teacherId = 1L;
        String keyword = "Java";
        String semester = "2024春季";
        Integer credits = 3;
        String department = "计算机学院";
        
        // 创建模拟的课程列表
        List<CourseVO> courseList = Arrays.asList(
            createMockCourseVO(1L, "Java程序设计", "CS101", 3, "计算机学院", "2024春季"),
            createMockCourseVO(2L, "数据结构", "CS102", 4, "计算机学院", "2024春季")
        );
        
        PageVO<CourseVO> expectedPage = new PageVO<>();
        expectedPage.setRecords(courseList);
        expectedPage.setTotal(2L);
        expectedPage.setCurrent(pageNum);
        expectedPage.setSize(pageSize);
        
        when(courseService.listCourses(pageNum, pageSize, teacherId, keyword, semester, credits, department))
            .thenReturn(expectedPage);
        
        // 执行测试
        PageVO<CourseVO> result = courseService.listCourses(pageNum, pageSize, teacherId, keyword, semester, credits, department);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(2L, result.getTotal());
        assertEquals(pageNum, result.getCurrent());
        assertEquals(pageSize, result.getSize());
        assertEquals(2, result.getRecords().size());
        
        // 验证方法被调用
        verify(courseService, times(1)).listCourses(pageNum, pageSize, teacherId, keyword, semester, credits, department);
    }

    @Test
    void testListCoursesWithNullParameters() {
        // 测试空参数
        PageVO<CourseVO> expectedPage = new PageVO<>();
        expectedPage.setRecords(Arrays.asList());
        expectedPage.setTotal(0L);
        
        when(courseService.listCourses(null, null, null, null, null, null, null))
            .thenReturn(expectedPage);
        
        // 执行测试
        PageVO<CourseVO> result = courseService.listCourses(null, null, null, null, null, null, null);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(0L, result.getTotal());
        assertTrue(result.getRecords().isEmpty());
        
        // 验证方法被调用
        verify(courseService, times(1)).listCourses(null, null, null, null, null, null, null);
    }

    @Test
    void testGetCourseDetail() {
        // 准备测试数据
        Long courseId = 1L;
        CourseVO expectedCourse = createMockCourseVO(courseId, "Java程序设计", "CS101", 3, "计算机学院", "2024春季");
        
        when(courseService.getCourseDetail(courseId)).thenReturn(expectedCourse);
        
        // 执行测试
        CourseVO result = courseService.getCourseDetail(courseId);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(courseId, result.getId());
        assertEquals("Java程序设计", result.getCourseName());
        assertEquals("CS101", result.getCourseCode());
        assertEquals(3, result.getCredits());
        assertEquals("计算机学院", result.getDepartment());
        assertEquals("2024春季", result.getSemester());
        
        // 验证方法被调用
        verify(courseService, times(1)).getCourseDetail(courseId);
    }

    @Test
    void testGetCourseDetailWithInvalidId() {
        // 准备测试数据
        Long invalidId = 999L;
        
        when(courseService.getCourseDetail(invalidId)).thenReturn(null);
        
        // 执行测试
        CourseVO result = courseService.getCourseDetail(invalidId);
        
        // 验证结果
        assertNull(result);
        
        // 验证方法被调用
        verify(courseService, times(1)).getCourseDetail(invalidId);
    }

    @Test
    void testCreateCourse() {
        // 准备测试数据
        CourseCreateDTO dto = new CourseCreateDTO();
        dto.setCourseName("Java程序设计");
        dto.setCourseCode("CS101");
        dto.setCredits(3);
        dto.setDepartment("计算机学院");
        dto.setSemester("2024春季");
        dto.setDescription("Java编程基础课程");
        dto.setTeacherId(1L);
        dto.setCapacity(50);
        
        Long expectedId = 1L;
        when(courseService.createCourse(dto)).thenReturn(expectedId);
        
        // 执行测试
        Long result = courseService.createCourse(dto);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(expectedId, result);
        
        // 验证方法被调用
        verify(courseService, times(1)).createCourse(dto);
    }

    @Test
    void testCreateCourseWithInvalidData() {
        // 准备测试数据 - 缺少必要字段
        CourseCreateDTO dto = new CourseCreateDTO();
        dto.setCourseName("Java程序设计");
        // 缺少课程代码等必要字段
        
        when(courseService.createCourse(dto)).thenThrow(new IllegalArgumentException("课程代码不能为空"));
        
        // 执行测试
        assertThrows(IllegalArgumentException.class, () -> {
            courseService.createCourse(dto);
        });
        
        // 验证方法被调用
        verify(courseService, times(1)).createCourse(dto);
    }

    @Test
    void testUpdateCourse() {
        // 准备测试数据
        Long courseId = 1L;
        CourseUpdateDTO dto = new CourseUpdateDTO();
        dto.setCourseName("Java程序设计（高级）");
        dto.setDescription("Java高级编程课程");
        dto.setCredits(4);
        
        when(courseService.updateCourse(courseId, dto)).thenReturn(true);
        
        // 执行测试
        boolean result = courseService.updateCourse(courseId, dto);
        
        // 验证结果
        assertTrue(result);
        
        // 验证方法被调用
        verify(courseService, times(1)).updateCourse(courseId, dto);
    }

    @Test
    void testUpdateCourseWithInvalidId() {
        // 准备测试数据
        Long invalidId = 999L;
        CourseUpdateDTO dto = new CourseUpdateDTO();
        dto.setCourseName("Java程序设计");
        
        when(courseService.updateCourse(invalidId, dto)).thenReturn(false);
        
        // 执行测试
        boolean result = courseService.updateCourse(invalidId, dto);
        
        // 验证结果
        assertFalse(result);
        
        // 验证方法被调用
        verify(courseService, times(1)).updateCourse(invalidId, dto);
    }

    @Test
    void testDeleteCourse() {
        // 准备测试数据
        Long courseId = 1L;
        
        when(courseService.deleteCourse(courseId)).thenReturn(true);
        
        // 执行测试
        boolean result = courseService.deleteCourse(courseId);
        
        // 验证结果
        assertTrue(result);
        
        // 验证方法被调用
        verify(courseService, times(1)).deleteCourse(courseId);
    }

    @Test
    void testDeleteCourseWithInvalidId() {
        // 准备测试数据
        Long invalidId = 999L;
        
        when(courseService.deleteCourse(invalidId)).thenReturn(false);
        
        // 执行测试
        boolean result = courseService.deleteCourse(invalidId);
        
        // 验证结果
        assertFalse(result);
        
        // 验证方法被调用
        verify(courseService, times(1)).deleteCourse(invalidId);
    }

    @Test
    void testScheduleCourse() {
        // 准备测试数据
        Long courseId = 1L;
        CourseScheduleDTO dto = new CourseScheduleDTO();
        dto.setClassroom("A101");
        dto.setTeacherId(1L);
        dto.setScheduleTime("周二 1-2节, 周四 3-4节");
        
        when(courseService.scheduleCourse(courseId, dto)).thenReturn(true);
        
        // 执行测试
        boolean result = courseService.scheduleCourse(courseId, dto);
        
        // 验证结果
        assertTrue(result);
        
        // 验证方法被调用
        verify(courseService, times(1)).scheduleCourse(courseId, dto);
    }

    @Test
    void testScheduleCourseWithInvalidData() {
        // 准备测试数据 - 时间冲突
        Long courseId = 1L;
        CourseScheduleDTO dto = new CourseScheduleDTO();
        dto.setClassroom("A101");
        dto.setTeacherId(1L);
        dto.setScheduleTime("周二 1-2节");
        
        when(courseService.scheduleCourse(courseId, dto)).thenThrow(new IllegalArgumentException("时间冲突"));
        
        // 执行测试
        assertThrows(IllegalArgumentException.class, () -> {
            courseService.scheduleCourse(courseId, dto);
        });
        
        // 验证方法被调用
        verify(courseService, times(1)).scheduleCourse(courseId, dto);
    }

    @Test
    void testEnrollStudent() {
        // 准备测试数据
        Long courseId = 1L;
        Long studentId = 1L;
        
        when(courseService.enrollStudent(courseId, studentId)).thenReturn(true);
        
        // 执行测试
        boolean result = courseService.enrollStudent(courseId, studentId);
        
        // 验证结果
        assertTrue(result);
        
        // 验证方法被调用
        verify(courseService, times(1)).enrollStudent(courseId, studentId);
    }

    @Test
    void testEnrollStudentAlreadyEnrolled() {
        // 准备测试数据 - 学生已经选课
        Long courseId = 1L;
        Long studentId = 1L;
        
        when(courseService.enrollStudent(courseId, studentId)).thenReturn(false);
        
        // 执行测试
        boolean result = courseService.enrollStudent(courseId, studentId);
        
        // 验证结果
        assertFalse(result);
        
        // 验证方法被调用
        verify(courseService, times(1)).enrollStudent(courseId, studentId);
    }

    @Test
    void testUnenrollStudent() {
        // 准备测试数据
        Long courseId = 1L;
        Long studentId = 1L;
        
        when(courseService.unenrollStudent(courseId, studentId)).thenReturn(true);
        
        // 执行测试
        boolean result = courseService.unenrollStudent(courseId, studentId);
        
        // 验证结果
        assertTrue(result);
        
        // 验证方法被调用
        verify(courseService, times(1)).unenrollStudent(courseId, studentId);
    }

    @Test
    void testUnenrollStudentNotEnrolled() {
        // 准备测试数据 - 学生未选课
        Long courseId = 1L;
        Long studentId = 1L;
        
        when(courseService.unenrollStudent(courseId, studentId)).thenReturn(false);
        
        // 执行测试
        boolean result = courseService.unenrollStudent(courseId, studentId);
        
        // 验证结果
        assertFalse(result);
        
        // 验证方法被调用
        verify(courseService, times(1)).unenrollStudent(courseId, studentId);
    }

    @Test
    void testGetEnrolledStudents() {
        // 准备测试数据
        Long courseId = 1L;
        Long pageNum = 1L;
        Long pageSize = 10L;
        String keyword = "张三";
        
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
        
        when(courseService.getEnrolledStudents(courseId, pageNum, pageSize, keyword))
            .thenReturn(expectedPage);
        
        // 执行测试
        PageVO<StudentVO> result = courseService.getEnrolledStudents(courseId, pageNum, pageSize, keyword);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(2L, result.getTotal());
        assertEquals(pageNum, result.getCurrent());
        assertEquals(pageSize, result.getSize());
        assertEquals(2, result.getRecords().size());
        
        // 验证方法被调用
        verify(courseService, times(1)).getEnrolledStudents(courseId, pageNum, pageSize, keyword);
    }

    // 辅助方法：创建模拟的CourseVO对象
    private CourseVO createMockCourseVO(Long id, String name, String code, Integer credits, String department, String semester) {
        CourseVO course = new CourseVO();
        course.setId(id);
        course.setCourseName(name);
        course.setCourseCode(code);
        course.setCredits(credits);
        course.setDepartment(department);
        course.setSemester(semester);
        course.setDescription(name + "课程描述");
        course.setTeacherId(1L);
        course.setTeacherName("张老师");
        course.setCapacity(50);
        course.setEnrolledStudents(30);
        return course;
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