// src/main/java/com/example/aicourse/dto/knowledge/RecommendationGenerationRequestDTO.java
package com.example.aicourse.dto.knowledge;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RecommendationGenerationRequestDTO {

    @NotNull(message = "学生ID不能为空")
    private Long studentId;

    @NotNull(message = "课程ID不能为空")
    private Long courseId;
}