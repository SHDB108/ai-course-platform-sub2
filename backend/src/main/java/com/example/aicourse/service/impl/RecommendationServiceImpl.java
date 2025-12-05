package com.example.aicourse.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.aicourse.client.KnowledgeGraphClient;
import com.example.aicourse.dto.knowledge.RecommendationStatusUpdateDTO;
import com.example.aicourse.entity.KnowledgePoint;
import com.example.aicourse.entity.LearningRecommendation;
import com.example.aicourse.entity.KnowledgePointProgress;
import com.example.aicourse.entity.VideoProgress;
import com.example.aicourse.repository.LearningRecommendationMapper;
import com.example.aicourse.repository.KnowledgePointProgressMapper;
import com.example.aicourse.repository.VideoProgressMapper;
import com.example.aicourse.service.LlmService;
import com.example.aicourse.service.RecommendationService;
import com.example.aicourse.service.VideoProgressService;
import com.example.aicourse.vo.KnowledgeGraphVO;
import com.example.aicourse.vo.analytics.KnowledgePointPerformanceVO;
import com.example.aicourse.vo.knowledge.LearningRecommendationVO;
import com.example.aicourse.vo.resource.ResourceVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RecommendationServiceImpl implements RecommendationService {

    private final LearningRecommendationMapper recommendationMapper;
    private final LlmService llmService;
    private final KnowledgePointProgressMapper knowledgePointProgressMapper;
    private final KnowledgeGraphClient knowledgeGraphClient;
    private final VideoProgressService videoProgressService;
    private final VideoProgressMapper videoProgressMapper;
    private final ObjectMapper objectMapper;

    // 视频行为分析阈值配置
    private static final int SEGMENT_REPLAY_THRESHOLD = 3; // 同一段重复观看3次以上视为困难
    private static final double TIME_RATIO_THRESHOLD = 1.5; // 总观看时长超过视频时长1.5倍视为困难
    private static final int MIN_SEGMENTS_FOR_STRUGGLE = 5; // 最少5个片段才判定为困难

    @Autowired
    public RecommendationServiceImpl(LearningRecommendationMapper recommendationMapper,
                                   LlmService llmService,
                                   KnowledgePointProgressMapper knowledgePointProgressMapper,
                                   KnowledgeGraphClient knowledgeGraphClient,
                                   VideoProgressService videoProgressService,
                                   VideoProgressMapper videoProgressMapper,
                                   ObjectMapper objectMapper) {
        this.recommendationMapper = recommendationMapper;
        this.llmService = llmService;
        this.knowledgePointProgressMapper = knowledgePointProgressMapper;
        this.knowledgeGraphClient = knowledgeGraphClient;
        this.videoProgressService = videoProgressService;
        this.videoProgressMapper = videoProgressMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public int generateRecommendationsForStudent(Long studentId, Long courseId) {
        log.info("开始为学生ID: {} 在课程ID: {} 中生成学习推荐（基于全知识点掌握度分析）。", studentId, courseId);

        // 1. 获取学生在该课程下的所有知识点掌握进度
        List<KnowledgePointProgress> allProgress = knowledgePointProgressMapper.findByStudentAndCourse(studentId, courseId);

        if (CollectionUtils.isEmpty(allProgress)) {
            log.info("学生ID: {} 当前无学习数据，生成基础推荐。", studentId);
            return generateBasicRecommendations(studentId, courseId);
        }

        // 2. 构建知识点掌握度概览（包含所有知识点的掌握情况）
        StringBuilder profileBuilder = new StringBuilder();
        profileBuilder.append("课程知识点掌握情况：\n");

        for (KnowledgePointProgress progress : allProgress) {
            // 获取知识点名称
            String knowledgePointName = knowledgeGraphClient.getKnowledgePoint(progress.getKnowledgePointId())
                    .map(KnowledgePoint::getName)
                    .orElse("未知知识点");

            String masteryLevel = progress.getMasteryLevel() != null ? progress.getMasteryLevel() : "UNKNOWN";
            String masteryLevelChinese = translateMasteryLevel(masteryLevel);

            profileBuilder.append(String.format("- %s: %s", knowledgePointName, masteryLevelChinese));

            // 如果有掌握分数，也加上
            if (progress.getMasteryScore() != null) {
                profileBuilder.append(String.format(" (掌握度: %d/100)", progress.getMasteryScore()));
            }
            profileBuilder.append("\n");
        }

        String studentProfile = profileBuilder.toString();
        log.info("学生掌握度概览:\n{}", studentProfile);

        // 3. 构建提示词，要求LLM基于完整的掌握情况给出综合建议
        String prompt = buildComprehensiveRecommendationPrompt(studentProfile, courseId);

        // 4. 调用LLM生成建议（期望返回JSON数组格式）
        List<String> suggestions;
        try {
            suggestions = llmService.generateJson(prompt, new TypeReference<List<String>>() {});

            if (CollectionUtils.isEmpty(suggestions)) {
                log.warn("LLM返回的建议列表为空，使用降级策略");
                suggestions = generateFallbackSuggestions(allProgress);
            }
        } catch (Exception e) {
            log.error("调用LLM生成综合建议失败，使用降级策略", e);
            suggestions = generateFallbackSuggestions(allProgress);
        }

        // 5. 标记旧推荐为过时
        LambdaUpdateWrapper<LearningRecommendation> updateWrapper = Wrappers.<LearningRecommendation>lambdaUpdate()
                .eq(LearningRecommendation::getStudentId, studentId)
                .eq(LearningRecommendation::getCourseId, courseId)
                .eq(LearningRecommendation::getIsDismissed, 0)
                .set(LearningRecommendation::getIsDismissed, 2);
        recommendationMapper.update(null, updateWrapper);

        // 6. 保存新推荐到数据库
        int count = 0;
        for (String suggestion : suggestions) {
            if (suggestion == null || suggestion.isBlank()) {
                continue;
            }

            LearningRecommendation recommendation = new LearningRecommendation();
            recommendation.setStudentId(studentId);
            recommendation.setCourseId(courseId);
            recommendation.setRecommendationType("COMPREHENSIVE"); // 新类型：综合建议
            recommendation.setTargetId(null); // 不针对单个知识点
            recommendation.setReason(suggestion);
            recommendation.setIsDismissed(0);

            recommendationMapper.insert(recommendation);
            count++;
            log.info("生成综合学习建议 #{}: {}", count, suggestion);
        }

        log.info("成功为学生ID: {} 生成了 {} 条综合学习推荐。", studentId, count);
        return count;
    }

    /**
     * 构建综合推荐提示词
     */
    private String buildComprehensiveRecommendationPrompt(String studentProfile, Long courseId) {
        return String.format("""
            你是一位专业的AI学习顾问。请根据学生在课程中的完整知识点掌握情况，提供个性化的学习建议。

            %s

            请基于以上掌握情况，生成3条简短、具体且可操作的学习建议。要求：
            1. 考虑学生的整体学习状态，不要只关注单个薄弱点
            2. 建议应该有优先级，先巩固基础再提升难点
            3. 语言友好、鼓励性强
            4. 每条建议控制在30字以内

            请严格按照以下JSON格式返回（返回一个字符串数组）：
            ["建议1", "建议2", "建议3"]

            不要包含任何其他文字说明，只返回JSON数组。
            """, studentProfile);
    }

    /**
     * 翻译掌握等级为中文
     */
    private String translateMasteryLevel(String masteryLevel) {
        return switch (masteryLevel) {
            case "EXPERT" -> "精通";
            case "MASTERED" -> "已掌握";
            case "PARTIAL" -> "部分掌握";
            case "LEARNING" -> "学习中";
            case "UNKNOWN" -> "未学习";
            default -> masteryLevel;
        };
    }

    /**
     * 生成降级建议（当LLM调用失败时）
     */
    private List<String> generateFallbackSuggestions(List<KnowledgePointProgress> allProgress) {
        List<String> suggestions = new ArrayList<>();

        // 统计各掌握等级的知识点数量
        long weakCount = allProgress.stream()
                .filter(p -> "LEARNING".equals(p.getMasteryLevel()) || "PARTIAL".equals(p.getMasteryLevel()))
                .count();
        long masteredCount = allProgress.stream()
                .filter(p -> "MASTERED".equals(p.getMasteryLevel()) || "EXPERT".equals(p.getMasteryLevel()))
                .count();

        if (weakCount > masteredCount) {
            suggestions.add("建议优先巩固基础知识点，打好学习基础");
            suggestions.add("针对薄弱环节多做练习，加深理解");
            suggestions.add("定期复习已学内容，建立知识体系");
        } else if (masteredCount > 0) {
            suggestions.add("你的基础掌握不错，可以尝试更有挑战性的内容");
            suggestions.add("继续保持良好的学习习惯，稳步提升");
            suggestions.add("多做综合练习，融会贯通各知识点");
        } else {
            suggestions.add("开始学习本课程的基础知识点，建立扎实的理论基础");
            suggestions.add("定期复习已学内容，加深理解和记忆");
            suggestions.add("积极参与课堂讨论，多与同学交流学习心得");
        }

        return suggestions;
    }

    /**
     * 分析学生在课程中的视频观看行为
     * @return Map<VideoResourceId, VideoBehaviorInsight>
     */
    private Map<Long, VideoBehaviorInsight> analyzeStudentVideoBehavior(Long studentId, Long courseId) {
        log.info("分析学生ID: {} 在课程ID: {} 的视频观看行为", studentId, courseId);

        Map<Long, VideoBehaviorInsight> insightMap = new HashMap<>();

        try {
            // 获取课程的所有视频资源
            List<ResourceVO> resources = knowledgeGraphClient.getCourseResources(courseId);
            if (CollectionUtils.isEmpty(resources)) {
                log.warn("课程ID: {} 没有资源", courseId);
                return insightMap;
            }

            List<Long> videoResourceIds = resources.stream()
                    .filter(r -> "VIDEO".equalsIgnoreCase(r.getType()))
                    .map(ResourceVO::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (videoResourceIds.isEmpty()) {
                log.warn("课程ID: {} 没有视频资源", courseId);
                return insightMap;
            }

            // 获取学生的视频观看记录
            List<VideoProgress> progressList = videoProgressMapper.selectByStudentAndResourceIds(studentId, videoResourceIds);
            if (CollectionUtils.isEmpty(progressList)) {
                log.info("学生ID: {} 在课程ID: {} 还没有视频观看记录", studentId, courseId);
                return insightMap;
            }

            // 分析每个视频的观看行为
            for (VideoProgress progress : progressList) {
                try {
                    VideoBehaviorInsight insight = analyzeVideoBehavior(progress);
                    if (insight != null) {
                        insightMap.put(progress.getResourceId(), insight);

                        if (insight.isDifficult()) {
                            log.info("检测到困难视频 - ResourceID: {}, 原因: {}",
                                    progress.getResourceId(), insight.getDifficultyReason());
                        }
                    }
                } catch (Exception e) {
                    log.error("分析视频进度失败 - ResourceID: {}", progress.getResourceId(), e);
                }
            }

        } catch (Exception e) {
            log.error("分析学生视频行为时发生异常", e);
        }

        return insightMap;
    }

    /**
     * 分析单个视频的观看行为
     * 规则：
     * 1. 同一片段重复观看次数超过阈值
     * 2. 观看片段总数超过阈值
     * 3. 总观看时长显著超过视频时长
     */
    private VideoBehaviorInsight analyzeVideoBehavior(VideoProgress progress) {
        VideoBehaviorInsight insight = new VideoBehaviorInsight();
        insight.setResourceId(progress.getResourceId());
        insight.setCompletion(progress.getCompletion());

        if (progress.getProgress() == null || progress.getProgress().isBlank()) {
            return insight;
        }

        try {
            // 解析progress JSON
            Map<String, Object> progressData = objectMapper.readValue(
                    progress.getProgress(),
                    new TypeReference<Map<String, Object>>() {}
            );

            Integer elapsed = (Integer) progressData.get("elapsed");
            List<List<Integer>> segments = (List<List<Integer>>) progressData.get("segments");

            if (segments == null || segments.isEmpty()) {
                return insight;
            }

            insight.setTotalElapsedTime(elapsed != null ? elapsed : 0);
            insight.setSegmentCount(segments.size());

            // 规则1: 检查片段数量是否过多（表示反复观看）
            if (segments.size() >= MIN_SEGMENTS_FOR_STRUGGLE) {
                insight.setDifficult(true);
                insight.setDifficultyReason("反复观看多个片段（共" + segments.size() + "个片段）");
                insight.setReplayCount(segments.size());
            }

            // 规则2: 分析是否有重复观看的片段
            Map<String, Integer> segmentReplayCount = new HashMap<>();
            for (List<Integer> segment : segments) {
                if (segment.size() >= 2) {
                    String key = segment.get(0) + "-" + segment.get(1);
                    segmentReplayCount.put(key, segmentReplayCount.getOrDefault(key, 0) + 1);
                }
            }

            int maxReplayCount = segmentReplayCount.values().stream()
                    .max(Integer::compareTo)
                    .orElse(0);

            if (maxReplayCount >= SEGMENT_REPLAY_THRESHOLD) {
                insight.setDifficult(true);
                insight.setDifficultyReason(
                    insight.getDifficultyReason() != null
                        ? insight.getDifficultyReason() + "；同一片段重复观看" + maxReplayCount + "次"
                        : "同一片段重复观看" + maxReplayCount + "次"
                );
                insight.setReplayCount(maxReplayCount);
            }

            // 规则3: 计算总观看时长（所有片段的时长总和）
            int totalWatchTime = 0;
            for (List<Integer> segment : segments) {
                if (segment.size() >= 2) {
                    totalWatchTime += (segment.get(1) - segment.get(0));
                }
            }

            if (elapsed != null && elapsed > 0) {
                double timeRatio = (double) totalWatchTime / elapsed;
                if (timeRatio >= TIME_RATIO_THRESHOLD) {
                    insight.setDifficult(true);
                    insight.setDifficultyReason(
                        insight.getDifficultyReason() != null
                            ? insight.getDifficultyReason() + "；观看时长为视频时长的" + String.format("%.1f", timeRatio) + "倍"
                            : "观看时长为视频时长的" + String.format("%.1f", timeRatio) + "倍"
                    );
                }
            }

        } catch (Exception e) {
            log.error("解析视频进度JSON失败 - ResourceID: {}", progress.getResourceId(), e);
        }

        return insight;
    }

    /**
     * 构建视频资源到知识点的映射关系
     * 策略：通过文件名关键词匹配知识点名称
     */
    private Map<Long, Set<Long>> buildVideoToKnowledgePointMapping(Long courseId, Set<Long> videoResourceIds) {
        Map<Long, Set<Long>> mapping = new HashMap<>();

        if (videoResourceIds.isEmpty()) {
            return mapping;
        }

        try {
            // 获取课程的知识图谱
            KnowledgeGraphVO graph = knowledgeGraphClient.getCourseGraph(courseId);
            if (graph == null || CollectionUtils.isEmpty(graph.getNodes())) {
                log.warn("课程ID: {} 没有知识图谱", courseId);
                return mapping;
            }

            // 构建节点ID到节点的映射
            Map<String, KnowledgeGraphVO.NodeVO> nodeMap = graph.getNodes().stream()
                    .collect(Collectors.toMap(KnowledgeGraphVO.NodeVO::getId, node -> node));

            // 获取视频资源信息
            List<ResourceVO> resources = knowledgeGraphClient.getCourseResources(courseId);
            Map<Long, ResourceVO> resourceMap = resources.stream()
                    .filter(r -> videoResourceIds.contains(r.getId()))
                    .collect(Collectors.toMap(ResourceVO::getId, r -> r));

            // 对每个视频资源，尝试匹配知识点
            for (Long videoId : videoResourceIds) {
                ResourceVO resource = resourceMap.get(videoId);
                if (resource == null || resource.getFilename() == null) {
                    continue;
                }

                Set<Long> matchedKpIds = new HashSet<>();
                String filename = resource.getFilename().toLowerCase();

                // 尝试通过文件名匹配知识点
                for (KnowledgeGraphVO.NodeVO node : graph.getNodes()) {
                    String nodeName = node.getName().toLowerCase();

                    // 策略1: 文件名包含知识点名称
                    if (filename.contains(nodeName) || nodeName.contains(filename.replace(".mp4", "").replace("_", " "))) {
                        try {
                            matchedKpIds.add(Long.parseLong(node.getId()));
                        } catch (NumberFormatException e) {
                            log.warn("节点ID非数字: {}", node.getId());
                        }
                    }

                    // 策略2: 提取关键词匹配（简化版）
                    String[] kpKeywords = nodeName.split("[\\s,，、]");
                    for (String keyword : kpKeywords) {
                        if (keyword.length() >= 2 && filename.contains(keyword)) {
                            try {
                                matchedKpIds.add(Long.parseLong(node.getId()));
                            } catch (NumberFormatException e) {
                                log.warn("节点ID非数字: {}", node.getId());
                            }
                            break;
                        }
                    }
                }

                if (!matchedKpIds.isEmpty()) {
                    mapping.put(videoId, matchedKpIds);
                    log.debug("视频 {} 映射到知识点: {}", resource.getFilename(), matchedKpIds);
                }
            }

        } catch (Exception e) {
            log.error("构建视频到知识点映射时失败", e);
        }

        return mapping;
    }

    /**
     * 为指定知识点查找关联的视频困难信息
     */
    private VideoBehaviorInsight findVideoInsightForKnowledgePoint(
            Long knowledgePointId,
            Map<Long, Set<Long>> videoToKnowledgePointMap,
            Map<Long, VideoBehaviorInsight> videoBehaviorMap) {

        for (Map.Entry<Long, Set<Long>> entry : videoToKnowledgePointMap.entrySet()) {
            if (entry.getValue().contains(knowledgePointId)) {
                VideoBehaviorInsight insight = videoBehaviorMap.get(entry.getKey());
                if (insight != null && insight.isDifficult()) {
                    return insight;
                }
            }
        }
        return null;
    }

    /**
     * 构建增强的推荐提示词（包含视频行为上下文）
     */
    private String buildEnhancedRecommendationPrompt(
            String knowledgePointName,
            boolean hasVideoDifficulty,
            VideoBehaviorInsight videoInsight) {

        if (hasVideoDifficulty && videoInsight != null) {
            return String.format("""
                你是一个循循善诱的AI助教。请为在知识点 "%s" 上遇到困难的学生，生成一句简短、友好且鼓励性的学习建议。

                重要背景信息：
                - 学生在学习该知识点相关视频时，表现出了困难信号：%s
                - 这表明学生在理解该内容时需要反复回顾和思考

                请结合这一观察，给出具体且贴心的学习建议。直接返回建议文本即可，不要包含其它任何内容。
                """, knowledgePointName, videoInsight.getDifficultyReason());
        } else {
            return String.format("""
                你是一个循循善诱的AI助教。请为在知识点 "%s" 上遇到困难的学生，生成一句简短、友好且鼓励性的学习建议。直接返回建议文本即可，不要包含其它任何内容。
                """, knowledgePointName);
        }
    }

    /**
     * 构建降级的推荐文本（LLM调用失败时使用）
     */
    private String buildFallbackRecommendationText(
            String knowledgePointName,
            boolean hasVideoDifficulty,
            VideoBehaviorInsight videoInsight) {

        if (hasVideoDifficulty && videoInsight != null) {
            return String.format(
                "我们注意到你在学习「%s」的相关视频时进行了多次回看（%s）。这说明这个知识点需要更多时间消化。建议你结合教材和练习题，从多个角度理解这个概念！",
                knowledgePointName,
                videoInsight.getDifficultyReason()
            );
        } else {
            return "在学习「" + knowledgePointName + "」时遇到困难了吗？别担心，回顾一下相关的课程材料，你一定能掌握它！";
        }
    }

    /**
     * 为新学生或表现良好的学生生成基础学习推荐
     */
    private int generateBasicRecommendations(Long studentId, Long courseId) {
        log.info("为学生ID: {} 在课程ID: {} 生成基础推荐", studentId, courseId);

        try {
            // 标记旧推荐为过时
            LambdaUpdateWrapper<LearningRecommendation> updateWrapper = Wrappers.<LearningRecommendation>lambdaUpdate()
                    .eq(LearningRecommendation::getStudentId, studentId)
                    .eq(LearningRecommendation::getCourseId, courseId)
                    .eq(LearningRecommendation::getIsDismissed, 0)
                    .set(LearningRecommendation::getIsDismissed, 2);
            recommendationMapper.update(null, updateWrapper);

            // 生成通用的学习建议
            String[] basicRecommendations = {
                "开始学习本课程的基础知识点，建立扎实的理论基础",
                "定期复习已学内容，加深理解和记忆",
                "积极参与课堂讨论，多与同学交流学习心得"
            };

            int count = 0;
            for (String recommendation : basicRecommendations) {
                LearningRecommendation entity = new LearningRecommendation();
                entity.setStudentId(studentId);
                entity.setCourseId(courseId);
                entity.setRecommendationType("GENERAL");
                entity.setTargetId(null);
                entity.setReason(recommendation);
                entity.setIsDismissed(0);

                recommendationMapper.insert(entity);
                count++;
                log.info("已生成基础推荐: {}", recommendation);
            }

            return count;
        } catch (Exception e) {
            log.error("生成基础推荐时发生错误", e);
            return 0;
        }
    }

    /**
     * 直接从knowledge_point_progress表获取学生的薄弱知识点
     */
    private List<KnowledgePointPerformanceVO> getWeakKnowledgePoints(Long studentId, Long courseId) {
        log.info("从数据库直接获取学生ID: {} 在课程ID: {} 的薄弱知识点", studentId, courseId);

        // 查询学生在该课程下掌握度为"待加强"的知识点
        List<KnowledgePointProgress> progressList = knowledgePointProgressMapper.selectList(
            Wrappers.<KnowledgePointProgress>lambdaQuery()
                .eq(KnowledgePointProgress::getStudentId, studentId)
                .eq(KnowledgePointProgress::getCourseId, courseId)
                .in(KnowledgePointProgress::getMasteryLevel, Arrays.asList("LEARNING", "PARTIAL"))
        );

        if (CollectionUtils.isEmpty(progressList)) {
            log.info("学生ID: {} 在课程ID: {} 下无薄弱知识点记录", studentId, courseId);
            return Collections.emptyList();
        }

        // 转换为KnowledgePointPerformanceVO
        return progressList.stream().map(progress -> {
            // 获取知识点信息
            KnowledgePointPerformanceVO vo = new KnowledgePointPerformanceVO();
            vo.setKnowledgePointId(progress.getKnowledgePointId());
            knowledgeGraphClient.getKnowledgePoint(progress.getKnowledgePointId())
                    .ifPresentOrElse(kp -> vo.setKnowledgePointName(kp.getName()), () -> vo.setKnowledgePointName("未知知识点"));
            vo.setMasteryLevel(progress.getMasteryLevel());
            if (progress.getMasteryScore() != null) {
                vo.setAverageScore(progress.getMasteryScore().doubleValue());
            }

            log.info("找到薄弱知识点: {} (ID: {}), 掌握度: {}", vo.getKnowledgePointName(), vo.getKnowledgePointId(), vo.getMasteryLevel());
            return vo;
        }).toList();
    }

    @Override
    public List<LearningRecommendationVO> getRecommendations(Long studentId, Long courseId, String recommendationType, Integer count) {
        log.info("为学生ID: {} 在课程ID: {} 获取最多 {} 条学习推荐。", studentId, courseId, count);

        int limit = (count == null || count <= 0) ? 5 : count;
        List<LearningRecommendationVO> recommendations = recommendationMapper.selectEnrichedRecommendations(studentId, courseId, limit);

        return CollectionUtils.isEmpty(recommendations) ? Collections.emptyList() : recommendations;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRecommendationStatus(Long recommendationId, RecommendationStatusUpdateDTO dto) {
        LearningRecommendation recommendation = recommendationMapper.selectById(recommendationId);
        if (recommendation != null) {
            if ("DISMISSED".equalsIgnoreCase(dto.getStatus())) {
                recommendation.setIsDismissed(1); // 1 = 用户已忽略
                recommendationMapper.updateById(recommendation);
            }
        }
    }

    /**
     * 视频行为洞察数据结构
     */
    @Data
    private static class VideoBehaviorInsight {
        private Long resourceId;              // 视频资源ID
        private Integer completion;           // 完成度 0-100
        private int totalElapsedTime;         // 总观看时长（秒）
        private int segmentCount;             // 观看片段数量
        private int replayCount;              // 重复观看次数
        private boolean isDifficult;          // 是否判定为困难视频
        private String difficultyReason;      // 困难原因描述
    }
}
