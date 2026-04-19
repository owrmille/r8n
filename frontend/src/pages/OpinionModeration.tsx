import { useMemo, useState } from "react";
import { motion } from "framer-motion";
import { Check, X } from "lucide-react";
import RatingBadge from "@/components/RatingBadge";
import ReviewerAvatar from "@/components/ReviewerAvatar";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Textarea } from "@/components/ui/textarea";
import { cn } from "@/lib/utils";

type PendingOpinion = {
  id: string;
  subjectName: string;
  subjectType: string;
  reviewerName: string;
  submittedAt: string;
  rating: number;
  linkedList: string;
  subjectiveOpinion: string;
  objectiveFacts: string;
};

type ModerationDecision = PendingOpinion & {
  decision: "approved" | "rejected";
  decidedAt: string;
  rejectionReason?: string;
};

const initialPendingOpinions: PendingOpinion[] = [
  {
    id: "op-espresso-lab",
    subjectName: "Espresso Lab Mitte",
    subjectType: "Café",
    reviewerName: "Alex Krüger",
    submittedAt: "8 minutes ago",
    rating: 8.5,
    linkedList: "Best espresso in Berlin",
    subjectiveOpinion:
      "Excellent extraction and consistent bar work, but the review mentions a barista by name, so it should be checked before publication to a wider trusted circle.",
    objectiveFacts: "Double espresso, medium roast, ordered on Apr 18 at 09:10, total €3.20.",
  },
  {
    id: "op-minister-consulting",
    subjectName: "Public policy consulting workshop",
    subjectType: "Service",
    reviewerName: "Mia Svensson",
    submittedAt: "31 minutes ago",
    rating: 4,
    linkedList: "Consultants and facilitators",
    subjectiveOpinion:
      "Strong presentation style, but the reviewer alleges the organizer used political connections to bypass normal procurement expectations.",
    objectiveFacts: "Half-day workshop, invoice total €1,600, attended by 6 participants.",
  },
  {
    id: "op-bakery-kreuzberg",
    subjectName: "Neighbourhood Bakery Kreuzberg",
    subjectType: "Shop",
    reviewerName: "Tobias Richter",
    submittedAt: "1 hour ago",
    rating: 7,
    linkedList: "Weekend breakfast spots",
    subjectiveOpinion:
      "Great laminated pastry texture and reliable coffee, but the complaint about 'unsafe storage' should be reviewed for factual support before it stays attached to the shop.",
    objectiveFacts: "Pain au chocolat, cappuccino, queue time ~12 minutes, paid €8.40.",
  },
];

const recentDecisionsSeed: ModerationDecision[] = [
  {
    id: "resolved-vacuum-brand",
    subjectName: "Dyson V15 Detect",
    subjectType: "Product",
    reviewerName: "Sophie Chen",
    submittedAt: "Earlier today",
    rating: 6.5,
    linkedList: "Home appliances 2026",
    subjectiveOpinion: "Battery life is fine, but the draft contained an unsupported claim about hidden defects.",
    objectiveFacts: "Home use for 3 weeks, hardwood floors, apartment size 78m².",
    decision: "rejected",
    decidedAt: "24 minutes ago",
    rejectionReason: "Please remove the hidden defect claim or add verifiable facts that support it.",
  },
];

const OpinionModeration = () => {
  const [pendingOpinions, setPendingOpinions] = useState(initialPendingOpinions);
  const [resolvedOpinions, setResolvedOpinions] = useState(recentDecisionsSeed);
  const [rejectingOpinionId, setRejectingOpinionId] = useState<string | null>(null);
  const [rejectionReason, setRejectionReason] = useState("");
  const [rejectionError, setRejectionError] = useState<string | null>(null);

  const rejectingOpinion = useMemo(
    () => pendingOpinions.find((opinion) => opinion.id === rejectingOpinionId) ?? null,
    [pendingOpinions, rejectingOpinionId],
  );

  const closeRejectDialog = () => {
    setRejectingOpinionId(null);
    setRejectionReason("");
    setRejectionError(null);
  };

  const storeDecision = (
    opinion: PendingOpinion,
    decision: ModerationDecision["decision"],
    reason?: string,
  ) => {
    setPendingOpinions((current) => current.filter((item) => item.id !== opinion.id));
    setResolvedOpinions((current) => [
      {
        ...opinion,
        decision,
        decidedAt: "Just now",
        rejectionReason: reason,
      },
      ...current,
    ]);
  };

  const handleApprove = (opinionId: string) => {
    const opinion = pendingOpinions.find((item) => item.id === opinionId);
    if (!opinion) {
      return;
    }

    storeDecision(opinion, "approved");
  };

  const handleReject = () => {
    if (!rejectingOpinion) {
      return;
    }

    const trimmedReason = rejectionReason.trim();
    if (!trimmedReason) {
      setRejectionError("Rejection reason is required.");
      return;
    }

    storeDecision(rejectingOpinion, "rejected", trimmedReason);
    closeRejectDialog();
  };

  return (
    <div className="mx-auto max-w-6xl px-4 py-8 md:px-8 md:py-12">
      <motion.div
        initial={{ opacity: 0, y: 12 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.4 }}
        className="mb-10"
      >
        <h1 className="mb-2 text-3xl font-semibold tracking-tight text-foreground md:text-4xl">
          Opinion Moderation
        </h1>
        <p className="max-w-3xl text-sm text-muted-foreground">
          Review submitted opinions before they move beyond private draft review. Reject actions
          always require a reason so the author can revise the text safely.
        </p>
      </motion.div>

      <motion.section
        initial={{ opacity: 0, y: 8 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.3, delay: 0.1 }}
        className="mb-12"
      >
        <div className="mb-4 flex items-center justify-between gap-3">
          <div>
            <h2 className="text-lg font-semibold tracking-tight text-foreground">Queue</h2>
            <p className="text-sm text-muted-foreground">
              Opinions that were submitted for manual review.
            </p>
          </div>
          <Badge variant="outline" className="rounded-full border-border px-3 py-1 text-xs font-medium">
            {pendingOpinions.length} pending
          </Badge>
        </div>

        {pendingOpinions.length > 0 ? (
          <div className="space-y-4">
            {pendingOpinions.map((opinion) => (
              <Card key={opinion.id} className="overflow-hidden rounded-2xl border-border">
                <CardContent className="p-0">
                  <div className="border-b border-border px-5 py-4 md:px-6">
                    <div className="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
                      <div className="flex items-start gap-3">
                        <ReviewerAvatar name={opinion.reviewerName} size="md" />
                        <div className="min-w-0">
                          <div className="mb-2 flex flex-wrap items-center gap-2">
                            <h3 className="text-base font-semibold text-foreground">{opinion.subjectName}</h3>
                            <Badge variant="outline" className="border-border text-muted-foreground">
                              {opinion.subjectType}
                            </Badge>
                          </div>
                          <p className="text-sm text-muted-foreground">
                            Submitted by {opinion.reviewerName} to{" "}
                            <span className="font-medium text-foreground/80">{opinion.linkedList}</span> ·{" "}
                            {opinion.submittedAt}
                          </p>
                        </div>
                      </div>

                      <div className="flex flex-wrap items-center gap-2 md:justify-end">
                        <RatingBadge value={opinion.rating} />
                      </div>
                    </div>
                  </div>

                  <div className="px-5 py-5 md:px-6">
                    <div className="space-y-4">
                      <div>
                        <p className="mb-1 text-xs font-semibold uppercase tracking-[0.14em] text-muted-foreground">
                          Subjective opinion
                        </p>
                        <p className="text-sm leading-relaxed text-foreground/85">{opinion.subjectiveOpinion}</p>
                      </div>
                      <div>
                        <p className="mb-1 text-xs font-semibold uppercase tracking-[0.14em] text-muted-foreground">
                          Objective facts
                        </p>
                        <p className="text-sm leading-relaxed text-muted-foreground">{opinion.objectiveFacts}</p>
                      </div>
                    </div>
                  </div>

                  <div className="flex flex-col gap-2 border-t border-border bg-muted/20 px-5 py-4 md:flex-row md:justify-end md:px-6">
                    <Button
                      variant="outline"
                      className="border-destructive/20 text-destructive hover:bg-destructive/5 hover:text-destructive"
                      onClick={() => {
                        setRejectingOpinionId(opinion.id);
                        setRejectionError(null);
                      }}
                    >
                      <X className="h-4 w-4" />
                      Reject
                    </Button>
                    <Button onClick={() => handleApprove(opinion.id)}>
                      <Check className="h-4 w-4" />
                      Approve
                    </Button>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        ) : (
          <Card className="rounded-2xl border-dashed border-border">
            <CardContent className="py-14 text-center">
              <p className="text-base font-medium text-foreground">No opinions are waiting for review.</p>
              <p className="mt-2 text-sm text-muted-foreground">
                New submissions will appear here as soon as they enter the moderation queue.
              </p>
            </CardContent>
          </Card>
        )}
      </motion.section>

      <motion.section
        initial={{ opacity: 0, y: 8 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.3, delay: 0.15 }}
      >
        <div className="mb-4 flex items-center justify-between gap-3">
          <div>
            <h2 className="text-lg font-semibold tracking-tight text-foreground">Recent decisions</h2>
            <p className="text-sm text-muted-foreground">
              Useful for checking the latest moderator actions.
            </p>
          </div>
          <Badge variant="outline" className="rounded-full border-border px-3 py-1 text-xs font-medium">
            {resolvedOpinions.length} logged
          </Badge>
        </div>

        <div className="space-y-3">
          {resolvedOpinions.map((opinion) => (
            <Card key={opinion.id} className="rounded-2xl border-border">
              <CardContent className="flex flex-col gap-4 px-5 py-4 md:flex-row md:items-start md:justify-between md:px-6">
                <div className="min-w-0">
                  <div className="mb-1 flex flex-wrap items-center gap-2">
                    <p className="text-sm font-semibold text-foreground">{opinion.subjectName}</p>
                    <Badge
                      className={cn(
                        opinion.decision === "approved"
                          ? "bg-primary/10 text-primary hover:bg-primary/10"
                          : "bg-destructive/10 text-destructive hover:bg-destructive/10",
                      )}
                    >
                      {opinion.decision === "approved" ? "Approved" : "Rejected"}
                    </Badge>
                  </div>
                  <p className="text-sm text-muted-foreground">
                    {opinion.reviewerName} · {opinion.decidedAt}
                  </p>
                  {opinion.rejectionReason && (
                    <p className="mt-3 text-sm leading-relaxed text-foreground/85">
                      <span className="font-medium text-foreground">Reason:</span> {opinion.rejectionReason}
                    </p>
                  )}
                </div>
                <div className="flex items-center gap-2">
                  <RatingBadge value={opinion.rating} />
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      </motion.section>

      <Dialog open={Boolean(rejectingOpinion)} onOpenChange={(open) => !open && closeRejectDialog()}>
        <DialogContent className="sm:max-w-lg">
          <DialogHeader>
            <DialogTitle>Reject opinion</DialogTitle>
            <DialogDescription>
              Add a clear reason so the author can revise{" "}
              <span className="font-medium text-foreground">{rejectingOpinion?.subjectName}</span> safely.
            </DialogDescription>
          </DialogHeader>

          <div className="space-y-2">
            <label htmlFor="rejection-reason" className="text-sm font-medium text-foreground">
              Rejection reason
            </label>
            <Textarea
              id="rejection-reason"
              value={rejectionReason}
              onChange={(event) => {
                setRejectionReason(event.target.value);
                if (rejectionError) {
                  setRejectionError(null);
                }
              }}
              placeholder="Explain what must be changed before this opinion can be approved."
              className={cn(rejectionError && "border-destructive focus-visible:ring-destructive")}
            />
            {rejectionError && <p className="text-sm text-destructive">{rejectionError}</p>}
          </div>

          <DialogFooter>
            <Button variant="outline" onClick={closeRejectDialog}>
              Cancel
            </Button>
            <Button variant="destructive" onClick={handleReject}>
              Reject with reason
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default OpinionModeration;
