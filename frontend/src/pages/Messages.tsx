import { useMemo, useState } from "react";
import type { KeyboardEvent, ReactNode } from "react";
import { motion } from "framer-motion";
import {
  Headphones,
  MessageCircle,
  Plus,
  SendHorizontal,
  UserRound,
} from "lucide-react";
import UserAvatar from "@/components/UserAvatar";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import type { SupportMessageDto, SupportThreadSummaryDto } from "@/lib/api/messaging";
import { MOCK_MESSAGE_THREADS, type MessageDirection, type MessageThread, type ThreadMessage } from "@/lib/messages";
import {
  useAddSupportThreadMessageMutation,
  useCreateSupportThreadMutation,
  useMe,
  useSupportThreadMessages,
  useSupportThreadSummaries,
} from "@/lib/server-state";
import { cn } from "@/lib/utils";

const SUPPORT_THREADS_PAGE = { page: 0, size: 50 } as const;
const SUPPORT_MESSAGES_PAGE = { page: 0, size: 100 } as const;

function getBubbleClasses(direction: MessageDirection) {
  return direction === "outgoing"
    ? "rounded-br-md bg-primary text-primary-foreground shadow-sm"
    : "rounded-bl-md border border-border bg-muted/60 text-foreground";
}

function formatMessageDate(value: string | null): string {
  if (!value) {
    return "Just now";
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return new Intl.DateTimeFormat(undefined, {
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
    month: "short",
  }).format(date);
}

function mapSupportSummaryToThread(summary: SupportThreadSummaryDto): MessageThread {
  const updatedAt = formatMessageDate(summary.lastMessageAt ?? summary.createdAt);

  return {
    id: summary.id,
    subject: "Support conversation",
    participantName: "R8N Support",
    participantLastSeenAt: null,
    participantRole: "Support",
    context: "Support conversation",
    supportViewerRole: summary.viewerRole,
    updatedAt,
    unreadCount: 0,
    messages: [
      {
        id: `support-preview-${summary.id}`,
        direction: "incoming",
        authorName: "R8N Support",
        body: summary.lastMessageText ?? "No messages yet.",
        sentAt: updatedAt,
      },
    ],
  };
}

function mapSupportMessageToThreadMessage(
  message: SupportMessageDto,
  threadViewerRole: SupportThreadSummaryDto["viewerRole"],
  currentUserId: string | undefined,
): ThreadMessage {
  const isCurrentUser =
    threadViewerRole === "REQUESTER"
      ? message.authorUserId === currentUserId
      : message.authorRole === "SUPPORT";

  return {
    id: message.id,
    direction: isCurrentUser ? "outgoing" : "incoming",
    authorName: isCurrentUser
      ? "You"
      : message.authorRole === "SUPPORT"
        ? "R8N Support"
        : "User",
    body: message.text,
    sentAt: formatMessageDate(message.createdAt),
  };
}

const Messages = () => {
  const [directThreads, setDirectThreads] = useState<MessageThread[]>(
    () => MOCK_MESSAGE_THREADS.filter((thread) => thread.participantRole !== "Support"),
  );
  const [activeThreadId, setActiveThreadId] = useState<string | null>(null);
  const [drafts, setDrafts] = useState<Record<string, string>>({});
  const [isNewMessageDialogOpen, setIsNewMessageDialogOpen] = useState(false);
  const [newRecipient, setNewRecipient] = useState("");
  const [newMessage, setNewMessage] = useState("");
  const me = useMe();
  const supportThreadsQuery = useSupportThreadSummaries({
    pageable: SUPPORT_THREADS_PAGE,
  });
  const createSupportThreadMutation = useCreateSupportThreadMutation({
    onSuccess: (thread) => {
      setActiveThreadId(thread.id);
      setNewRecipient("");
      setNewMessage("");
      setIsNewMessageDialogOpen(false);
    },
  });

  const supportThreads = useMemo(
    () => supportThreadsQuery.data?.items.map(mapSupportSummaryToThread) ?? [],
    [supportThreadsQuery.data?.items],
  );

  const threads = useMemo(
    () => [...supportThreads, ...directThreads],
    [directThreads, supportThreads],
  );

  const activeThread = threads.find((thread) => thread.id === activeThreadId) ?? null;

  const updateDraft = (threadId: string, value: string) => {
    setDrafts((current) => ({
      ...current,
      [threadId]: value,
    }));
  };

  const sendMessage = (threadId: string) => {
    const draft = drafts[threadId]?.trim();

    if (!draft) {
      return;
    }

    setDirectThreads((current) => {
      const thread = current.find((item) => item.id === threadId);
      if (!thread) {
        return current;
      }

      const nextMessage: ThreadMessage = {
        id: `msg-${threadId}-${Date.now()}`,
        direction: "outgoing",
        authorName: "You",
        body: draft,
        sentAt: "Just now",
      };

      const updatedThread: MessageThread = {
        ...thread,
        updatedAt: "Just now",
        unreadCount: 0,
        messages: [...thread.messages, nextMessage],
      };

      return [
        updatedThread,
        ...current.filter((item) => item.id !== threadId),
      ];
    });

    setDrafts((current) => ({
      ...current,
      [threadId]: "",
    }));
  };

  const createThread = () => {
    const recipientName = newRecipient.trim();
    const messageBody = newMessage.trim();

    if (!recipientName || !messageBody) {
      return;
    }

    const normalizedRecipient = recipientName.toLowerCase();
    const isSupportThread = normalizedRecipient.includes("support");

    if (isSupportThread) {
      createSupportThreadMutation.mutate({ initialMessage: messageBody });
      return;
    }

    const threadId = `thread-new-${Date.now()}`;
    const createdThread: MessageThread = {
      id: threadId,
      subject: `Conversation with ${recipientName}`,
      participantName: recipientName,
      participantLastSeenAt: null,
      participantRole: "User",
      context: "Direct message",
      updatedAt: "Just now",
      unreadCount: 0,
      messages: [
        {
          id: `msg-${threadId}-1`,
          direction: "outgoing",
          authorName: "You",
          body: messageBody,
          sentAt: "Just now",
        },
      ],
    };

    setDirectThreads((current) => [createdThread, ...current]);
    setActiveThreadId(threadId);
    setDrafts((current) => ({
      ...current,
      [threadId]: "",
    }));
    setNewRecipient("");
    setNewMessage("");
    setIsNewMessageDialogOpen(false);
  };

  return (
    <div className="flex h-[calc(100vh-3.5rem)] min-h-0 flex-col overflow-hidden bg-background md:h-[calc(100vh-3rem)]">
      <motion.header
        initial={{ opacity: 0, y: 8 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.3 }}
        className="flex h-16 shrink-0 items-center justify-between border-b border-border bg-background px-4 md:px-6"
      >
        <h1 className="text-2xl font-semibold tracking-normal text-foreground">
          Messages
        </h1>
        <Button
          type="button"
          className="rounded-lg"
          onClick={() => setIsNewMessageDialogOpen(true)}
        >
          <Plus className="h-4 w-4" />
          New message
        </Button>
      </motion.header>

      <motion.div
        initial={{ opacity: 0, y: 8 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.3, delay: 0.05 }}
        className="grid min-h-0 flex-1 grid-cols-1 grid-rows-[220px_minmax(0,1fr)] overflow-hidden md:grid-cols-[280px_minmax(0,1fr)] md:grid-rows-none"
      >
        <aside className="flex min-h-0 flex-col border-b border-border bg-card md:border-b-0 md:border-r">
          <div className="min-h-0 flex-1 overflow-y-auto py-3">
            {supportThreadsQuery.isLoading && (
              <StatusRow>Loading support conversations...</StatusRow>
            )}
            {supportThreadsQuery.isError && (
              <StatusRow variant="error">Support conversations could not be loaded.</StatusRow>
            )}
            {!supportThreadsQuery.isLoading && !supportThreadsQuery.isError && threads.length === 0 && (
              <StatusRow>No conversations yet.</StatusRow>
            )}
            {threads.map((thread) => (
              <ThreadListItem
                key={thread.id}
                isActive={thread.id === activeThreadId}
                onSelect={() => setActiveThreadId(thread.id)}
                thread={thread}
              />
            ))}
          </div>
        </aside>

        <section className="min-h-0 overflow-hidden bg-background">
          {activeThread ? (
            activeThread.participantRole === "Support" ? (
              <SupportChatPanel
                currentUserId={me.data?.id}
                draft={drafts[activeThread.id] ?? ""}
                onDraftChange={(value) => updateDraft(activeThread.id, value)}
                onDraftSent={() => updateDraft(activeThread.id, "")}
                thread={activeThread}
              />
            ) : (
              <ChatPanel
                draft={drafts[activeThread.id] ?? ""}
                messages={activeThread.messages}
                onDraftChange={(value) => updateDraft(activeThread.id, value)}
                onSend={() => sendMessage(activeThread.id)}
                thread={activeThread}
              />
            )
          ) : (
            <EmptyChatState />
          )}
        </section>
      </motion.div>

      <Dialog open={isNewMessageDialogOpen} onOpenChange={setIsNewMessageDialogOpen}>
        <DialogContent className="rounded-2xl">
          <DialogHeader>
            <DialogTitle>New message</DialogTitle>
            <DialogDescription>
              Start a private conversation with another user or R8N support.
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div className="space-y-2">
              <label htmlFor="new-message-recipient" className="text-sm font-medium text-foreground">
                Recipient
              </label>
              <Input
                id="new-message-recipient"
                value={newRecipient}
                onChange={(event) => setNewRecipient(event.target.value)}
                placeholder="Enter a name or R8N Support"
              />
            </div>
            <div className="space-y-2">
              <label htmlFor="new-message-body" className="text-sm font-medium text-foreground">
                Message
              </label>
              <Textarea
                id="new-message-body"
                value={newMessage}
                onChange={(event) => setNewMessage(event.target.value)}
                placeholder="Write the first message..."
                className="min-h-[120px] resize-none"
              />
            </div>
          </div>
          <DialogFooter>
            <Button
              type="button"
              variant="outline"
              className="rounded-lg"
              onClick={() => setIsNewMessageDialogOpen(false)}
            >
              Cancel
            </Button>
            <Button
              type="button"
              className="rounded-lg"
              disabled={
                !newRecipient.trim() ||
                !newMessage.trim() ||
                createSupportThreadMutation.isPending
              }
              onClick={createThread}
            >
              <SendHorizontal className="h-4 w-4" />
              Start thread
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
};

interface StatusRowProps {
  children: ReactNode;
  variant?: "default" | "error";
}

const StatusRow = ({ children, variant = "default" }: StatusRowProps) => (
  <div
    className={cn(
      "mx-3 rounded-lg border px-3 py-3 text-sm",
      variant === "error"
        ? "border-destructive/30 bg-destructive/5 text-destructive"
        : "border-border bg-background text-muted-foreground",
    )}
  >
    {children}
  </div>
);

interface ThreadListItemProps {
  isActive: boolean;
  onSelect: () => void;
  thread: MessageThread;
}

const ThreadListItem = ({ isActive, onSelect, thread }: ThreadListItemProps) => {
  const lastMessage = thread.messages[thread.messages.length - 1];
  const isSupportThread = thread.participantRole === "Support";
  const Icon = isSupportThread ? Headphones : UserRound;

  return (
    <div
      role="button"
      tabIndex={0}
      aria-label={`Select thread with ${thread.participantName}`}
      onClick={onSelect}
      onKeyDown={(event) => {
        if (event.key === "Enter" || event.key === " ") {
          event.preventDefault();
          onSelect();
        }
      }}
      className={cn(
        "relative mx-2 flex cursor-pointer gap-3 rounded-lg px-3 py-3 outline-none transition-colors focus-visible:ring-2 focus-visible:ring-ring",
        isActive
          ? "bg-primary/10 text-foreground before:absolute before:inset-y-2 before:left-0 before:w-1 before:rounded-full before:bg-primary"
          : "hover:bg-muted/60",
      )}
    >
      <div className="mt-0.5 flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-muted text-muted-foreground">
        <Icon className="h-4 w-4" />
      </div>
      <div className="min-w-0 flex-1">
        <div className="mb-1 flex min-w-0 items-center gap-2">
          <p className="truncate text-sm font-semibold text-foreground">
            {thread.participantName}
          </p>
          {thread.unreadCount > 0 && (
            <span className="inline-flex h-4 min-w-4 items-center justify-center rounded-full bg-accent px-1 text-[10px] font-semibold text-accent-foreground">
              {thread.unreadCount}
            </span>
          )}
        </div>
        <p className="truncate text-xs text-muted-foreground">
          {lastMessage.body}
        </p>
      </div>
      <span className="shrink-0 pt-0.5 text-[11px] text-muted-foreground/75">
        {thread.updatedAt}
      </span>
    </div>
  );
};

interface ChatPanelProps {
  draft: string;
  messages: ThreadMessage[];
  onDraftChange: (value: string) => void;
  onSend: () => void;
  thread: MessageThread;
}

const ChatPanel = ({
  draft,
  messages,
  onDraftChange,
  onSend,
  thread,
}: ChatPanelProps) => (
  <div className="flex h-full min-h-0 flex-col">
    <ChatHeader thread={thread} />
    <MessageList messages={messages} />
    <MessageComposer
      draft={draft}
      isSending={false}
      onDraftChange={onDraftChange}
      onSend={onSend}
      participantName={thread.participantName}
    />
  </div>
);

interface SupportChatPanelProps {
  currentUserId: string | undefined;
  draft: string;
  onDraftChange: (value: string) => void;
  onDraftSent: () => void;
  thread: MessageThread;
}

const SupportChatPanel = ({
  currentUserId,
  draft,
  onDraftChange,
  onDraftSent,
  thread,
}: SupportChatPanelProps) => {
  const messagesQuery = useSupportThreadMessages({
    pageable: SUPPORT_MESSAGES_PAGE,
    threadId: thread.id,
  });
  const addMessageMutation = useAddSupportThreadMessageMutation({
    onSuccess: () => onDraftSent(),
  });
  const messages =
    messagesQuery.data?.items.map((message) =>
      mapSupportMessageToThreadMessage(
        message,
        thread.supportViewerRole ?? "REQUESTER",
        currentUserId,
      ),
    ) ?? [];

  const sendSupportMessage = () => {
    const text = draft.trim();
    if (!text) {
      return;
    }

    addMessageMutation.mutate({
      request: { text },
      threadId: thread.id,
    });
  };

  return (
    <div className="flex h-full min-h-0 flex-col">
      <ChatHeader thread={thread} />
      {messagesQuery.isLoading && (
        <PanelStatus>Loading messages...</PanelStatus>
      )}
      {messagesQuery.isError && (
        <PanelStatus variant="error">Messages could not be loaded.</PanelStatus>
      )}
      {!messagesQuery.isLoading && !messagesQuery.isError && messages.length === 0 && (
        <PanelStatus>No messages in this support conversation yet.</PanelStatus>
      )}
      {messages.length > 0 && <MessageList messages={messages} />}
      <MessageComposer
        draft={draft}
        isSending={addMessageMutation.isPending}
        onDraftChange={onDraftChange}
        onSend={sendSupportMessage}
        participantName={thread.participantName}
      />
    </div>
  );
};

interface ChatHeaderProps {
  thread: MessageThread;
}

const ChatHeader = ({ thread }: ChatHeaderProps) => {
  const isSupportThread = thread.participantRole === "Support";
  const Icon = isSupportThread ? Headphones : UserRound;

  return (
    <header className="flex h-16 shrink-0 items-center gap-3 border-b border-border bg-card px-4 md:px-6">
      {isSupportThread ? (
        <div className="flex h-10 w-10 items-center justify-center rounded-full bg-muted text-muted-foreground">
          <Icon className="h-5 w-5" />
        </div>
      ) : (
        <UserAvatar
          name={thread.participantName}
          lastSeenAt={thread.participantLastSeenAt}
          size="md"
        />
      )}
      <div className="min-w-0 flex-1">
        <h2 className="truncate text-sm font-semibold text-foreground">
          {thread.participantName}
        </h2>
        <p className="truncate text-xs text-muted-foreground">
          {thread.participantRole} · Last message {thread.updatedAt}
        </p>
      </div>
    </header>
  );
};

interface MessageListProps {
  messages: ThreadMessage[];
}

const MessageList = ({ messages }: MessageListProps) => (
  <div className="min-h-0 flex-1 space-y-4 overflow-y-auto px-4 py-5 md:px-6">
    {messages.map((message) => (
      <article
        key={message.id}
        className={cn(
          "flex",
          message.direction === "outgoing" ? "justify-end" : "justify-start",
        )}
      >
        <div
          className={cn(
            "max-w-[82%] rounded-2xl px-4 py-3",
            getBubbleClasses(message.direction),
          )}
        >
          <div className="mb-1.5 flex flex-wrap items-center gap-2">
            <span className="text-sm font-medium">
              {message.authorName}
            </span>
            <span
              className={cn(
                "text-[10px]",
                message.direction === "outgoing"
                  ? "text-primary-foreground/70"
                  : "text-muted-foreground/70",
              )}
            >
              {message.sentAt}
            </span>
          </div>
          <p className="text-sm leading-6">
            {message.body}
          </p>
        </div>
      </article>
    ))}
  </div>
);

interface PanelStatusProps {
  children: ReactNode;
  variant?: "default" | "error";
}

const PanelStatus = ({ children, variant = "default" }: PanelStatusProps) => (
  <div className="min-h-0 flex-1 p-4 md:p-6">
    <div
      className={cn(
        "rounded-lg border px-4 py-3 text-sm",
        variant === "error"
          ? "border-destructive/30 bg-destructive/5 text-destructive"
          : "border-border bg-card text-muted-foreground",
      )}
    >
      {children}
    </div>
  </div>
);

interface MessageComposerProps {
  draft: string;
  isSending: boolean;
  onDraftChange: (value: string) => void;
  onSend: () => void;
  participantName: string;
}

const MessageComposer = ({
  draft,
  isSending,
  onDraftChange,
  onSend,
  participantName,
}: MessageComposerProps) => {
  const sendOnShortcut = (event: KeyboardEvent<HTMLTextAreaElement>) => {
    if (event.key === "Enter" && (event.metaKey || event.ctrlKey)) {
      event.preventDefault();
      onSend();
    }
  };

  return (
    <div className="shrink-0 border-t border-border bg-background px-4 py-4 md:px-6">
      <div className="rounded-xl border border-border bg-card p-3 shadow-premium">
        <Textarea
          value={draft}
          onChange={(event) => onDraftChange(event.target.value)}
          onKeyDown={sendOnShortcut}
          placeholder={`Message ${participantName}...`}
          className="min-h-[92px] resize-none border-0 px-0 py-0 shadow-none focus-visible:ring-0"
        />
        <div className="mt-3 flex items-center justify-between gap-3">
          <p className="text-xs text-muted-foreground">
            ⌘+Enter to send
          </p>
          <Button
            type="button"
            size="sm"
            className="rounded-lg"
            disabled={!draft.trim() || isSending}
            onClick={onSend}
          >
            <SendHorizontal className="h-4 w-4" />
            Send
          </Button>
        </div>
      </div>
    </div>
  );
};

const EmptyChatState = () => (
  <div className="flex h-full min-h-0 items-center justify-center px-6">
    <div className="text-center">
      <div className="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-full bg-muted text-muted-foreground">
        <MessageCircle className="h-6 w-6" />
      </div>
      <p className="text-sm text-muted-foreground">
        выбери тред или начни новый
      </p>
    </div>
  </div>
);

export default Messages;
