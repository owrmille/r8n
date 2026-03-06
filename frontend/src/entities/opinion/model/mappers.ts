import type { OpinionDto, WeightedOpinionReferenceDto } from '@/entities/opinion/api/dto'
import type { Opinion, OpinionComponent } from '@/entities/opinion/model/types'

const fromWeightedOpinionReferenceDto = (dto: WeightedOpinionReferenceDto): OpinionComponent => {
  return {
    id: dto.id,
    opinionId: dto.opinion,
    weight: dto.weight,
  }
}

export const fromOpinionDto = (dto: OpinionDto): Opinion => {
  return {
    id: dto.id,
    ownerId: dto.owner,
    ownerName: dto.ownerName,
    subjectId: dto.subject,
    subjectName: dto.subjectName,
    subjective: dto.subjective,
    objective: dto.objective,
    mark: dto.mark,
    componentMark: dto.componentMark,
    components: dto.components.map(fromWeightedOpinionReferenceDto),
    status: dto.status,
    timestamp: new Date(dto.timestamp),
  }
}

