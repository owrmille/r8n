import type {
  GetIncomingAccessRequestsRequestDto,
  GetOutgoingAccessRequestsRequestDto,
} from "@/lib/api/access-requests";
import type {
  GetMyOpinionListsRequestDto,
  GetOpinionListRequestDto,
  GetOpinionListSummaryRequestDto,
  SearchOpinionListsRequestDto,
} from "@/lib/api/opinion-lists";
import type {
  GetOpinionByIdRequestDto,
  GetOpinionForSubjectRequestDto,
} from "@/lib/api/opinions";
import type {
  GetSelectorsForSubjectRequestDto,
  GetSelectorsForUrlRequestDto,
} from "@/lib/api/selectors";

export const authKeys = {
  session: ["auth", "session"] as const,
};

export const opinionsKeys = {
  all: ["opinions"] as const,
  detail: (request: Omit<GetOpinionByIdRequestDto, "accessToken">) => ["opinions", "detail", request.id] as const,
  forSubject: (request: Omit<GetOpinionForSubjectRequestDto, "accessToken">) => [
    "opinions",
    "for-subject",
    request.subjectId,
  ] as const,
};

export const opinionListsKeys = {
  all: ["opinion-lists"] as const,
  mine: (request: Omit<GetMyOpinionListsRequestDto, "accessToken">) => ["opinion-lists", "mine", request.pageable] as const,
  summary: (request: Omit<GetOpinionListSummaryRequestDto, "accessToken">) => [
    "opinion-lists",
    "summary",
    request.listId,
  ] as const,
  detail: (request: Omit<GetOpinionListRequestDto, "accessToken">) => [
    "opinion-lists",
    "detail",
    request.listId,
  ] as const,
  search: (request: Omit<SearchOpinionListsRequestDto, "accessToken">) => [
    "opinion-lists",
    "search",
    request.filters ?? null,
    request.pageable,
  ] as const,
};

export const accessRequestsKeys = {
  all: ["access-requests"] as const,
  incoming: (request: Omit<GetIncomingAccessRequestsRequestDto, "accessToken">) => [
    "access-requests",
    "incoming",
    request.filters ?? null,
    request.pageable,
  ] as const,
  outgoing: (request: Omit<GetOutgoingAccessRequestsRequestDto, "accessToken">) => [
    "access-requests",
    "outgoing",
    request.filters ?? null,
    request.pageable,
  ] as const,
};

export const selectorsKeys = {
  all: ["selectors"] as const,
  forSubject: (request: Omit<GetSelectorsForSubjectRequestDto, "accessToken">) => [
    "selectors",
    "for-subject",
    request.subjectId,
    request.pageable,
  ] as const,
  forUrl: (request: Omit<GetSelectorsForUrlRequestDto, "accessToken">) => [
    "selectors",
    "for-url",
    request.url,
    request.pageable,
  ] as const,
};

export const queryKeys = {
  auth: authKeys,
  opinions: opinionsKeys,
  opinionLists: opinionListsKeys,
  accessRequests: accessRequestsKeys,
  selectors: selectorsKeys,
};
