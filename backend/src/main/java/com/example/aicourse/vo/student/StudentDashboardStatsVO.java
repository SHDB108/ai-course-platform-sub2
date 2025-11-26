package com.example.aicourse.vo.student;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentDashboardStatsVO {
    private Integer myCourses;
    private Integer pendingTasks;
    private Integer weeklySubmissions;
    private Integer unreadMessages;
    private TodoItemsVO todoItems;
    private Integer projects;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TodoItemsVO {
        private Integer pending;
        private Integer total;
    }
}