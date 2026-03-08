import type { OpinionDto } from '@/entities/opinion/api/dto'
import { fromOpinionDto } from '@/entities/opinion/model/mappers'
import type { Opinion } from '@/entities/opinion/model/types'
import { http } from '@/shared/api/http'

export const unlinkComponent = async (linkId: string): Promise<Opinion> => {
  const dto = await http.delete<OpinionDto>('/opinions/unlink', {
    query: { linkId },
  })

  return fromOpinionDto(dto)
}

