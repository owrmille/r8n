import type { HttpClient } from "@/lib/http-client";
import { httpClient } from "@/lib/http-client";
import {
  createPageQuery,
  type PageRequestDto,
  type PageResponseDto,
  type Uuid,
} from "@/lib/api/shared";

export type SupportParticipantRoleEnumDto = "USER" | "SUPPORT";
export type SupportThreadViewerRoleEnumDto = "REQUESTER" | "SUPPORT";

export interface SupportThreadSummaryDto {
  id: Uuid;
  ownerUserId: Uuid;
  viewerRole: SupportThreadViewerRoleEnumDto;
  createdAt: string;
  lastMessageAt: string | null;
  lastMessageText: string | null;
}

export interface SupportMessageDto {
  id: Uuid;
  threadId: Uuid;
  authorUserId: Uuid;
  authorRole: SupportParticipantRoleEnumDto;
  text: string;
  createdAt: string;
}

export interface CreateSupportThreadRequestDto {
  initialMessage: string;
}

export interface CreateSupportMessageRequestDto {
  text: string;
}

export interface GetSupportThreadSummariesRequestDto {
  pageable: PageRequestDto;
}

export interface GetSupportThreadMessagesRequestDto {
  pageable: PageRequestDto;
  threadId: Uuid;
}

export interface AddSupportThreadMessageRequestDto {
  request: CreateSupportMessageRequestDto;
  threadId: Uuid;
}

export function createMessagingApi(client: HttpClient = httpClient) {
  return {
    getSupportThreadSummaries(
      request: GetSupportThreadSummariesRequestDto,
    ): Promise<PageResponseDto<SupportThreadSummaryDto>> {
      return client.get<PageResponseDto<SupportThreadSummaryDto>>(
        "/messaging/support/threads",
        {
          auth: "required",
          query: createPageQuery(request.pageable),
        },
      );
    },

    createSupportThread(
      request: CreateSupportThreadRequestDto,
    ): Promise<SupportThreadSummaryDto> {
      return client.post<SupportThreadSummaryDto>("/messaging/support/threads", {
        auth: "required",
        body: request,
      });
    },

    getSupportThreadMessages(
      request: GetSupportThreadMessagesRequestDto,
    ): Promise<PageResponseDto<SupportMessageDto>> {
      return client.get<PageResponseDto<SupportMessageDto>>(
        `/messaging/support/threads/${request.threadId}/messages`,
        {
          auth: "required",
          query: createPageQuery(request.pageable),
        },
      );
    },

    deleteSupportThread(threadId: Uuid): Promise<void> {
      return client.delete<void>(`/messaging/support/threads/${threadId}`, {
        auth: "required",
      });
    },

    addSupportThreadMessage(
      request: AddSupportThreadMessageRequestDto,
    ): Promise<SupportMessageDto> {
      return client.post<SupportMessageDto, CreateSupportMessageRequestDto>(
        `/messaging/support/threads/${request.threadId}/messages`,
        {
          auth: "required",
          body: request.request,
        },
      );
    },
  };
}

export const messagingApi = createMessagingApi();
