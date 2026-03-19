import { useState } from "react";
import { motion } from "framer-motion";
import { Check, X, Clock, XCircle, Eye, EyeOff, Trash2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import ReviewerAvatar from "@/components/ReviewerAvatar";

const incomingRequests = [
  { from: "Sophie Chen", list: "Best espresso in Berlin", date: "2h ago" },
  { from: "Tobias Richter", list: "Date night restaurants", date: "1d ago" },
  { from: "Marcus Weber", list: "Cheap lunch under €10", date: "3d ago" },
];

const outgoingRequests = [
  { to: "Mia Svensson", list: "Nordic brunch spots", date: "1d ago", status: "pending" as const },
  { to: "Tobias Richter", list: "Hidden gems in Kreuzberg", date: "3d ago", status: "pending" as const },
  { to: "Alex Krüger", list: "Best espresso in Berlin", date: "1w ago", status: "approved" as const },
  { to: "Sophie Chen", list: "Vegan fine dining Berlin", date: "2w ago", status: "declined" as const },
];

const Requests = () => {
  const [hiddenIndices, setHiddenIndices] = useState<number[]>([]);
  const [deletedIndices, setDeletedIndices] = useState<number[]>([]);
  const [showHidden, setShowHidden] = useState(false);

  const handleHide = (index: number) => {
    setHiddenIndices((prev) => [...prev, index]);
  };

  const handleUnhide = (index: number) => {
    setHiddenIndices((prev) => prev.filter((i) => i !== index));
  };

  const handleDelete = (index: number) => {
    setDeletedIndices((prev) => [...prev, index]);
  };

  const visibleRequests = incomingRequests
    .map((req, i) => ({ ...req, originalIndex: i }))
    .filter((req) => !hiddenIndices.includes(req.originalIndex) && !deletedIndices.includes(req.originalIndex));

  const hiddenRequests = incomingRequests
    .map((req, i) => ({ ...req, originalIndex: i }))
    .filter((req) => hiddenIndices.includes(req.originalIndex));

  return (
    <div className="mx-auto max-w-4xl px-4 py-8 md:px-8 md:py-12">
      <motion.div
        initial={{ opacity: 0, y: 12 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.4 }}
        className="mb-10"
      >
        <h1 className="text-2xl md:text-3xl font-semibold tracking-tight text-foreground mb-1">Access Requests</h1>
        <p className="text-sm text-muted-foreground">Manage who can access your lists.</p>
      </motion.div>

      {/* Incoming */}
      <motion.section
        initial={{ opacity: 0, y: 8 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.3, delay: 0.1 }}
        className="mb-12"
      >
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-lg font-semibold tracking-tight text-foreground">
            Incoming
            <span className="ml-2 inline-flex h-5 w-5 items-center justify-center rounded-full bg-accent text-[10px] font-mono font-semibold text-accent-foreground">
              {visibleRequests.length}
            </span>
          </h2>
          {hiddenRequests.length > 0 && (
            <Button
              variant="ghost"
              size="sm"
              className="h-7 text-xs text-muted-foreground gap-1.5"
              onClick={() => setShowHidden(!showHidden)}
            >
              {showHidden ? <EyeOff className="h-3 w-3" /> : <Eye className="h-3 w-3" />}
              {showHidden ? "Hide" : "Show"} hidden ({hiddenRequests.length})
            </Button>
          )}
        </div>

        {visibleRequests.length > 0 && (
          <div className="rounded-2xl border border-border overflow-hidden">
            {visibleRequests.map((req) => (
              <div
                key={req.originalIndex}
                className="flex items-center gap-4 border-b border-border last:border-0 px-5 py-4"
              >
                <ReviewerAvatar name={req.from} size="sm" />
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium text-foreground truncate">{req.from}</p>
                  <p className="text-xs text-muted-foreground truncate">
                    Wants access to: <span className="font-medium text-foreground/70">{req.list}</span>
                  </p>
                </div>
                <span className="text-[10px] text-muted-foreground/60 shrink-0">{req.date}</span>
                <div className="flex gap-2 shrink-0">
                  <Button variant="default" size="sm" className="h-8 rounded-lg text-xs">
                    <Check className="mr-1 h-3 w-3" />
                    Approve
                  </Button>
                  <Button
                    variant="ghost"
                    size="sm"
                    className="h-8 rounded-lg text-xs text-muted-foreground"
                    onClick={() => handleHide(req.originalIndex)}
                  >
                    <EyeOff className="mr-1 h-3 w-3" />
                    Hide
                  </Button>
                  <Button
                    variant="ghost"
                    size="sm"
                    className="h-8 rounded-lg text-xs text-destructive hover:text-destructive"
                    onClick={() => handleDelete(req.originalIndex)}
                  >
                    <Trash2 className="h-3 w-3" />
                  </Button>
                </div>
              </div>
            ))}
          </div>
        )}

        {showHidden && hiddenRequests.length > 0 && (
          <div className="mt-3 rounded-2xl border border-dashed border-border overflow-hidden opacity-60">
            {hiddenRequests.map((req) => (
              <div
                key={req.originalIndex}
                className="flex items-center gap-4 border-b border-border last:border-0 px-5 py-4"
              >
                <ReviewerAvatar name={req.from} size="sm" />
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium text-foreground truncate">{req.from}</p>
                  <p className="text-xs text-muted-foreground truncate">
                    Wants access to: <span className="font-medium text-foreground/70">{req.list}</span>
                  </p>
                </div>
                <span className="text-[10px] text-muted-foreground/60 shrink-0">{req.date}</span>
                <Button
                  variant="ghost"
                  size="sm"
                  className="h-8 rounded-lg text-xs text-muted-foreground"
                  onClick={() => handleUnhide(req.originalIndex)}
                >
                  <Eye className="mr-1 h-3 w-3" />
                  Unhide
                </Button>
              </div>
            ))}
          </div>
        )}
      </motion.section>

      {/* Outgoing */}
      <motion.section
        initial={{ opacity: 0, y: 8 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.3, delay: 0.15 }}
      >
        <h2 className="mb-4 text-lg font-semibold tracking-tight text-foreground">Outgoing</h2>
        <div className="rounded-2xl border border-border overflow-hidden">
          {outgoingRequests.map((req, i) => (
            <div
              key={i}
              className="flex items-center gap-4 border-b border-border last:border-0 px-5 py-4"
            >
              <ReviewerAvatar name={req.to} size="sm" />
              <div className="flex-1 min-w-0">
                <p className="text-sm font-medium text-foreground truncate">{req.to}</p>
                <p className="text-xs text-muted-foreground truncate">{req.list}</p>
              </div>
              <span className="text-[10px] text-muted-foreground/60 shrink-0">{req.date}</span>
              <div className="shrink-0">
                {req.status === "pending" && (
                  <span className="inline-flex items-center gap-1 text-xs text-muted-foreground">
                    <Clock className="h-3 w-3" />
                    Pending
                  </span>
                )}
                {req.status === "approved" && (
                  <span className="inline-flex items-center gap-1 text-xs text-primary">
                    <Check className="h-3 w-3" />
                    Approved
                  </span>
                )}
                {req.status === "declined" && (
                  <span className="inline-flex items-center gap-1 text-xs text-destructive">
                    <XCircle className="h-3 w-3" />
                    Declined
                  </span>
                )}
              </div>
            </div>
          ))}
        </div>
      </motion.section>
    </div>
  );
};

export default Requests;
