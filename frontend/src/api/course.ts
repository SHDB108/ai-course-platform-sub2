import request from '@/utils/request'
import type { Result } from '@/utils/request'
import type { KnowledgeGraphVO } from '@/types/graph'

// Mock data for development
const mockGraphData: KnowledgeGraphVO = {
  nodes: [
    { id: 'root', name: 'Machine Learning', category: 0 },
    { id: 'ch1', name: 'Supervised Learning', category: 1 },
    { id: 'ch2', name: 'Unsupervised Learning', category: 1 },
    { id: 'ch3', name: 'Deep Learning', category: 1 },
    { id: 'kp1', name: 'Linear Regression', category: 2, masteryLevel: 'MASTERED' },
    { id: 'kp2', name: 'Logistic Regression', category: 2, masteryLevel: 'MASTERED' },
    { id: 'kp3', name: 'Decision Trees', category: 2, masteryLevel: 'LEARNING' },
    { id: 'kp4', name: 'K-Means Clustering', category: 2, masteryLevel: 'LEARNING' },
    { id: 'kp5', name: 'PCA', category: 2, masteryLevel: 'WEAK' },
    { id: 'kp6', name: 'Neural Networks', category: 2, masteryLevel: 'WEAK' },
    { id: 'kp7', name: 'CNN', category: 2, masteryLevel: 'UNKNOWN' },
    { id: 'kp8', name: 'RNN', category: 2, masteryLevel: 'UNKNOWN' }
  ],
  edges: [
    { source: 'root', target: 'ch1', label: 'contains' },
    { source: 'root', target: 'ch2', label: 'contains' },
    { source: 'root', target: 'ch3', label: 'contains' },
    { source: 'ch1', target: 'kp1', label: 'includes' },
    { source: 'ch1', target: 'kp2', label: 'includes' },
    { source: 'ch1', target: 'kp3', label: 'includes' },
    { source: 'ch2', target: 'kp4', label: 'includes' },
    { source: 'ch2', target: 'kp5', label: 'includes' },
    { source: 'ch3', target: 'kp6', label: 'includes' },
    { source: 'ch3', target: 'kp7', label: 'includes' },
    { source: 'ch3', target: 'kp8', label: 'includes' },
    { source: 'kp1', target: 'kp2', label: 'prerequisite' },
    { source: 'kp6', target: 'kp7', label: 'prerequisite' },
    { source: 'kp6', target: 'kp8', label: 'prerequisite' }
  ]
}

const USE_MOCK = import.meta.env.DEV

// Get course knowledge graph
export const getCourseGraph = (courseId: number): Promise<Result<KnowledgeGraphVO>> => {
  if (USE_MOCK) {
    return Promise.resolve({
      code: 0,
      msg: 'success',
      data: mockGraphData
    })
  }

  return request({
    url: `/v1/knowledge-graph/${courseId}`,
    method: 'get'
  })
}

// Course detail info
export interface CourseDetailVO {
  id: number
  courseName: string
  teacherName: string
  description: string
  credits: number
  startDate: string
  endDate: string
  coverUrl?: string
}

const mockCourseDetail: CourseDetailVO = {
  id: 1,
  courseName: 'Advanced Machine Learning',
  teacherName: 'Dr. Zhang Wei',
  description: 'A comprehensive course covering supervised, unsupervised, and deep learning techniques.',
  credits: 4,
  startDate: '2024-09-01',
  endDate: '2025-01-15',
  coverUrl: ''
}

// Get course detail
export const getCourseDetail = (courseId: number): Promise<Result<CourseDetailVO>> => {
  if (USE_MOCK) {
    return Promise.resolve({
      code: 0,
      msg: 'success',
      data: { ...mockCourseDetail, id: courseId }
    })
  }

  return request({
    url: `/v1/courses/${courseId}`,
    method: 'get'
  })
}
