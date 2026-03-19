import { motion } from "framer-motion";
import { Search } from "lucide-react";
import ReviewCard from "@/components/ReviewCard";
import OpinionListCard from "@/components/OpinionListCard";
import ReviewerAvatar from "@/components/ReviewerAvatar";
import { Button } from "@/components/ui/button";
import { Link } from "react-router-dom";

const trendingReviewers = [
  { id: "alex-kruger", name: "Alex Krüger", reviews: 47, bio: "Berlin food writer" },
  { id: "mia-svensson", name: "Mia Svensson", reviews: 31, bio: "Nordic cuisine enthusiast" },
  { id: "tobias-richter", name: "Tobias Richter", reviews: 22, bio: "Weekend explorer" },
  { id: "sophie-chen", name: "Sophie Chen", reviews: 19, bio: "Coffee & dessert focused" },
];

const featuredLists = [
  {
    title: "Michelin-worthy but unstarred",
    description: "Restaurants that deserve recognition but fly under the radar.",
    reviewCount: 9,
    authorName: "Alex Krüger",
    hasAccess: false,
    accessStatus: "none" as const,
  },
  {
    title: "Vegan fine dining Berlin",
    description: "Plant-based restaurants that don't compromise on technique or flavor.",
    reviewCount: 6,
    authorName: "Sophie Chen",
    hasAccess: false,
  },
];

const Discover = () => {
  return (
    <div className="mx-auto max-w-4xl px-4 py-8 md:px-8 md:py-12">
      <motion.div
        initial={{ opacity: 0, y: 12 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.4 }}
        className="mb-10"
      >
        <h1 className="text-2xl md:text-3xl font-semibold tracking-tight text-foreground mb-2">Discover</h1>
        <p className="text-sm text-muted-foreground">Find reviewers and lists worth following.</p>
      </motion.div>

      {/* Search */}
      <div className="relative mb-10">
        <Search className="absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
        <input
          type="text"
          placeholder="Search restaurants or reviewers..."
          className="w-full rounded-xl border border-border bg-card py-3 pl-11 pr-4 text-sm text-foreground placeholder:text-muted-foreground/60 focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary/40 transition-all"
        />
      </div>

      {/* Suggested Reviewers */}
      <motion.section
        initial={{ opacity: 0, y: 8 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.3, delay: 0.1 }}
        className="mb-12"
      >
        <h2 className="mb-4 text-lg font-semibold tracking-tight text-foreground">Suggested Reviewers</h2>
        <div className="grid gap-3 sm:grid-cols-2">
          {trendingReviewers.map((r, i) => (
            <Link
              key={i}
              to={`/profile/${r.id}`}
              className="flex items-center gap-3 rounded-2xl border border-border bg-card p-4 transition-shadow hover:shadow-premium"
            >
              <ReviewerAvatar name={r.name} size="md" />
              <div className="flex-1 min-w-0">
                <p className="text-sm font-medium text-foreground truncate">{r.name}</p>
                <p className="text-xs text-muted-foreground">{r.bio}</p>
              </div>
              <span className="text-xs font-mono text-muted-foreground">{r.reviews}</span>
            </Link>
          ))}
        </div>
      </motion.section>

      {/* Featured Lists */}
      <motion.section
        initial={{ opacity: 0, y: 8 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.3, delay: 0.15 }}
      >
        <h2 className="mb-4 text-lg font-semibold tracking-tight text-foreground">Featured Lists</h2>
        <div className="grid gap-4 sm:grid-cols-2">
          {featuredLists.map((list, i) => (
            <OpinionListCard key={i} {...list} />
          ))}
        </div>
      </motion.section>
    </div>
  );
};

export default Discover;
