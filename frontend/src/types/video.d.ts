// Matches Java: VideoProgressVO
export interface VideoProgressVO {
  id: number
  resourceId: number
  progress: string // JSON string: '{"elapsed":120, "segments":[[0,60]]}'
  completion: number // 0-100
}

// Parsed progress data structure
export interface VideoProgressData {
  elapsed: number // Current playback position in seconds
  segments: Array<[number, number]> // Watched segments [[start, end], ...]
}

// Save progress request DTO
export interface VideoProgressSaveDTO {
  resourceId: number
  progress: string // Stringified VideoProgressData
  completion: number
}

// Video resource info
export interface VideoResourceVO {
  id: number | string // Can be knowledge point ID (e.g., 'kp_22') or resource ID
  realResourceId?: number // Actual t_resource ID for progress tracking
  title: string
  url: string
  duration: number // Total duration in seconds
  courseName: string
  chapterName: string
  courseId?: number // Course ID for querying progress
}

// Video study statistics
export interface VideoStudyStatisticsVO {
  resourceId: number
  totalWatchTime: number
  completionRate: number
  lastWatchedAt: string
}
