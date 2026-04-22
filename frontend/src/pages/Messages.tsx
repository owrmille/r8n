import { useMemo, useState } from "react";
import { motion } from "framer-motion";
import {
  ChevronDown,
  Clock,
  Inbox,
  Send,
} from "lucide-react";
import ReviewerAvatar from "@/components/ReviewerAvatar";
import {
  Collapsible,
  CollapsibleContent,
  CollapsibleTrigger,
} from "@/components/ui/collapsible";
import { cn } from "@/lib/utils";

type MessageDirection = "incoming" | "outgoing";
type MessageFilter = "all" | "inbox" | "outbox" | "support";

interface ThreadMessage {
  id: string;
  direction: MessageDirection;
  authorName: string;
  body: string;
  sentAt: string;
}

interface MessageThread {
  id: string;
  subject: string;
  participantName: string;
  participantRole: string;
  context: string;
  updatedAt: string;
  unreadCount: number;
  messages: ThreadMessage[];
}

const MOCK_THREADS: MessageThread[] = [
  {
    id: "thread-user-1",
    subject: "Question about your coffee grinder review",
    participantName: "Marta Keller",
    participantRole: "User",
    context: "Direct message",
    updatedAt: "18m ago",
    unreadCount: 2,
    messages: [
      {
        id: "msg-user-1",
        direction: "incoming",
        authorName: "Marta Keller",
        body: "I saw your note about the grinder noise level. Was that during daily use or only while testing?",
        sentAt: "09:42",
      },
      {
        id: "msg-user-2",
        direction: "outgoing",
        authorName: "You",
        body: "Daily use. It was fine for short grinding, but too loud for the small kitchen during service hours.",
        sentAt: "09:51",
      },
      {
        id: "msg-user-3",
        direction: "incoming",
        authorName: "Marta Keller",
        body: "That helps, thanks. I will compare it with the quieter model before ordering.",
        sentAt: "10:04",
      },
    ],
  },
  {
    id: "thread-support-1",
    subject: "Export archive request",
    participantName: "R8N Support",
    participantRole: "Support",
    context: "Support conversation",
    updatedAt: "1h ago",
    unreadCount: 0,
    messages: [
      {
        id: "msg-support-1",
        direction: "outgoing",
        authorName: "You",
        body: "I requested an export of my account data this morning. Can you confirm when it will be ready?",
        sentAt: "Yesterday",
      },
      {
        id: "msg-support-2",
        direction: "incoming",
        authorName: "R8N Support",
        body: "Your export is being prepared. We will notify you here when the archive is ready to download.",
        sentAt: "Today",
      },
    ],
  },
  {
    id: "thread-user-2",
    subject: "Supplier recommendation follow-up",
    participantName: "Elena Rossi",
    participantRole: "User",
    context: "Direct message",
    updatedAt: "Yesterday",
    unreadCount: 1,
    messages: [
      {
        id: "msg-user-4",
        direction: "incoming",
        authorName: "Elena Rossi",
        body: "Do you still recommend the packaging supplier from your March list?",
        sentAt: "Yesterday",
      },
      {
        id: "msg-user-5",
        direction: "outgoing",
        authorName: "You",
        body: "Yes, but only for small batches. Their lead time became inconsistent for larger orders.",
        sentAt: "Yesterday",
      },
    ],
  },
];

const FILTERS: Array<{ id: MessageFilter; label: string }> = [
  { id: "all", label: "All" },
  { id: "inbox", label: "Inbox" },
  { id: "outbox", label: "Outbox" },
  { id: "support", label: "Support" },
];

function getDirectionMeta(direction: MessageDirection) {
  if (direction === "outgoing") {
    return {
      label: "From you",
      icon: Send,
      className: "border-primary/20 bg-primary/5 text-primary",
    };
  }

  return {
    label: "To you",
    icon: Inbox,
    className: "border-accent/20 bg-accent/5 text-accent",
  };
}

const Messages = () => {
  const firstThreadId = MOCK_THREADS[0]?.id ?? "";
  const [openThreads, setOpenThreads] = useState<string[]>([firstThreadId]);
  const [activeFilter, setActiveFilter] = useState<MessageFilter>("all");

  const filteredThreads = useMemo(
    () =>
      MOCK_THREADS.filter((thread) => {
        const firstDirection = thread.messages[0]?.direction;

        if (activeFilter === "inbox") {
          return firstDirection === "incoming";
        }

        if (activeFilter === "outbox") {
          return firstDirection === "outgoing";
        }

        if (activeFilter === "support") {
          return thread.participantRole === "Support";
        }

        return true;
      }),
    [activeFilter],
  );

  const toggleThread = (threadId: string) => {
    setOpenThreads((current) =>
      current.includes(threadId)
        ? current.filter((id) => id !== threadId)
        : [...current, threadId],
    );
  };

  return (
    <div className="mx-auto max-w-4xl px-4 py-8 md:px-8 md:py-12">
      <motion.div
        initial={{ opacity: 0, y: 12 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.4 }}
        className="mb-10"
      >
        <h1 className="mb-1 text-2xl font-semibold tracking-tight text-foreground md:text-3xl">
          Messages
        </h1>
        <p className="text-sm text-muted-foreground">
          Private conversations with other users and R8N support.
        </p>
      </motion.div>

      <div className="mb-6 flex flex-wrap gap-2" aria-label="Message filters">
        {FILTERS.map((filter) => (
          <button
            key={filter.id}
            type="button"
            onClick={() => setActiveFilter(filter.id)}
            className={cn(
              "rounded-xl border px-4 py-2 text-sm font-medium transition-colors",
              activeFilter === filter.id
                ? "border-primary/20 bg-primary/5 text-foreground"
                : "border-border bg-card text-muted-foreground hover:bg-muted/50 hover:text-foreground",
            )}
          >
            {filter.label}
          </button>
        ))}
      </div>

      <motion.section
        initial={{ opacity: 0, y: 8 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.3, delay: 0.1 }}
        className="space-y-4"
      >
        {filteredThreads.map((thread) => {
          const isOpen = openThreads.includes(thread.id);
          const firstMessage = thread.messages[0];
          const remainingMessages = thread.messages.slice(1);
          const directionMeta = getDirectionMeta(firstMessage.direction);
          const DirectionIcon = directionMeta.icon;

          return (
            <Collapsible
              key={thread.id}
              open={isOpen}
              onOpenChange={() => toggleThread(thread.id)}
              className="overflow-hidden rounded-2xl border border-border bg-card shadow-card"
            >
              <div className="border-b border-border/70 px-5 py-4">
                <div className="flex items-start gap-4">
                  <ReviewerAvatar name={thread.participantName} size="md" />
                  <div className="min-w-0 flex-1">
                    <div className="mb-1 flex flex-wrap items-center gap-2">
                      <h2 className="truncate text-sm font-semibold text-foreground">
                        {thread.subject}
                      </h2>
                      {thread.unreadCount > 0 && (
                        <span className="inline-flex h-5 min-w-5 items-center justify-center rounded-full bg-accent px-1.5 text-[10px] font-mono font-semibold text-accent-foreground">
                          {thread.unreadCount}
                        </span>
                      )}
                    </div>
                    <p className="text-xs text-muted-foreground">
                      {thread.participantName} · {thread.participantRole} · {thread.context}
                    </p>
                  </div>
                  <div className="flex shrink-0 items-center gap-1.5 text-[10px] text-muted-foreground/70">
                    <Clock className="h-3 w-3" />
                    {thread.updatedAt}
                  </div>
                </div>
              </div>

              <CollapsibleTrigger asChild>
                <button
                  type="button"
                  className="flex w-full items-start gap-4 px-5 py-4 text-left transition-colors hover:bg-muted/40 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
                  aria-label={`${isOpen ? "Collapse" : "Expand"} thread with ${thread.participantName}`}
                >
                  <div className="mt-1 flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-muted text-muted-foreground">
                    <DirectionIcon className="h-4 w-4" />
                  </div>
                  <div className="min-w-0 flex-1">
                    <div className="mb-1.5 flex flex-wrap items-center gap-2">
                      <span className="text-sm font-medium text-foreground">
                        {firstMessage.authorName}
                      </span>
                      <span
                        className={cn(
                          "inline-flex items-center gap-1 rounded-full border px-2 py-0.5 text-[10px] font-medium",
                          directionMeta.className,
                        )}
                      >
                        <DirectionIcon className="h-3 w-3" />
                        {directionMeta.label}
                      </span>
                      <span className="text-[10px] text-muted-foreground/70">
                        {firstMessage.sentAt}
                      </span>
                    </div>
                    <p className="line-clamp-2 text-sm leading-6 text-foreground/85">
                      {firstMessage.body}
                    </p>
                  </div>
                  <ChevronDown
                    className={cn(
                      "mt-1 h-4 w-4 shrink-0 text-muted-foreground transition-transform",
                      isOpen && "rotate-180",
                    )}
                  />
                </button>
              </CollapsibleTrigger>

              <CollapsibleContent>
                <div className="border-t border-border/70 px-5 py-4">
                  <div className="space-y-4">
                    {remainingMessages.map((message) => {
                      const messageMeta = getDirectionMeta(message.direction);
                      const MessageIcon = messageMeta.icon;

                      return (
                        <article
                          key={message.id}
                          className={cn(
                            "flex gap-3",
                            message.direction === "outgoing" && "flex-row-reverse",
                          )}
                        >
                          <div className="mt-1 flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-muted text-muted-foreground">
                            <MessageIcon className="h-4 w-4" />
                          </div>
                          <div
                            className={cn(
                              "max-w-[82%] rounded-2xl border border-border bg-background px-4 py-3",
                              message.direction === "outgoing" &&
                                "border-primary/20 bg-primary/5",
                            )}
                          >
                            <div className="mb-1.5 flex flex-wrap items-center gap-2">
                              <span className="text-sm font-medium text-foreground">
                                {message.authorName}
                              </span>
                              <span
                                className={cn(
                                  "inline-flex items-center gap-1 rounded-full border px-2 py-0.5 text-[10px] font-medium",
                                  messageMeta.className,
                                )}
                              >
                                <MessageIcon className="h-3 w-3" />
                                {messageMeta.label}
                              </span>
                              <span className="text-[10px] text-muted-foreground/70">
                                {message.sentAt}
                              </span>
                            </div>
                            <p className="text-sm leading-6 text-foreground/85">
                              {message.body}
                            </p>
                          </div>
                        </article>
                      );
                    })}
                  </div>

                </div>
              </CollapsibleContent>
            </Collapsible>
          );
        })}
      </motion.section>
    </div>
  );
};

export default Messages;
