import { useMutation } from "@tanstack/react-query";
import type { UseMutationOptions } from "@tanstack/react-query";
import { authApi } from "@/lib/api";
import type {
  AuthenticationTokenDto,
  LoginRequestDto,
} from "@/lib/api/auth";
import { setAuthSession } from "@/lib/server-state/auth-store";
import type { ApiErrorMeta } from "@/lib/server-state/query-client";

export function useLoginMutation(
  options?: UseMutationOptions<
    AuthenticationTokenDto,
    Error,
    LoginRequestDto,
    unknown
  >,
) {
  return useMutation({
    mutationFn: authApi.login,
    ...options,
    meta: {
      errorTitle: "Sign in failed",
      ...options?.meta,
    } as ApiErrorMeta,
    onSuccess: (data, variables, context) => {
      setAuthSession(data);
      options?.onSuccess?.(data, variables, context);
    },
  });
}
