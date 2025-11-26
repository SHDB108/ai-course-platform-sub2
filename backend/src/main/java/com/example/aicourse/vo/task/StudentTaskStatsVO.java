package com.example.aicourse.vo.task;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentTaskStatsVO {
    private Integer totalTasks;
    private Integer pendingTasks;
    private Integer inProgressTasks;
    private Integer completedTasks;
    private Integer overdueTasks;
    private Double completionRate;
    private Integer thisWeekCompleted;
    private Integer thisMonthCompleted;
}