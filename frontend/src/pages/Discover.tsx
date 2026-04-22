import { useState } from "react";
import { motion } from "framer-motion";
import { Search } from "lucide-react";
import OpinionListCard from "@/components/OpinionListCard";
import UserAvatar from "@/components/UserAvatar";
import { QueryState } from "@/components/server-state/QueryState";
import { Link } from "react-router-dom";
import { useSearchOpinionLists } from "@/lib/server-state/hooks/opinion-lists";

// Trending reviewers: no user search endpoint yet
const trendingReviewers = [
  { id: "alex-kruger", name: "Alex Krüger", reviews: 47, bio: "Berlin food writer" },
  { id: "mia-svensson", name: "Mia Svensson", reviews: 31, bio: "Nordic cuisine enthusiast" },
  { id: "tobias-richter", name: "Tobias Richter", reviews: 22, bio: "Weekend explorer" },
  { id: "sophie-chen", name: "Sophie Chen", reviews: 19, bio: "Coffee & dessert focused" },
];

const Discover = () => {
  const [query, setQuery] = useState("");

  const { data, isLoading, isError, error, refetch } = useSearchOpinionLists({
    filters: query.length >= 2 ? { nameSubstring: query } : undefined,
    pageable: { page: 0, size: 20 },
  });

  const lists = data?.items ?? [];

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
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="Search lists by name..."
          className="w-full rounded-xl border border-border bg-card py-3 pl-11 pr-4 text-sm text-foreground placeholder:text-muted-foreground/60 focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary/40 transition-all"
        />
      </div>

      {/* Suggested Reviewers — no user search endpoint yet */}
      {!query && (
        <motion.section
          initial={{ opacity: 0, y: 8 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.3, delay: 0.1 }}
          className="mb-12"
        >
          <h2 className="mb-4 text-lg font-semibold tracking-tight text-foreground">Suggested Reviewers</h2>
          <div className="grid gap-3 sm:grid-cols-2">
            {trendingReviewers.map((r) => (
              <Link
                key={r.id}
                to={`/profile/${r.id}`}
                className="flex items-center gap-3 rounded-2xl border border-border bg-card p-4 transition-shadow hover:shadow-premium"
              >
                <UserAvatar name={r.name} size="md" />
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium text-foreground truncate">{r.name}</p>
                  <p className="text-xs text-muted-foreground">{r.bio}</p>
                </div>
                <span className="text-xs font-mono text-muted-foreground">{r.reviews}</span>
              </Link>
            ))}
          </div>
        </motion.section>
      )}

      {/* Lists */}
      <motion.section
        initial={{ opacity: 0, y: 8 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.3, delay: 0.15 }}
      >
        <h2 className="mb-4 text-lg font-semibold tracking-tight text-foreground">
          {query.length >= 2 ? `Results for "${query}"` : "Lists"}
        </h2>

        <QueryState
          isLoading={isLoading}
          isError={isError}
          error={error}
          isEmpty={lists.length === 0}
          emptyMessage={query.length >= 2 ? "No lists match your search." : "No lists found."}
          onRetry={refetch}
        >
          <div className="grid gap-4 sm:grid-cols-2">
            {lists.map((list) => (
              <Link key={list.listId} to={`/list/${list.listId}`}>
                <OpinionListCard
                  title={list.listName}
                  description=""
                  reviewCount={list.opinionsCount}
                  authorId={list.owner}
                  authorName={list.ownerName}
                  hasAccess={false}
                />
              </Link>
            ))}
          </div>
        </QueryState>
      </motion.section>
    </div>
  );
};

export default Discover;
