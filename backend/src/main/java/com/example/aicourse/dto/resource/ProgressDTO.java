package com.example.aicourse.dto.resource;

import lombok.Data;

import java.util.List;

/**
 * 前端上报的视频播放进度结构
 */
@Data
public class ProgressDTO {
    /** 已播放时长（秒） */
    private Double elapsed;
    /** 视频总时长（秒） */
    private Double duration;
    /** 已观看片段列表，每个 entry 为 [startSec, endSec] */
    private List<List<Double>> segments;
    /** 完成度（0–100） */
    private Integer completion;
}