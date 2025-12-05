package com.example.aicourse.vo.knowledge;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * API 11.3 学习推荐响应
 */
@Data
public class LearningRecommendationVO {
    private Long id;
    private String type;
    private Long targetId;
    private String targetName;
    private String reason;
    private AssociatedResourceVO associatedResource;

    private String priority;    // 对应 SQL: 'MEDIUM' AS priority
    private String status;      // 对应 SQL: ... AS status
    private String title;       // 对应 SQL: ... AS title
    private LocalDateTime createdAt; // 对应 SQL: r.gmt_create AS createdAt

    @Data
    public static class AssociatedResourceVO {
        private Long id;
        private String filename;
    }
}