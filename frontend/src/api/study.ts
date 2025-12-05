import request from '@/utils/request'

/**
 * Get knowledge point progress list for a course
 * @param courseId Course ID
 * @returns List of knowledge point progress
 */
export function getKnowledgePointProgressList(courseId: number) {
  return request({
    url: `/v1/study/knowledge-points/course/${courseId}`,
    method: 'get'
  })
}

/**
 * Update knowledge point progress
 * @param kpId Knowledge point ID
 * @param data Update data (including notes)
 * @returns Update result
 */
export function updateKnowledgePoint(kpId: number, data: any) {
  return request({
    url: `/v1/study/knowledge-points/${kpId}`,
    method: 'put',
    data
  })
}
