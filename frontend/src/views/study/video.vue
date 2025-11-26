<template>
  <div class="video-study" v-loading="loading">
    <!-- Video Player Section -->
    <el-card class="video-card">
      <div class="video-header">
        <div class="video-info">
          <h1 class="video-title">{{ videoResource?.title }}</h1>
          <div class="video-meta">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item>{{ videoResource?.courseName }}</el-breadcrumb-item>
              <el-breadcrumb-item>{{ videoResource?.chapterName }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
        </div>
        <div class="video-actions">
          <el-button @click="goBack">
            <el-icon><Back /></el-icon>
            Back
          </el-button>
        </div>
      </div>

      <div class="video-container">
        <video
          v-if="videoResource && videoResource.url"
          ref="videoRef"
          :src="videoResource?.url"
          controls
          @timeupdate="onTimeUpdate"
          @loadedmetadata="onVideoLoaded"
          @ended="onVideoEnded"
          @play="onVideoPlay"
          @pause="onVideoPause"
        >
          Your browser does not support the video tag.
        </video>
        <div v-else class="video-placeholder">
          <el-empty description="Resource Pending Integration">
            <template #image>
              <el-icon :size="100" color="#909399">
                <VideoCameraFilled />
              </el-icon>
            </template>
            <p>
              No video source configured for this knowledge point. System supports integration with Local Files (MP4) or External Links (e.g., Bilibili).
            </p>
          </el-empty>
        </div>
      </div>

      <!-- Progress Bar -->
      <div class="progress-section">
        <div class="progress-header">
          <span>Learning Progress</span>
          <span class="completion">{{ completion }}% Complete</span>
        </div>
        <el-progress
          :percentage="completion"
          :color="getProgressColor(completion)"
          :stroke-width="10"
        />
        <div class="watched-segments">
          <div
            v-for="(segment, index) in watchedSegments"
            :key="index"
            class="segment"
            :style="getSegmentStyle(segment)"
          ></div>
        </div>
      </div>
    </el-card>

    <!-- Video Stats -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon :size="32" color="#1890ff"><VideoCamera /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ formatDuration(currentTime) }}</div>
              <div class="stat-label">Current Position</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon :size="32" color="#52c41a"><Clock /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ formatDuration(totalDuration) }}</div>
              <div class="stat-label">Total Duration</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon :size="32" color="#faad14"><Finished /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ formatDuration(totalWatched) }}</div>
              <div class="stat-label">Time Watched</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Notes Section (Optional) -->
    <el-card class="notes-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">Study Notes</span>
          <el-button type="primary" size="small" @click="saveNotes">
            <el-icon><DocumentAdd /></el-icon>
            Save Notes
          </el-button>
        </div>
      </template>
      <el-input
        v-model="notes"
        type="textarea"
        :rows="4"
        placeholder="Take notes while learning..."
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Back, VideoCamera, Clock, Finished, DocumentAdd, VideoCameraFilled } from '@element-plus/icons-vue'
import {
  getVideoResource,
  getVideoProgress,
  saveVideoProgress,
  parseProgress,
  stringifyProgress,
  calculateCompletion
} from '@/api/video'
import type { VideoResourceVO } from '@/types/video'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const videoRef = ref<HTMLVideoElement | null>(null)
const videoResource = ref<VideoResourceVO | null>(null)
const notes = ref('')

// Video state
const currentTime = ref(0)
const totalDuration = ref(0)
const isPlaying = ref(false)
const watchedSegments = ref<Array<[number, number]>>([])
const currentSegmentStart = ref<number | null>(null)

// Save interval
let saveInterval: number | null = null

const resourceId = computed(() => Number(route.params.resourceId) || 1)

const completion = computed(() => {
  return calculateCompletion(watchedSegments.value, totalDuration.value)
})

const totalWatched = computed(() => {
  return watchedSegments.value.reduce((sum, [start, end]) => sum + (end - start), 0)
})

const loadVideoData = async () => {
  loading.value = true
  try {
    const [resourceRes, progressRes] = await Promise.all([
      getVideoResource(resourceId.value),
      getVideoProgress(resourceId.value)
    ])

    videoResource.value = resourceRes.data
    totalDuration.value = resourceRes.data.duration

    // Parse saved progress
    const progressData = parseProgress(progressRes.data.progress)
    watchedSegments.value = progressData.segments
    currentTime.value = progressData.elapsed

    // Seek to last position after video loads
  } catch (error: any) {
    ElMessage.error(error.message || 'Failed to load video')
  } finally {
    loading.value = false
  }
}

const onVideoLoaded = () => {
  if (videoRef.value && currentTime.value > 0) {
    videoRef.value.currentTime = currentTime.value
  }
  if (videoRef.value) {
    totalDuration.value = videoRef.value.duration
  }
}

const onTimeUpdate = () => {
  if (videoRef.value) {
    currentTime.value = videoRef.value.currentTime
  }
}

const onVideoPlay = () => {
  isPlaying.value = true
  currentSegmentStart.value = currentTime.value
}

const onVideoPause = () => {
  isPlaying.value = false
  recordSegment()
}

const onVideoEnded = () => {
  isPlaying.value = false
  recordSegment()
  saveProgress()
  ElMessage.success('Video completed!')
}

const recordSegment = () => {
  if (currentSegmentStart.value !== null) {
    const end = currentTime.value
    if (end > currentSegmentStart.value) {
      watchedSegments.value.push([currentSegmentStart.value, end])
      mergeSegments()
    }
    currentSegmentStart.value = null
  }
}

const mergeSegments = () => {
  const sorted = [...watchedSegments.value].sort((a, b) => a[0] - b[0])
  const merged: Array<[number, number]> = []

  for (const seg of sorted) {
    if (merged.length === 0 || merged[merged.length - 1][1] < seg[0] - 1) {
      merged.push([...seg] as [number, number])
    } else {
      merged[merged.length - 1][1] = Math.max(merged[merged.length - 1][1], seg[1])
    }
  }

  watchedSegments.value = merged
}

const saveProgress = async () => {
  if (!videoResource.value) return

  try {
    await saveVideoProgress({
      resourceId: resourceId.value,
      progress: stringifyProgress(currentTime.value, watchedSegments.value),
      completion: completion.value
    })
  } catch (error: any) {
    console.error('Failed to save progress:', error)
  }
}

const getSegmentStyle = (segment: [number, number]) => {
  const left = (segment[0] / totalDuration.value) * 100
  const width = ((segment[1] - segment[0]) / totalDuration.value) * 100
  return {
    left: `${left}%`,
    width: `${width}%`
  }
}

const formatDuration = (seconds: number) => {
  const mins = Math.floor(seconds / 60)
  const secs = Math.floor(seconds % 60)
  return `${mins}:${secs.toString().padStart(2, '0')}`
}

const getProgressColor = (progress: number) => {
  if (progress < 30) return '#f5222d'
  if (progress < 70) return '#faad14'
  return '#52c41a'
}

const goBack = () => {
  saveProgress()
  router.back()
}

const saveNotes = () => {
  ElMessage.success('Notes saved!')
}

// Auto-save every 10 seconds while playing
watch(isPlaying, (playing) => {
  if (playing) {
    saveInterval = window.setInterval(saveProgress, 10000)
  } else if (saveInterval) {
    clearInterval(saveInterval)
    saveInterval = null
  }
})

onMounted(() => {
  loadVideoData()
})

onUnmounted(() => {
  if (saveInterval) {
    clearInterval(saveInterval)
  }
  saveProgress()
})
</script>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.video-study {
  max-width: 1200px;
  margin: 0 auto;

  .video-card {
    margin-bottom: 20px;

    .video-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 16px;

      .video-info {
        .video-title {
          margin: 0 0 8px 0;
          font-size: 20px;
          font-weight: 600;
          color: $text-primary;
        }

        .video-meta {
          font-size: 13px;
          color: $text-secondary;
        }
      }
    }

    .video-container {
      width: 100%;
      background: #f0f2f5;
      border-radius: $border-radius-md;
      overflow: hidden;
      margin-bottom: 20px;
      min-height: 450px;
      display: flex;
      align-items: center;
      justify-content: center;

      video {
        width: 100%;
        max-height: 500px;
        display: block;
        background-color: #000;
      }

      .video-placeholder {
        text-align: center;

        p {
          margin-top: 16px;
          font-size: 14px;
          color: $text-secondary;
          line-height: 1.6;
          max-width: 500px;
        }
      }
    }

    .progress-section {
      .progress-header {
        display: flex;
        justify-content: space-between;
        margin-bottom: 8px;
        font-size: 14px;

        .completion {
          font-weight: 600;
          color: $primary-color;
        }
      }

      .watched-segments {
        position: relative;
        height: 6px;
        background-color: $background-color;
        border-radius: 3px;
        margin-top: 8px;

        .segment {
          position: absolute;
          height: 100%;
          background-color: $primary-color;
          border-radius: 3px;
          opacity: 0.6;
        }
      }
    }
  }

  .stats-row {
    margin-bottom: 20px;

    .stat-card {
      .stat-content {
        display: flex;
        align-items: center;
        gap: 16px;

        .stat-info {
          .stat-value {
            font-size: 24px;
            font-weight: 600;
            color: $text-primary;
          }

          .stat-label {
            font-size: 13px;
            color: $text-secondary;
          }
        }
      }
    }
  }

  .notes-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .card-title {
        font-size: 16px;
        font-weight: 600;
        color: $text-primary;
      }
    }
  }
}
</style>
