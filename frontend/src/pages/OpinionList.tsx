import { useState, useCallback } from "react";
import { useParams, Link } from "react-router-dom";
import { motion, AnimatePresence } from "framer-motion";
import { ArrowLeft, List, ChevronDown, Plus } from "lucide-react";
import UserAvatar from "@/components/UserAvatar";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { QueryState } from "@/components/server-state/QueryState";
import { cn } from "@/lib/utils";
import { useOpinionList, useLinkOpinionToListMutation } from "@/lib/server-state/hooks/opinion-lists";
import { useOpinion, useCreateOpinionMutation, useAdjustOpinionComponentWeightMutation } from "@/lib/server-state/hooks/opinions";
import type { OpinionSummaryDto, WeightedOpinionReferenceDto } from "@/lib/api/opinions";
import type { Uuid } from "@/lib/api/shared";

const OpinionListPage = () => {
  const { id: listId } = useParams<{ id: string }>();
  const [expandedItem, setExpandedItem] = useState<string | null>(null);

  const { data, isLoading, isError, error, refetch } = useOpinionList(
    { listId: listId! },
    { enabled: !!listId },
  );

  const adjustWeight = useAdjustOpinionComponentWeightMutation();
  const createOpinion = useCreateOpinionMutation();
  const linkOpinion = useLinkOpinionToListMutation();

  const summaries = data?.opinionSummaries ?? [];

  const handleAdjustWeight = useCallback((linkId: Uuid, weight: number) => {
    adjustWeight.mutate({ linkId, weight });
  }, [adjustWeight]);

  const handleAddReview = useCallback(async (
    subjectId: Uuid,
    mark: number,
    subjective: string,
    objective: string,
  ) => {
    const opinion = await createOpinion.mutateAsync({
      subjectId,
      mark,
      subjective: [subjective],
      objective: [objective],
    });
    await linkOpinion.mutateAsync({ listId: listId!, opinionId: opinion.id });
  }, [createOpinion, linkOpinion, listId]);

  return (
    <div className="mx-auto max-w-5xl px-4 py-8 md:px-8 md:py-12">
      <Link
        to="/lists"
        className="mb-6 inline-flex items-center gap-1.5 text-xs text-muted-foreground hover:text-foreground transition-colors"
      >
        <ArrowLeft className="h-3 w-3" />
        Back to lists
      </Link>

      <QueryState
        isLoading={isLoading}
        isError={isError}
        error={error}
        isEmpty={false}
        onRetry={refetch}
      >
        <>
          <motion.div
            initial={{ opacity: 0, y: 12 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.4 }}
            className="mb-8"
          >
            <div className="flex items-center gap-3 mb-2">
              <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-primary/8">
                <List className="h-5 w-5 text-primary" />
              </div>
              <h1 className="text-2xl md:text-3xl font-semibold tracking-tight text-foreground font-body">
                {data?.ownerName} · {data?.listName}
              </h1>
            </div>
            <p className="text-xs text-muted-foreground">
              {summaries.length} {summaries.length === 1 ? "subject" : "subjects"}
            </p>
          </motion.div>

          <div className="flex flex-wrap gap-2 mb-6">
            <Button variant="default" size="sm" className="rounded-lg text-xs" asChild>
              <Link to="/review/create">
                <Plus className="mr-1 h-3 w-3" /> Add new
              </Link>
            </Button>
          </div>

          <motion.div
            initial={{ opacity: 0, y: 8 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.3, delay: 0.1 }}
            className="rounded-2xl border border-border overflow-hidden bg-card"
          >
            {summaries.length === 0 ? (
              <p className="px-5 py-8 text-center text-sm text-muted-foreground">No subjects in this list yet.</p>
            ) : (
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-border bg-muted/30">
                    <th className="px-4 py-3 text-left font-medium text-muted-foreground text-xs">Name</th>
                    <th className="px-4 py-3 text-left font-medium text-muted-foreground text-xs">My rating</th>
                    <th className="px-4 py-3 text-left font-medium text-muted-foreground text-xs">Weighted rating</th>
                    <th className="px-4 py-3 w-8"></th>
                  </tr>
                </thead>
                <tbody>
                  {summaries.map((summary) => (
                    <ItemRow
                      key={summary.subject}
                      listId={listId!}
                      summary={summary}
                      isExpanded={expandedItem === summary.subject}
                      onToggle={() => setExpandedItem(expandedItem === summary.subject ? null : summary.subject)}
                      onAdjustWeight={handleAdjustWeight}
                      onAddReview={handleAddReview}
                      isAdjustingWeight={adjustWeight.isPending}
                      isAddingReview={createOpinion.isPending || linkOpinion.isPending}
                    />
                  ))}
                </tbody>
              </table>
            )}
          </motion.div>
        </>
      </QueryState>
    </div>
  );
};

const ItemRow = ({
  summary,
  isExpanded,
  onToggle,
  onAdjustWeight,
  onAddReview,
  isAdjustingWeight,
  isAddingReview,
}: {
  summary: OpinionSummaryDto;
  isExpanded: boolean;
  onToggle: () => void;
  onAdjustWeight: (linkId: Uuid, weight: number) => void;
  onAddReview: (subjectId: Uuid, mark: number, subjective: string, objective: string) => Promise<void>;
  isAdjustingWeight: boolean;
  isAddingReview: boolean;
}) => {
  const [showForm, setShowForm] = useState(false);
  const [subjective, setSubjective] = useState("");
  const [objective, setObjective] = useState("");
  const [rating, setRating] = useState("");

  const hasMyReview = summary.ownMark !== null;

  const handleSubmit = async () => {
    const ratingNum = parseFloat(rating);
    if (!subjective.trim() || !objective.trim() || isNaN(ratingNum) || ratingNum < 0 || ratingNum > 10) return;
    try {
      await onAddReview(summary.subject, ratingNum, subjective.trim(), objective.trim());
      setShowForm(false);
      setSubjective("");
      setObjective("");
      setRating("");
    } catch {
      // error surfaced via mutation meta errorTitle toast — keep form open so user can retry
    }
  };

  return (
    <>
      <tr
        className={cn(
          "border-b border-border last:border-0 cursor-pointer transition-colors hover:bg-muted/20",
          isExpanded && "bg-muted/20"
        )}
        onClick={onToggle}
      >
        <td className="px-4 py-3 font-medium text-foreground">{summary.subjectName}</td>
        <td className="px-4 py-3 font-mono text-foreground">
          {summary.ownMark !== null ? summary.ownMark.toFixed(1) : "—"}
        </td>
        <td className="px-4 py-3 font-mono font-semibold text-foreground">
          {summary.componentMark !== null ? summary.componentMark.toFixed(2) : "—"}
        </td>
        <td className="px-4 py-3">
          <ChevronDown
            className={cn(
              "h-4 w-4 text-muted-foreground transition-transform",
              isExpanded && "rotate-180"
            )}
          />
        </td>
      </tr>

      <AnimatePresence>
        {isExpanded && (
          <tr>
            <td colSpan={4} className="p-0">
              <motion.div
                initial={{ height: 0, opacity: 0 }}
                animate={{ height: "auto", opacity: 1 }}
                exit={{ height: 0, opacity: 0 }}
                transition={{ duration: 0.2 }}
                className="overflow-hidden"
              >
                <div className="border-t border-border bg-muted/10 px-4 py-4">
                  <div className="flex items-center justify-between mb-3">
                    <span className="text-sm font-medium text-foreground">{summary.subjectName}</span>
                    {!hasMyReview && (
                      <Button
                        variant="outline"
                        size="sm"
                        className="rounded-lg text-xs h-7"
                        onClick={(e) => { e.stopPropagation(); setShowForm(!showForm); }}
                      >
                        <Plus className="mr-1 h-3 w-3" /> Add my review
                      </Button>
                    )}
                  </div>

                  {summary.opinions.length > 0 && (
                    <div className="rounded-xl border border-border overflow-hidden bg-card">
                      <table className="w-full text-xs">
                        <thead>
                          <tr className="border-b border-border bg-muted/20">
                            <th className="px-3 py-2 text-left font-medium text-muted-foreground">User</th>
                            <th className="px-3 py-2 text-left font-medium text-muted-foreground">Subjective opinion</th>
                            <th className="px-3 py-2 text-left font-medium text-muted-foreground hidden sm:table-cell">Objective facts</th>
                            <th className="px-3 py-2 text-left font-medium text-muted-foreground">Rating</th>
                            <th className="px-3 py-2 text-left font-medium text-muted-foreground hidden md:table-cell">Trust</th>
                            <th className="px-3 py-2 text-left font-medium text-muted-foreground hidden lg:table-cell">Status</th>
                          </tr>
                        </thead>
                        <tbody>
                          {summary.opinions.map((ref) => (
                            <OpinionRow
                              key={ref.id}
                              ref_={ref}
                              onWeightChange={onAdjustWeight}
                              isAdjustingWeight={isAdjustingWeight}
                            />
                          ))}
                        </tbody>
                      </table>
                    </div>
                  )}

                  <AnimatePresence>
                    {showForm && (
                      <motion.div
                        initial={{ height: 0, opacity: 0 }}
                        animate={{ height: "auto", opacity: 1 }}
                        exit={{ height: 0, opacity: 0 }}
                        transition={{ duration: 0.15 }}
                        className="overflow-hidden"
                      >
                        <div className="mt-3 rounded-xl border border-border bg-card p-4 space-y-3">
                          <p className="text-xs font-medium text-foreground">Add your review</p>
                          <div className="grid gap-3 sm:grid-cols-2">
                            <div>
                              <label className="text-[10px] font-medium text-muted-foreground mb-1 block">Subjective opinion</label>
                              <Input
                                placeholder="How did it taste?"
                                value={subjective}
                                onChange={(e) => setSubjective(e.target.value)}
                                className="h-8 text-xs"
                              />
                            </div>
                            <div>
                              <label className="text-[10px] font-medium text-muted-foreground mb-1 block">Objective facts</label>
                              <Input
                                placeholder="Temperature, roast, etc."
                                value={objective}
                                onChange={(e) => setObjective(e.target.value)}
                                className="h-8 text-xs"
                              />
                            </div>
                          </div>
                          <div className="flex items-end gap-3">
                            <div>
                              <label className="text-[10px] font-medium text-muted-foreground mb-1 block">Rating (0–10)</label>
                              <Input
                                type="number"
                                min={0}
                                max={10}
                                step={0.1}
                                placeholder="0.0"
                                value={rating}
                                onChange={(e) => setRating(e.target.value)}
                                className="h-8 w-20 text-xs font-mono"
                              />
                            </div>
                            <div className="flex gap-2">
                              <Button
                                size="sm"
                                className="h-8 rounded-lg text-xs"
                                disabled={isAddingReview}
                                onClick={handleSubmit}
                              >
                                Submit
                              </Button>
                              <Button
                                variant="ghost"
                                size="sm"
                                className="h-8 rounded-lg text-xs"
                                onClick={() => setShowForm(false)}
                              >
                                Cancel
                              </Button>
                            </div>
                          </div>
                        </div>
                      </motion.div>
                    )}
                  </AnimatePresence>
                </div>
              </motion.div>
            </td>
          </tr>
        )}
      </AnimatePresence>
    </>
  );
};

const OpinionRow = ({
  ref_,
  onWeightChange,
  isAdjustingWeight,
}: {
  ref_: WeightedOpinionReferenceDto;
  onWeightChange: (linkId: Uuid, weight: number) => void;
  isAdjustingWeight: boolean;
}) => {
  const { data: opinion, isLoading } = useOpinion({ id: ref_.opinion });

  if (isLoading) {
    return (
      <tr className="border-b border-border last:border-0">
        <td colSpan={6} className="px-3 py-2.5 text-muted-foreground">Loading…</td>
      </tr>
    );
  }

  return (
    <tr className="border-b border-border last:border-0">
      <td className="px-3 py-2.5 text-foreground font-medium">
        <div className="flex items-center gap-2">
          <UserAvatar userId={opinion?.owner} name={opinion?.ownerName ?? "?"} size="sm" />
          {opinion?.ownerName ?? "—"}
        </div>
      </td>
      <td className="px-3 py-2.5 text-muted-foreground">{opinion?.subjective.join(", ") ?? "—"}</td>
      <td className="px-3 py-2.5 text-muted-foreground hidden sm:table-cell">{opinion?.objective.join(", ") ?? "—"}</td>
      <td className="px-3 py-2.5 font-mono font-medium text-foreground">
        {opinion?.mark !== null && opinion?.mark !== undefined ? opinion.mark.toFixed(1) : "—"}
      </td>
      <td className="px-3 py-1.5 font-mono text-muted-foreground hidden md:table-cell">
        <Input
          type="number"
          min={0}
          max={1}
          step={0.1}
          key={ref_.weight}
          defaultValue={ref_.weight}
          disabled={isAdjustingWeight}
          onChange={(e) => {
            const val = parseFloat(e.target.value);
            if (!isNaN(val)) onWeightChange(ref_.id, Math.min(1, Math.max(0, val)));
          }}
          onClick={(e) => e.stopPropagation()}
          className="h-7 w-16 px-2 text-xs font-mono bg-transparent border-border"
        />
      </td>
      <td className="px-3 py-2.5 text-muted-foreground hidden lg:table-cell capitalize">
        {opinion?.status.toLowerCase().replace("_", " ") ?? "—"}
      </td>
    </tr>
  );
};

export default OpinionListPage;
