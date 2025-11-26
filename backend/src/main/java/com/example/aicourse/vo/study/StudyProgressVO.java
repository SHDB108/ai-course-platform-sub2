package com.example.aicourse.vo.study;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 学习进度响应VO
 */
@Data
public class StudyProgressVO {
    private Long id;
    private Long studentId;
    private Long courseId;
    private String courseName;
    
    // 总体进度
    private Integer totalProgress;
    private String status;
    private LocalDateTime lastStudyTime;
    private LocalDateTime startDate;
    private LocalDateTime expectedEndDate;
    
    // 各模块进度
    private ModuleProgress videoProgress;
    private ModuleProgress taskProgress;
    private ModuleProgress examProgress;
    private ModuleProgress knowledgeProgress;
    
    // 学习统计
    private StudyTimeStats studyTimeStats;
    
    // 完成统计
    private CompletionStats completionStats;
    
    @Data
    public static class ModuleProgress {
        private Integer progress;       // 进度百分比
        private Integer completed;      // 已完成数量
        private Integer total;          // 总数量
        private String status;          // 状态
    }
    
    @Data
    public static class StudyTimeStats {
        private Integer totalStudyTime;   // 总学习时长
        private Integer todayStudyTime;   // 今日学习时长
        private Integer weekStudyTime;    // 本周学习时长
        private Integer monthStudyTime;   // 本月学习时长
        private Double averageDaily;      // 日均学习时长
    }
    
    @Data
    public static class CompletionStats {
        private Integer completedVideos;
        private Integer totalVideos;
        private Integer completedTasks;
        private Integer totalTasks;
        private Integer completedExams;
        private Integer totalExams;
        private Integer masteredKnowledge;
        private Integer totalKnowledge;
    }
}