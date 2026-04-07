import type { HttpClient } from "@/lib/http-client";
import { httpClient } from "@/lib/http-client";

export interface LoginRequestDto {
  login: string;
  password: string;
}

export interface AuthenticationTokenDto {
  accessToken: string;
  expiresInMilliseconds: number;
  refreshToken: string;
}

export interface RefreshAuthenticationRequestDto {
  refreshToken: string;
}

export function createAuthApi(client: HttpClient = httpClient) {
  return {
    login(request: LoginRequestDto): Promise<AuthenticationTokenDto> {
      return client.post<AuthenticationTokenDto, LoginRequestDto>("/auth/login", {
        body: request,
      });
    },

    logout(): Promise<void> {
      return client.post<void>("/auth/logout");
    },

    refresh(
      request: RefreshAuthenticationRequestDto,
    ): Promise<AuthenticationTokenDto> {
      return client.post<AuthenticationTokenDto>("/auth/refresh", {
        query: {
          refreshToken: request.refreshToken,
        },
      });
    },
  };
}

export const authApi = createAuthApi();
