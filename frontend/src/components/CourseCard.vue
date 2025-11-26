<template>
  <el-card class="course-card" :body-style="{ padding: '0' }" @click="handleClick">
    <div class="course-cover">
      <img v-if="coverUrl" :src="coverUrl" :alt="courseName" />
      <div v-else class="cover-placeholder">
        <el-icon :size="40"><Reading /></el-icon>
      </div>
    </div>
    <div class="course-info">
      <h3 class="course-name" :title="courseName">{{ courseName }}</h3>
      <p class="course-teacher">
        <el-icon><User /></el-icon>
        <span>{{ teacherName }}</span>
      </p>
      <div class="course-meta">
        <el-tag size="small" type="info">{{ credits }} Credits</el-tag>
        <span class="start-date">
          <el-icon><Calendar /></el-icon>
          {{ formatDate(startDate) }}
        </span>
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { Reading, User, Calendar } from '@element-plus/icons-vue'

interface Props {
  id: number
  courseName: string
  teacherName: string
  coverUrl?: string
  credits: number
  startDate: string
}

const props = defineProps<Props>()

const emit = defineEmits<{
  click: [id: number]
}>()

const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  })
}

const handleClick = () => {
  emit('click', props.id)
}
</script>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.course-card {
  cursor: pointer;
  transition: all 0.3s ease;
  overflow: hidden;

  &:hover {
    transform: translateY(-4px);
    box-shadow: $shadow-md;

    .course-cover img {
      transform: scale(1.05);
    }
  }

  .course-cover {
    height: 140px;
    overflow: hidden;
    background-color: $background-color;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.3s ease;
    }

    .cover-placeholder {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, $primary-light 0%, $primary-color 100%);
      color: $white;
    }
  }

  .course-info {
    padding: 16px;

    .course-name {
      font-size: 16px;
      font-weight: 600;
      color: $text-primary;
      margin: 0 0 8px 0;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .course-teacher {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 13px;
      color: $text-secondary;
      margin: 0 0 12px 0;

      .el-icon {
        font-size: 14px;
      }
    }

    .course-meta {
      display: flex;
      align-items: center;
      justify-content: space-between;

      .start-date {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 12px;
        color: $text-disabled;

        .el-icon {
          font-size: 12px;
        }
      }
    }
  }
}
</style>
