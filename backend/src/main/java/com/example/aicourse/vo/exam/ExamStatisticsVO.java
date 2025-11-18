package com.example.aicourse.vo.exam;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Minimal exam statistics returned from assessment service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamStatisticsVO {
    private Integer totalExams;
    private Integer completedExams;
    private Integer pendingExams;
    private Integer expiredExams;
    private Double averageScore;
    private Integer passedExams;
    private Integer failedExams;
}
