package com.example.aicourse.service;

import com.example.aicourse.dto.knowledge.RecommendationStatusUpdateDTO;
import com.example.aicourse.vo.knowledge.LearningRecommendationVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RecommendationService {

    /**
     * 【新增】为学生生成个性化学习推荐
     * <p>
     * 此方法是推荐系统的核心引擎，应为异步或后台定时任务调用。
     * 它会分析学生的整体表现（如薄弱知识点），然后调用LLM生成具体的学习建议，
     * 并将这些建议持久化到 t_learning_recommendation 表中。
     *
     * @param studentId 需要生成推荐的学生ID
     * @param courseId 相关的课程ID
     * @return 生成的推荐数量
     */
    int generateRecommendationsForStudent(Long studentId, Long courseId);


    /**
     * 获取学习推荐 (API 11.3)
     * <p>
     * 此方法从数据库中读取已经为学生生成好的推荐内容。
     *
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @param recommendationType 推荐类型 (可选)
     * @param count 需要获取的推荐数量
     * @return 学习推荐VO列表
     */
    List<LearningRecommendationVO> getRecommendations(Long studentId, Long courseId, String recommendationType, Integer count);

    /**
     * 更新推荐状态 (API 11.4)
     */
    void updateRecommendationStatus(Long recommendationId, RecommendationStatusUpdateDTO dto);
}