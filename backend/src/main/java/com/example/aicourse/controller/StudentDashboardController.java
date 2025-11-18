package com.example.aicourse.controller;

import com.example.aicourse.service.StudentService;
import com.example.aicourse.utils.CurrentUserUtil;
import com.example.aicourse.utils.Result;
import com.example.aicourse.vo.MyDashboardVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 聚合后的“学习数据驾驶舱”接口，前端仅需一次请求即可获取首页所需全部卡片数据。
 */
@RestController
@RequestMapping("/api/v1/student")
public class StudentDashboardController {

    private final StudentService studentService;

    @Autowired
    public StudentDashboardController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/my-dashboard")
    public Result<MyDashboardVO> getMyDashboard() {
        Long studentId = CurrentUserUtil.getCurrentUser().getId();
        MyDashboardVO data = studentService.getMyDashboardData(studentId);
        return Result.ok(data);
    }
}
