import type { HttpClient } from "@/lib/http-client";
import { httpClient } from "@/lib/http-client";
import type { Uuid } from "@/lib/api/shared";

export interface UsernameDto {
  id: Uuid;
  name: string;
}

export interface UserProfileDto {
  id: Uuid;
  name: string;
  status: string;
  lastOnline: string | null;
  about: string | null;
  location: string | null;
}

export function createUsersApi(client: HttpClient = httpClient) {
  return {
    getMe(): Promise<UsernameDto> {
      return client.get<UsernameDto>("/users/me", { auth: "required" });
    },

    getUser(id: Uuid): Promise<UserProfileDto> {
      return client.get<UserProfileDto>(`/users/${id}`, { auth: "required" });
    },
  };
}

export const usersApi = createUsersApi();
