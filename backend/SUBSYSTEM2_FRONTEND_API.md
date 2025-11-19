# SUBSYSTEM 2 · Student-Facing API

This document summarizes every HTTP endpoint exposed by the “学习向导” (Subsystem 2) backend for browser or mobile clients used by students. All endpoints return a unified envelope:

```json
{
  "code": 0,
  "msg": "success",
  "data": { /* payload described below */ }
}
```

`code = 0` indicates success; any non-zero code should be treated as an error. Unless otherwise noted, all paths are relative to the backend base URL (e.g. `https://api.example.com`).

---

## 1. Student Dashboard & Task Center

### 1.1 获取学生选修课程列表
- **Method**: `GET`
- **Path**: `/api/v1/students/me/courses`
- **Description**: Returns the paginated course list displayed on the student home page.
- **Query Params**:
  - `pageNum` (default `1`)
  - `pageSize` (default `10`)
  - `keyword` – optional fuzzy search on course name/code.
- **Sample Response** (`Result<PageVO<CourseVO>>`)
```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 301,
        "courseCode": "AI301",
        "courseName": "深度学习实践",
        "name": "深度学习实践",
        "description": "项目式课程，包含 GNN/LLM 案例。",
        "credits": 3,
        "hours": 48,
        "duration": 2160,
        "teacherName": "李老师",
        "status": "ACTIVE",
        "startDate": "2025-03-01",
        "endDate": "2025-06-30",
        "maxStudents": 60,
        "enrolledStudents": 54
      }
    ],
    "total": 6,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

> **Notes**
> - `hours` 表示课程总课时。
> - `duration` 以分钟为单位（内部按 `hours * 45` 计算，符合最新后端实现）。

### 1.2 获取学生任务列表
- **Method**: `GET`
- **Path**: `/api/v1/students/me/tasks`
- **Description**: Lists tasks/homework/quizzes assigned to the student.
- **Query Params**:
  - `pageNum`, `pageSize`, `keyword`
  - `status` – optional filter (`NOT_SUBMITTED`, `SUBMITTED`, `GRADED`, etc.)
- **Sample Response** (`Result<PageVO<StudentTaskVO>>`)
```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "records": [
      {
        "taskId": 9101,
        "taskTitle": "GNN 作业 1",
        "taskType": "ASSIGNMENT",
        "deadline": "2025-03-15T23:59:59",
        "maxScore": 100,
        "courseId": 301,
        "courseName": "深度学习实践",
        "teacherName": "李老师",
        "submissionStatus": "NOT_SUBMITTED",
        "isOverdue": false,
        "daysUntilDeadline": 5
      }
    ],
    "total": 12,
    "size": 10,
    "current": 1,
    "pages": 2
  }
}
```

### 1.3 学习驾驶舱概览
- **Method**: `GET`
- **Path**: `/api/v1/students/me/dashboard/stats`
- **Description**: Dashboard counters shown on the student landing page.
- **Sample Response**
```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "myCourses": 4,
    "pendingTasks": 3,
    "weeklySubmissions": 5,
    "unreadMessages": 2,
    "projects": 1,
    "todoItems": { "pending": 4, "total": 10 }
  }
}
```

### 1.4 任务统计卡片
- **Method**: `GET`
- **Path**: `/api/v1/students/me/tasks/stats`
- **Description**: Returns totals for task widgets (pending/完成率等).
- **Sample Response**
```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "totalTasks": 20,
    "pendingTasks": 3,
    "inProgressTasks": 2,
    "completedTasks": 15,
    "overdueTasks": 1,
    "completionRate": 75.0,
    "thisWeekCompleted": 4,
    "thisMonthCompleted": 12
  }
}
```

---

## 2. 学习进度与分析 (`/api/v1/study`)

### 2.1 获取单个课程的学习进度
- **Method**: `GET`
- **Path**: `/api/v1/study/progress/course/{courseId}`
- **Description**: Drives the “学习导航图”及课程进度面板。
- **Sample Response** (`StudyProgressVO`)
```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "studentId": 42,
    "courseId": 301,
    "courseName": "深度学习实践",
    "totalProgress": 68,
    "videoProgress": { "progress": 80, "completed": 8, "total": 10 },
    "taskProgress": { "progress": 60, "completed": 3, "total": 5 },
    "examProgress": { "progress": 50, "completed": 1, "total": 2 },
    "knowledgeProgress": { "progress": 55, "completed": 22, "total": 40 },
    "studyTimeStats": {
      "totalStudyTime": 720,
      "todayStudyTime": 60,
      "weekStudyTime": 240,
      "monthStudyTime": 540,
      "averageDaily": 48.0
    }
  }
}
```

### 2.2 获取学生全部课程进度
- **Method**: `GET`
- **Path**: `/api/v1/study/progress`
- **Description**: Returns a list of `StudyProgressVO`, one per course.

### 2.3 手动刷新进度
- **Method**: `PUT`
- **Path**: `/api/v1/study/progress/course/{courseId}`
- **Description**: Forces the backend to recompute module progress; no request body.

### 2.4 课程级学习分析
- **Method**: `GET`
- **Path**: `/api/v1/study/analysis/course/{courseId}`
- **Description**: Provides dashboard data for charts such as时间/模块/薄弱点分析。

### 2.5 全局学习分析
- **Method**: `GET`
- **Path**: `/api/v1/study/analysis`
- **Description**: Aggregated stats across all enrolled courses (用于能力雷达图等)。

Sample payload for both analysis endpoints:
```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "studentId": 42,
    "courseId": 301,
    "timeAnalysis": { "todayMinutes": 90, "weekMinutes": 420, "dailyAverage": 60.0 },
    "progressAnalysis": {
      "completionRate": 0.68,
      "progressTrend": "ACCELERATING",
      "estimatedDaysToComplete": 12
    },
    "knowledgeAnalysis": {
      "totalKnowledgePoints": 40,
      "masteredPoints": 20,
      "weakPoints": 5,
      "weakAreas": [
        {
          "knowledgePointName": "注意力机制",
          "difficulty": "HIGH",
          "successRate": 0.4,
          "recommendedAction": "复习第 3.2 节视频"
        }
      ]
    },
    "predictions": ["本周可完成 2 个章节"],
    "recommendations": ["优先复习 GNN 与 Transformer 章节"]
  }
}
```

---

## 3. 学习会话管理

### 3.1 开始学习会话
- **Method**: `POST`
- **Path**: `/api/v1/study/sessions`
- **Body** (`StudySessionCreateDTO`):
```json
{
  "courseId": 301,
  "sessionType": "VIDEO",
  "resourceId": 8812,
  "resourceTitle": "Graph Attention Networks",
  "deviceType": "PC",
  "browserInfo": "Chrome 123",
  "ipAddress": "10.0.0.12",
  "notes": "准备观看第 3 节"
}
```
- **Response**: `data` contains the new `sessionId`.

### 3.2 结束学习会话
- **Method**: `PUT`
- **Path**: `/api/v1/study/sessions/{sessionId}`
- **Body** (`StudySessionUpdateDTO`) – mark duration、completion、笔记:
```json
{
  "duration": 45,
  "effectiveTime": 38,
  "completionRate": 90,
  "result": "COMPLETED",
  "score": 95,
  "notes": "重点看了注意力推导"
}
```

### 3.3 查看学习会话历史
- **Method**: `GET`
- **Path**: `/api/v1/study/sessions`
- **Query Params**: `courseId` (optional), `limit` (default `20`).
- **Response**: list of `StudySessionVO` entries.

---

## 4. 学习计划 & AI 推荐计划

### 4.1 创建自定义学习计划
- **Method**: `POST`
- **Path**: `/api/v1/study/plans`
- **Body** (`StudyPlanCreateDTO`) – specify `planName`, `planType`, timelines, goals, milestones.
- **Response**: newly created plan ID.

### 4.2 查询学习计划
- **Method**: `GET`
- **Path**: `/api/v1/study/plans`
- **Query Params**: optional `courseId`.
- **Response**: list of `StudyPlanVO`. Example entry:
```json
{
  "id": 7001,
  "courseId": 301,
  "courseName": "深度学习实践",
  "planName": "Transformer 复习计划",
  "planType": "WEEKLY",
  "status": "ACTIVE",
  "progress": 40,
  "goals": ["完成第 3 章视频", "整理注意力机制笔记"],
  "milestones": [
    { "title": "Week1", "targetDate": "2025-03-10T20:00:00", "completed": false }
  ],
  "isAiGenerated": false
}
```

### 4.3 更新计划进度
- **Method**: `PUT`
- **Path**: `/api/v1/study/plans/{planId}/progress`
- **Query Param**: `progress` (0–100). No body.

### 4.4 一键生成 AI 学习计划
- **Method**: `POST`
- **Path**: `/api/v1/study/plans/course/{courseId}/generate`
- **Query Param**: `planType` (default `WEEKLY`).
- **Response**: `StudyPlanVO` describing the auto-generated plan.

---

## 5. 知识图谱与掌握状态

### 5.1 获取课程知识图谱结构
- **Method**: `GET`
- **Path**: `/api/v1/knowledge-graph/{courseId}`
- **Description**: Returns the nodes/edges structure for ECharts/G6 to render the navigation map.
- **Sample Response**
```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "nodes": [
      { "id": "kp1", "name": "矩阵基础", "category": 0 },
      { "id": "kp2", "name": "自注意力机制", "category": 1 },
      { "id": "kp3", "name": "Transformer 编码器", "category": 1 }
    ],
    "edges": [
      { "source": "kp1", "target": "kp2", "label": "Prerequisite" },
      { "source": "kp2", "target": "kp3", "label": "Builds On" }
    ]
  }
}
```

### 5.2 更新知识点掌握度
- **Method**: `PUT`
- **Path**: `/api/v1/study/knowledge-points/{knowledgePointId}`
- **Body** (`KnowledgePointProgressUpdateDTO`):
```json
{
  "masteryLevel": "LEARNING",
  "masteryScore": 65,
  "confidence": 70,
  "isCorrect": true,
  "studyTime": 15,
  "learningContext": "完成 GAT 课堂练习"
}
```

### 5.3 查询课程内的知识点掌握情况
- **Method**: `GET`
- **Path**: `/api/v1/study/knowledge-points/course/{courseId}`
- **Response**: array of objects (each包含 `knowledgePointId`, `courseId`, `masteryLevel`, `accuracy`, `knowledgePointName`, etc.).

### 5.4 获取待复习的知识点
- **Method**: `GET`
- **Path**: `/api/v1/study/knowledge-points/review`
- **Description**: Returns the same structure as 5.2 but limited to items whose `reviewStatus = 'SCHEDULED'`.

---

## 6. 个性化推荐 (`/api/v1/recommendations`)

### 6.1 获取当前学生的学习推荐
- **Method**: `GET`
- **Path**: `/api/v1/recommendations`
- **Query Params**:
  - `courseId` (required)
  - `type` (optional, e.g. `KNOWLEDGE_POINT`, `GENERAL`)
  - `count` (default `5`)
- **Sample Response**
```json
{
  "code": 0,
  "msg": "success",
  "data": [
    {
      "id": 15001,
      "recommendationType": "KNOWLEDGE_POINT",
      "targetId": 8801,
      "targetName": "注意力机制",
      "reason": "掌握度低于 60%，建议重温第 3.2 节视频",
      "associatedResource": { "id": 9901, "filename": "attention.pdf" }
    }
  ]
}
```

### 6.2 更新推荐状态
- **Method**: `PUT`
- **Path**: `/api/v1/recommendations/{id}/status`
- **Body** (`RecommendationStatusUpdateDTO`)
```json
{ "status": "DISMISSED" }   // or "VIEWED"
```

### 6.3 学生主动生成推荐
- **Method**: `POST`
- **Path**: `/api/v1/recommendations/my-recommendations`
- **Query Param**: `courseId`
- **Description**: Triggers background generation; `data` contains a human-readable message (e.g. “成功生成 3 条学习推荐”).

---

## 7. 视频学习进度 (`/api/v1/videos`)

### 7.1 上报视频学习进度
- **Method**: `POST`
- **Path**: `/api/v1/videos/{resourceId}/progress`
- **Body** (`ProgressDTO`)
```json
{
  "elapsed": 180.5,
  "duration": 900.0,
  "segments": [[0, 60], [120, 200]],
  "completion": 45
}
```
- **Description**: The backend infers the current student ID from the security上下文并更新 `VideoProgress`。

### 7.2 查询单个视频的学生进度
- **Method**: `GET`
- **Path**: `/api/v1/videos/{resourceId}/progress`
- **Sample Response**
```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "resourceId": 8812,
    "studentId": 42,
    "progress": "{\"elapsed\": 420, \"completion\": 80}",
    "completion": 80,
    "gmtModified": "2025-03-05T10:22:00"
  }
}
```

### 7.3 课程视频热力图/完成率统计
- **Method**: `GET`
- **Path**: `/api/v1/videos/course/{courseId}/statistics`
- **Query Param**: `personalView` (optional, default `false`). When `true`, stats are scoped to the current student; otherwise class-level aggregates are returned.
- **Sample Response**
```json
{
  "code": 0,
  "msg": "success",
  "data": [
    {
      "resourceId": 8812,
      "filename": "graph_attention_nets.mp4",
      "totalViews": 120,
      "averageCompletion": 0.76,
      "heatmapData": [
        { "segment": "0-1min", "views": 90 },
        { "segment": "1-2min", "views": 75 }
      ]
    }
  ]
}
```

---

## 8. 备注
- Every endpoint responds with the unified `Result<T>` wrapper. Frontend code should always check `code` before consuming `data`.
- Date/time fields use ISO-8601 (`yyyy-MM-dd` or `yyyy-MM-dd'T'HH:mm:ss`) unless otherwise specified.
- When the application runs in the default `mock` profile, endpoints still behave the same but underlying data may be placeholders.

This document is intended to guide the front-end “学习向导” team when integrating the student portal, learning cockpit, and personalized recommendation UI components.
