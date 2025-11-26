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
  id: number
  title: string
  url: string
  duration: number // Total duration in seconds
  courseName: string
  chapterName: string
}

// Video study statistics
export interface VideoStudyStatisticsVO {
  resourceId: number
  totalWatchTime: number
  completionRate: number
  lastWatchedAt: string
}
