import type { OpinionDto } from '@/entities/opinion/api/dto'
import { fromOpinionDto } from '@/entities/opinion/model/mappers'
import type { Opinion } from '@/entities/opinion/model/types'
import { http } from '@/shared/api/http'

export interface LinkComponentPayload {
  parentOpinionId: string
  childOpinionId: string
  weight: number
}

export const linkComponent = async (payload: LinkComponentPayload): Promise<Opinion> => {
  const dto = await http.post<OpinionDto>('/opinions/link', {
    query: {
      parentOpinionId: payload.parentOpinionId,
      childOpinionId: payload.childOpinionId,
      weight: payload.weight,
    },
  })

  return fromOpinionDto(dto)
}
