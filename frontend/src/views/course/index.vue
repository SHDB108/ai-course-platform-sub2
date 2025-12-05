<template>
  <div class="course-detail" v-loading="loading">
    <!-- Course Header -->
    <el-card class="course-header-card">
      <div class="course-header">
        <div class="course-info">
          <h1 class="course-title">{{ courseDetail?.courseName }}</h1>
          <p class="course-teacher">
            <el-icon><User /></el-icon>
            {{ courseDetail?.teacherName }}
          </p>
          <p class="course-description">{{ courseDetail?.description }}</p>
          <div class="course-meta">
            <el-tag>{{ courseDetail?.credits }} Credits</el-tag>
            <span class="date-range">
              <el-icon><Calendar /></el-icon>
              {{ formatDate(courseDetail?.startDate) }} - {{ formatDate(courseDetail?.endDate) }}
            </span>
          </div>
        </div>
      </div>
    </el-card>

    <!-- Knowledge Graph Section -->
    <el-card class="graph-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">Knowledge Graph</span>
          <div class="graph-legend">
            <span class="legend-item">
              <span class="legend-dot root"></span> Course
            </span>
            <span class="legend-item">
              <span class="legend-dot chapter"></span> Chapter
            </span>
            <div class="legend-divider"></div>
            <span class="legend-title">Mastery Level:</span>
            <span class="legend-item">
              <span class="legend-dot mastered"></span> Mastered
            </span>
            <span class="legend-item">
              <span class="legend-dot learning"></span> Learning
            </span>
            <span class="legend-item">
              <span class="legend-dot weak"></span> Weak
            </span>
            <span class="legend-item">
              <span class="legend-dot unknown"></span> Unknown
            </span>
          </div>
        </div>
      </template>
      <div ref="graphContainer" class="graph-container"></div>
      <el-empty v-if="!graphData && !loading" description="No knowledge graph available" />
    </el-card>

    <!-- AI Recommendations Section -->
    <el-card class="recommendations-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">
            <el-icon><MagicStick /></el-icon>
            AI 智能推荐
          </span>
          <div class="header-actions">
            <el-button
              type="warning"
              @click="navigateToExamSystem"
            >
              <el-icon><Monitor /></el-icon>
              进入AI考试系统
            </el-button>
            <el-button
              type="primary"
              :loading="generatingRecommendations"
              @click="handleGenerateRecommendations"
            >
              <el-icon><MagicStick /></el-icon>
              {{ generatingRecommendations ? 'AI 诊断中...' : 'AI 智能诊断' }}
            </el-button>
          </div>
        </div>
      </template>

      <div v-loading="recommendationsLoading" class="recommendations-content">
        <div v-if="recommendations.length === 0 && !recommendationsLoading" class="empty-recommendations">
          <el-empty description="暂无推荐，点击“AI 智能诊断”生成个性化学习建议" />
        </div>

        <div v-else class="recommendations-list">
          <div
            v-for="rec in recommendations"
            :key="rec.id"
            class="recommendation-card"
          >
            <div class="recommendation-header">
              <div class="recommendation-tags">
                <el-tag :type="getRecommendationTypeColor(rec.type)" size="small">
                  {{ getRecommendationTypeText(rec.type) }}
                </el-tag>
                <el-tag v-if="rec.priority" :type="getPriorityColor(rec.priority)" size="small">
                  {{ rec.priority }}
                </el-tag>
              </div>
              <div class="recommendation-title">{{ rec.title }}</div>
            </div>
            <div class="recommendation-body">
              <div class="recommendation-reason">
                <el-icon class="reason-icon"><MagicStick /></el-icon>
                <span>{{ rec.reason }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- Selected Node Info -->
    <el-drawer
      v-model="drawerVisible"
      :title="selectedNode?.label"
      direction="rtl"
      size="400px"
    >
      <div class="node-detail" v-if="selectedNode">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="Name">{{ selectedNode.label }}</el-descriptions-item>
          <el-descriptions-item label="Type">
            <el-tag :type="getNodeTypeTag(selectedNode.category)">
              {{ getNodeTypeName(selectedNode.category) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item v-if="selectedNode.masteryLevel" label="Mastery Level">
            <el-tag :type="getMasteryTag(selectedNode.masteryLevel)">
              {{ selectedNode.masteryLevel }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="ID">{{ selectedNode.id }}</el-descriptions-item>
        </el-descriptions>
        <div class="node-actions" v-if="selectedNode.category === 2">
          <el-button type="primary" @click="startStudy">
            <el-icon><VideoPlay /></el-icon>
            Start Learning
          </el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Calendar, VideoPlay, MagicStick, Monitor } from '@element-plus/icons-vue'
import { Graph } from '@antv/g6'
import { getCourseGraph, getCourseDetail } from '@/api/course'
import { generateRecommendations, getRecommendations } from '@/api/recommendation'
import { useUserStore } from '@/stores/user'
import type { CourseDetailVO } from '@/api/course'
import type { KnowledgeGraphVO, MasteryLevel } from '@/types/graph'
import type { LearningRecommendationVO } from '@/types/recommendation'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const courseDetail = ref<CourseDetailVO | null>(null)
const graphData = ref<KnowledgeGraphVO | null>(null)
const graphContainer = ref<HTMLElement | null>(null)
const drawerVisible = ref(false)
const selectedNode = ref<{ id: string; label: string; category: number; masteryLevel?: MasteryLevel } | null>(null)

// AI Recommendations state
const recommendations = ref<LearningRecommendationVO[]>([])
const recommendationsLoading = ref(false)
const generatingRecommendations = ref(false)

// User store for SSO context propagation
const userStore = useUserStore()

let graphInstance: Graph | null = null

const courseId = Number(route.params.id)

// Node colors by category (for Root and Chapter)
const nodeColors = {
  0: { fill: '#001f3f', stroke: '#001f3f' }, // Root - Navy
  1: { fill: '#1890ff', stroke: '#1890ff' }  // Chapter - Primary Blue
}

// Mastery level colors for Knowledge Points
const masteryColors = {
  MASTERED: { fill: '#52c41a', stroke: '#389e0d' },  // Green
  LEARNING: { fill: '#faad14', stroke: '#d48806' },  // Yellow/Orange
  WEAK: { fill: '#f5222d', stroke: '#cf1322' },      // Red
  UNKNOWN: { fill: '#d9d9d9', stroke: '#bfbfbf' }    // Gray
}

// Get node color based on category and mastery level
const getNodeColor = (category: number, masteryLevel?: MasteryLevel) => {
  if (category === 2 && masteryLevel) {
    // Knowledge Point - use mastery level color
    return masteryColors[masteryLevel] || masteryColors.UNKNOWN
  }
  // Root or Chapter - use category color
  return nodeColors[category as keyof typeof nodeColors] || { fill: '#87ceeb', stroke: '#1890ff' }
}

const loadCourseData = async () => {
  loading.value = true
  try {
    const [detailRes, graphRes] = await Promise.all([
      getCourseDetail(courseId),
      getCourseGraph(courseId)
    ])
    courseDetail.value = detailRes.data
    graphData.value = graphRes.data
    await nextTick()
    initGraph()
  } catch (error: any) {
    ElMessage.error(error.message || 'Failed to load course data')
  } finally {
    loading.value = false
  }
}

const initGraph = () => {
  if (!graphContainer.value || !graphData.value) return

  const { nodes, edges } = graphData.value
  const width = graphContainer.value.clientWidth
  const height = 500

  // Transform nodes for G6
  const g6Nodes = nodes.map(node => {
    const colors = getNodeColor(node.category, node.masteryLevel)
    return {
      id: node.id,
      label: node.name,
      category: node.category,
      masteryLevel: node.masteryLevel,
      style: {
        fill: colors.fill,
        stroke: colors.stroke,
        lineWidth: 2
      },
      labelCfg: {
        style: {
          fill: node.category === 0 ? '#fff' : '#333',
          fontSize: node.category === 0 ? 14 : 12
        }
      },
      size: node.category === 0 ? 60 : node.category === 1 ? 45 : 35
    }
  })

  // Transform edges for G6
  const g6Edges = edges.map(edge => ({
    source: edge.source,
    target: edge.target,
    label: edge.label,
    style: {
      stroke: '#d9d9d9',
      lineWidth: 1,
      endArrow: true
    },
    labelCfg: {
      autoRotate: true,
      style: {
        fill: '#999',
        fontSize: 10
      }
    }
  }))

  graphInstance = new Graph({
    container: graphContainer.value,
    width,
    height,
    data: {
      nodes: g6Nodes,
      edges: g6Edges
    },
    layout: {
      type: 'force',
      preventOverlap: true,
      nodeSpacing: 50,
      linkDistance: 120
    },
    node: {
      style: {
        labelText: (d: any) => d.label,
        labelPlacement: 'center',
        labelFill: '#333'
      }
    },
    edge: {
      style: {
        labelText: (d: any) => d.label || '',
        endArrow: true
      }
    },
    behaviors: ['drag-canvas', 'zoom-canvas', 'drag-element'],
    autoFit: 'view'
  })

  // Node click event
  graphInstance.on('node:click', (evt: any) => {
    const nodeData = evt.target?.id ? g6Nodes.find(n => n.id === evt.target.id) : null
    if (nodeData) {
      selectedNode.value = {
        id: nodeData.id,
        label: nodeData.label,
        category: nodeData.category,
        masteryLevel: nodeData.masteryLevel
      }
      drawerVisible.value = true
    }
  })

  graphInstance.render()
}

const formatDate = (dateStr?: string) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  })
}

const getNodeTypeName = (category: number) => {
  const names = ['Course', 'Chapter', 'Knowledge Point']
  return names[category] || 'Unknown'
}

const getNodeTypeTag = (category: number) => {
  const types = ['', 'warning', 'success'] as const
  return types[category] || 'info'
}

const getMasteryTag = (masteryLevel: MasteryLevel) => {
  const tags = {
    MASTERED: 'success',
    LEARNING: 'warning',
    WEAK: 'danger',
    UNKNOWN: 'info'
  } as const
  return tags[masteryLevel] || 'info'
}

const startStudy = () => {
  if (selectedNode.value) {
    router.push(`/study/video/${selectedNode.value.id}`)
  }
}

// AI Recommendations handlers
const handleGenerateRecommendations = async () => {
  generatingRecommendations.value = true
  try {
    await generateRecommendations(courseId)
    ElMessage.success('AI诊断完成')
    // Automatically fetch the recommendations after generation
    await loadRecommendations()
  } catch (error: any) {
    ElMessage.error(error.message || '生成推荐失败')
  } finally {
    generatingRecommendations.value = false
  }
}

const loadRecommendations = async () => {
  recommendationsLoading.value = true
  try {
    const res = await getRecommendations(courseId)
    recommendations.value = res.data || []
  } catch (error: any) {
    ElMessage.error(error.message || '加载推荐失败')
  } finally {
    recommendationsLoading.value = false
  }
}

const getRecommendationTypeColor = (type: string) => {
  const colors = {
    KNOWLEDGE_POINT: 'primary',
    RESOURCE: 'success',
    STUDY_PLAN: 'warning'
  } as const
  return colors[type as keyof typeof colors] || 'info'
}

const getRecommendationTypeText = (type: string) => {
  const texts = {
    KNOWLEDGE_POINT: '知识点',
    RESOURCE: '资源',
    STUDY_PLAN: '学习计划'
  }
  return texts[type as keyof typeof texts] || type
}

const getPriorityColor = (priority: string) => {
  const colors = {
    HIGH: 'danger',
    MEDIUM: 'warning',
    LOW: 'info'
  } as const
  return colors[priority as keyof typeof colors] || 'info'
}

// Cross-system navigation handler
const navigateToExamSystem = () => {
  const studentId = userStore.userId || 1
  const examSystemUrl = `http://172.22.126.152:5173/student/exam?studentId=${studentId}`
  window.open(examSystemUrl, '_blank')
}

const handleResize = () => {
  if (graphInstance && graphContainer.value) {
    graphInstance.setSize(graphContainer.value.clientWidth, 500)
  }
}

onMounted(() => {
  loadCourseData()
  loadRecommendations()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  graphInstance?.destroy()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.course-detail {
  .course-header-card {
    margin-bottom: 20px;

    .course-header {
      .course-info {
        .course-title {
          margin: 0 0 12px 0;
          font-size: 24px;
          font-weight: 600;
          color: $text-primary;
        }

        .course-teacher {
          display: flex;
          align-items: center;
          gap: 6px;
          margin: 0 0 12px 0;
          font-size: 14px;
          color: $text-secondary;
        }

        .course-description {
          margin: 0 0 16px 0;
          font-size: 14px;
          color: $text-secondary;
          line-height: 1.6;
        }

        .course-meta {
          display: flex;
          align-items: center;
          gap: 16px;

          .date-range {
            display: flex;
            align-items: center;
            gap: 6px;
            font-size: 13px;
            color: $text-disabled;
          }
        }
      }
    }
  }

  .graph-card {
    .card-header {
      display: flex;
      align-items: center;
      justify-content: space-between;

      .card-title {
        font-size: 16px;
        font-weight: 600;
        color: $text-primary;
      }

      .graph-legend {
        display: flex;
        gap: 16px;
        align-items: center;
        flex-wrap: wrap;

        .legend-divider {
          width: 1px;
          height: 16px;
          background-color: $border-color;
        }

        .legend-title {
          font-size: 12px;
          font-weight: 600;
          color: $text-primary;
        }

        .legend-item {
          display: flex;
          align-items: center;
          gap: 6px;
          font-size: 12px;
          color: $text-secondary;

          .legend-dot {
            width: 12px;
            height: 12px;
            border-radius: 50%;

            &.root {
              background-color: #001f3f;
            }

            &.chapter {
              background-color: #1890ff;
            }

            &.mastered {
              background-color: #52c41a;
            }

            &.learning {
              background-color: #faad14;
            }

            &.weak {
              background-color: #f5222d;
            }

            &.unknown {
              background-color: #d9d9d9;
            }
          }
        }
      }
    }

    .graph-container {
      width: 100%;
      height: 500px;
      border: 1px solid $border-color;
      border-radius: $border-radius-md;
      background-color: #fafafa;
    }
  }

  .node-detail {
    .node-actions {
      margin-top: 24px;
      display: flex;
      gap: 12px;
    }
  }

  .recommendations-card {
    margin-top: 20px;

    .card-header {
      display: flex;
      align-items: center;
      justify-content: space-between;

      .card-title {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 16px;
        font-weight: 600;
        color: $text-primary;
      }

      .header-actions {
        display: flex;
        gap: 12px;
      }
    }

    .recommendations-content {
      min-height: 100px;

      .empty-recommendations {
        padding: 20px 0;
      }

      .recommendations-list {
        display: flex;
        flex-direction: column;
        gap: 16px;

        .recommendation-card {
          padding: 16px;
          background-color: #fafafa;
          border: 1px solid $border-color;
          border-radius: $border-radius-md;
          transition: all 0.3s;

          &:hover {
            background-color: #f0f0f0;
            box-shadow: $shadow-sm;
          }

          .recommendation-header {
            margin-bottom: 12px;

            .recommendation-tags {
              display: flex;
              gap: 8px;
              margin-bottom: 8px;
            }

            .recommendation-title {
              font-size: 15px;
              font-weight: 600;
              color: $text-primary;
              line-height: 1.5;
            }
          }

          .recommendation-body {
            .recommendation-reason {
              display: flex;
              align-items: flex-start;
              gap: 8px;
              font-size: 14px;
              color: $text-secondary;
              line-height: 1.6;

              .reason-icon {
                margin-top: 2px;
                color: $primary-color;
                flex-shrink: 0;
              }
            }
          }
        }
      }
    }
  }
}
</style>
