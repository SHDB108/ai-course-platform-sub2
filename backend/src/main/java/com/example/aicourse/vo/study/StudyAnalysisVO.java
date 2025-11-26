package com.example.aicourse.vo.study;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 学习分析响应VO
 */
@Data
public class StudyAnalysisVO {
    private Long studentId;
    private Long courseId;
    private String courseName;
    
    // 整体统计
    private OverallStats overallStats;
    
    // 时间分析
    private TimeAnalysis timeAnalysis;
    
    // 进度分析
    private ProgressAnalysis progressAnalysis;
    
    // 知识点掌握分析
    private KnowledgeAnalysis knowledgeAnalysis;
    
    // 学习行为分析
    private BehaviorAnalysis behaviorAnalysis;
    
    // 预测和建议
    private List<String> predictions;
    private List<String> recommendations;
    
    @Data
    public static class OverallStats {
        private Integer totalCourses;           // 总课程数
        private Integer activeCourses;          // 活跃课程数
        private Integer completedCourses;       // 已完成课程数
        private Integer totalStudyHours;        // 总学习时长
        private Double averageProgress;         // 平均进度
        private String overallPerformance;      // 整体表现: EXCELLENT, GOOD, AVERAGE, POOR
    }
    
    @Data
    public static class TimeAnalysis {
        private Integer todayMinutes;           // 今日学习时长
        private Integer weekMinutes;            // 本周学习时长
        private Integer monthMinutes;           // 本月学习时长
        private Double dailyAverage;            // 日均学习时长
        private List<DailyStudyTime> dailyTrend;     // 每日学习趋势
        private String peakStudyTime;           // 学习高峰时段
        private Double consistency;             // 学习一致性评分
    }
    
    @Data
    public static class ProgressAnalysis {
        private Double completionRate;          // 完成率
        private String progressTrend;           // 进度趋势: ACCELERATING, STEADY, SLOWING, STAGNANT
        private Integer estimatedDaysToComplete; // 预计完成天数
        private List<ModuleProgress> moduleBreakdown; // 各模块进度分解
    }
    
    @Data
    public static class KnowledgeAnalysis {
        private Integer totalKnowledgePoints;   // 总知识点数
        private Integer masteredPoints;         // 已掌握知识点数
        private Integer learningPoints;         // 学习中知识点数
        private Integer weakPoints;             // 薄弱知识点数
        private List<WeakArea> weakAreas;       // 薄弱领域
        private List<String> strongAreas;       // 强项领域
        private Double masteryRate;             // 掌握率
    }
    
    @Data
    public static class BehaviorAnalysis {
        private String preferredStudyTime;      // 偏好学习时间
        private String studyPattern;            // 学习模式
        private Integer averageSessionDuration; // 平均学习时长
        private String devicePreference;        // 设备偏好
        private Double engagementScore;         // 参与度评分
        private List<String> learningHabits;    // 学习习惯
    }
    
    @Data
    public static class DailyStudyTime {
        private String date;
        private Integer minutes;
        private String sessionType;
    }
    
    @Data
    public static class ModuleProgress {
        private String moduleName;
        private Integer progress;
        private String status;
        private Integer timeSpent;
    }
    
    @Data
    public static class WeakArea {
        private String knowledgePointName;
        private String difficulty;
        private Integer attemptCount;
        private Double successRate;
        private String recommendedAction;
    }
}