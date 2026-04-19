import type { HttpClient, HttpQueryParams } from "@/lib/http-client";
import { httpClient } from "@/lib/http-client";
import {
  createPageQuery,
  type PageRequestDto,
  type PageResponseDto,
  type Uuid,
} from "@/lib/api/shared";
import type { OpinionSummaryDto } from "@/lib/api/opinions";

export type OpinionListPrivacyEnumDto = "PRIVATE" | "SEARCHABLE";

export interface OpinionListSummaryDto {
  grantedAccessCount: number;
  id: Uuid;
  listName: string;
  opinionsCount: number;
  owner: Uuid;
  ownerName: string;
  privacy: OpinionListPrivacyEnumDto;
}

export interface OpinionListDto {
  id: Uuid;
  listName: string;
  opinionSummaries: OpinionSummaryDto[];
  owner: Uuid;
  ownerName: string;
}

export interface GetOpinionListSummaryRequestDto {
  listId: Uuid;
}

export interface GetOpinionListRequestDto {
  listId: Uuid;
}

export interface RenameOpinionListRequestDto {
  listId: Uuid;
  name: string;
}

export interface SetOpinionListPrivacyRequestDto {
  listId: Uuid;
  privacy: OpinionListPrivacyEnumDto;
}

export interface LinkOpinionToListRequestDto {
  listId: Uuid;
  opinionId: Uuid;
}

export interface UnlinkOpinionFromListRequestDto {
  listId: Uuid;
  opinionId: Uuid;
}

export interface SearchOpinionListsFiltersDto {
  authorId?: Uuid;
  authorNameSubstring?: string;
  nameSubstring?: string;
}

export interface SearchOpinionListsRequestDto {
  filters?: SearchOpinionListsFiltersDto;
  pageable: PageRequestDto;
}

export interface SyncOpinionListsRequestDto {
  addedListId: Uuid;
  existingListId: Uuid;
}

export interface UnsyncOpinionListsRequestDto {
  existingListId: Uuid;
  removedListId: Uuid;
}

export interface GetMyOpinionListsRequestDto {
  pageable: PageRequestDto;
}

export function createOpinionListsApi(client: HttpClient = httpClient) {
  return {
    getMine(
      request: GetMyOpinionListsRequestDto,
    ): Promise<PageResponseDto<OpinionListSummaryDto>> {
      return client.get<PageResponseDto<OpinionListSummaryDto>>(
        "/opinion-lists/mine",
        {
          auth: "required",
          query: createPageQuery(request.pageable),
        },
      );
    },

    getSummary(
      request: GetOpinionListSummaryRequestDto,
    ): Promise<OpinionListSummaryDto> {
      return client.get<OpinionListSummaryDto>(
        `/opinion-lists/${request.listId}/summary`,
        {
          auth: "required",
        },
      );
    },

    getById(request: GetOpinionListRequestDto): Promise<OpinionListDto> {
      return client.get<OpinionListDto>(`/opinion-lists/${request.listId}`, {
        auth: "required",
      });
    },

    linkOpinion(request: LinkOpinionToListRequestDto): Promise<OpinionListDto> {
      return client.post<OpinionListDto>(`/opinion-lists/${request.listId}/link`, {
        auth: "required",
        query: {
          opinionId: request.opinionId,
        },
      });
    },

    rename(request: RenameOpinionListRequestDto): Promise<OpinionListDto> {
      return client.patch<OpinionListDto>(`/opinion-lists/${request.listId}/rename`, {
        auth: "required",
        query: {
          name: request.name,
        },
      });
    },

    search(
      request: SearchOpinionListsRequestDto,
    ): Promise<PageResponseDto<OpinionListSummaryDto>> {
      const query: HttpQueryParams = {
        ...createPageQuery(request.pageable),
        authorId: request.filters?.authorId,
        authorNameSubstring: request.filters?.authorNameSubstring,
        nameSubstring: request.filters?.nameSubstring,
      };

      return client.get<PageResponseDto<OpinionListSummaryDto>>(
        "/opinion-lists/search",
        {
          auth: "required",
          query,
        },
      );
    },

    setPrivacy(
      request: SetOpinionListPrivacyRequestDto,
    ): Promise<OpinionListDto> {
      return client.patch<OpinionListDto>(
        `/opinion-lists/${request.listId}/set-privacy`,
        {
          auth: "required",
          query: {
            privacy: request.privacy,
          },
        },
      );
    },

    sync(request: SyncOpinionListsRequestDto): Promise<OpinionListDto> {
      return client.post<OpinionListDto>(
        `/opinion-lists/${request.existingListId}/sync`,
        {
          auth: "required",
          query: {
            addedListId: request.addedListId,
          },
        },
      );
    },

    unlinkOpinion(
      request: UnlinkOpinionFromListRequestDto,
    ): Promise<OpinionListDto> {
      return client.patch<OpinionListDto>(
        `/opinion-lists/${request.listId}/unlink`,
        {
          auth: "required",
          query: {
            opinionId: request.opinionId,
          },
        },
      );
    },

    unsync(request: UnsyncOpinionListsRequestDto): Promise<OpinionListDto> {
      return client.post<OpinionListDto>(
        `/opinion-lists/${request.existingListId}/unsync`,
        {
          auth: "required",
          query: {
            removedListId: request.removedListId,
          },
        },
      );
    },
  };
}

export const opinionListsApi = createOpinionListsApi();
