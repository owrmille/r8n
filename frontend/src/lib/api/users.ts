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

export interface UploadMyAvatarRequestDto {
  file: File;
}

export function createUsersApi(client: HttpClient = httpClient) {
  return {
    getMe(): Promise<UsernameDto> {
      return client.get<UsernameDto>("/users/me", { auth: "required" });
    },

    getUser(id: Uuid): Promise<UserProfileDto> {
      return client.get<UserProfileDto>(`/users/${id}`, { auth: "required" });
    },

    getUserAvatar(id: Uuid): Promise<Blob | undefined> {
      return client.get<Blob | undefined>(`/users/${id}/avatar`, {
        auth: "required",
        headers: { Accept: "image/*" },
        responseType: "blob",
      });
    },

    uploadMyAvatar(request: UploadMyAvatarRequestDto): Promise<void> {
      const formData = new FormData();
      formData.append("file", request.file);

      return client.post<void, FormData>("/users/me/avatar", {
        auth: "required",
        body: formData,
      });
    },

    deleteMyAvatar(): Promise<void> {
      return client.delete<void>("/users/me/avatar", { auth: "required" });
    },
  };
}

export const usersApi = createUsersApi();
