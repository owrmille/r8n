import { useMutation, useQueryClient } from "@tanstack/react-query";
import type { UseMutationOptions } from "@tanstack/react-query";
import { authApi } from "@/lib/api";
import type {
  AuthenticationTokenDto,
  LoginRequestDto,
} from "@/lib/api/auth";
import { setAuthSession, clearAuthSession } from "@/lib/server-state/auth-store";
import { authKeys } from "@/lib/server-state/query-keys";
import type { ApiErrorMeta } from "@/lib/server-state/query-client";

export function useLoginMutation(
  options?: UseMutationOptions<
    AuthenticationTokenDto,
    Error,
    LoginRequestDto,
    unknown
  >,
) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: authApi.login,
    ...options,
    meta: {
      errorTitle: "Sign in failed",
      ...options?.meta,
    } as ApiErrorMeta,
    onSuccess: (data, variables, context) => {
      setAuthSession(data);
      queryClient.invalidateQueries({ queryKey: authKeys.session });
      options?.onSuccess?.(data, variables, context);
    },
  });
}

export function useLogoutMutation(
  options?: UseMutationOptions<void, Error, void, unknown>,
) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: authApi.logout,
    ...options,
    meta: {
      errorTitle: "Sign out failed",
      ...options?.meta,
    } as ApiErrorMeta,
    onSuccess: (data, variables, context) => {
      clearAuthSession();
      queryClient.clear();
      options?.onSuccess?.(data, variables, context);
    },
    onError: (error, variables, context) => {
      clearAuthSession();
      queryClient.clear();
      options?.onError?.(error, variables, context);
    },
  });
}

export function useRefreshTokenMutation(
  options?: UseMutationOptions<
    AuthenticationTokenDto,
    Error,
    void,
    unknown
  >,
) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: () => authApi.refresh(),
    ...options,
    meta: {
      errorTitle: "Session refresh failed",
      showErrorToast: false,
      ...options?.meta,
    } as ApiErrorMeta,
    onSuccess: (data, variables, context) => {
      setAuthSession(data);
      queryClient.invalidateQueries({ queryKey: authKeys.session });
      options?.onSuccess?.(data, variables, context);
    },
    onError: (error, variables, context) => {
      clearAuthSession();
      options?.onError?.(error, variables, context);
    },
  });
}
