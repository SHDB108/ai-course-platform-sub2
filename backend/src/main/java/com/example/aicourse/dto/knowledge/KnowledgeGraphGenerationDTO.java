package com.example.aicourse.dto.knowledge;

import java.util.List;
import lombok.Data;

/**
 * API 11.2 知识图谱生成请求
 */
@Data
public class KnowledgeGraphGenerationDTO {
    private List<Long> resourceIds;
    private Boolean autoUpdate;
    private String generationStrategy;
}