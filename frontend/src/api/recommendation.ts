import request from '@/utils/request'
import type { Result } from '@/utils/request'
import type { LearningRecommendationVO, RecommendationStatusUpdateDTO } from '@/types/recommendation'

// Mock data for development
const mockRecommendations: LearningRecommendationVO[] = [
  {
    id: 1,
    type: 'KNOWLEDGE_POINT',
    title: 'Review Matrix Multiplication',
    reason: 'Your accuracy was low (45%) in the last Linear Algebra quiz. Reviewing matrix multiplication fundamentals will help improve your understanding of advanced topics.',
    resourceLink: '/study/video/kp1',
    priority: 'HIGH',
    status: 'PENDING',
    createdAt: '2024-12-01T10:30:00'
  },
  {
    id: 2,
    type: 'RESOURCE',
    title: 'Watch: Introduction to Neural Networks',
    reason: 'Based on your learning path, this video will prepare you for the upcoming Deep Learning module. Students who watched this improved their test scores by 35%.',
    resourceLink: '/study/video/kp6',
    priority: 'HIGH',
    status: 'PENDING',
    createdAt: '2024-12-01T09:15:00'
  },
  {
    id: 3,
    type: 'STUDY_PLAN',
    title: 'Create Weekly Study Plan for Machine Learning',
    reason: 'You have 3 pending assignments due in the next 7 days. A structured study plan will help you manage your time effectively and avoid last-minute cramming.',
    resourceLink: '/study/plans',
    priority: 'MEDIUM',
    status: 'PENDING',
    createdAt: '2024-11-30T14:20:00'
  },
  {
    id: 4,
    type: 'KNOWLEDGE_POINT',
    title: 'Practice Decision Tree Algorithms',
    reason: 'This topic has a prerequisite relationship with Random Forests, which you\'ll study next week. Getting comfortable with decision trees now will make the transition smoother.',
    resourceLink: '/study/video/kp3',
    priority: 'MEDIUM',
    status: 'PENDING',
    createdAt: '2024-11-30T11:00:00'
  },
  {
    id: 5,
    type: 'RESOURCE',
    title: 'Read: Principal Component Analysis (PCA) Tutorial',
    reason: 'You marked this knowledge point as "WEAK" in your self-assessment. This comprehensive tutorial includes interactive examples to help solidify your understanding.',
    resourceLink: '/resources/pca-tutorial',
    priority: 'HIGH',
    status: 'PENDING',
    createdAt: '2024-11-29T16:45:00'
  },
  {
    id: 6,
    type: 'KNOWLEDGE_POINT',
    title: 'Review K-Means Clustering Concepts',
    reason: 'Other students in similar learning paths found this review helpful before tackling hierarchical clustering. It takes about 20 minutes and includes practice problems.',
    resourceLink: '/study/video/kp4',
    priority: 'LOW',
    status: 'PENDING',
    createdAt: '2024-11-29T10:30:00'
  },
  {
    id: 7,
    type: 'STUDY_PLAN',
    title: 'Join Study Group for Deep Learning',
    reason: 'Collaborative learning can improve retention by up to 40%. There\'s an active study group meeting twice weekly that aligns with your schedule.',
    resourceLink: '/study/groups',
    priority: 'LOW',
    status: 'PENDING',
    createdAt: '2024-11-28T13:15:00'
  }
]

const USE_MOCK = false

// Generate AI recommendations for a specific course
export const generateRecommendations = (courseId: number): Promise<Result<string>> => {
  if (USE_MOCK) {
    console.log('Mock generating recommendations for course:', courseId)
    return Promise.resolve({
      code: 0,
      msg: 'AI recommendations generated successfully',
      data: 'success'
    })
  }

  return request({
    url: `/v1/recommendations/my-recommendations?courseId=${courseId}`,
    method: 'post'
  })
}

// Get all recommendations for current student (optionally filtered by course)
export const getRecommendations = (courseId?: number): Promise<Result<LearningRecommendationVO[]>> => {
  if (USE_MOCK) {
    return Promise.resolve({
      code: 0,
      msg: 'success',
      data: mockRecommendations
    })
  }

  const url = courseId
    ? `/v1/recommendations?courseId=${courseId}`
    : '/v1/recommendations'

  return request({
    url,
    method: 'get'
  })
}

// Update recommendation status
export const updateRecommendationStatus = (
  data: RecommendationStatusUpdateDTO
): Promise<Result<void>> => {
  if (USE_MOCK) {
    console.log('Mock updating recommendation status:', data)
    return Promise.resolve({
      code: 0,
      msg: 'success',
      data: undefined as unknown as void
    })
  }

  return request({
    url: `/v1/recommendations/${data.recommendationId}/status`,
    method: 'put',
    data: { status: data.status }
  })
}

// Dismiss a recommendation
export const dismissRecommendation = (recommendationId: number): Promise<Result<void>> => {
  return updateRecommendationStatus({
    recommendationId,
    status: 'DISMISSED'
  })
}

// Mark recommendation as in progress
export const startRecommendation = (recommendationId: number): Promise<Result<void>> => {
  return updateRecommendationStatus({
    recommendationId,
    status: 'IN_PROGRESS'
  })
}

// Mark recommendation as completed
export const completeRecommendation = (recommendationId: number): Promise<Result<void>> => {
  return updateRecommendationStatus({
    recommendationId,
    status: 'COMPLETED'
  })
}
