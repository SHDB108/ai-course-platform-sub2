package com.example.aicourse.controller;

import com.example.aicourse.service.StudentService;
import com.example.aicourse.utils.Result;
import com.example.aicourse.vo.KnowledgeGraphVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class KnowledgeGraphController {

    private final StudentService studentService;

    @Autowired
    public KnowledgeGraphController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/api/v1/knowledge-graph/{courseId}")
    public Result<KnowledgeGraphVO> getCourseKnowledgeGraph(@PathVariable Long courseId) {
        KnowledgeGraphVO graph = studentService.getCourseKnowledgeGraph(courseId);
        return Result.ok(graph);
    }
}
