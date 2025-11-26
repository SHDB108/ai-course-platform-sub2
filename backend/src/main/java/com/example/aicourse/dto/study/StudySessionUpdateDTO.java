package com.example.aicourse.dto.study;

import lombok.Data;

/**
 * 学习会话更新DTO
 */
@Data
public class StudySessionUpdateDTO {
    private Integer duration;           // 学习时长(分钟)
    private Integer effectiveTime;      // 有效学习时长(分钟)
    private String progressData;        // 进度数据(JSON)
    private Integer completionRate;     // 完成率 (0-100)
    private String interactionData;     // 交互数据(JSON)
    private String result;              // 学习结果: COMPLETED, PARTIAL, SKIPPED, FAILED
    private Integer score;              // 得分
    private String notes;               // 学习笔记
    private String feedback;            // 反馈信息
}