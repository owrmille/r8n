import type { HttpClient } from "@/lib/http-client";
import { httpClient } from "@/lib/http-client";
import type { Uuid } from "@/lib/api/shared";

export type ExportStatusDto = "PENDING" | "IN_PROGRESS" | "COMPLETED" | "FAILED";

export interface ExportStateDto {
  userId: Uuid;
  status: ExportStatusDto;
  createdAt: string;
  completedAt: string | null;
  estimatedCompletionTime: string | null;
}

export function createMigrationApi(client: HttpClient = httpClient) {
  return {
    startExport(): Promise<void> {
      return client.post<void, undefined>("/export/start", { auth: "required" });
    },

    getExportStatus(): Promise<ExportStateDto> {
      return client.get<ExportStateDto>("/export/status", { auth: "required" });
    },

    downloadExport(): Promise<Blob> {
      return client.get<Blob>("/export/download", {
        auth: "required",
        responseType: "blob",
      });
    },

    importData(file: File): Promise<void> {
      const formData = new FormData();
      formData.append("file", file);

      return client.post<void, FormData>("/import", {
        auth: "required",
        body: formData,
      });
    },
  };
}

export const migrationApi = createMigrationApi();
