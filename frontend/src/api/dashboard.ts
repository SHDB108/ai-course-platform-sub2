import request from '@/utils/request'
import type { Result } from '@/utils/request'
import type { MyDashboardVO } from '@/types/dashboard'

// Mock data for development
const mockDashboardData: MyDashboardVO = {
  stats: {
    myCourses: 6,
    pendingTasks: 8,
    weeklySubmissions: 12,
    unreadMessages: 3,
    projects: 2,
    todoItems: {
      pending: 5,
      total: 15
    },
    capabilityRadar: [
      { dimension: 'Theory', score: 85 },
      { dimension: 'Practice', score: 72 },
      { dimension: 'Innovation', score: 68 },
      { dimension: 'Coding', score: 90 },
      { dimension: 'Communication', score: 75 },
      { dimension: 'Teamwork', score: 80 }
    ]
  },
  taskSummary: {
    totalTasks: 24,
    pendingTasks: 8,
    inProgressTasks: 4,
    completedTasks: 12,
    completionRate: 50
  },
  recentCourses: [
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
    }
  ],
  pendingTasks: [
    {
      taskId: 1,
      taskTitle: 'Machine Learning Assignment 3',
      courseName: 'Advanced Machine Learning',
      deadline: '2024-12-15',
      isOverdue: false
    },
    {
      taskId: 2,
      taskTitle: 'Binary Tree Implementation',
      courseName: 'Data Structures & Algorithms',
      deadline: '2024-12-10',
      isOverdue: false
    },
    {
      taskId: 3,
      taskTitle: 'React Project Submission',
      courseName: 'Web Development Fundamentals',
      deadline: '2024-12-08',
      isOverdue: true
    },
    {
      taskId: 4,
      taskTitle: 'SQL Query Optimization',
      courseName: 'Database Systems',
      deadline: '2024-12-20',
      isOverdue: false
    }
  ],
  progressSummary: [
    { courseId: 1, courseName: 'Advanced Machine Learning', totalProgress: 75 },
    { courseId: 2, courseName: 'Data Structures & Algorithms', totalProgress: 60 },
    { courseId: 3, courseName: 'Web Development Fundamentals', totalProgress: 85 },
    { courseId: 4, courseName: 'Database Systems', totalProgress: 45 }
  ]
}

// Use mock data in development, real API in production
const USE_MOCK = false

export const getDashboardData = (): Promise<Result<MyDashboardVO>> => {
  if (USE_MOCK) {
    return Promise.resolve({
      code: 0,
      msg: 'success',
      data: mockDashboardData
    })
  }

  return request({
    url: '/v1/student/dashboard/my',
    method: 'get'
  })
}
