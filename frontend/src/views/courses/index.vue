<template>
  <div class="courses-page" v-loading="loading">
    <div class="page-header">
      <h1 class="page-title">My Courses</h1>
      <p class="page-subtitle">Browse and manage your enrolled courses</p>
    </div>

    <el-row :gutter="20">
      <el-col
        v-for="course in courses"
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

    <el-empty v-if="!loading && courses.length === 0" description="No courses enrolled yet" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import CourseCard from '@/components/CourseCard.vue'

interface Course {
  id: number
  courseName: string
  teacherName: string
  coverUrl?: string
  credits: number
  startDate: string
}

const router = useRouter()
const loading = ref(false)
const courses = ref<Course[]>([])

// Mock data for development
const mockCourses: Course[] = [
  {
    id: 1,
    courseName: 'Advanced Machine Learning',
    teacherName: 'Dr. Zhang Wei',
    coverUrl: '',
    credits: 4,
    startDate: '2024-09-01'
  },
  {
    id: 2,
    courseName: 'Data Structures & Algorithms',
    teacherName: 'Prof. Li Ming',
    coverUrl: '',
    credits: 3,
    startDate: '2024-09-01'
  },
  {
    id: 3,
    courseName: 'Web Development Fundamentals',
    teacherName: 'Ms. Wang Fang',
    coverUrl: '',
    credits: 3,
    startDate: '2024-09-15'
  },
  {
    id: 4,
    courseName: 'Database Systems',
    teacherName: 'Dr. Chen Hui',
    coverUrl: '',
    credits: 4,
    startDate: '2024-10-01'
  },
  {
    id: 5,
    courseName: 'Computer Networks',
    teacherName: 'Prof. Liu Yang',
    coverUrl: '',
    credits: 3,
    startDate: '2024-10-15'
  },
  {
    id: 6,
    courseName: 'Operating Systems',
    teacherName: 'Dr. Zhao Ming',
    coverUrl: '',
    credits: 4,
    startDate: '2024-11-01'
  }
]

const loadCourses = async () => {
  loading.value = true
  try {
    // In production, this would call the API
    await new Promise(resolve => setTimeout(resolve, 500))
    courses.value = mockCourses
  } catch (error: any) {
    ElMessage.error(error.message || 'Failed to load courses')
  } finally {
    loading.value = false
  }
}

const goToCourse = (courseId: number) => {
  router.push(`/courses/${courseId}`)
}

onMounted(() => {
  loadCourses()
})
</script>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.courses-page {
  .page-header {
    margin-bottom: 24px;

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

  .el-col {
    margin-bottom: 20px;
  }
}
</style>
