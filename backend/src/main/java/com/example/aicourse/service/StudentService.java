package com.example.aicourse.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.aicourse.dto.student.StudentCreateDTO; // 导入 StudentCreateDTO
import com.example.aicourse.dto.student.StudentUpdateDTO; // 导入 StudentUpdateDTO
import com.example.aicourse.entity.Student;
import com.example.aicourse.vo.PageVO; // 导入 PageVO
import com.example.aicourse.vo.student.ImportResultVO; // 导入 ImportResultVO
import com.example.aicourse.vo.student.StudentVO; // 导入 StudentVO
import com.example.aicourse.vo.student.StudentDashboardStatsVO; // 导入 StudentDashboardStatsVO
import com.example.aicourse.vo.KnowledgeGraphVO;
import com.example.aicourse.vo.MyDashboardVO;
import org.springframework.core.io.Resource; // 导入 Resource
import org.springframework.http.ResponseEntity; // 导入 ResponseEntity
import org.springframework.web.multipart.MultipartFile; // 导入 MultipartFile

import java.io.IOException; // 导入 IOException
import java.util.List; // 导入 List

public interface StudentService extends IService<Student>{
    /**
     * API 3.1 获取学生列表 (分页)
     * @param pageNum 当前页码
     * @param pageSize 每页数量
     * @param keyword 搜索关键词（匹配姓名或学号）
     * @param major 按专业筛选
     * @param grade 按年级筛选
     * @return 分页的学生信息列表
     */
    PageVO<StudentVO> getStudentPage(Long pageNum, Long pageSize, String keyword, String major, String grade, String status);

    /**
     * API 3.2 获取学生详情
     * @param id 学生ID
     * @return 学生详细信息
     */
    StudentVO getStudentDetails(Long id);

    /**
     * API 3.3 新增学生
     * @param dto 学生创建请求DTO
     * @return 新创建的学生ID
     */
    Long createStudent(StudentCreateDTO dto);

    /**
     * API 3.4 更新学生信息
     * @param id 学生ID
     * @param dto 学生更新请求DTO
     * @return 更新成功返回true
     */
    boolean updateStudent(Long id, StudentUpdateDTO dto);

    /**
     * API 3.5 删除学生
     * @param id 学生ID
     * @return 删除成功返回true
     */
    boolean deleteStudent(Long id);

    /**
     * API 3.6 批量导入学生
     * @param file 包含学生信息的Excel或CSV文件
     * @return 导入结果
     * @throws IOException 文件处理异常
     */
    ImportResultVO importStudents(MultipartFile file) throws IOException;

    /**
     * API 3.8 更新学生状态
     * @param id 学生ID
     * @param status 新状态
     * @return 更新成功返回true
     */
    boolean updateStudentStatus(Long id, String status);

    /**
     * API 3.7 导出学生信息
     * @param major 按专业筛选
     * @param grade 按年级筛选
     * @param format 导出文件格式 ("xlsx" 或 "csv")
     * @return 包含文件资源的ResponseEntity
     * @throws IOException 文件处理异常
     */
    ResponseEntity<Resource> exportStudents(String major, String grade, String format) throws IOException;

    /**
     * API 3.8 获取学生选修的课程列表
     * @param studentId 学生ID
     * @param pageNum 当前页码
     * @param pageSize 每页数量
     * @param keyword 搜索关键词
     * @return 学生课程列表
     */
    PageVO<com.example.aicourse.vo.course.CourseVO> getStudentCourses(Long studentId, Long pageNum, Long pageSize, String keyword);
    
    /**
     * API 3.9 获取学生的任务列表
     * @param studentId 学生ID
     * @param pageNum 当前页码
     * @param pageSize 每页数量
     * @param keyword 搜索关键词
     * @param status 任务状态筛选
     * @return 学生任务列表
     */
    PageVO<com.example.aicourse.vo.task.StudentTaskVO> getStudentTasks(Long studentId, Long pageNum, Long pageSize, String keyword, String status);

    /**
     * API 3.10 获取学生仪表板统计数据
     * @param studentId 学生ID
     * @return 学生仪表板统计数据
     */
    StudentDashboardStatsVO getStudentDashboardStats(Long studentId);

    /**
     * API 3.11 获取学生任务统计数据
     * @param studentId 学生ID
     * @return 学生任务统计数据
     */
    com.example.aicourse.vo.task.StudentTaskStatsVO getStudentTaskStats(Long studentId);

    /**
     * 聚合后的学习驾驶舱数据（面向 /api/v1/student/my-dashboard）。
     */
    MyDashboardVO getMyDashboardData(Long studentId);

    /**
     * 获取课程的知识图谱结构（节点+边，用于图可视化）。
     */
    KnowledgeGraphVO getCourseKnowledgeGraph(Long courseId);
}
