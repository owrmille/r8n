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
  request: Omit<GetSelectorsForUrlRequestDto, "accessToken">,
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
    queryFn: (accessToken) => selectorsApi.getForUrl({ ...request, accessToken }),
    ...options,
  });
}

export function useSelectorsForSubject(
  request: Omit<GetSelectorsForSubjectRequestDto, "accessToken">,
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
    queryFn: (accessToken) => selectorsApi.getForSubject({ ...request, accessToken }),
    ...options,
  });
}

export function useSuggestSelectorMutation(
  options?: UseMutationOptions<
    SelectorDto,
    Error,
    Omit<SuggestSelectorRequestDto, "accessToken">,
    unknown
  >,
) {
  const invalidate = useApiInvalidation();

  return useAuthorizedMutation({
    mutationFn: (variables, accessToken) =>
      selectorsApi.suggest({ ...variables, accessToken }),
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
    Omit<DisagreeWithSelectorRequestDto, "accessToken">,
    unknown
  >,
) {
  const invalidate = useApiInvalidation();

  return useAuthorizedMutation({
    mutationFn: (variables, accessToken) =>
      selectorsApi.disagree({ ...variables, accessToken }),
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
