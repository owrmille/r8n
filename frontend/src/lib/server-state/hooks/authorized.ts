import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import type {
  UseMutationOptions,
  UseMutationResult,
  UseQueryOptions,
  UseQueryResult,
} from "@tanstack/react-query";
import { MissingAccessTokenError } from "@/lib/server-state/errors";
import { useAccessToken } from "@/lib/server-state/auth-store";

export function useAuthorizedQuery<TData, TError = Error, TQueryKey extends readonly unknown[] = readonly unknown[]>(
  options: Omit<UseQueryOptions<TData, TError, TData, TQueryKey>, "queryFn"> & {
    accessToken?: string | null;
    queryFn: (accessToken: string) => Promise<TData>;
  },
): UseQueryResult<TData, TError> {
  const sessionToken = useAccessToken();
  const accessToken = options.accessToken ?? sessionToken;
  const enabled = Boolean(accessToken) && (options.enabled ?? true);
  const { accessToken: _accessToken, ...queryOptions } = options;

  return useQuery({
    ...queryOptions,
    enabled,
    queryFn: () => options.queryFn(accessToken as string),
  });
}

export function useAuthorizedMutation<
  TData,
  TError = Error,
  TVariables = void,
  TContext = unknown,
>(
  options: Omit<
    UseMutationOptions<TData, TError, TVariables, TContext>,
    "mutationFn"
  > & {
    accessToken?: string | null;
    mutationFn: (variables: TVariables, accessToken: string) => Promise<TData>;
  },
): UseMutationResult<TData, TError, TVariables, TContext> {
  const sessionToken = useAccessToken();
  const accessToken = options.accessToken ?? sessionToken;
  const { accessToken: _accessToken, ...mutationOptions } = options;

  return useMutation({
    ...mutationOptions,
    mutationFn: (variables: TVariables) => {
      if (!accessToken) {
        return Promise.reject(new MissingAccessTokenError());
      }
      return options.mutationFn(variables, accessToken);
    },
  });
}

export function useApiInvalidation() {
  const queryClient = useQueryClient();

  return (queryKey: readonly unknown[]) =>
    queryClient.invalidateQueries({ queryKey });
}
