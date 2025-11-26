package com.example.aicourse.vo.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simplified task completion breakdown returned by Assessment service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskCompletionSummaryVO {
    private Long taskId;
    private String taskTitle;
    private Integer submittedCount;
    private Integer onTimeCount;
    private Double averageScore;
}
