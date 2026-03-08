export type OpinionStatusDto =
  | 'DRAFT'
  | 'PENDING_PREMODERATION'
  | 'PUBLISHED'
  | 'REJECTED'

export interface WeightedOpinionReferenceDto {
  id: string
  opinion: string
  weight: number
}

export interface OpinionDto {
  id: string
  owner: string
  ownerName: string
  subject: string
  subjectName: string
  subjective: string[]
  objective: string[]
  mark: number | null
  componentMark: number | null
  components: WeightedOpinionReferenceDto[]
  status: OpinionStatusDto
  timestamp: string
}

