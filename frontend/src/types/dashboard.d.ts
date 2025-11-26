// Matches Java: MyDashboardVO
export interface MyDashboardVO {
  stats: {
    myCourses: number
    pendingTasks: number
    weeklySubmissions: number
    unreadMessages: number
    projects: number
    todoItems: {
      pending: number
      total: number
    }
    capabilityRadar: Array<{
      dimension: string // e.g., "Theory", "Practice", "Innovation", "Coding"
      score: number // 0-100
    }>
  }
  taskSummary: {
    totalTasks: number
    pendingTasks: number
    inProgressTasks: number
    completedTasks: number
    completionRate: number
  }
  recentCourses: Array<{
    id: number
    courseName: string
    teacherName: string
    coverUrl?: string
    credits: number
    startDate: string
  }>
  pendingTasks: Array<{
    taskId: number
    taskTitle: string
    courseName: string
    deadline: string
    isOverdue: boolean
  }>
  progressSummary: Array<{
    courseId: number
    courseName: string
    totalProgress: number
  }>
}

// Stats card display type
export interface StatItem {
  title: string
  value: number | string
  icon: string
  color: string
  bgColor: string
}
