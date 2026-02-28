import type { OpinionDto } from '@/entities/opinion/api/dto'
import { fromOpinionDto } from '@/entities/opinion/model/mappers'
import type { Opinion } from '@/entities/opinion/model/types'
import { http } from '@/shared/api/http'

export interface UpdateOpinionPayload {
  opinionId: string
  subjective?: string[]
  objective?: string[]
  mark?: number | null
}

export const updateOpinion = async (payload: UpdateOpinionPayload): Promise<Opinion> => {
  const dto = await http.patch<OpinionDto>('/opinions/update', {
    query: {
      opinionId: payload.opinionId,
      subjective: payload.subjective ?? [],
      objective: payload.objective ?? [],
      mark: payload.mark ?? undefined,
    },
  })

  return fromOpinionDto(dto)
}

