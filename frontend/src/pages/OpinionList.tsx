import { useState, useCallback } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { ArrowLeft, List, ChevronDown, Plus } from "lucide-react";
import { Link } from "react-router-dom";
import ReviewerAvatar from "@/components/ReviewerAvatar";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { cn } from "@/lib/utils";

interface UserReview {
  user: string;
  subjectiveOpinion: string;
  objectiveFacts: string;
  rating: number;
  trust: number;
  status: string;
}

const CURRENT_USER = "jane doe";

interface ListItem {
  id: string;
  name: string;
  weightedRating: number;
  location: string;
  allLocations: string[];
  reviews: UserReview[];
}

const listData = {
  title: "Ivan · Cappuccino",
  description: "A curated collection of cappuccino reviews across Berlin.",
  authorName: "Ivan",
  connectedTo: "carl_cappuccino and 4 others",
};

const listItems: ListItem[] = [
  {
    id: "cappuccino1",
    name: "Cappuccino",
    weightedRating: 4.42,
    location: "Cafe Eins, 78 Third St, Berlin",
    allLocations: ["123 Main St, Berlin", "78 Third St, Berlin"],
    reviews: [
      { user: "ivan", subjectiveOpinion: "Balanced, slightly nutty", objectiveFacts: "Milk ~65°C, medium roast", rating: 4.5, trust: 1.0, status: "awaiting premoderation since Jan 30, 2026" },
      { user: "maria", subjectiveOpinion: "Very smooth", objectiveFacts: "No bitterness", rating: 5.0, trust: 0.8, status: "published Jan 5, 2026" },
      { user: "carl", subjectiveOpinion: "Too flat", objectiveFacts: "Over-foamed milk", rating: 2.1, trust: 0.2, status: "published Jan 15, 2026" },
    ],
  },
  {
    id: "thicc",
    name: "Cappuccino extra thicc",
    weightedRating: 4.10,
    location: "Cafe Zwei, 33 Fourth St, Berlin",
    allLocations: ["1 Double St, Berlin", "5b Crossing St, Berlin", "33 Fourth St, Berlin"],
    reviews: [
      { user: "maria", subjectiveOpinion: "Rich and creamy", objectiveFacts: "Double shot, oat milk", rating: 4.1, trust: 0.8, status: "published Feb 2, 2026" },
    ],
  },
  {
    id: "cappuccino2",
    name: "Cappuccino",
    weightedRating: 4.0,
    location: "Cafe Zwei, 33 Fourth St, Berlin",
    allLocations: ["1 Double St, Berlin", "5b Crossing St, Berlin", "33 Fourth St, Berlin"],
    reviews: [
      { user: "ivan", subjectiveOpinion: "Decent, a bit acidic", objectiveFacts: "Light roast, 60°C milk", rating: 4.0, trust: 1.0, status: "published Jan 20, 2026" },
      { user: "maria", subjectiveOpinion: "Underwhelming", objectiveFacts: "Thin crema", rating: 3.3, trust: 0.8, status: "published Jan 22, 2026" },
      { user: "carl", subjectiveOpinion: "Solid choice", objectiveFacts: "Good temperature, balanced", rating: 4.7, trust: 0.2, status: "published Jan 25, 2026" },
    ],
  },
];

const OpinionListPage = () => {
  const [expandedItem, setExpandedItem] = useState<string | null>(null);
  const [items, setItems] = useState<ListItem[]>(listItems);

  const toggleItem = (id: string) => {
    setExpandedItem(expandedItem === id ? null : id);
  };

  const handleTrustChange = useCallback((itemId: string, reviewIndex: number, newTrust: number) => {
    setItems(prev => prev.map(item => {
      if (item.id !== itemId) return item;
      const updatedReviews = item.reviews.map((r, i) =>
        i === reviewIndex ? { ...r, trust: Math.min(1, Math.max(0, newTrust)) } : r
      );
      const totalWeight = updatedReviews.reduce((sum, r) => sum + r.trust, 0);
      const weightedRating = totalWeight > 0
        ? updatedReviews.reduce((sum, r) => sum + r.rating * r.trust, 0) / totalWeight
        : 0;
      return { ...item, reviews: updatedReviews, weightedRating };
    }));
  }, []);

  const handleAddReview = useCallback((itemId: string, review: UserReview) => {
    setItems(prev => prev.map(item => {
      if (item.id !== itemId) return item;
      const updatedReviews = [...item.reviews, review];
      const totalWeight = updatedReviews.reduce((sum, r) => sum + r.trust, 0);
      const weightedRating = totalWeight > 0
        ? updatedReviews.reduce((sum, r) => sum + r.rating * r.trust, 0) / totalWeight
        : 0;
      return { ...item, reviews: updatedReviews, weightedRating };
    }));
  }, []);

  return (
    <div className="mx-auto max-w-5xl px-4 py-8 md:px-8 md:py-12">
      {/* Back */}
      <Link
        to="/lists"
        className="mb-6 inline-flex items-center gap-1.5 text-xs text-muted-foreground hover:text-foreground transition-colors"
      >
        <ArrowLeft className="h-3 w-3" />
        Back to lists
      </Link>

      {/* Header */}
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
            {listData.title}
          </h1>
        </div>
        <p className="text-sm text-muted-foreground mb-2">{listData.description}</p>
        <p className="text-xs text-muted-foreground">
          Connected to <span className="font-medium text-foreground">{listData.connectedTo}</span>
        </p>
      </motion.div>

      {/* Actions */}
      <div className="flex flex-wrap gap-2 mb-6">
        <Button variant="default" size="sm" className="rounded-lg text-xs">
          <Plus className="mr-1 h-3 w-3" /> Add new
        </Button>
      </div>

      {/* Items table */}
      <motion.div
        initial={{ opacity: 0, y: 8 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.3, delay: 0.1 }}
        className="rounded-2xl border border-border overflow-hidden bg-card"
      >
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-border bg-muted/30">
              <th className="px-4 py-3 text-left font-medium text-muted-foreground text-xs">Name</th>
              <th className="px-4 py-3 text-left font-medium text-muted-foreground text-xs">My rating</th>
              <th className="px-4 py-3 text-left font-medium text-muted-foreground text-xs">Weighted rating</th>
              <th className="px-4 py-3 text-left font-medium text-muted-foreground text-xs hidden md:table-cell">Location</th>
              <th className="px-4 py-3 text-left font-medium text-muted-foreground text-xs hidden lg:table-cell">All locations</th>
              <th className="px-4 py-3 w-8"></th>
            </tr>
          </thead>
          <tbody>
            {items.map((item) => (
              <ItemRow
                key={item.id}
                item={item}
                isExpanded={expandedItem === item.id}
                onToggle={() => toggleItem(item.id)}
                onTrustChange={handleTrustChange}
                onAddReview={handleAddReview}
              />
            ))}
          </tbody>
        </table>
      </motion.div>
    </div>
  );
};

const ItemRow = ({
  item,
  isExpanded,
  onToggle,
  onTrustChange,
  onAddReview,
}: {
  item: ListItem;
  isExpanded: boolean;
  onToggle: () => void;
  onTrustChange: (itemId: string, reviewIndex: number, newTrust: number) => void;
  onAddReview: (itemId: string, review: UserReview) => void;
}) => {
  const [showForm, setShowForm] = useState(false);
  const [subjective, setSubjective] = useState("");
  const [objective, setObjective] = useState("");
  const [rating, setRating] = useState("");

  const hasMyReview = item.reviews.some(r => r.user.toLowerCase() === CURRENT_USER);

  const handleSubmit = () => {
    const ratingNum = parseFloat(rating);
    if (!subjective.trim() || !objective.trim() || isNaN(ratingNum) || ratingNum < 0 || ratingNum > 10) return;
    onAddReview(item.id, {
      user: CURRENT_USER,
      subjectiveOpinion: subjective.trim(),
      objectiveFacts: objective.trim(),
      rating: ratingNum,
      trust: 1.0,
      status: `published ${new Date().toLocaleDateString("en-US", { month: "short", day: "numeric", year: "numeric" })}`,
    });
    setShowForm(false);
    setSubjective("");
    setObjective("");
    setRating("");
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
        <td className="px-4 py-3 font-medium text-foreground">{item.name}</td>
        <td className="px-4 py-3 font-mono text-foreground">
          {(() => {
            const myReview = item.reviews.find(r => r.user.toLowerCase() === CURRENT_USER);
            return myReview ? myReview.rating.toFixed(1) : "—";
          })()}
        </td>
        <td className="px-4 py-3 font-mono font-semibold text-foreground">
          {item.weightedRating.toFixed(2)}
        </td>
        <td className="px-4 py-3 text-muted-foreground hidden md:table-cell text-xs">
          <span className="text-primary hover:underline cursor-pointer">{item.location}</span>
        </td>
        <td className="px-4 py-3 text-xs hidden lg:table-cell">
          {item.allLocations.map((loc, i) => (
            <span key={i}>
              {i > 0 && ", "}
              <span className="text-primary hover:underline cursor-pointer">{loc}</span>
            </span>
          ))}
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
            <td colSpan={6} className="p-0">
              <motion.div
                initial={{ height: 0, opacity: 0 }}
                animate={{ height: "auto", opacity: 1 }}
                exit={{ height: 0, opacity: 0 }}
                transition={{ duration: 0.2 }}
                className="overflow-hidden"
              >
                <div className="border-t border-border bg-muted/10 px-4 py-4">
                  <div className="flex items-center justify-between mb-3">
                    <span className="text-sm font-medium text-foreground">
                      {item.name} · {item.location.split(",")[0]}
                    </span>
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
                        {item.reviews.map((review, i) => (
                          <tr key={i} className="border-b border-border last:border-0">
                            <td className="px-3 py-2.5 text-foreground font-medium">{review.user}</td>
                            <td className="px-3 py-2.5 text-muted-foreground">{review.subjectiveOpinion}</td>
                            <td className="px-3 py-2.5 text-muted-foreground hidden sm:table-cell">{review.objectiveFacts}</td>
                            <td className="px-3 py-2.5 font-mono font-medium text-foreground">{review.rating.toFixed(1)}</td>
                            <td className="px-3 py-1.5 font-mono text-muted-foreground hidden md:table-cell">
                              <Input
                                type="number"
                                min={0}
                                max={1}
                                step={0.1}
                                value={review.trust}
                                onChange={(e) => {
                                  const val = parseFloat(e.target.value);
                                  if (!isNaN(val)) onTrustChange(item.id, i, val);
                                }}
                                onClick={(e) => e.stopPropagation()}
                                className="h-7 w-16 px-2 text-xs font-mono bg-transparent border-border"
                              />
                            </td>
                            <td className="px-3 py-2.5 text-muted-foreground hidden lg:table-cell">{review.status}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>

                  {/* Add review form */}
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
                              <Button size="sm" className="h-8 rounded-lg text-xs" onClick={handleSubmit}>
                                Submit
                              </Button>
                              <Button variant="ghost" size="sm" className="h-8 rounded-lg text-xs" onClick={() => setShowForm(false)}>
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

export default OpinionListPage;
