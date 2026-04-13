import type { QueryKey, UseMutationOptions, UseQueryOptions } from "@tanstack/react-query";
import { selectorsApi } from "@/lib/api";
import type {
  DisagreeWithSelectorRequestDto,
  GetSelectorsForSubjectRequestDto,
  GetSelectorsForUrlRequestDto,
  SelectorDto,
  SuggestSelectorRequestDto,
  SupportThreadDto,
} from "@/lib/api/selectors";
import type { PageResponseDto } from "@/lib/api/shared";
import { selectorsKeys } from "@/lib/server-state/query-keys";
import type { ApiErrorMeta } from "@/lib/server-state/query-client";
import { useApiInvalidation, useAuthorizedMutation, useAuthorizedQuery } from "@/lib/server-state/hooks/authorized";

export function useSelectorsForUrl(
  request: GetSelectorsForUrlRequestDto,
  options?: Omit<
    UseQueryOptions<
      PageResponseDto<SelectorDto>,
      Error,
      PageResponseDto<SelectorDto>,
      QueryKey
    >,
    "queryKey" | "queryFn"
  >,
) {
  return useAuthorizedQuery({
    queryKey: selectorsKeys.forUrl(request),
    queryFn: () => selectorsApi.getForUrl(request),
    ...options,
  });
}

export function useSelectorsForSubject(
  request: GetSelectorsForSubjectRequestDto,
  options?: Omit<
    UseQueryOptions<
      PageResponseDto<SelectorDto>,
      Error,
      PageResponseDto<SelectorDto>,
      QueryKey
    >,
    "queryKey" | "queryFn"
  >,
) {
  return useAuthorizedQuery({
    queryKey: selectorsKeys.forSubject(request),
    queryFn: () => selectorsApi.getForSubject(request),
    ...options,
  });
}

export function useSuggestSelectorMutation(
  options?: UseMutationOptions<
    SelectorDto,
    Error,
    SuggestSelectorRequestDto,
    unknown
  >,
) {
  const invalidate = useApiInvalidation();

  return useAuthorizedMutation({
    mutationFn: (variables) => selectorsApi.suggest(variables),
    ...options,
    meta: {
      errorTitle: "Selector suggestion failed",
      ...options?.meta,
    } as ApiErrorMeta,
    onSuccess: (data, variables, context) => {
      invalidate(selectorsKeys.all);
      options?.onSuccess?.(data, variables, context);
    },
  });
}

export function useDisagreeWithSelectorMutation(
  options?: UseMutationOptions<
    SupportThreadDto,
    Error,
    DisagreeWithSelectorRequestDto,
    unknown
  >,
) {
  const invalidate = useApiInvalidation();

  return useAuthorizedMutation({
    mutationFn: (variables) => selectorsApi.disagree(variables),
    ...options,
    meta: {
      errorTitle: "Selector feedback failed",
      ...options?.meta,
    } as ApiErrorMeta,
    onSuccess: (data, variables, context) => {
      invalidate(selectorsKeys.all);
      options?.onSuccess?.(data, variables, context);
    },
  });
}
