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
 * Student Dashboard Controller.
 * Aggregates data for the student's "Learning Cockpit".
 */
@RestController
@RequestMapping("/api/v1/student")
public class StudentDashboardController {

    private final StudentService studentService;

    @Autowired
    public StudentDashboardController(StudentService studentService) {
        this.studentService = studentService;
    }

    // FIXED: Changed path from "/my-dashboard" to "/dashboard/my" to match frontend request
    @GetMapping("/dashboard/my")
    public Result<MyDashboardVO> getMyDashboard() {
        Long studentId = CurrentUserUtil.getCurrentUser().getId();
        MyDashboardVO data = studentService.getMyDashboardData(studentId);
        return Result.ok(data);
    }
}