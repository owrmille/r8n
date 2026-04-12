import type { QueryKey, UseMutationOptions, UseQueryOptions } from "@tanstack/react-query";
import { opinionListsApi } from "@/lib/api";
import type {
  GetMyOpinionListsRequestDto,
  GetOpinionListRequestDto,
  GetOpinionListSummaryRequestDto,
  LinkOpinionToListRequestDto,
  OpinionListDto,
  OpinionListSummaryDto,
  RenameOpinionListRequestDto,
  SearchOpinionListsRequestDto,
  SetOpinionListPrivacyRequestDto,
  SyncOpinionListsRequestDto,
  UnlinkOpinionFromListRequestDto,
  UnsyncOpinionListsRequestDto,
} from "@/lib/api/opinion-lists";
import type { PageResponseDto } from "@/lib/api/shared";
import { opinionListsKeys } from "@/lib/server-state/query-keys";
import type { ApiErrorMeta } from "@/lib/server-state/query-client";
import { useApiInvalidation, useAuthorizedMutation, useAuthorizedQuery } from "@/lib/server-state/hooks/authorized";

export function useMyOpinionLists(
  request: Omit<GetMyOpinionListsRequestDto, "accessToken">,
  options?: Omit<
    UseQueryOptions<
      PageResponseDto<OpinionListSummaryDto>,
      Error,
      PageResponseDto<OpinionListSummaryDto>,
      QueryKey
    >,
    "queryKey" | "queryFn"
  >,
) {
  return useAuthorizedQuery({
    queryKey: opinionListsKeys.mine(request),
    queryFn: (accessToken) => opinionListsApi.getMine({ ...request, accessToken }),
    ...options,
  });
}

export function useOpinionListSummary(
  request: Omit<GetOpinionListSummaryRequestDto, "accessToken">,
  options?: Omit<
    UseQueryOptions<OpinionListSummaryDto, Error, OpinionListSummaryDto, QueryKey>,
    "queryKey" | "queryFn"
  >,
) {
  return useAuthorizedQuery({
    queryKey: opinionListsKeys.summary(request),
    queryFn: (accessToken) => opinionListsApi.getSummary({ ...request, accessToken }),
    ...options,
  });
}

export function useOpinionList(
  request: Omit<GetOpinionListRequestDto, "accessToken">,
  options?: Omit<
    UseQueryOptions<OpinionListDto, Error, OpinionListDto, QueryKey>,
    "queryKey" | "queryFn"
  >,
) {
  return useAuthorizedQuery({
    queryKey: opinionListsKeys.detail(request),
    queryFn: (accessToken) => opinionListsApi.getById({ ...request, accessToken }),
    ...options,
  });
}

export function useSearchOpinionLists(
  request: Omit<SearchOpinionListsRequestDto, "accessToken">,
  options?: Omit<
    UseQueryOptions<
      PageResponseDto<OpinionListSummaryDto>,
      Error,
      PageResponseDto<OpinionListSummaryDto>,
      QueryKey
    >,
    "queryKey" | "queryFn"
  >,
) {
  return useAuthorizedQuery({
    queryKey: opinionListsKeys.search(request),
    queryFn: (accessToken) => opinionListsApi.search({ ...request, accessToken }),
    ...options,
  });
}

export function useRenameOpinionListMutation(
  options?: UseMutationOptions<
    OpinionListDto,
    Error,
    Omit<RenameOpinionListRequestDto, "accessToken">,
    unknown
  >,
) {
  const invalidate = useApiInvalidation();

  return useAuthorizedMutation({
    mutationFn: (variables, accessToken) =>
      opinionListsApi.rename({ ...variables, accessToken }),
    ...options,
    meta: {
      errorTitle: "Rename failed",
      ...options?.meta,
    } as ApiErrorMeta,
    onSuccess: (data, variables, context) => {
      invalidate(opinionListsKeys.all);
      options?.onSuccess?.(data, variables, context);
    },
  });
}

export function useSetOpinionListPrivacyMutation(
  options?: UseMutationOptions<
    OpinionListDto,
    Error,
    Omit<SetOpinionListPrivacyRequestDto, "accessToken">,
    unknown
  >,
) {
  const invalidate = useApiInvalidation();

  return useAuthorizedMutation({
    mutationFn: (variables, accessToken) =>
      opinionListsApi.setPrivacy({ ...variables, accessToken }),
    ...options,
    meta: {
      errorTitle: "Privacy update failed",
      ...options?.meta,
    } as ApiErrorMeta,
    onSuccess: (data, variables, context) => {
      invalidate(opinionListsKeys.all);
      options?.onSuccess?.(data, variables, context);
    },
  });
}

export function useLinkOpinionToListMutation(
  options?: UseMutationOptions<
    OpinionListDto,
    Error,
    Omit<LinkOpinionToListRequestDto, "accessToken">,
    unknown
  >,
) {
  const invalidate = useApiInvalidation();

  return useAuthorizedMutation({
    mutationFn: (variables, accessToken) =>
      opinionListsApi.linkOpinion({ ...variables, accessToken }),
    ...options,
    meta: {
      errorTitle: "Link opinion failed",
      ...options?.meta,
    } as ApiErrorMeta,
    onSuccess: (data, variables, context) => {
      invalidate(opinionListsKeys.all);
      options?.onSuccess?.(data, variables, context);
    },
  });
}

export function useUnlinkOpinionFromListMutation(
  options?: UseMutationOptions<
    OpinionListDto,
    Error,
    Omit<UnlinkOpinionFromListRequestDto, "accessToken">,
    unknown
  >,
) {
  const invalidate = useApiInvalidation();

  return useAuthorizedMutation({
    mutationFn: (variables, accessToken) =>
      opinionListsApi.unlinkOpinion({ ...variables, accessToken }),
    ...options,
    meta: {
      errorTitle: "Unlink opinion failed",
      ...options?.meta,
    } as ApiErrorMeta,
    onSuccess: (data, variables, context) => {
      invalidate(opinionListsKeys.all);
      options?.onSuccess?.(data, variables, context);
    },
  });
}

export function useSyncOpinionListsMutation(
  options?: UseMutationOptions<
    OpinionListDto,
    Error,
    Omit<SyncOpinionListsRequestDto, "accessToken">,
    unknown
  >,
) {
  const invalidate = useApiInvalidation();

  return useAuthorizedMutation({
    mutationFn: (variables, accessToken) =>
      opinionListsApi.sync({ ...variables, accessToken }),
    ...options,
    meta: {
      errorTitle: "List sync failed",
      ...options?.meta,
    } as ApiErrorMeta,
    onSuccess: (data, variables, context) => {
      invalidate(opinionListsKeys.all);
      options?.onSuccess?.(data, variables, context);
    },
  });
}

export function useUnsyncOpinionListsMutation(
  options?: UseMutationOptions<
    OpinionListDto,
    Error,
    Omit<UnsyncOpinionListsRequestDto, "accessToken">,
    unknown
  >,
) {
  const invalidate = useApiInvalidation();

  return useAuthorizedMutation({
    mutationFn: (variables, accessToken) =>
      opinionListsApi.unsync({ ...variables, accessToken }),
    ...options,
    meta: {
      errorTitle: "List unsync failed",
      ...options?.meta,
    } as ApiErrorMeta,
    onSuccess: (data, variables, context) => {
      invalidate(opinionListsKeys.all);
      options?.onSuccess?.(data, variables, context);
    },
  });
}
