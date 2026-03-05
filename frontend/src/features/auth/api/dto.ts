export interface LoginRequestDto {
  login: string
  password: string
}

export interface AuthenticationTokenDto {
  accessToken: string
  refreshToken: string
  expiresInMilliseconds: number
}

