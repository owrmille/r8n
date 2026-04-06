import type { HttpQueryParams } from "@/lib/http-client";

export type Uuid = string;

export type SortDirectionDto = "ASC" | "DESC";

export interface SortDto {
  direction?: SortDirectionDto;
  property: string;
}

export interface PageRequestDto {
  page: number;
  size: number;
  sort?: readonly SortDto[];
}

export interface PageResponseDto<TItem> {
  items: TItem[];
  page: number;
  size: number;
  total: number;
}

export interface AuthorizedRequestDto {
  accessToken: string;
}

export function createAuthorizationHeaders(accessToken: string): HeadersInit {
  return {
    Authorization: `Bearer ${accessToken}`,
  };
}

export function createPageQuery(pageable: PageRequestDto): HttpQueryParams {
  const query: HttpQueryParams = {
    page: pageable.page,
    size: pageable.size,
  };

  if (pageable.sort && pageable.sort.length > 0) {
    query.sort = pageable.sort.map(
      ({ direction = "ASC", property }) => `${property},${direction}`,
    );
  }

  return query;
}
