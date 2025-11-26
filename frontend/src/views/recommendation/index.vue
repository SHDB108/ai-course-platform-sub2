<template>
  <div class="recommendations-page" v-loading="loading">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">AI Personalized Recommendations</h1>
        <p class="page-subtitle">Smart learning suggestions tailored to your progress and goals</p>
      </div>
      <el-button type="primary" @click="refreshRecommendations">
        <el-icon><Refresh /></el-icon>
        Refresh
      </el-button>
    </div>

    <!-- Priority Filters -->
    <div class="filter-bar">
      <el-radio-group v-model="selectedPriority" @change="filterRecommendations">
        <el-radio-button label="ALL">All</el-radio-button>
        <el-radio-button label="HIGH">High Priority</el-radio-button>
        <el-radio-button label="MEDIUM">Medium Priority</el-radio-button>
        <el-radio-button label="LOW">Low Priority</el-radio-button>
      </el-radio-group>
    </div>

    <!-- Recommendations List -->
    <div class="recommendations-list">
      <el-card
        v-for="rec in filteredRecommendations"
        :key="rec.id"
        class="recommendation-card"
        :class="`priority-${rec.priority.toLowerCase()}`"
      >
        <div class="card-header">
          <div class="header-left">
            <el-tag :type="getPriorityTagType(rec.priority)" size="small">
              {{ rec.priority }}
            </el-tag>
            <el-tag :type="getTypeTagType(rec.type)" size="small" effect="plain">
              {{ formatType(rec.type) }}
            </el-tag>
          </div>
          <div class="header-right">
            <span class="timestamp">{{ formatTimestamp(rec.createdAt) }}</span>
          </div>
        </div>

        <div class="card-body">
          <div class="recommendation-icon">
            <el-icon :size="40" :color="getPriorityColor(rec.priority)">
              <component :is="getTypeIcon(rec.type)" />
            </el-icon>
          </div>
          <div class="recommendation-content">
            <h3 class="recommendation-title">{{ rec.title }}</h3>
            <div class="ai-reason">
              <el-icon class="ai-icon"><MagicStick /></el-icon>
              <p class="reason-text">{{ rec.reason }}</p>
            </div>
          </div>
        </div>

        <div class="card-footer">
          <el-button
            type="primary"
            @click="startLearning(rec)"
            :disabled="rec.status === 'COMPLETED' || rec.status === 'DISMISSED'"
          >
            <el-icon><VideoPlay /></el-icon>
            {{ getButtonText(rec) }}
          </el-button>
          <el-button
            v-if="rec.status === 'PENDING'"
            @click="dismissRecommendation(rec.id)"
          >
            <el-icon><Close /></el-icon>
            Dismiss
          </el-button>
          <el-button
            v-if="rec.status === 'IN_PROGRESS'"
            type="success"
            @click="markCompleted(rec.id)"
          >
            <el-icon><Check /></el-icon>
            Mark Complete
          </el-button>
        </div>
      </el-card>
    </div>

    <el-empty
      v-if="!loading && filteredRecommendations.length === 0"
      description="No recommendations available"
    >
      <el-button type="primary" @click="refreshRecommendations">Generate Recommendations</el-button>
    </el-empty>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Refresh,
  VideoPlay,
  Close,
  Check,
  MagicStick,
  Reading,
  Document,
  Calendar
} from '@element-plus/icons-vue'
import {
  getRecommendations,
  dismissRecommendation as dismissRec,
  startRecommendation,
  completeRecommendation
} from '@/api/recommendation'
import type { LearningRecommendationVO } from '@/types/recommendation'

const router = useRouter()
const loading = ref(false)
const recommendations = ref<LearningRecommendationVO[]>([])
const selectedPriority = ref('ALL')

const filteredRecommendations = computed(() => {
  if (selectedPriority.value === 'ALL') {
    return recommendations.value.filter(rec => rec.status !== 'DISMISSED' && rec.status !== 'COMPLETED')
  }
  return recommendations.value.filter(
    rec => rec.priority === selectedPriority.value &&
           rec.status !== 'DISMISSED' &&
           rec.status !== 'COMPLETED'
  )
})

const loadRecommendations = async () => {
  loading.value = true
  try {
    const res = await getRecommendations()
    recommendations.value = res.data
  } catch (error: any) {
    ElMessage.error(error.message || 'Failed to load recommendations')
  } finally {
    loading.value = false
  }
}

const refreshRecommendations = () => {
  ElMessage.info('Refreshing recommendations...')
  loadRecommendations()
}

const filterRecommendations = () => {
  // Filter is computed, just trigger re-render
}

const startLearning = async (rec: LearningRecommendationVO) => {
  if (rec.status === 'PENDING') {
    try {
      await startRecommendation(rec.id)
      rec.status = 'IN_PROGRESS'
      ElMessage.success('Started learning!')
    } catch (error: any) {
      ElMessage.error('Failed to update status')
    }
  }

  // Navigate to resource
  if (rec.resourceLink) {
    if (rec.resourceLink.startsWith('/')) {
      router.push(rec.resourceLink)
    } else {
      window.open(rec.resourceLink, '_blank')
    }
  }
}

const dismissRecommendation = async (id: number) => {
  try {
    await dismissRec(id)
    const rec = recommendations.value.find(r => r.id === id)
    if (rec) rec.status = 'DISMISSED'
    ElMessage.success('Recommendation dismissed')
  } catch (error: any) {
    ElMessage.error('Failed to dismiss recommendation')
  }
}

const markCompleted = async (id: number) => {
  try {
    await completeRecommendation(id)
    const rec = recommendations.value.find(r => r.id === id)
    if (rec) rec.status = 'COMPLETED'
    ElMessage.success('Great job! Recommendation completed!')
  } catch (error: any) {
    ElMessage.error('Failed to mark as completed')
  }
}

const getPriorityTagType = (priority: string) => {
  const types = { HIGH: 'danger', MEDIUM: 'warning', LOW: 'info' } as const
  return types[priority as keyof typeof types] || 'info'
}

const getPriorityColor = (priority: string) => {
  const colors = { HIGH: '#f5222d', MEDIUM: '#faad14', LOW: '#1890ff' }
  return colors[priority as keyof typeof colors] || '#1890ff'
}

const getTypeTagType = (type: string) => {
  const types = { KNOWLEDGE_POINT: 'success', RESOURCE: 'warning', STUDY_PLAN: 'info' } as const
  return types[type as keyof typeof types] || 'info'
}

const getTypeIcon = (type: string) => {
  const icons = { KNOWLEDGE_POINT: Reading, RESOURCE: Document, STUDY_PLAN: Calendar }
  return icons[type as keyof typeof icons] || Reading
}

const formatType = (type: string) => {
  return type.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, l => l.toUpperCase())
}

const formatTimestamp = (timestamp?: string) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  const now = new Date()
  const diffMs = now.getTime() - date.getTime()
  const diffMins = Math.floor(diffMs / 60000)
  const diffHours = Math.floor(diffMs / 3600000)
  const diffDays = Math.floor(diffMs / 86400000)

  if (diffMins < 60) return `${diffMins}m ago`
  if (diffHours < 24) return `${diffHours}h ago`
  if (diffDays < 7) return `${diffDays}d ago`
  return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' })
}

const getButtonText = (rec: LearningRecommendationVO) => {
  if (rec.status === 'COMPLETED') return 'Completed'
  if (rec.status === 'DISMISSED') return 'Dismissed'
  if (rec.status === 'IN_PROGRESS') return 'Continue Learning'
  return 'Start Learning'
}

onMounted(() => {
  loadRecommendations()
})
</script>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.recommendations-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 24px;

    .header-content {
      .page-title {
        margin: 0 0 8px 0;
        font-size: 24px;
        font-weight: 600;
        color: $text-primary;
      }

      .page-subtitle {
        margin: 0;
        font-size: 14px;
        color: $text-secondary;
      }
    }
  }

  .filter-bar {
    margin-bottom: 24px;
  }

  .recommendations-list {
    display: flex;
    flex-direction: column;
    gap: 20px;

    .recommendation-card {
      border-left: 4px solid transparent;
      transition: all 0.3s ease;

      &.priority-high {
        border-left-color: #f5222d;
      }

      &.priority-medium {
        border-left-color: #faad14;
      }

      &.priority-low {
        border-left-color: #1890ff;
      }

      &:hover {
        box-shadow: $shadow-md;
        transform: translateY(-2px);
      }

      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 16px;

        .header-left {
          display: flex;
          gap: 8px;
        }

        .header-right {
          .timestamp {
            font-size: 12px;
            color: $text-disabled;
          }
        }
      }

      .card-body {
        display: flex;
        gap: 20px;
        margin-bottom: 20px;

        .recommendation-icon {
          flex-shrink: 0;
          width: 60px;
          height: 60px;
          display: flex;
          align-items: center;
          justify-content: center;
          background-color: $background-color;
          border-radius: $border-radius-lg;
        }

        .recommendation-content {
          flex: 1;

          .recommendation-title {
            margin: 0 0 12px 0;
            font-size: 18px;
            font-weight: 600;
            color: $text-primary;
          }

          .ai-reason {
            display: flex;
            gap: 10px;
            padding: 12px;
            background: linear-gradient(135deg, #e6f7ff 0%, #f0f5ff 100%);
            border-left: 3px solid $primary-color;
            border-radius: $border-radius-md;

            .ai-icon {
              flex-shrink: 0;
              margin-top: 2px;
              color: $primary-color;
            }

            .reason-text {
              margin: 0;
              font-size: 14px;
              line-height: 1.6;
              color: $text-secondary;
            }
          }
        }
      }

      .card-footer {
        display: flex;
        gap: 12px;
        padding-top: 16px;
        border-top: 1px solid $border-color;
      }
    }
  }
}
</style>
