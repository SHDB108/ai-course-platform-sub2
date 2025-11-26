package com.example.aicourse.service;

import com.example.aicourse.dto.resource.ProgressDTO;
import com.example.aicourse.vo.resource.VideoProgressVO;
import com.example.aicourse.vo.resource.VideoStudyStatisticsVO;

import java.util.List;
import java.util.Map;

public interface VideoProgressService {
    /**
     * 记录视频播放进度 (对应 API 7.7)
     */
    void record(Long resId, Long studentId, ProgressDTO dto);

    /**
     * 获取学生视频学习进度 (对应 API 7.8)
     */
    VideoProgressVO getStudentProgress(Long resourceId, Long studentId);

    /**
     * 获取课程视频学习统计 (对应 API 7.9)
     */
    List<VideoStudyStatisticsVO> getCourseVideoStatistics(Long courseId, Long studentId);

    /**
     * 汇总学生在某课程下的视频学习进度。
     * @return 包含 totalVideos/completedVideos/averageProgress 键的Map
     */
    Map<String, Integer> getStudentCourseProgressSummary(Long studentId, Long courseId);
}
