<template>
  <div class="dashboard" v-loading="loading">
    <!-- Stats Row -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="12" :md="6">
        <StatCard
          title="My Courses"
          :value="dashboardData?.stats.myCourses || 0"
          :icon="Reading"
          color="#1890ff"
          bgColor="#e6f7ff"
        />
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <StatCard
          title="Pending Tasks"
          :value="dashboardData?.stats.pendingTasks || 0"
          :icon="Clock"
          color="#faad14"
          bgColor="#fff7e6"
        />
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <StatCard
          title="Weekly Submissions"
          :value="dashboardData?.stats.weeklySubmissions || 0"
          :icon="DocumentChecked"
          color="#52c41a"
          bgColor="#f6ffed"
        />
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <StatCard
          title="Unread Messages"
          :value="dashboardData?.stats.unreadMessages || 0"
          :icon="Message"
          color="#eb2f96"
          bgColor="#fff0f6"
        />
      </el-col>
    </el-row>

    <!-- Main Content Row -->
    <el-row :gutter="20" class="content-row">
      <!-- Left: Task Summary Chart & Pending Tasks -->
      <el-col :xs="24" :lg="16">
        <!-- Task Summary -->
        <el-card class="section-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">Task Summary</span>
              <el-tag :type="completionTagType">{{ dashboardData?.taskSummary.completionRate || 0 }}% Complete</el-tag>
            </div>
          </template>
          <el-row :gutter="20">
            <el-col :span="12">
              <div ref="chartRef" class="chart-container"></div>
            </el-col>
            <el-col :span="12">
              <div class="task-stats">
                <div class="task-stat-item">
                  <span class="stat-label">Total Tasks</span>
                  <span class="stat-value">{{ dashboardData?.taskSummary.totalTasks || 0 }}</span>
                </div>
                <div class="task-stat-item">
                  <span class="stat-label">Completed</span>
                  <span class="stat-value success">{{ dashboardData?.taskSummary.completedTasks || 0 }}</span>
                </div>
                <div class="task-stat-item">
                  <span class="stat-label">In Progress</span>
                  <span class="stat-value warning">{{ dashboardData?.taskSummary.inProgressTasks || 0 }}</span>
                </div>
                <div class="task-stat-item">
                  <span class="stat-label">Pending</span>
                  <span class="stat-value danger">{{ dashboardData?.taskSummary.pendingTasks || 0 }}</span>
                </div>
              </div>
            </el-col>
          </el-row>
        </el-card>

        <!-- Pending Tasks -->
        <el-card class="section-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">Upcoming Deadlines</span>
            </div>
          </template>
          <el-timeline>
            <el-timeline-item
              v-for="task in dashboardData?.pendingTasks"
              :key="task.taskId"
              :type="task.isOverdue ? 'danger' : 'primary'"
              :hollow="!task.isOverdue"
            >
              <div class="task-item">
                <div class="task-info">
                  <h4 class="task-title">{{ task.taskTitle }}</h4>
                  <p class="task-course">{{ task.courseName }}</p>
                </div>
                <div class="task-deadline" :class="{ overdue: task.isOverdue }">
                  <el-icon><Calendar /></el-icon>
                  <span>{{ formatDate(task.deadline) }}</span>
                  <el-tag v-if="task.isOverdue" type="danger" size="small">Overdue</el-tag>
                </div>
              </div>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-if="!dashboardData?.pendingTasks?.length" description="No pending tasks" />
        </el-card>
      </el-col>

      <!-- Right: Course Progress -->
      <el-col :xs="24" :lg="8">
        <el-card class="section-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">Course Progress</span>
            </div>
          </template>
          <div class="progress-list">
            <div
              v-for="course in dashboardData?.progressSummary"
              :key="course.courseId"
              class="progress-item"
            >
              <div class="progress-info">
                <span class="course-name">{{ course.courseName }}</span>
                <span class="progress-value">{{ course.totalProgress }}%</span>
              </div>
              <el-progress
                :percentage="course.totalProgress"
                :color="getProgressColor(course.totalProgress)"
                :stroke-width="8"
              />
            </div>
          </div>
          <el-empty v-if="!dashboardData?.progressSummary?.length" description="No course data" />
        </el-card>

        <!-- Capability Model Radar Chart -->
        <el-card class="section-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">Capability Model</span>
            </div>
          </template>
          <div ref="radarChartRef" class="radar-chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Recent Courses Row -->
    <el-card class="section-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">Recent Courses</span>
          <el-button type="primary" link @click="goToAllCourses">View All</el-button>
        </div>
      </template>
      <el-row :gutter="20">
        <el-col
          v-for="course in dashboardData?.recentCourses"
          :key="course.id"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
        >
          <CourseCard
            :id="course.id"
            :courseName="course.courseName"
            :teacherName="course.teacherName"
            :coverUrl="course.coverUrl"
            :credits="course.credits"
            :startDate="course.startDate"
            @click="goToCourse"
          />
        </el-col>
      </el-row>
      <el-empty v-if="!dashboardData?.recentCourses?.length" description="No courses enrolled" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Reading, Clock, DocumentChecked, Message, Calendar } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import StatCard from '@/components/StatCard.vue'
import CourseCard from '@/components/CourseCard.vue'
import { getDashboardData } from '@/api/dashboard'
import type { MyDashboardVO } from '@/types/dashboard'

const router = useRouter()
const loading = ref(false)
const dashboardData = ref<MyDashboardVO | null>(null)
const chartRef = ref<HTMLElement | null>(null)
const radarChartRef = ref<HTMLElement | null>(null)
let chartInstance: echarts.ECharts | null = null
let radarChartInstance: echarts.ECharts | null = null

const completionTagType = computed(() => {
  const rate = dashboardData.value?.taskSummary.completionRate || 0
  if (rate >= 80) return 'success'
  if (rate >= 50) return 'warning'
  return 'danger'
})

const loadDashboard = async () => {
  loading.value = true
  try {
    const res = await getDashboardData()
    dashboardData.value = res.data
    await nextTick()
    initChart()
    initRadarChart()
  } catch (error: any) {
    ElMessage.error(error.message || 'Failed to load dashboard data')
  } finally {
    loading.value = false
  }
}

const initChart = () => {
  if (!chartRef.value || !dashboardData.value) return

  chartInstance = echarts.init(chartRef.value)
  const { completedTasks, inProgressTasks, pendingTasks } = dashboardData.value.taskSummary

  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'horizontal',
      bottom: 0,
      itemWidth: 12,
      itemHeight: 12
    },
    series: [
      {
        name: 'Tasks',
        type: 'pie',
        radius: ['45%', '70%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 4,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 14,
            fontWeight: 'bold'
          }
        },
        data: [
          { value: completedTasks, name: 'Completed', itemStyle: { color: '#52c41a' } },
          { value: inProgressTasks, name: 'In Progress', itemStyle: { color: '#faad14' } },
          { value: pendingTasks, name: 'Pending', itemStyle: { color: '#f5222d' } }
        ]
      }
    ]
  }

  chartInstance.setOption(option)
}

const initRadarChart = () => {
  if (!radarChartRef.value || !dashboardData.value) return

  const capabilityData = dashboardData.value.stats.capabilityRadar || []

  radarChartInstance = echarts.init(radarChartRef.value)

  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}'
    },
    radar: {
      indicator: capabilityData.map(item => ({
        name: item.dimension,
        max: 100
      })),
      shape: 'polygon',
      splitNumber: 5,
      name: {
        textStyle: {
          color: '#333',
          fontSize: 12
        }
      },
      splitLine: {
        lineStyle: {
          color: '#d9d9d9'
        }
      },
      splitArea: {
        areaStyle: {
          color: ['rgba(24, 144, 255, 0.05)', 'rgba(24, 144, 255, 0.1)']
        }
      },
      axisLine: {
        lineStyle: {
          color: '#d9d9d9'
        }
      }
    },
    series: [
      {
        name: 'Capability Score',
        type: 'radar',
        data: [
          {
            value: capabilityData.map(item => item.score),
            name: 'My Capabilities',
            areaStyle: {
              color: 'rgba(24, 144, 255, 0.3)'
            },
            itemStyle: {
              color: '#1890ff'
            },
            lineStyle: {
              color: '#1890ff',
              width: 2
            }
          }
        ],
        emphasis: {
          lineStyle: {
            width: 4
          }
        }
      }
    ]
  }

  radarChartInstance.setOption(option)
}

const handleResize = () => {
  chartInstance?.resize()
  radarChartInstance?.resize()
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleDateString('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric'
  })
}

const getProgressColor = (progress: number) => {
  if (progress < 30) return '#f5222d'
  if (progress < 70) return '#faad14'
  return '#52c41a'
}

const goToCourse = (courseId: number) => {
  router.push(`/courses/${courseId}`)
}

const goToAllCourses = () => {
  router.push('/courses')
}

onMounted(() => {
  loadDashboard()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  chartInstance?.dispose()
  radarChartInstance?.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.dashboard {
  .stats-row {
    margin-bottom: 20px;

    .el-col {
      margin-bottom: 20px;
    }
  }

  .content-row {
    margin-bottom: 20px;
  }

  .section-card {
    margin-bottom: 20px;

    .card-header {
      display: flex;
      align-items: center;
      justify-content: space-between;

      .card-title {
        font-size: 16px;
        font-weight: 600;
        color: $text-primary;
      }
    }
  }

  .chart-container {
    height: 200px;
  }

  .radar-chart-container {
    height: 300px;
    width: 100%;
  }

  .task-stats {
    display: flex;
    flex-direction: column;
    justify-content: center;
    height: 200px;
    gap: 16px;

    .task-stat-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 16px;
      background-color: $background-color;
      border-radius: $border-radius-md;

      .stat-label {
        color: $text-secondary;
        font-size: 14px;
      }

      .stat-value {
        font-size: 18px;
        font-weight: 600;
        color: $text-primary;

        &.success { color: $success-color; }
        &.warning { color: $warning-color; }
        &.danger { color: $error-color; }
      }
    }
  }

  .task-item {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;

    .task-info {
      .task-title {
        margin: 0 0 4px 0;
        font-size: 14px;
        font-weight: 500;
        color: $text-primary;
      }

      .task-course {
        margin: 0;
        font-size: 12px;
        color: $text-secondary;
      }
    }

    .task-deadline {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 12px;
      color: $text-secondary;

      &.overdue {
        color: $error-color;
      }
    }
  }

  .progress-list {
    display: flex;
    flex-direction: column;
    gap: 20px;

    .progress-item {
      .progress-info {
        display: flex;
        justify-content: space-between;
        margin-bottom: 8px;

        .course-name {
          font-size: 14px;
          color: $text-primary;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
          max-width: 180px;
        }

        .progress-value {
          font-size: 14px;
          font-weight: 600;
          color: $text-secondary;
        }
      }
    }
  }

  :deep(.el-timeline-item__wrapper) {
    padding-left: 20px;
  }
}
</style>
