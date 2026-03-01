import type { AuthenticationTokenDto, LoginRequestDto } from '@/features/auth/api/dto'
import { fromAuthenticationTokenDto } from '@/features/auth/model/mappers'
import type { AuthSession } from '@/features/auth/model/authSession'
import { http } from '@/shared/api/http'

export const login = async (payload: LoginRequestDto): Promise<AuthSession> => {
  const dto = await http.post<AuthenticationTokenDto>('/auth/login', {
    body: payload,
    auth: false,
  })

  return fromAuthenticationTokenDto(dto)
}

