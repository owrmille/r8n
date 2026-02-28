export type OpinionStatus =
  | 'DRAFT'
  | 'PENDING_PREMODERATION'
  | 'PUBLISHED'
  | 'REJECTED'

export interface OpinionComponent {
  id: string
  opinionId: string
  weight: number
}

export interface Opinion {
  id: string
  ownerId: string
  ownerName: string
  subjectId: string
  subjectName: string
  subjective: string[]
  objective: string[]
  mark: number | null
  componentMark: number | null
  components: OpinionComponent[]
  status: OpinionStatus
  timestamp: Date
}

