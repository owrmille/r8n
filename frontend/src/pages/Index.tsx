import { Search, Plus } from "lucide-react";
import { useState } from "react";
import { motion } from "framer-motion";
import ReviewCard from "@/components/ReviewCard";
import OpinionListCard from "@/components/OpinionListCard";
import { Button } from "@/components/ui/button";
import { Link } from "react-router-dom";
import { cn } from "@/lib/utils";

const CATEGORIES = ["All", "Restaurant", "Café", "Product", "Service", "Hotel", "Shop"];

const mockReviews = [
  {
    restaurantName: "Coda Dessert Bar",
    reviewerName: "Alex Krüger",
    timeAgo: "2h ago",
    rating: 9.5,
    weightedRating: 8.7,
    networkCount: 3,
    content:
      "Extraordinary tasting menu. The black sesame course was revelatory — bitter, sweet, and textural in ways I've never experienced. Service was impeccable without being performative. Worth every euro.",
    category: "Restaurant",
  },
  {
    restaurantName: "Dyson V15 Detect",
    reviewerName: "Mia Svensson",
    timeAgo: "1d ago",
    rating: 7,
    weightedRating: 6.4,
    networkCount: 2,
    content:
      "Great suction power and the laser dust detection is genuinely useful. Battery life is solid. But it's heavy and the price is hard to justify when competitors perform similarly.",
    category: "Product",
  },
  {
    restaurantName: "Nobelhart & Schmutzig",
    reviewerName: "Tobias Richter",
    timeAgo: "3d ago",
    rating: 8.5,
    weightedRating: 9.1,
    networkCount: 4,
    content:
      "Still the most intellectually stimulating dining experience in Berlin. The 'brutally local' concept continues to challenge and delight.",
    category: "Restaurant",
  },
  {
    restaurantName: "Bonanza Coffee",
    reviewerName: "Sophie Chen",
    timeAgo: "5d ago",
    rating: 6,
    weightedRating: 7.2,
    networkCount: 5,
    content:
      "Reliable neighbourhood café with great atmosphere. Coffee is solid if unspectacular. The real draw is the vibe — perfect for long weekend mornings with a book.",
    category: "Café",
  },
];

const mockLists = [
  {
    title: "Best espresso in Berlin",
    description: "A curated guide to the city's finest espresso — from specialty roasters to hidden neighbourhood gems.",
    reviewCount: 12,
    authorName: "Alex Krüger",
    hasAccess: true,
  },
  {
    title: "Date night restaurants",
    description: "Intimate, thoughtfully designed spaces where the food matches the ambiance.",
    reviewCount: 8,
    authorName: "Mia Svensson",
    hasAccess: false,
    accessStatus: "none" as const,
  },
  {
    title: "Cheap lunch under €10",
    description: "Great food doesn't need to be expensive. My favorite affordable lunch spots across Mitte and Kreuzberg.",
    reviewCount: 15,
    authorName: "Jane Doe",
    hasAccess: true,
  },
];

const Dashboard = () => {
  const [activeCategory, setActiveCategory] = useState("All");

  const filteredReviews = activeCategory === "All"
    ? mockReviews
    : mockReviews.filter((r) => r.category === activeCategory);

  return (
    <div className="mx-auto max-w-4xl px-4 py-8 md:px-8 md:py-12">
      {/* Header */}
      <motion.div
        initial={{ opacity: 0, y: 12 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.4 }}
        className="mb-10"
      >
        <h1 className="font-body text-3xl md:text-4xl font-semibold text-foreground mb-2 tracking-tight">
          Good evening, Jane
        </h1>
        <p className="text-muted-foreground text-sm">
          3 new reviews from your network · 2 pending requests
        </p>
      </motion.div>

      {/* Search */}
      <motion.div
        initial={{ opacity: 0, y: 8 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.3, delay: 0.1 }}
        className="mb-6"
      >
        <div className="relative">
          <Search className="absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
          <input
            type="text"
            placeholder="Search items, suppliers, or reviewers..."
            className="w-full rounded-xl border border-border bg-card py-3 pl-11 pr-4 text-sm text-foreground placeholder:text-muted-foreground/60 focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary/40 transition-all"
          />
        </div>
      </motion.div>

      {/* Category Filter */}
      <motion.div
        initial={{ opacity: 0, y: 8 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.3, delay: 0.12 }}
        className="mb-10 flex gap-2 overflow-x-auto pb-1 scrollbar-none"
      >
        {CATEGORIES.map((cat) => (
          <button
            key={cat}
            onClick={() => setActiveCategory(cat)}
            className={cn(
              "shrink-0 rounded-full border px-3.5 py-1.5 text-xs font-medium transition-all",
              activeCategory === cat
                ? "border-primary bg-primary text-primary-foreground"
                : "border-border bg-card text-muted-foreground hover:border-primary/30"
            )}
          >
            {cat}
          </button>
        ))}
      </motion.div>

      {/* My Lists */}
      <motion.section
        initial={{ opacity: 0, y: 8 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.3, delay: 0.15 }}
        className="mb-12"
      >
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-lg font-semibold tracking-tight text-foreground">Your Lists</h2>
          <Link to="/lists">
            <Button variant="ghost" size="sm" className="text-xs text-muted-foreground">
              View all
            </Button>
          </Link>
        </div>
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {mockLists
            .filter((l) => l.hasAccess)
            .map((list, i) => (
              <OpinionListCard key={i} {...list} />
            ))}
          <motion.div
            whileTap={{ scale: 0.98 }}
            className="flex cursor-pointer items-center justify-center rounded-2xl border-2 border-dashed border-border p-5 text-muted-foreground transition-colors hover:border-primary/30 hover:text-primary"
          >
            <Link to="/lists/create" className="flex items-center gap-2 text-sm font-medium">
              <Plus className="h-4 w-4" />
              New List
            </Link>
          </motion.div>
        </div>
      </motion.section>

      {/* Activity Feed */}
      <motion.section
        initial={{ opacity: 0, y: 8 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.3, delay: 0.2 }}
      >
        <h2 className="mb-4 text-lg font-semibold tracking-tight text-foreground">From Your Network</h2>
        <div className="space-y-4">
          {filteredReviews.length > 0 ? (
            filteredReviews.map((review, i) => (
              <ReviewCard key={i} {...review} />
            ))
          ) : (
            <div className="rounded-2xl border border-dashed border-border py-12 text-center">
              <p className="text-sm text-muted-foreground">No reviews in this category yet.</p>
            </div>
          )}
        </div>

        {filteredReviews.length > 0 && (
          <div className="mt-8 flex justify-center">
            <Button variant="outline" className="rounded-xl px-8">
              Load More
            </Button>
          </div>
        )}
      </motion.section>

      {/* Legal footer */}
      <footer className="mt-16 border-t border-border pt-6 pb-4">
        <p className="text-[11px] text-muted-foreground/60 leading-relaxed max-w-2xl">
          <strong className="text-muted-foreground/80">Protected Communication:</strong> Reviews
          on R8N are private and only visible to users you manually approve. In accordance with
          German privacy standards, this constitutes private correspondence, not public broadcast.
        </p>
      </footer>
    </div>
  );
};

export default Dashboard;
