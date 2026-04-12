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

type OpinionByIdKey = Omit<GetOpinionByIdRequestDto, "accessToken">;
type OpinionForSubjectKey = Omit<GetOpinionForSubjectRequestDto, "accessToken">;

type MyOpinionListsKey = Omit<GetMyOpinionListsRequestDto, "accessToken">;
type OpinionListSummaryKey = Omit<GetOpinionListSummaryRequestDto, "accessToken">;
type OpinionListKey = Omit<GetOpinionListRequestDto, "accessToken">;
type SearchOpinionListsKey = Omit<SearchOpinionListsRequestDto, "accessToken">;

type IncomingAccessRequestsKey = Omit<GetIncomingAccessRequestsRequestDto, "accessToken">;
type OutgoingAccessRequestsKey = Omit<GetOutgoingAccessRequestsRequestDto, "accessToken">;

type SelectorsForUrlKey = Omit<GetSelectorsForUrlRequestDto, "accessToken">;
type SelectorsForSubjectKey = Omit<GetSelectorsForSubjectRequestDto, "accessToken">;

export const authKeys = {
  session: ["auth", "session"] as const,
};

export const opinionsKeys = {
  all: ["opinions"] as const,
  detail: (request: OpinionByIdKey) => ["opinions", "detail", request.id] as const,
  forSubject: (request: OpinionForSubjectKey) => [
    "opinions",
    "for-subject",
    request.subjectId,
  ] as const,
};

export const opinionListsKeys = {
  all: ["opinion-lists"] as const,
  mine: (request: MyOpinionListsKey) => ["opinion-lists", "mine", request.pageable] as const,
  summary: (request: OpinionListSummaryKey) => [
    "opinion-lists",
    "summary",
    request.listId,
  ] as const,
  detail: (request: OpinionListKey) => [
    "opinion-lists",
    "detail",
    request.listId,
  ] as const,
  search: (request: SearchOpinionListsKey) => [
    "opinion-lists",
    "search",
    request.filters ?? null,
    request.pageable,
  ] as const,
};

export const accessRequestsKeys = {
  all: ["access-requests"] as const,
  incoming: (request: IncomingAccessRequestsKey) => [
    "access-requests",
    "incoming",
    request.filters ?? null,
    request.pageable,
  ] as const,
  outgoing: (request: OutgoingAccessRequestsKey) => [
    "access-requests",
    "outgoing",
    request.filters ?? null,
    request.pageable,
  ] as const,
};

export const selectorsKeys = {
  all: ["selectors"] as const,
  forSubject: (request: SelectorsForSubjectKey) => [
    "selectors",
    "for-subject",
    request.subjectId,
    request.pageable,
  ] as const,
  forUrl: (request: SelectorsForUrlKey) => [
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
