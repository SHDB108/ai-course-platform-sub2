package com.example.aicourse.vo.knowledge;

import lombok.Data;

/**
 * API 11.3 学习推荐响应
 */
@Data
public class LearningRecommendationVO {
    private Long id;
    private String recommendationType;
    private Long targetId;
    private String targetName;
    private String reason;
    private AssociatedResourceVO associatedResource;

    @Data
    public static class AssociatedResourceVO {
        private Long id;
        private String filename;
    }
}