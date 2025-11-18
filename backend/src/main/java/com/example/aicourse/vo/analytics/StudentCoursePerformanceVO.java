package com.example.aicourse.vo.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Minimal representation of a student's course performance summary.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentCoursePerformanceVO {
    private Long courseId;
    private String courseName;
    private Double completionRate;
    private Double averageScore;
}
