package com.example.aicourse.vo.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lightweight VO describing knowledge-point mastery statistics.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgePointPerformanceVO {
    private Long knowledgePointId;
    private String knowledgePointName;
    private String masteryLevel;
    private Double averageScore;
}
