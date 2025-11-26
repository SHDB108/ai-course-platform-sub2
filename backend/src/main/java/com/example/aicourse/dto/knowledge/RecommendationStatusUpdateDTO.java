package com.example.aicourse.dto.knowledge;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * API 11.4 标记推荐状态请求
 */
@Data
public class RecommendationStatusUpdateDTO {
    @NotBlank
    private String status; // "VIEWED", "DISMISSED"
}