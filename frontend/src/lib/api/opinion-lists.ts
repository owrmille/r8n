import type { HttpClient, HttpQueryParams } from "@/lib/http-client";
import { httpClient } from "@/lib/http-client";
import {
  createAuthorizationHeaders,
  createPageQuery,
  type AuthorizedRequestDto,
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

export interface GetOpinionListSummaryRequestDto extends AuthorizedRequestDto {
  listId: Uuid;
}

export interface GetOpinionListRequestDto extends AuthorizedRequestDto {
  listId: Uuid;
}

export interface RenameOpinionListRequestDto extends AuthorizedRequestDto {
  listId: Uuid;
  name: string;
}

export interface SetOpinionListPrivacyRequestDto extends AuthorizedRequestDto {
  listId: Uuid;
  privacy: OpinionListPrivacyEnumDto;
}

export interface LinkOpinionToListRequestDto extends AuthorizedRequestDto {
  listId: Uuid;
  opinionId: Uuid;
}

export interface UnlinkOpinionFromListRequestDto extends AuthorizedRequestDto {
  listId: Uuid;
  opinionId: Uuid;
}

export interface SearchOpinionListsFiltersDto {
  authorId?: Uuid;
  authorNameSubstring?: string;
  nameSubstring?: string;
}

export interface SearchOpinionListsRequestDto extends AuthorizedRequestDto {
  filters?: SearchOpinionListsFiltersDto;
  pageable: PageRequestDto;
}

export interface SyncOpinionListsRequestDto extends AuthorizedRequestDto {
  addedList: Uuid;
  existingList: Uuid;
}

export interface UnsyncOpinionListsRequestDto extends AuthorizedRequestDto {
  existingList: Uuid;
  removedList: Uuid;
}

export interface GetMyOpinionListsRequestDto extends AuthorizedRequestDto {
  pageable: PageRequestDto;
}

export function createOpinionListsApi(client: HttpClient = httpClient) {
  return {
    getMine(
      request: GetMyOpinionListsRequestDto,
    ): Promise<PageResponseDto<OpinionListSummaryDto>> {
      return client.get<PageResponseDto<OpinionListSummaryDto>>(
        "/opinionLists/mine",
        {
          headers: createAuthorizationHeaders(request.accessToken),
          query: createPageQuery(request.pageable),
        },
      );
    },

    getSummary(
      request: GetOpinionListSummaryRequestDto,
    ): Promise<OpinionListSummaryDto> {
      return client.get<OpinionListSummaryDto>("/opinionLists/summary", {
        headers: createAuthorizationHeaders(request.accessToken),
        query: {
          listId: request.listId,
        },
      });
    },

    getById(request: GetOpinionListRequestDto): Promise<OpinionListDto> {
      return client.get<OpinionListDto>("/opinionLists/get", {
        headers: createAuthorizationHeaders(request.accessToken),
        query: {
          listId: request.listId,
        },
      });
    },

    linkOpinion(request: LinkOpinionToListRequestDto): Promise<OpinionListDto> {
      return client.post<OpinionListDto>("/opinionLists/linkOpinion", {
        headers: createAuthorizationHeaders(request.accessToken),
        query: {
          listId: request.listId,
          opinionId: request.opinionId,
        },
      });
    },

    rename(request: RenameOpinionListRequestDto): Promise<OpinionListDto> {
      return client.patch<OpinionListDto>("/opinionLists/rename", {
        headers: createAuthorizationHeaders(request.accessToken),
        query: {
          listId: request.listId,
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
        "/opinionLists/search",
        {
          headers: createAuthorizationHeaders(request.accessToken),
          query,
        },
      );
    },

    setPrivacy(
      request: SetOpinionListPrivacyRequestDto,
    ): Promise<OpinionListDto> {
      return client.patch<OpinionListDto>("/opinionLists/setPrivacy", {
        headers: createAuthorizationHeaders(request.accessToken),
        query: {
          listId: request.listId,
          privacy: request.privacy,
        },
      });
    },

    sync(request: SyncOpinionListsRequestDto): Promise<OpinionListDto> {
      return client.post<OpinionListDto>("/opinionLists/sync", {
        headers: createAuthorizationHeaders(request.accessToken),
        query: {
          addedList: request.addedList,
          existingList: request.existingList,
        },
      });
    },

    unlinkOpinion(
      request: UnlinkOpinionFromListRequestDto,
    ): Promise<OpinionListDto> {
      return client.patch<OpinionListDto>("/opinionLists/unlinkOpinion", {
        headers: createAuthorizationHeaders(request.accessToken),
        query: {
          listId: request.listId,
          opinionId: request.opinionId,
        },
      });
    },

    unsync(request: UnsyncOpinionListsRequestDto): Promise<OpinionListDto> {
      return client.post<OpinionListDto>("/opinionLists/unsync", {
        headers: createAuthorizationHeaders(request.accessToken),
        query: {
          existingList: request.existingList,
          removedList: request.removedList,
        },
      });
    },
  };
}

export const opinionListsApi = createOpinionListsApi();
