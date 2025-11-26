// Matches Java: LearningRecommendationVO
export interface LearningRecommendationVO {
  id: number
  type: 'KNOWLEDGE_POINT' | 'RESOURCE' | 'STUDY_PLAN'
  title: string // e.g., "Review Matrix Multiplication"
  reason: string // AI generated reason, e.g., "Your accuracy was low in the last quiz."
  resourceLink?: string
  priority: 'HIGH' | 'MEDIUM' | 'LOW'
  status?: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'DISMISSED'
  createdAt?: string
}

// Priority type
export type RecommendationPriority = 'HIGH' | 'MEDIUM' | 'LOW'

// Type enum
export type RecommendationType = 'KNOWLEDGE_POINT' | 'RESOURCE' | 'STUDY_PLAN'

// Status enum
export type RecommendationStatus = 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'DISMISSED'

// Update recommendation status DTO
export interface RecommendationStatusUpdateDTO {
  recommendationId: number
  status: RecommendationStatus
}
