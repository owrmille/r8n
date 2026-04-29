import { fireEvent, render, screen } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import Messages from "@/pages/Messages";

const mocks = vi.hoisted(() => ({
  addSupportThreadMessage: vi.fn(),
  createSupportThread: vi.fn(),
}));

vi.mock("@/lib/server-state", () => ({
  useAddSupportThreadMessageMutation: (options?: { onSuccess?: () => void }) => ({
    isPending: false,
    mutate: mocks.addSupportThreadMessage.mockImplementation(() => options?.onSuccess?.()),
  }),
  useCreateSupportThreadMutation: (options?: { onSuccess?: (thread: { id: string }) => void }) => ({
    isPending: false,
    mutate: mocks.createSupportThread.mockImplementation(() =>
      options?.onSuccess?.({ id: "support-thread-new" }),
    ),
  }),
  useMe: () => ({
    data: { id: "current-user", name: "You", roles: ["USER"] },
  }),
  useSupportThreadMessages: () => ({
    data: {
      items: [
        {
          authorRole: "USER",
          authorUserId: "current-user",
          createdAt: "2026-04-29T09:30:00Z",
          id: "support-message-1",
          text: "I requested an export of my account data this morning. Can you confirm when it will be ready?",
          threadId: "support-thread-1",
        },
        {
          authorRole: "SUPPORT",
          authorUserId: "support-user",
          createdAt: "2026-04-29T09:35:00Z",
          id: "support-message-2",
          text: "Your export is being prepared. We will notify you here when the archive is ready to download.",
          threadId: "support-thread-1",
        },
      ],
      page: 0,
      size: 100,
      total: 2,
    },
    isError: false,
    isLoading: false,
  }),
  useSupportThreadSummaries: () => ({
    data: {
      items: [
        {
          createdAt: "2026-04-29T09:30:00Z",
          id: "support-thread-1",
          lastMessageAt: "2026-04-29T09:35:00Z",
          lastMessageText: "Your export is being prepared. We will notify you here when the archive is ready to download.",
          ownerUserId: "current-user",
          viewerRole: "REQUESTER",
        },
      ],
      page: 0,
      size: 50,
      total: 1,
    },
    isError: false,
    isLoading: false,
  }),
}));

describe("Messages page", () => {
  beforeEach(() => {
    mocks.addSupportThreadMessage.mockClear();
    mocks.createSupportThread.mockClear();
  });

  it("shows the latest message in the thread list and opens the selected chat", () => {
    render(<Messages />);

    expect(
      screen.getByText("Your export is being prepared. We will notify you here when the archive is ready to download."),
    ).toBeInTheDocument();
    expect(
      screen.queryByText("I requested an export of my account data this morning. Can you confirm when it will be ready?"),
    ).not.toBeInTheDocument();

    fireEvent.click(screen.getByRole("button", { name: "Select thread with R8N Support" }));

    expect(
      screen.getByText("I requested an export of my account data this morning. Can you confirm when it will be ready?"),
    ).toBeInTheDocument();
  });

  it("does not show incoming and outgoing direction labels", () => {
    render(<Messages />);

    expect(screen.queryByText("To you")).not.toBeInTheDocument();
    expect(screen.queryByText("From you")).not.toBeInTheDocument();
  });

  it("shows last seen information when hovering a thread participant avatar", async () => {
    render(<Messages />);

    fireEvent.click(screen.getByRole("button", { name: "Select thread with Marta Keller" }));

    const trigger = screen.getByRole("button", { name: "Marta Keller presence" });
    fireEvent.pointerEnter(trigger, { pointerType: "mouse" });
    fireEvent.mouseEnter(trigger);

    expect(await screen.findByText("Last seen 18 minutes ago")).toBeInTheDocument();
  });

  it("does not show inbox outbox and support filter buttons", () => {
    render(<Messages />);

    expect(screen.queryByRole("button", { name: "Inbox" })).not.toBeInTheDocument();
    expect(screen.queryByRole("button", { name: "Outbox" })).not.toBeInTheDocument();
    expect(screen.queryByRole("button", { name: "Support" })).not.toBeInTheDocument();
    expect(screen.getByText("Marta Keller")).toBeInTheDocument();
    expect(screen.getByText("Elena Rossi")).toBeInTheDocument();
  });

  it("sends a new message in an expanded thread", () => {
    render(<Messages />);

    fireEvent.click(screen.getByRole("button", { name: "Select thread with R8N Support" }));
    fireEvent.change(
      screen.getByPlaceholderText("Message R8N Support..."),
      { target: { value: "Thanks, please send it here once it is ready." } },
    );
    fireEvent.click(screen.getByRole("button", { name: "Send" }));

    expect(mocks.addSupportThreadMessage).toHaveBeenCalledWith({
      request: { text: "Thanks, please send it here once it is ready." },
      threadId: "support-thread-1",
    });
    expect(screen.getByPlaceholderText("Message R8N Support...")).toHaveValue("");
  });

  it("creates a support thread through the backend mutation", () => {
    render(<Messages />);

    fireEvent.click(screen.getByRole("button", { name: "New message" }));
    fireEvent.change(screen.getByLabelText("Recipient"), {
      target: { value: "R8N Support" },
    });
    fireEvent.change(screen.getByLabelText("Message"), {
      target: { value: "Need help with a review." },
    });
    fireEvent.click(screen.getByRole("button", { name: "Start thread" }));

    expect(mocks.createSupportThread).toHaveBeenCalledWith({
      initialMessage: "Need help with a review.",
    });
  });

  it("creates a new thread from the new message dialog", () => {
    render(<Messages />);

    fireEvent.click(screen.getByRole("button", { name: "New message" }));
    fireEvent.change(screen.getByLabelText("Recipient"), {
      target: { value: "Lina Hartmann" },
    });
    fireEvent.change(screen.getByLabelText("Message"), {
      target: { value: "Hi, I wanted to ask about your supplier shortlist." },
    });
    fireEvent.click(screen.getByRole("button", { name: "Start thread" }));

    expect(screen.getAllByText("Lina Hartmann")).toHaveLength(2);
    expect(
      screen.getAllByText("Hi, I wanted to ask about your supplier shortlist."),
    ).toHaveLength(2);
    expect(screen.getByPlaceholderText("Message Lina Hartmann...")).toBeInTheDocument();
  });
});
