package com.example.aicourse.dto.study;

import lombok.Data;

/**
 * 知识点掌握更新DTO
 */
@Data
public class KnowledgePointProgressUpdateDTO {
    private String masteryLevel;       // 掌握等级: UNKNOWN, LEARNING, PARTIAL, MASTERED, EXPERT
    private Integer masteryScore;      // 掌握分数 (0-100)
    private Integer confidence;        // 置信度 (0-100)
    private Boolean isCorrect;         // 本次是否正确(用于统计)
    private Integer studyTime;         // 本次学习时长(分钟)
    private String learningContext;    // 学习上下文(可选)
    private String notes;              // 学习笔记
}