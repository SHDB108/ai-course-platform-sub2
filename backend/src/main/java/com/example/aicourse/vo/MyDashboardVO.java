package com.example.aicourse.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Aggregated payload for /api/v1/student/my-dashboard.
 * Combines stats, summaries, and snippet lists used on the “学习数据驾驶舱”.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyDashboardVO {

    private DashboardStatsVO stats;
    private TaskStatsVO taskSummary;
    private List<RecentCourseVO> recentCourses;
    private List<UpcomingTaskVO> pendingTasks;
    private List<CourseProgressSummaryVO> progressSummary;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardStatsVO {
        private Integer myCourses;
        private Integer pendingTasks;
        private Integer weeklySubmissions;
        private Integer unreadMessages;
        private Integer projects;
        private TodoItems todoItems;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class TodoItems {
            private Integer pending;
            private Integer total;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskStatsVO {
        private Integer totalTasks;
        private Integer pendingTasks;
        private Integer inProgressTasks;
        private Integer completedTasks;
        private Integer overdueTasks;
        private Double completionRate;
        private Integer thisWeekCompleted;
        private Integer thisMonthCompleted;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentCourseVO {
        private Long id;
        private String courseName;
        private String teacherName;
        private String coverUrl;
        private Integer credits;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpcomingTaskVO {
        private Long taskId;
        private String taskTitle;
        private String courseName;
        private LocalDateTime deadline;
        private Boolean isOverdue;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseProgressSummaryVO {
        private Long courseId;
        private String courseName;
        private Integer totalProgress; // percentage 0-100
    }
}
