import type { OpinionDto } from '@/entities/opinion/api/dto'
import { fromOpinionDto } from '@/entities/opinion/model/mappers'
import type { Opinion } from '@/entities/opinion/model/types'
import { http } from '@/shared/api/http'

export interface AdjustComponentWeightPayload {
  linkId: string
  weight: number
}

export const adjustComponentWeight = async (
  payload: AdjustComponentWeightPayload,
): Promise<Opinion> => {
  const dto = await http.patch<OpinionDto>('/opinions/adjustComponentWeight', {
    query: {
      linkId: payload.linkId,
      weight: payload.weight,
    },
  })

  return fromOpinionDto(dto)
}
