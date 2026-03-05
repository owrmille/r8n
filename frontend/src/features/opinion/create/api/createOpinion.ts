import type { OpinionDto } from '@/entities/opinion/api/dto'
import { fromOpinionDto } from '@/entities/opinion/model/mappers'
import type { Opinion } from '@/entities/opinion/model/types'
import { http } from '@/shared/api/http'

export interface CreateOpinionPayload {
  subjectId: string
  subjective?: string[]
  objective?: string[]
  mark?: number | null
}

export const createOpinion = async (payload: CreateOpinionPayload): Promise<Opinion> => {
  const dto = await http.post<OpinionDto>('/opinions/add', {
    query: {
      subjectId: payload.subjectId,
      subjective: payload.subjective ?? [],
      objective: payload.objective ?? [],
      mark: payload.mark ?? undefined,
    },
  })

  return fromOpinionDto(dto)
}

