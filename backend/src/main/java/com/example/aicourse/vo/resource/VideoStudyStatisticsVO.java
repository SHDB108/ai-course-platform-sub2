package com.example.aicourse.vo.resource;

import java.util.List;
import lombok.Data;

/**
 * API 7.9 视频学习统计响应
 */
@Data
public class VideoStudyStatisticsVO {
    private Long resourceId;
    private String filename;
    private int totalViews;
    private double averageCompletion;
    private List<HeatmapData> heatmapData;

    @Data
    public static class HeatmapData {
        private String segment;
        private int views;
    }
}