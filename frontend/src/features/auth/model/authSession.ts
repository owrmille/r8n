export interface AuthSession {
  accessToken: string
  refreshToken: string
  expiresInMs: number
}

export interface AuthCredentials {
  login: string
  password: string
}

