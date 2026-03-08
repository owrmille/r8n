import type { AuthenticationTokenDto } from '@/features/auth/api/dto'
import type { AuthSession } from '@/features/auth/model/authSession'
import { fromAuthenticationTokenDto } from '@/features/auth/model/mappers'
import { http } from '@/shared/api/http'

export const refreshSession = async (refreshToken: string): Promise<AuthSession> => {
  const dto = await http.post<AuthenticationTokenDto>('/auth/refresh', {
    query: { refreshToken },
    auth: false,
  })

  return fromAuthenticationTokenDto(dto)
}

