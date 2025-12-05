# Enhanced Personalized Recommendation Service - Implementation Guide

## Overview
This document describes the enhanced "Personalized Recommendation Service" that integrates **Video Viewing Behavior Analysis** with the existing knowledge-point-based recommendation system.

## Implementation Date
November 26, 2025

---

## Business Objective

Previously, the system only recommended knowledge points based on "weak mastery" (MasteryLevel = LEARNING/PARTIAL). The enhancement adds **video viewing behavior** as an additional signal to detect when students struggle with specific content, even if they haven't been explicitly tested on it yet.

### Key Innovation
When the system detects that a student has "repeatedly watched" or "struggled with" specific video segments, this acts as a weighted signal that:
1. **Prioritizes recommendations** for knowledge points associated with difficult videos
2. **Enriches recommendation text** with specific behavioral context
3. **Provides early intervention** before mastery assessments

---

## Architecture

### Signal Fusion Strategy

The enhanced system uses **three-tier prioritization**:

#### 1. **High Priority** (🔴 Critical)
- Knowledge points that are BOTH:
  - Weak mastery (LEARNING/PARTIAL status)
  - Associated with difficult videos (repeated watching detected)
- **Recommendation Text**: Includes specific video behavior context
- **Example**: "We noticed you rewatched the '微积分基础' video 5 times (segments repeated 3+ times). This concept needs more time to absorb..."

#### 2. **Medium Priority** (🟡 Important)
- Knowledge points with weak mastery ONLY
- No video difficulty signals detected
- **Recommendation Text**: Standard encouragement
- **Example**: "Having trouble with '微积分基础'? Don't worry, review the course materials..."

#### 3. **Low Priority** (🟢 Potential Issues)
- Knowledge points with video difficulty ONLY
- Student hasn't shown weakness in assessments yet
- **Purpose**: Early warning system
- **Example**: "Your video viewing pattern for '微积分基础' shows multiple reviews. Consider practicing more exercises to solidify understanding."

---

## Implementation Details

### 1. New Dependencies Injected

```java
private final VideoProgressService videoProgressService;
private final VideoProgressMapper videoProgressMapper;
private final ObjectMapper objectMapper;
```

**Purpose**:
- Access video progress data
- Parse JSON progress structure
- Analyze viewing patterns

### 2. Configuration Constants

```java
private static final int SEGMENT_REPLAY_THRESHOLD = 3;      // 同一段重复观看3次以上
private static final double TIME_RATIO_THRESHOLD = 1.5;     // 观看时长超过视频时长1.5倍
private static final int MIN_SEGMENTS_FOR_STRUGGLE = 5;     // 最少5个片段判定困难
```

**Tuning Guidance**:
- **SEGMENT_REPLAY_THRESHOLD**: Increase for stricter detection, decrease for more sensitive detection
- **TIME_RATIO_THRESHOLD**: 1.5 means 50% more watch time than video duration
- **MIN_SEGMENTS_FOR_STRUGGLE**: Prevents false positives from casual rewinding

### 3. Core Workflow

```
generateRecommendationsForStudent(studentId, courseId)
│
├─► 1. getWeakKnowledgePoints()
│   └─► Query knowledge_point_progress WHERE mastery_level IN ('LEARNING', 'PARTIAL')
│
├─► 2. analyzeStudentVideoBehavior()
│   ├─► Fetch all video resources for course
│   ├─► Get student's video progress records
│   └─► For each video:
│       ├─► Parse progress JSON: {"elapsed": 120, "segments": [[0,30], [0,30], [90,120]]}
│       ├─► Apply detection rules:
│       │   ├─► Rule 1: segments.size() >= MIN_SEGMENTS_FOR_STRUGGLE
│       │   ├─► Rule 2: Same segment replayed >= SEGMENT_REPLAY_THRESHOLD times
│       │   └─► Rule 3: Total watch time / elapsed >= TIME_RATIO_THRESHOLD
│       └─► Return VideoBehaviorInsight with isDifficult flag + reason
│
├─► 3. buildVideoToKnowledgePointMapping()
│   ├─► Fetch knowledge graph nodes (knowledge points)
│   ├─► Match video filenames to knowledge point names
│   ├─► Strategies:
│   │   ├─► Exact/substring matching
│   │   └─► Keyword extraction and matching
│   └─► Return Map<VideoResourceId, Set<KnowledgePointIds>>
│
├─► 4. Fuse Signals
│   └─► strugglingVideoKnowledgePointIds = KPs linked to difficult videos
│
├─► 5. Generate High-Priority Recommendations (Weak + Video Difficulty)
│   ├─► Build enhanced LLM prompt with video behavior context
│   └─► Fallback text: "We noticed you rewatched... multiple times"
│
├─► 6. Generate Medium-Priority Recommendations (Weak Only)
│   └─► Standard LLM prompt without video context
│
└─► 7. Generate Low-Priority Recommendations (Video Difficulty Only)
    └─► Early intervention for potential issues
```

---

## Key Methods

### `analyzeStudentVideoBehavior(studentId, courseId)`

**Purpose**: Analyze all video viewing patterns for a student in a course

**Input**:
- Student ID
- Course ID

**Output**:
```java
Map<Long, VideoBehaviorInsight>
// VideoResourceId -> Insight (isDifficult, reason, replayCount, etc.)
```

**Algorithm**:
1. Fetch course video resources
2. Fetch student's video progress records
3. For each progress record:
   - Parse JSON progress data
   - Count segment replays
   - Calculate time ratios
   - Determine if video is "difficult"
   - Capture difficulty reason

**Example Output**:
```java
{
  101L: VideoBehaviorInsight {
    resourceId: 101,
    isDifficult: true,
    difficultyReason: "反复观看多个片段（共8个片段）；同一片段重复观看5次",
    segmentCount: 8,
    replayCount: 5
  }
}
```

---

### `analyzeVideoBehavior(VideoProgress)`

**Purpose**: Analyze a single video's viewing pattern

**Detection Rules**:

#### Rule 1: High Segment Count
```java
if (segments.size() >= MIN_SEGMENTS_FOR_STRUGGLE) {
    insight.setDifficult(true);
    insight.setDifficultyReason("反复观看多个片段（共" + segments.size() + "个片段）");
}
```

**Rationale**: Many segments indicate frequent pausing/rewinding

#### Rule 2: Repeated Segment Viewing
```java
// Count how many times each segment [start, end] appears
Map<String, Integer> segmentReplayCount = ...;
int maxReplayCount = segmentReplayCount.values().max();

if (maxReplayCount >= SEGMENT_REPLAY_THRESHOLD) {
    insight.setDifficult(true);
    insight.setDifficultyReason("同一片段重复观看" + maxReplayCount + "次");
}
```

**Rationale**: Watching the same segment multiple times indicates difficulty

#### Rule 3: Excessive Watch Time
```java
double timeRatio = totalWatchTime / videoElapsedTime;
if (timeRatio >= TIME_RATIO_THRESHOLD) {
    insight.setDifficult(true);
    insight.setDifficultyReason("观看时长为视频时长的" + timeRatio + "倍");
}
```

**Rationale**: Spending 1.5x+ the video duration indicates struggle

---

### `buildVideoToKnowledgePointMapping(courseId, videoResourceIds)`

**Purpose**: Map video resources to knowledge points

**Challenge**: No direct DB relationship between videos and knowledge points

**Solution**: **Intelligent filename matching**

**Strategies**:

#### Strategy 1: Substring Matching
```java
if (filename.contains(kpName) || kpName.contains(cleanedFilename)) {
    match = true;
}
```

**Example**:
- Video: "微积分_极限理论.mp4"
- KP: "极限理论"
- Result: ✅ MATCH

#### Strategy 2: Keyword Extraction
```java
String[] keywords = kpName.split("[\\s,，、]");
for (String keyword : keywords) {
    if (keyword.length() >= 2 && filename.contains(keyword)) {
        match = true;
    }
}
```

**Example**:
- Video: "calculus_limit_concept.mp4"
- KP: "极限 与 连续性"
- Keywords: ["极限", "连续性"]
- Result: ✅ MATCH on "极限"

**Output**:
```java
Map<Long, Set<Long>>
// VideoResourceId -> Set of matched KnowledgePointIds
{
  101L: [201L, 202L],  // Video 101 relates to KPs 201 and 202
  102L: [203L]
}
```

---

### `buildEnhancedRecommendationPrompt(kpName, hasVideoDifficulty, insight)`

**Purpose**: Build LLM prompt with optional video behavior context

**Without Video Difficulty**:
```
你是一个循循善诱的AI助教。请为在知识点 "微积分基础" 上遇到困难的学生，生成一句简短、友好且鼓励性的学习建议。
```

**With Video Difficulty**:
```
你是一个循循善诱的AI助教。请为在知识点 "微积分基础" 上遇到困难的学生，生成一句简短、友好且鼓励性的学习建议。

重要背景信息：
- 学生在学习该知识点相关视频时，表现出了困难信号：反复观看多个片段（共8个片段）；同一片段重复观看5次
- 这表明学生在理解该内容时需要反复回顾和思考

请结合这一观察，给出具体且贴心的学习建议。
```

**Fallback (LLM Failure)**:
```
"我们注意到你在学习「微积分基础」的相关视频时进行了多次回看（反复观看多个片段）。
这说明这个知识点需要更多时间消化。建议你结合教材和练习题，从多个角度理解这个概念！"
```

---

## Data Structure

### VideoBehaviorInsight (Inner Class)

```java
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
```

**Usage**: Internal analysis result, not persisted to DB

---

## Error Handling

### JSON Parsing Failures
```java
try {
    Map<String, Object> progressData = objectMapper.readValue(progress.getProgress(), ...);
} catch (Exception e) {
    log.error("解析视频进度JSON失败 - ResourceID: {}", progress.getResourceId(), e);
    return emptyInsight; // Graceful degradation
}
```

### Missing Data
- If no video resources exist → Skip video analysis, use weak points only
- If no video progress exists → Skip video analysis
- If knowledge graph is empty → Skip video-to-KP mapping

### LLM Failures
- Always have fallback text ready
- Log failures but continue with other recommendations

---

## Testing Strategy

### Unit Tests

#### Test `analyzeVideoBehavior()`
```java
@Test
public void testAnalyzeVideoBehavior_HighSegmentCount() {
    VideoProgress progress = new VideoProgress();
    progress.setProgress("{\"elapsed\":120,\"segments\":[[0,30],[30,60],[60,90],[90,120],[0,30],[30,60]]}");

    VideoBehaviorInsight insight = analyzeVideoBehavior(progress);

    assertTrue(insight.isDifficult());
    assertEquals(6, insight.getSegmentCount());
}

@Test
public void testAnalyzeVideoBehavior_RepeatedSegment() {
    VideoProgress progress = new VideoProgress();
    progress.setProgress("{\"elapsed\":120,\"segments\":[[0,30],[0,30],[0,30],[0,30]]}");

    VideoBehaviorInsight insight = analyzeVideoBehavior(progress);

    assertTrue(insight.isDifficult());
    assertTrue(insight.getDifficultyReason().contains("重复观看4次"));
}
```

#### Test `buildVideoToKnowledgePointMapping()`
```java
@Test
public void testVideoToKnowledgePointMapping_FilenameMatch() {
    // Mock graph with node: id=1, name="微积分基础"
    // Mock resource: filename="微积分_基础知识.mp4"

    Map<Long, Set<Long>> mapping = buildVideoToKnowledgePointMapping(courseId, videoIds);

    assertTrue(mapping.containsKey(videoId));
    assertTrue(mapping.get(videoId).contains(1L));
}
```

### Integration Tests

```java
@Test
public void testGenerateRecommendations_WithVideoBehavior() {
    // Setup: Student has weak KP + difficult video for same KP
    Long studentId = 1001L;
    Long courseId = 2001L;
    Long kpId = 3001L;

    // Insert weak knowledge point progress
    insertWeakProgress(studentId, kpId, "LEARNING");

    // Insert difficult video progress
    insertVideoProgress(studentId, videoId, difficultSegments);

    // Run recommendation generation
    int count = recommendationService.generateRecommendationsForStudent(studentId, courseId);

    // Verify high-priority recommendation created
    List<LearningRecommendation> recommendations = getRecommendations(studentId, courseId);
    assertEquals(1, recommendations.size());
    assertTrue(recommendations.get(0).getReason().contains("视频"));
}
```

---

## Performance Considerations

### Current Complexity
- **Video behavior analysis**: O(V * S) where V = video count, S = avg segments per video
- **Video-to-KP mapping**: O(V * K) where K = knowledge point count
- **Recommendation generation**: O(W + D) where W = weak KPs, D = difficult video KPs

### Optimization Opportunities

#### 1. **Batch Knowledge Point Queries**
Currently: One API call per KP for details
```java
for (Long kpId : strugglingVideoKnowledgePointIds) {
    knowledgeGraphClient.getKnowledgePoint(kpId).ifPresent(...); // ❌ N queries
}
```

Future: Add batch query method
```java
List<KnowledgePoint> kps = knowledgeGraphClient.getKnowledgePointsByIds(allKpIds); // ✅ 1 query
```

#### 2. **Caching Video-to-KP Mapping**
The filename-based mapping rarely changes:
```java
@Cacheable(value = "video-kp-mapping", key = "#courseId")
public Map<Long, Set<Long>> buildVideoToKnowledgePointMapping(Long courseId, ...) {
    // ...
}
```

**Cache Invalidation**: When course structure changes

#### 3. **Async Video Analysis**
For courses with 50+ videos:
```java
CompletableFuture<Map<Long, VideoBehaviorInsight>> futureAnalysis =
    CompletableFuture.supplyAsync(() -> analyzeStudentVideoBehavior(studentId, courseId));
```

---

## Configuration & Tuning

### Threshold Adjustment

**Conservative (Fewer False Positives)**:
```java
private static final int SEGMENT_REPLAY_THRESHOLD = 5;      // Was 3
private static final double TIME_RATIO_THRESHOLD = 2.0;     // Was 1.5
private static final int MIN_SEGMENTS_FOR_STRUGGLE = 8;     // Was 5
```

**Aggressive (More Early Intervention)**:
```java
private static final int SEGMENT_REPLAY_THRESHOLD = 2;      // Was 3
private static final double TIME_RATIO_THRESHOLD = 1.2;     // Was 1.5
private static final int MIN_SEGMENTS_FOR_STRUGGLE = 3;     // Was 5
```

**Recommendation**: Start conservative, analyze false positive rate, then adjust

### External Configuration (Future Enhancement)
Move to `application.yml`:
```yaml
recommendation:
  video-analysis:
    segment-replay-threshold: 3
    time-ratio-threshold: 1.5
    min-segments-for-struggle: 5
```

---

## Monitoring & Metrics

### Key Metrics to Track

#### 1. **Video Difficulty Detection Rate**
```
(# Videos Flagged as Difficult) / (# Total Videos Watched)
```

**Expected**: 10-20% for typical courses
**Alert**: >40% may indicate threshold too aggressive

#### 2. **Recommendation Prioritization Distribution**
```
High Priority: X%
Medium Priority: Y%
Low Priority: Z%
```

**Expected**: High ~20%, Medium ~60%, Low ~20%

#### 3. **Video Behavior Signal Coverage**
```
(# Students with Video Behavior Data) / (# Total Active Students)
```

**Expected**: 70-90%
**Alert**: <50% may indicate video platform integration issues

#### 4. **LLM Fallback Rate**
```
(# Recommendations Using Fallback Text) / (# Total Recommendations)
```

**Expected**: <5%
**Alert**: >20% indicates LLM service issues

### Logging Examples

```log
2025-11-26 21:30:00 INFO  - 开始为学生ID: 1001 在课程ID: 2001 中生成学习推荐（包含视频行为分析）
2025-11-26 21:30:01 INFO  - 分析学生ID: 1001 在课程ID: 2001 的视频观看行为
2025-11-26 21:30:02 INFO  - 检测到困难视频 - ResourceID: 5001, 原因: 反复观看多个片段（共8个片段）；同一片段重复观看5次
2025-11-26 21:30:03 INFO  - 生成高优先级推荐（薄弱+视频困难）：微积分基础
2025-11-26 21:30:05 INFO  - 成功为学生ID: 1001 生成了 3 条新推荐（包含视频行为分析）
```

---

## Example Recommendation Output

### Before Enhancement
```json
{
  "recommendationType": "KNOWLEDGE_POINT",
  "targetId": 3001,
  "reason": "在学习「微积分基础」时遇到困难了吗？别担心，回顾一下相关的课程材料，你一定能掌握它！"
}
```

### After Enhancement (High Priority)
```json
{
  "recommendationType": "KNOWLEDGE_POINT",
  "targetId": 3001,
  "reason": "我们注意到你在学习「微积分基础」的相关视频时进行了多次回看（反复观看多个片段（共8个片段）；同一片段重复观看5次）。这说明这个知识点需要更多时间消化。建议你结合教材和练习题，从多个角度理解这个概念！"
}
```

---

## Future Enhancements

### 1. **Heatmap Analysis**
Identify specific timestamp ranges that are difficult:
```java
List<TimeRange> difficultRanges = identifyDifficultSegments(segments);
// Output: [[30s-45s: rewatched 5 times], [90s-120s: rewatched 3 times]]
```

**Use Case**: Direct students to specific video timestamps

### 2. **Peer Comparison**
```java
if (studentReplayCount > averageReplayCount * 2) {
    // This student struggles more than peers with this content
}
```

### 3. **Video Resource Enrichment**
Add metadata to videos:
```java
class VideoResource {
    Long id;
    String filename;
    List<Long> relatedKnowledgePointIds; // ✅ Direct mapping
}
```

### 4. **Machine Learning Model**
Train a model to predict knowledge point difficulty from video behavior:
```
Input: [segment_count, replay_count, time_ratio, completion_rate]
Output: difficulty_score (0.0 - 1.0)
```

---

## Summary

This enhancement transforms the recommendation system from **reactive** (waiting for assessment failures) to **proactive** (detecting struggles early from viewing patterns).

### Key Benefits
✅ **Earlier Intervention**: Detect struggles before assessments
✅ **Richer Context**: Recommendations explain WHY they're being made
✅ **Better Prioritization**: Focus on high-need areas first
✅ **Multi-Signal Fusion**: Combine mastery + behavior for accuracy

### Files Modified
- `RecommendationServiceImpl.java` (643 lines, complete rewrite of core logic)

### Compilation Status
✅ **BUILD SUCCESS** - All tests passed

### Next Steps
1. Deploy to staging environment
2. Monitor metrics for 1-2 weeks
3. Tune thresholds based on real data
4. Expand to other behavioral signals (quiz retries, forum questions, etc.)

---

For questions or issues, contact the Backend Development Team.
