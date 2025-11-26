// Mastery level enum for knowledge points
export type MasteryLevel = 'MASTERED' | 'LEARNING' | 'WEAK' | 'UNKNOWN'

// Matches Java: KnowledgeGraphVO
export interface KnowledgeGraphVO {
  nodes: Array<{
    id: string
    name: string
    category: number // 0=Root, 1=Chapter, 2=KnowledgePoint
    masteryLevel?: MasteryLevel // Color rendering based on student mastery
  }>
  edges: Array<{
    source: string
    target: string
    label: string
  }>
}

// Node category enum for clarity
export enum NodeCategory {
  ROOT = 0,
  CHAPTER = 1,
  KNOWLEDGE_POINT = 2
}

// G6 node configuration type
export interface G6NodeConfig {
  id: string
  label: string
  category: number
  masteryLevel?: MasteryLevel
  style?: {
    fill?: string
    stroke?: string
    lineWidth?: number
  }
}

// G6 edge configuration type
export interface G6EdgeConfig {
  source: string
  target: string
  label?: string
  style?: {
    stroke?: string
    lineWidth?: number
  }
}

// G6 graph data format
export interface G6GraphData {
  nodes: G6NodeConfig[]
  edges: G6EdgeConfig[]
}
