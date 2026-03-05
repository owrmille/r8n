import type { OpinionDto } from '@/entities/opinion/api/dto'
import { fromOpinionDto } from '@/entities/opinion/model/mappers'
import type { Opinion } from '@/entities/opinion/model/types'
import { http } from '@/shared/api/http'

export const getOpinionById = async (id: string): Promise<Opinion> => {
  const dto = await http.get<OpinionDto>('/opinions/id', {
    query: { id },
  })

  return fromOpinionDto(dto)
}

