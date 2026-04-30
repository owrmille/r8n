import type { UseMutationOptions, UseQueryOptions, QueryKey } from "@tanstack/react-query";
import { HttpError } from "@/lib/http-client";
import {
  useAuthorizedMutation,
  useAuthorizedQuery,
  useApiInvalidation,
} from "@/lib/server-state/hooks/authorized";
import { migrationApi } from "@/lib/api/migration";
import type { ExportStateDto } from "@/lib/api/migration";
import { migrationKeys } from "@/lib/server-state/query-keys";
import type { ApiErrorMeta } from "@/lib/server-state/query-client";

export function useExportStatus(
  options?: Omit<
    UseQueryOptions<ExportStateDto | null, Error, ExportStateDto | null, QueryKey>,
    "queryKey" | "queryFn"
  >,
) {
  return useAuthorizedQuery({
    queryKey: migrationKeys.exportStatus(),
    queryFn: async () => {
      try {
        return await migrationApi.getExportStatus();
      } catch (error) {
        if (error instanceof HttpError && error.status === 404) {
          return null;
        }
        throw error;
      }
    },
    ...options,
  });
}

export function useStartExportMutation(
  options?: UseMutationOptions<void, Error, void, unknown>,
) {
  const invalidate = useApiInvalidation();

  return useAuthorizedMutation({
    mutationFn: () => migrationApi.startExport(),
    ...options,
    meta: {
      errorTitle: "Export failed",
      ...options?.meta,
    } as ApiErrorMeta,
    onSuccess: (data, variables, context) => {
      invalidate(migrationKeys.all);
      options?.onSuccess?.(data, variables, context);
    },
  });
}

export function useImportDataMutation(
  options?: UseMutationOptions<void, Error, File, unknown>,
) {
  return useAuthorizedMutation({
    mutationFn: (file) => migrationApi.importData(file),
    ...options,
    meta: {
      errorTitle: "Import failed",
      ...options?.meta,
    } as ApiErrorMeta,
  });
}
