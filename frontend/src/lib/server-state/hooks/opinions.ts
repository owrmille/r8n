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
  request: Omit<GetOpinionByIdRequestDto, "accessToken">,
  options?: Omit<
    UseQueryOptions<OpinionDto, Error, OpinionDto, QueryKey>,
    "queryKey" | "queryFn"
  >,
) {
  return useAuthorizedQuery({
    queryKey: opinionsKeys.detail(request),
    queryFn: (accessToken) => opinionsApi.getById({ ...request, accessToken }),
    ...options,
  });
}

export function useOpinionForSubject(
  request: Omit<GetOpinionForSubjectRequestDto, "accessToken">,
  options?: Omit<
    UseQueryOptions<OpinionDto, Error, OpinionDto, QueryKey>,
    "queryKey" | "queryFn"
  >,
) {
  return useAuthorizedQuery({
    queryKey: opinionsKeys.forSubject(request),
    queryFn: (accessToken) => opinionsApi.getForSubject({ ...request, accessToken }),
    ...options,
  });
}

export function useCreateOpinionMutation(
  options?: UseMutationOptions<
    OpinionDto,
    Error,
    Omit<CreateOpinionRequestDto, "accessToken">,
    unknown
  >,
) {
  const invalidate = useApiInvalidation();

  return useAuthorizedMutation({
    mutationFn: (variables, accessToken) =>
      opinionsApi.create({ ...variables, accessToken }),
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
    Omit<UpdateOpinionRequestDto, "accessToken">,
    unknown
  >,
) {
  const invalidate = useApiInvalidation();

  return useAuthorizedMutation({
    mutationFn: (variables, accessToken) =>
      opinionsApi.update({ ...variables, accessToken }),
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
    Omit<DeleteOpinionRequestDto, "accessToken">,
    unknown
  >,
) {
  const invalidate = useApiInvalidation();

  return useAuthorizedMutation({
    mutationFn: (variables, accessToken) =>
      opinionsApi.delete({ ...variables, accessToken }),
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
    Omit<LinkOpinionComponentRequestDto, "accessToken">,
    unknown
  >,
) {
  const invalidate = useApiInvalidation();

  return useAuthorizedMutation({
    mutationFn: (variables, accessToken) =>
      opinionsApi.linkComponent({ ...variables, accessToken }),
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
    Omit<UnlinkOpinionComponentRequestDto, "accessToken">,
    unknown
  >,
) {
  const invalidate = useApiInvalidation();

  return useAuthorizedMutation({
    mutationFn: (variables, accessToken) =>
      opinionsApi.unlinkComponent({ ...variables, accessToken }),
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
    Omit<AdjustOpinionComponentWeightRequestDto, "accessToken">,
    unknown
  >,
) {
  const invalidate = useApiInvalidation();

  return useAuthorizedMutation({
    mutationFn: (variables, accessToken) =>
      opinionsApi.adjustComponentWeight({ ...variables, accessToken }),
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
