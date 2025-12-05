package com.example.aicourse.controller;

import com.example.aicourse.utils.CurrentUserUtil;
import com.example.aicourse.utils.Result;
import com.example.aicourse.dto.knowledge.RecommendationGenerationRequestDTO;
import com.example.aicourse.dto.knowledge.RecommendationStatusUpdateDTO;
import com.example.aicourse.service.RecommendationService;
import com.example.aicourse.vo.knowledge.LearningRecommendationVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @Autowired
    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    /**
     * API 11.3: 获取当前学生的学习推荐
     * @param courseId 课程ID
     * @param type 推荐类型 (可选)
     * @param count 返回数量 (可选)
     * @return 学习推荐VO列表
     */
    @GetMapping
    @PreAuthorize("hasRole('STUDENT')")
    public Result<List<LearningRecommendationVO>> getMyRecommendations(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false, defaultValue = "5") Integer count) {

        Long currentUserId = CurrentUserUtil.getCurrentUser().getId();
        List<LearningRecommendationVO> recommendations = recommendationService.getRecommendations(currentUserId, courseId, type, count);
        return Result.ok(recommendations);
    }

    /**
     * 后台接口: 为指定学生生成新的学习推荐
     * @param request 包含学生ID和课程ID
     * @return 操作结果
     */
    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public Result<String> generateRecommendationsForStudent(@Valid @RequestBody RecommendationGenerationRequestDTO request) {
        // RecommendationService中的方法应为异步，所以这里会立即返回
        recommendationService.generateRecommendationsForStudent(request.getStudentId(), request.getCourseId());
        return Result.ok("学习推荐生成任务已启动，请稍后查看结果。");
    }

    /**
     * API 11.4: 更新学习推荐的状态
     * @param id 推荐记录ID
     * @param dto 包含新状态的请求体
     * @return 操作结果
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<Void> updateRecommendationStatus(
            @PathVariable Long id,
            @Valid @RequestBody RecommendationStatusUpdateDTO dto) {

        // TODO: 这里应该增加一步校验，确保当前用户是这条推荐的所有者
        recommendationService.updateRecommendationStatus(id, dto);
        return Result.ok();
    }

    /**
     * API 11.5: 学生自己生成学习推荐
     * @param courseId 课程ID
     * @return 操作结果
     */
    @PostMapping("/my-recommendations")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<String> generateMyRecommendations(@RequestParam Long courseId) {
        Long currentUserId = CurrentUserUtil.getCurrentUser().getId();
        int count = recommendationService.generateRecommendationsForStudent(currentUserId, courseId);
        return Result.ok("成功生成 " + count + " 条学习推荐");
    }
}