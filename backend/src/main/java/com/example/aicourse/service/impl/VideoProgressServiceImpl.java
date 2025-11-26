package com.example.aicourse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.aicourse.client.KnowledgeGraphClient;
import com.example.aicourse.dto.resource.ProgressDTO;
import com.example.aicourse.entity.VideoProgress;
import com.example.aicourse.repository.VideoProgressMapper;
import com.example.aicourse.service.VideoProgressService;
import com.example.aicourse.vo.resource.VideoProgressVO;
import com.example.aicourse.vo.resource.ResourceVO;
import com.example.aicourse.vo.resource.VideoStudyStatisticsVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class VideoProgressServiceImpl implements VideoProgressService {

    private final VideoProgressMapper mapper;
    private final ObjectMapper objectMapper;
    private final KnowledgeGraphClient knowledgeGraphClient;

    @Autowired
    public VideoProgressServiceImpl(VideoProgressMapper mapper,
                                    ObjectMapper objectMapper,
                                    KnowledgeGraphClient knowledgeGraphClient) {
        this.mapper = mapper;
        this.objectMapper = objectMapper;
        this.knowledgeGraphClient = knowledgeGraphClient;
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
        List<ResourceVO> resources = knowledgeGraphClient.getCourseResources(courseId);
        if (CollectionUtils.isEmpty(resources)) {
            return Collections.emptyList();
        }

        List<ResourceVO> videoResources = resources.stream()
                .filter(resource -> "VIDEO".equalsIgnoreCase(resource.getType()))
                .collect(Collectors.toList());
        if (videoResources.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> resourceIds = videoResources.stream()
                .map(ResourceVO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (resourceIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<VideoProgress> progressList = mapper.selectByResourceIds(resourceIds);
        if (progressList == null) {
            progressList = Collections.emptyList();
        }

        Map<Long, List<VideoProgress>> progressByResource = progressList.stream()
                .collect(Collectors.groupingBy(VideoProgress::getResourceId));

        List<VideoStudyStatisticsVO> stats = new ArrayList<>();
        for (ResourceVO resource : videoResources) {
            List<VideoProgress> resourceProgress = progressByResource.getOrDefault(resource.getId(), Collections.emptyList());
            VideoStudyStatisticsVO vo = new VideoStudyStatisticsVO();
            vo.setResourceId(resource.getId());
            vo.setFilename(resource.getFilename());
            vo.setTotalViews(resourceProgress.size());

            double avgCompletion = resourceProgress.stream()
                    .map(VideoProgress::getCompletion)
                    .filter(Objects::nonNull)
                    .mapToInt(Integer::intValue)
                    .average()
                    .orElse(0.0);
            vo.setAverageCompletion(avgCompletion);
            vo.setHeatmapData(new ArrayList<>());
            stats.add(vo);
        }

        return stats;
    }

    @Override
    public Map<String, Integer> getStudentCourseProgressSummary(Long studentId, Long courseId) {
        Map<String, Integer> summary = new HashMap<>();
        summary.put("totalVideos", 0);
        summary.put("completedVideos", 0);
        summary.put("averageProgress", 0);

        List<ResourceVO> resources = knowledgeGraphClient.getCourseResources(courseId);
        if (CollectionUtils.isEmpty(resources)) {
            return summary;
        }

        List<Long> resourceIds = resources.stream()
                .filter(resource -> resource.getType() != null && resource.getType().equalsIgnoreCase("VIDEO"))
                .map(ResourceVO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (resourceIds.isEmpty()) {
            return summary;
        }

        summary.put("totalVideos", resourceIds.size());

        List<VideoProgress> progressList = mapper.selectByStudentAndResourceIds(studentId, resourceIds);
        if (progressList == null) {
            progressList = Collections.emptyList();
        }

        int completedVideos = (int) progressList.stream()
                .map(VideoProgress::getCompletion)
                .filter(Objects::nonNull)
                .filter(completion -> completion >= 90) // treat >=90% completion as finished
                .count();
        summary.put("completedVideos", completedVideos);

        int sumCompletion = progressList.stream()
                .map(VideoProgress::getCompletion)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        int totalVideos = summary.get("totalVideos");
        int averageProgress = totalVideos > 0
                ? (int) Math.round(sumCompletion / (double) totalVideos)
                : 0;
        summary.put("averageProgress", averageProgress);

        return summary;
    }
}
