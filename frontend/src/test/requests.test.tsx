import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import Requests from "@/pages/Requests";
import type { AccessRequestDto } from "@/lib/api/access-requests";

const REQUESTER_ID = "44444444-4444-4444-4444-444444444444";
const OWNER_ID = "33333333-3333-3333-3333-333333333333";
const LIST_ID = "22222222-2222-2222-2222-222222222222";
const STORAGE_KEY = "r8n.access-requests.processed-intents";

const {
  acceptMutate,
  cancelMutate,
  declineMutate,
  hideMutate,
  createListMutateAsync,
  syncListsMutateAsync,
  outgoingItemsRef,
  approvedIncomingItemsRef,
} = vi.hoisted(() => ({
  acceptMutate: vi.fn(),
  cancelMutate: vi.fn(),
  declineMutate: vi.fn(),
  hideMutate: vi.fn(),
  createListMutateAsync: vi.fn(),
  syncListsMutateAsync: vi.fn(),
  outgoingItemsRef: { current: [] as AccessRequestDto[] },
  approvedIncomingItemsRef: { current: [] as AccessRequestDto[] },
}));

vi.mock("@/components/UserAvatar", () => ({
  default: ({ name }: { name: string }) => <span>{name}</span>,
}));

vi.mock("@/lib/server-state/hooks/access-requests", () => ({
  useIncomingAccessRequests: vi.fn((request) => {
    if (request.filters?.status === "ACCEPTED") {
      return {
        data: { items: approvedIncomingItemsRef.current, total: approvedIncomingItemsRef.current.length },
        error: null,
        isError: false,
        isLoading: false,
        refetch: vi.fn(),
      };
    }
    return {
      data: { items: [], total: 0 },
      error: null,
      isError: false,
      isLoading: false,
      refetch: vi.fn(),
    };
  }),
  useOutgoingAccessRequests: vi.fn(() => ({
    data: { items: outgoingItemsRef.current, total: outgoingItemsRef.current.length },
    error: null,
    isError: false,
    isLoading: false,
    refetch: vi.fn(),
  })),
  useAcceptIncomingAccessRequestMutation: vi.fn(() => ({ isPending: false, mutate: acceptMutate })),
  useDeclineIncomingAccessRequestMutation: vi.fn(() => ({ isPending: false, mutate: declineMutate })),
  useHideIncomingAccessRequestMutation: vi.fn(() => ({ isPending: false, mutate: hideMutate })),
  useCancelOutgoingAccessRequestMutation: vi.fn(() => ({ isPending: false, mutate: cancelMutate })),
}));

vi.mock("@/lib/server-state/hooks/opinion-lists", () => ({
  useCreateOpinionListMutation: vi.fn(() => ({
    isPending: false,
    mutate: vi.fn(),
    mutateAsync: createListMutateAsync,
  })),
  useMyOpinionLists: vi.fn(() => ({
    data: { items: [], total: 0 },
    error: null,
    isError: false,
    isLoading: false,
    refetch: vi.fn(),
  })),
  useSyncOpinionListsMutation: vi.fn(() => ({
    isPending: false,
    mutate: vi.fn(),
    mutateAsync: syncListsMutateAsync,
  })),
}));

vi.mock("@/hooks/use-toast", () => ({
  toast: vi.fn(),
}));

const buildRow = (overrides: Partial<AccessRequestDto> = {}): AccessRequestDto => ({
  id: "11111111-1111-1111-1111-111111111111",
  opinionListId: LIST_ID,
  opinionListName: "Private Cafe List",
  owner: OWNER_ID,
  ownerName: "Owner User",
  requester: REQUESTER_ID,
  requesterName: "Requester User",
  status: "ACCEPTED",
  timestamp: "2026-04-29T08:00:00.000Z",
  ...overrides,
});

beforeEach(() => {
  localStorage.clear();
  outgoingItemsRef.current = [];
  approvedIncomingItemsRef.current = [];
  acceptMutate.mockReset();
  cancelMutate.mockReset();
  declineMutate.mockReset();
  hideMutate.mockReset();
  createListMutateAsync.mockReset();
  syncListsMutateAsync.mockReset();
  createListMutateAsync.mockResolvedValue({ id: "99999999-9999-9999-9999-999999999999" });
  syncListsMutateAsync.mockResolvedValue(undefined);
});

afterEach(() => {
  localStorage.clear();
});

describe("Requests page", () => {
  it("lets an owner revoke an approved incoming access request", () => {
    approvedIncomingItemsRef.current = [
      buildRow({
        id: "55555555-5555-5555-5555-555555555555",
        status: "ACCEPTED",
      }),
    ];

    render(<Requests />);

    fireEvent.click(screen.getByRole("button", { name: /show access granted/i }));
    fireEvent.click(screen.getByRole("button", { name: /revoke/i }));

    expect(declineMutate).toHaveBeenCalledWith({
      requestId: "55555555-5555-5555-5555-555555555555",
    });
  });

  it("auto-runs the COPY intent exactly once on an ACCEPTED outgoing row", async () => {
    const row = buildRow({ id: "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa", intent: "COPY" });
    outgoingItemsRef.current = [row];

    render(<Requests />);

    await waitFor(() => {
      expect(createListMutateAsync).toHaveBeenCalledTimes(1);
    });
    expect(createListMutateAsync).toHaveBeenCalledWith({
      name: "Copy of Private Cafe List",
      privacy: "PRIVATE",
    });
    await waitFor(() => {
      expect(syncListsMutateAsync).toHaveBeenCalledTimes(1);
    });
    expect(syncListsMutateAsync).toHaveBeenCalledWith({
      existingListId: "99999999-9999-9999-9999-999999999999",
      addedListId: LIST_ID,
      weight: 1.0,
    });

    const stored = JSON.parse(localStorage.getItem(STORAGE_KEY) ?? "[]");
    expect(stored).toContain("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
  });

  it("auto-runs the MERGE intent exactly once with the chosen target list", async () => {
    const targetList = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb";
    const row = buildRow({
      id: "cccccccc-cccc-cccc-cccc-cccccccccccc",
      intent: "MERGE",
      targetListId: targetList,
    });
    outgoingItemsRef.current = [row];

    render(<Requests />);

    await waitFor(() => {
      expect(syncListsMutateAsync).toHaveBeenCalledTimes(1);
    });
    expect(syncListsMutateAsync).toHaveBeenCalledWith({
      existingListId: targetList,
      addedListId: LIST_ID,
      weight: 1.0,
    });
    expect(createListMutateAsync).not.toHaveBeenCalled();

    const stored = JSON.parse(localStorage.getItem(STORAGE_KEY) ?? "[]");
    expect(stored).toContain("cccccccc-cccc-cccc-cccc-cccccccccccc");
  });

  it("does not re-run an intent that has already been processed (localStorage dedup)", async () => {
    const rowId = "dddddddd-dddd-dddd-dddd-dddddddddddd";
    localStorage.setItem(STORAGE_KEY, JSON.stringify([rowId]));
    outgoingItemsRef.current = [buildRow({ id: rowId, intent: "COPY" })];

    render(<Requests />);

    await new Promise((resolve) => setTimeout(resolve, 30));
    expect(createListMutateAsync).not.toHaveBeenCalled();
    expect(syncListsMutateAsync).not.toHaveBeenCalled();
  });

  it("ignores ACCEPTED rows with intent NONE", async () => {
    outgoingItemsRef.current = [
      buildRow({ id: "eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee", intent: "NONE" }),
      buildRow({ id: "ffffffff-ffff-ffff-ffff-ffffffffffff" }),
    ];

    render(<Requests />);

    await new Promise((resolve) => setTimeout(resolve, 30));
    expect(createListMutateAsync).not.toHaveBeenCalled();
    expect(syncListsMutateAsync).not.toHaveBeenCalled();
  });

  it("does not run the intent for a row that is not yet ACCEPTED", async () => {
    outgoingItemsRef.current = [
      buildRow({
        id: "12121212-1212-1212-1212-121212121212",
        status: "SENT",
        intent: "COPY",
      }),
    ];

    render(<Requests />);

    await new Promise((resolve) => setTimeout(resolve, 30));
    expect(createListMutateAsync).not.toHaveBeenCalled();
    expect(syncListsMutateAsync).not.toHaveBeenCalled();
  });
});
