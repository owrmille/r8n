import type { HttpClient } from "@/lib/http-client";
import { httpClient } from "@/lib/http-client";

export interface LoginRequestDto {
  login: string;
  password: string;
}

export interface AuthenticationTokenDto {
  accessToken: string;
  expiresInMilliseconds: number;
}

export function createAuthApi(client: HttpClient = httpClient) {
  return {
    login(request: LoginRequestDto): Promise<AuthenticationTokenDto> {
      return client.post<AuthenticationTokenDto, LoginRequestDto>("/auth/login", {
        body: request,
        credentials: "include",
      });
    },

    logout(): Promise<void> {
      return client.post<void>("/auth/logout", {
        credentials: "include",
      });
    },

    refresh(): Promise<AuthenticationTokenDto> {
      return client.post<AuthenticationTokenDto>("/auth/refresh", {
        credentials: "include",
      });
    },
  };
}

export const authApi = createAuthApi();
