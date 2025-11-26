import request from '@/utils/request'
import type { Result } from '@/utils/request'
import type { VideoProgressVO, VideoProgressSaveDTO, VideoResourceVO } from '@/types/video'

// Mock data for development
const mockVideoResource: VideoResourceVO = {
  id: 1,
  title: 'Introduction to Neural Networks',
  url: 'https://www.w3schools.com/html/mov_bbb.mp4', // Sample video for testing
  duration: 600, // 10 minutes
  courseName: 'Advanced Machine Learning',
  chapterName: 'Deep Learning'
}

const mockVideoProgress: VideoProgressVO = {
  id: 1,
  resourceId: 1,
  progress: JSON.stringify({ elapsed: 120, segments: [[0, 120]] }),
  completion: 20
}

const USE_MOCK = false
// Get video resource info
export const getVideoResource = (resourceId: number): Promise<Result<VideoResourceVO>> => {
  if (USE_MOCK) {
    return Promise.resolve({
      code: 0,
      msg: 'success',
      data: { ...mockVideoResource, id: resourceId }
    })
  }

  return request({
    url: `/v1/resources/${resourceId}`,
    method: 'get'
  })
}

// Get video progress
export const getVideoProgress = (resourceId: number): Promise<Result<VideoProgressVO>> => {
  if (USE_MOCK) {
    return Promise.resolve({
      code: 0,
      msg: 'success',
      data: { ...mockVideoProgress, resourceId }
    })
  }

  return request({
    url: `/v1/videos/${resourceId}/progress`,
    method: 'get'
  })
}

// Save video progress
export const saveVideoProgress = (data: VideoProgressSaveDTO): Promise<Result<void>> => {
  if (USE_MOCK) {
    console.log('Mock saving progress:', data)
    return Promise.resolve({
      code: 0,
      msg: 'success',
      data: undefined as unknown as void
    })
  }

  return request({
    url: `/v1/videos/${data.resourceId}/progress`,
    method: 'post',
    data
  })
}

// Utility: Parse progress JSON string
export const parseProgress = (progressStr: string): { elapsed: number; segments: Array<[number, number]> } => {
  try {
    return JSON.parse(progressStr)
  } catch {
    return { elapsed: 0, segments: [] }
  }
}

// Utility: Stringify progress data
export const stringifyProgress = (elapsed: number, segments: Array<[number, number]>): string => {
  return JSON.stringify({ elapsed, segments })
}

// Utility: Calculate completion percentage from segments
export const calculateCompletion = (segments: Array<[number, number]>, totalDuration: number): number => {
  if (totalDuration === 0) return 0

  // Merge overlapping segments
  const sorted = [...segments].sort((a, b) => a[0] - b[0])
  const merged: Array<[number, number]> = []

  for (const seg of sorted) {
    if (merged.length === 0 || merged[merged.length - 1][1] < seg[0]) {
      merged.push(seg)
    } else {
      merged[merged.length - 1][1] = Math.max(merged[merged.length - 1][1], seg[1])
    }
  }

  const watchedDuration = merged.reduce((sum, [start, end]) => sum + (end - start), 0)
  return Math.min(100, Math.round((watchedDuration / totalDuration) * 100))
}
