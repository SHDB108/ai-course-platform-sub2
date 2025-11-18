package com.example.aicourse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.aicourse.dto.resource.ProgressDTO;
import com.example.aicourse.entity.VideoProgress;
import com.example.aicourse.repository.VideoProgressMapper;
import com.example.aicourse.service.VideoProgressService;
import com.example.aicourse.vo.resource.VideoProgressVO;
import com.example.aicourse.vo.resource.VideoStudyStatisticsVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class VideoProgressServiceImpl implements VideoProgressService {

    private final VideoProgressMapper mapper;
    private final ObjectMapper objectMapper;

    @Autowired
    public VideoProgressServiceImpl(VideoProgressMapper mapper, ObjectMapper objectMapper) {
        this.mapper = mapper;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void record(Long resId, Long studentId, ProgressDTO dto) {
        LambdaQueryWrapper<VideoProgress> query = new LambdaQueryWrapper<VideoProgress>()
                .eq(VideoProgress::getResourceId, resId)
                .eq(VideoProgress::getStudentId, studentId);
        VideoProgress vp = mapper.selectOne(query);

        if (vp == null) {
            vp = new VideoProgress();
            vp.setResourceId(resId);
            vp.setStudentId(studentId);
        }

        try {
            String progressJson = objectMapper.writeValueAsString(dto);
            vp.setProgress(progressJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("视频进度序列化失败", e);
        }

        if (vp.getCompletion() != null) {
            vp.setCompletion(Math.max(vp.getCompletion(), dto.getCompletion()));
        } else {
            vp.setCompletion(dto.getCompletion());
        }

        if (vp.getId() == null) {
            mapper.insert(vp);
        } else {
            mapper.updateById(vp);
        }
    }

    @Override
    public VideoProgressVO getStudentProgress(Long resourceId, Long studentId) {
        LambdaQueryWrapper<VideoProgress> query = new LambdaQueryWrapper<VideoProgress>()
                .eq(VideoProgress::getResourceId, resourceId)
                .eq(VideoProgress::getStudentId, studentId);
        VideoProgress vp = mapper.selectOne(query);

        if (vp == null) {
            return null;
        }

        VideoProgressVO vo = new VideoProgressVO();
        BeanUtils.copyProperties(vp, vo);
        return vo;
    }

    @Override
    public List<VideoStudyStatisticsVO> getCourseVideoStatistics(Long courseId, Long studentId) {
        List<VideoStudyStatisticsVO> stats = mapper.selectCourseVideoStatistics(courseId);
        stats.forEach(s -> s.setHeatmapData(new ArrayList<>()));
        return stats;
    }
}
