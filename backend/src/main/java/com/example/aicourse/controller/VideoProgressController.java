package com.example.aicourse.controller;

import com.example.aicourse.dto.resource.ProgressDTO;
import com.example.aicourse.service.VideoProgressService;
import com.example.aicourse.utils.CurrentUserUtil;
import com.example.aicourse.utils.Result;
import com.example.aicourse.vo.resource.VideoProgressVO;
import com.example.aicourse.vo.resource.VideoStudyStatisticsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/videos") //  路径根据API文档建议修改
@Validated
public class VideoProgressController {

    private final VideoProgressService service;

    @Autowired
    public VideoProgressController(VideoProgressService service) {
        this.service = service;
    }

    /**
     * API 7.7 上报视频学习进度（自动识别当前学生）。
     */
    @PostMapping("/{resourceId}/progress")
    public Result<Void> report(@PathVariable Long resourceId, @RequestBody ProgressDTO dto) {
        Long studentId = CurrentUserUtil.getCurrentUser().getId();
        service.record(resourceId, studentId, dto);
        return Result.ok();
    }

    /**
     * API 7.8 获取学生视频学习进度（仅当前学生）。
     */
    @GetMapping("/{resourceId}/progress")
    public Result<VideoProgressVO> getProgress(@PathVariable Long resourceId) {
        Long studentId = CurrentUserUtil.getCurrentUser().getId();
        VideoProgressVO vo = service.getStudentProgress(resourceId, studentId);
        return Result.ok(vo);
    }

    /**
     * API 7.9 获取课程视频学习统计，可通过 personalView 切换为“仅自己”。
     */
    @GetMapping("/course/{courseId}/statistics")
    public Result<List<VideoStudyStatisticsVO>> getStatistics(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "false") boolean personalView) {
        Long studentId = personalView ? CurrentUserUtil.getCurrentUser().getId() : null;
        List<VideoStudyStatisticsVO> list = service.getCourseVideoStatistics(courseId, studentId);
        return Result.ok(list);
    }
}
