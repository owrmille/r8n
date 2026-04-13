import type { QueryKey, UseMutationOptions, UseQueryOptions } from "@tanstack/react-query";
import { accessRequestsApi } from "@/lib/api";
import type {
  AccessRequestActionRequestDto,
  AccessRequestDto,
  CreateOutgoingAccessRequestRequestDto,
  GetIncomingAccessRequestsRequestDto,
  GetOutgoingAccessRequestsRequestDto,
} from "@/lib/api/access-requests";
import type { PageResponseDto } from "@/lib/api/shared";
import { accessRequestsKeys } from "@/lib/server-state/query-keys";
import type { ApiErrorMeta } from "@/lib/server-state/query-client";
import { useApiInvalidation, useAuthorizedMutation, useAuthorizedQuery } from "@/lib/server-state/hooks/authorized";

export function useIncomingAccessRequests(
  request: Omit<GetIncomingAccessRequestsRequestDto, "accessToken">,
  options?: Omit<
    UseQueryOptions<
      PageResponseDto<AccessRequestDto>,
      Error,
      PageResponseDto<AccessRequestDto>,
      QueryKey
    >,
    "queryKey" | "queryFn"
  >,
) {
  return useAuthorizedQuery({
    queryKey: accessRequestsKeys.incoming(request),
    queryFn: (accessToken) => accessRequestsApi.getIncoming({ ...request, accessToken }),
    ...options,
  });
}

export function useOutgoingAccessRequests(
  request: Omit<GetOutgoingAccessRequestsRequestDto, "accessToken">,
  options?: Omit<
    UseQueryOptions<
      PageResponseDto<AccessRequestDto>,
      Error,
      PageResponseDto<AccessRequestDto>,
      QueryKey
    >,
    "queryKey" | "queryFn"
  >,
) {
  return useAuthorizedQuery({
    queryKey: accessRequestsKeys.outgoing(request),
    queryFn: (accessToken) => accessRequestsApi.getOutgoing({ ...request, accessToken }),
    ...options,
  });
}

export function useAcceptIncomingAccessRequestMutation(
  options?: UseMutationOptions<
    AccessRequestDto,
    Error,
    Omit<AccessRequestActionRequestDto, "accessToken">,
    unknown
  >,
) {
  const invalidate = useApiInvalidation();

  return useAuthorizedMutation({
    mutationFn: (variables, accessToken) =>
      accessRequestsApi.acceptIncoming({ ...variables, accessToken }),
    ...options,
    meta: {
      errorTitle: "Request approval failed",
      ...options?.meta,
    } as ApiErrorMeta,
    onSuccess: (data, variables, context) => {
      invalidate(accessRequestsKeys.all);
      options?.onSuccess?.(data, variables, context);
    },
  });
}

export function useDeclineIncomingAccessRequestMutation(
  options?: UseMutationOptions<
    AccessRequestDto,
    Error,
    Omit<AccessRequestActionRequestDto, "accessToken">,
    unknown
  >,
) {
  const invalidate = useApiInvalidation();

  return useAuthorizedMutation({
    mutationFn: (variables, accessToken) =>
      accessRequestsApi.declineIncoming({ ...variables, accessToken }),
    ...options,
    meta: {
      errorTitle: "Request decline failed",
      ...options?.meta,
    } as ApiErrorMeta,
    onSuccess: (data, variables, context) => {
      invalidate(accessRequestsKeys.all);
      options?.onSuccess?.(data, variables, context);
    },
  });
}

export function useHideIncomingAccessRequestMutation(
  options?: UseMutationOptions<
    AccessRequestDto,
    Error,
    Omit<AccessRequestActionRequestDto, "accessToken">,
    unknown
  >,
) {
  const invalidate = useApiInvalidation();

  return useAuthorizedMutation({
    mutationFn: (variables, accessToken) =>
      accessRequestsApi.hideIncoming({ ...variables, accessToken }),
    ...options,
    meta: {
      errorTitle: "Request hide failed",
      ...options?.meta,
    } as ApiErrorMeta,
    onSuccess: (data, variables, context) => {
      invalidate(accessRequestsKeys.all);
      options?.onSuccess?.(data, variables, context);
    },
  });
}

export function useCreateOutgoingAccessRequestMutation(
  options?: UseMutationOptions<
    AccessRequestDto,
    Error,
    Omit<CreateOutgoingAccessRequestRequestDto, "accessToken">,
    unknown
  >,
) {
  const invalidate = useApiInvalidation();

  return useAuthorizedMutation({
    mutationFn: (variables, accessToken) =>
      accessRequestsApi.createOutgoing({ ...variables, accessToken }),
    ...options,
    meta: {
      errorTitle: "Request creation failed",
      ...options?.meta,
    } as ApiErrorMeta,
    onSuccess: (data, variables, context) => {
      invalidate(accessRequestsKeys.all);
      options?.onSuccess?.(data, variables, context);
    },
  });
}

export function useCancelOutgoingAccessRequestMutation(
  options?: UseMutationOptions<
    AccessRequestDto,
    Error,
    Omit<AccessRequestActionRequestDto, "accessToken">,
    unknown
  >,
) {
  const invalidate = useApiInvalidation();

  return useAuthorizedMutation({
    mutationFn: (variables, accessToken) =>
      accessRequestsApi.cancelOutgoing({ ...variables, accessToken }),
    ...options,
    meta: {
      errorTitle: "Request cancel failed",
      ...options?.meta,
    } as ApiErrorMeta,
    onSuccess: (data, variables, context) => {
      invalidate(accessRequestsKeys.all);
      options?.onSuccess?.(data, variables, context);
    },
  });
}
