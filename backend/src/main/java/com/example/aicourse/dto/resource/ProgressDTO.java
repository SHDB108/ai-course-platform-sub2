package com.example.aicourse.dto.resource;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * 前端上报的视频播放进度结构
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProgressDTO {
    /** 资源ID */
    private Long resourceId;
    /** 已播放时长（秒） */
    private Double elapsed;
    /** 视频总时长（秒） */
    private Double duration;
    /** 已观看片段列表，每个 entry 为 [startSec, endSec] */
    private List<List<Double>> segments;
    /** 完成度（0–100） */
    private Integer completion;
}