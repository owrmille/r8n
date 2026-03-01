import type { AuthenticationTokenDto } from '@/features/auth/api/dto'
import type { AuthSession } from '@/features/auth/model/authSession'

export const fromAuthenticationTokenDto = (dto: AuthenticationTokenDto): AuthSession => {
  return {
    accessToken: dto.accessToken,
    refreshToken: dto.refreshToken,
    expiresInMs: dto.expiresInMilliseconds,
  }
}

export const toAuthenticationTokenDto = (session: AuthSession): AuthenticationTokenDto => {
  return {
    accessToken: session.accessToken,
    refreshToken: session.refreshToken,
    expiresInMilliseconds: session.expiresInMs,
  }
}

