import { http } from '@/shared/api/http'

export const deleteOpinion = async (opinionId: string): Promise<void> => {
  await http.delete<null>('/opinions/delete', {
    query: { opinionId },
  })
}

