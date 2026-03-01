import type { OpinionDto } from '@/entities/opinion/api/dto'
import { fromOpinionDto } from '@/entities/opinion/model/mappers'
import type { Opinion } from '@/entities/opinion/model/types'
import { http } from '@/shared/api/http'

export const getOpinionForSubject = async (subjectId: string): Promise<Opinion> => {
  const dto = await http.get<OpinionDto>('/opinions/for', {
    query: { subjectId },
  })

  return fromOpinionDto(dto)
}

