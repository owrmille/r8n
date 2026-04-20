import { useAuthorizedQuery } from "@/lib/server-state/hooks/authorized";
import { usersApi } from "@/lib/api/users";
import { usersKeys } from "@/lib/server-state/query-keys";
import type { Uuid } from "@/lib/api/shared";

export function useMe() {
  return useAuthorizedQuery({
    queryKey: usersKeys.me(),
    queryFn: () => usersApi.getMe(),
  });
}

export function useUserProfile(id: Uuid) {
  return useAuthorizedQuery({
    queryKey: usersKeys.detail(id),
    queryFn: () => usersApi.getUser(id),
    enabled: !!id,
  });
}
