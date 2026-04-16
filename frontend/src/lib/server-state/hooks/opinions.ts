import type { QueryKey, UseMutationOptions, UseQueryOptions } from "@tanstack/react-query";
import { opinionsApi } from "@/lib/api";
import type {
  AdjustOpinionComponentWeightRequestDto,
  CreateOpinionRequestDto,
  DeleteOpinionRequestDto,
  GetOpinionByIdRequestDto,
  GetOpinionForSubjectRequestDto,
  LinkOpinionComponentRequestDto,
  OpinionDto,
  UnlinkOpinionComponentRequestDto,
  UpdateOpinionRequestDto,
} from "@/lib/api/opinions";
import { opinionListsKeys, opinionsKeys } from "@/lib/server-state/query-keys";
import type { ApiErrorMeta } from "@/lib/server-state/query-client";
import { useApiInvalidation, useAuthorizedMutation, useAuthorizedQuery } from "@/lib/server-state/hooks/authorized";

export function useOpinion(
  request: GetOpinionByIdRequestDto,
  options?: Omit<
    UseQueryOptions<OpinionDto, Error, OpinionDto, QueryKey>,
    "queryKey" | "queryFn"
  >,
) {
  return useAuthorizedQuery({
    queryKey: opinionsKeys.detail(request),
    queryFn: () => opinionsApi.getById(request),
    ...options,
  });
}

export function useOpinionForSubject(
  request: GetOpinionForSubjectRequestDto,
  options?: Omit<
    UseQueryOptions<OpinionDto, Error, OpinionDto, QueryKey>,
    "queryKey" | "queryFn"
  >,
) {
  return useAuthorizedQuery({
    queryKey: opinionsKeys.forSubject(request),
    queryFn: () => opinionsApi.getForSubject(request),
    ...options,
  });
}

export function useCreateOpinionMutation(
  options?: UseMutationOptions<
    OpinionDto,
    Error,
    CreateOpinionRequestDto,
    unknown
  >,
) {
  const invalidate = useApiInvalidation();

  return useAuthorizedMutation({
    mutationFn: (variables) => opinionsApi.create(variables),
    ...options,
    meta: {
      errorTitle: "Opinion creation failed",
      ...options?.meta,
    } as ApiErrorMeta,
    onSuccess: (data, variables, context) => {
      invalidate(opinionsKeys.all);
      invalidate(opinionListsKeys.all);
      options?.onSuccess?.(data, variables, context);
    },
  });
}

export function useUpdateOpinionMutation(
  options?: UseMutationOptions<
    OpinionDto,
    Error,
    UpdateOpinionRequestDto,
    unknown
  >,
) {
  const invalidate = useApiInvalidation();

  return useAuthorizedMutation({
    mutationFn: (variables) => opinionsApi.update(variables),
    ...options,
    meta: {
      errorTitle: "Opinion update failed",
      ...options?.meta,
    } as ApiErrorMeta,
    onSuccess: (data, variables, context) => {
      invalidate(opinionsKeys.all);
      invalidate(opinionListsKeys.all);
      options?.onSuccess?.(data, variables, context);
    },
  });
}

export function useDeleteOpinionMutation(
  options?: UseMutationOptions<
    void,
    Error,
    DeleteOpinionRequestDto,
    unknown
  >,
) {
  const invalidate = useApiInvalidation();

  return useAuthorizedMutation({
    mutationFn: (variables) => opinionsApi.delete(variables),
    ...options,
    meta: {
      errorTitle: "Opinion deletion failed",
      ...options?.meta,
    } as ApiErrorMeta,
    onSuccess: (data, variables, context) => {
      invalidate(opinionsKeys.all);
      invalidate(opinionListsKeys.all);
      options?.onSuccess?.(data, variables, context);
    },
  });
}

export function useLinkOpinionComponentMutation(
  options?: UseMutationOptions<
    OpinionDto,
    Error,
    LinkOpinionComponentRequestDto,
    unknown
  >,
) {
  const invalidate = useApiInvalidation();

  return useAuthorizedMutation({
    mutationFn: (variables) => opinionsApi.linkComponent(variables),
    ...options,
    meta: {
      errorTitle: "Opinion component link failed",
      ...options?.meta,
    } as ApiErrorMeta,
    onSuccess: (data, variables, context) => {
      invalidate(opinionsKeys.all);
      invalidate(opinionListsKeys.all);
      options?.onSuccess?.(data, variables, context);
    },
  });
}

export function useUnlinkOpinionComponentMutation(
  options?: UseMutationOptions<
    OpinionDto,
    Error,
    UnlinkOpinionComponentRequestDto,
    unknown
  >,
) {
  const invalidate = useApiInvalidation();

  return useAuthorizedMutation({
    mutationFn: (variables) => opinionsApi.unlinkComponent(variables),
    ...options,
    meta: {
      errorTitle: "Opinion component unlink failed",
      ...options?.meta,
    } as ApiErrorMeta,
    onSuccess: (data, variables, context) => {
      invalidate(opinionsKeys.all);
      invalidate(opinionListsKeys.all);
      options?.onSuccess?.(data, variables, context);
    },
  });
}

export function useAdjustOpinionComponentWeightMutation(
  options?: UseMutationOptions<
    OpinionDto,
    Error,
    AdjustOpinionComponentWeightRequestDto,
    unknown
  >,
) {
  const invalidate = useApiInvalidation();

  return useAuthorizedMutation({
    mutationFn: (variables) => opinionsApi.adjustComponentWeight(variables),
    ...options,
    meta: {
      errorTitle: "Opinion component update failed",
      ...options?.meta,
    } as ApiErrorMeta,
    onSuccess: (data, variables, context) => {
      invalidate(opinionsKeys.all);
      invalidate(opinionListsKeys.all);
      options?.onSuccess?.(data, variables, context);
    },
  });
}
