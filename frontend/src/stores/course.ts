import { defineStore } from 'pinia'
import { ref } from 'vue'

export interface Course {
  id: number
  courseName: string
  teacherName: string
  description: string
  coverImage: string
  progress: number
}

export const useCourseStore = defineStore('course', () => {
  const currentCourse = ref<Course | null>(null)
  const courseList = ref<Course[]>([])
  const loading = ref(false)

  const setCurrentCourse = (course: Course | null) => {
    currentCourse.value = course
  }

  const setCourseList = (courses: Course[]) => {
    courseList.value = courses
  }

  const setLoading = (value: boolean) => {
    loading.value = value
  }

  const clearCourseData = () => {
    currentCourse.value = null
    courseList.value = []
  }

  return {
    currentCourse,
    courseList,
    loading,
    setCurrentCourse,
    setCourseList,
    setLoading,
    clearCourseData
  }
})
