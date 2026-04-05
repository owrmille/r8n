import { Plus } from "lucide-react";
import { motion } from "framer-motion";
import { Link } from "react-router-dom";
import OpinionListCard from "@/components/OpinionListCard";

const mockLists = [
  {
    id: "best-espresso",
    title: "Best espresso in Berlin",
    description: "A curated guide to the city's finest espresso — from specialty roasters to hidden neighbourhood gems.",
    reviewCount: 12,
    authorName: "Jane Doe",
    hasAccess: true,
  },
  {
    id: "date-night",
    title: "Date night restaurants",
    description: "Intimate, thoughtfully designed spaces where the food matches the ambiance.",
    reviewCount: 8,
    authorName: "Jane Doe",
    hasAccess: true,
  },
  {
    id: "cheap-lunch",
    title: "Cheap lunch under €10",
    description: "Great food doesn't need to be expensive. My favorite affordable lunch spots across Mitte and Kreuzberg.",
    reviewCount: 15,
    authorName: "Jane Doe",
    hasAccess: true,
  },
  {
    id: "best-cocktails",
    title: "Best cocktail bars",
    description: "From speakeasies to rooftop bars — the best places to get a well-crafted drink in the city.",
    reviewCount: 6,
    authorName: "Jane Doe",
    hasAccess: true,
  },
];

const MyLists = () => {
  return (
    <div className="mx-auto max-w-4xl px-4 py-8 md:px-8 md:py-12">
      <motion.div
        initial={{ opacity: 0, y: 12 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.4 }}
        className="mb-10"
      >
        <h1 className="font-body text-3xl md:text-4xl font-semibold text-foreground mb-2 tracking-tight">
          My Lists
        </h1>
        <p className="text-muted-foreground text-sm">
          {mockLists.length} lists
        </p>
      </motion.div>

      {/* My Lists */}
      <motion.section
        initial={{ opacity: 0, y: 8 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.3, delay: 0.1 }}
        className="mb-12"
      >
        <h2 className="mb-4 text-lg font-semibold tracking-tight text-foreground">Created by you</h2>
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {mockLists.map((list) => (
            <Link key={list.id} to={`/list/${list.id}`}>
              <OpinionListCard {...list} />
            </Link>
          ))}
          <motion.div
            whileTap={{ scale: 0.98 }}
            className="flex cursor-pointer items-center justify-center rounded-2xl border-2 border-dashed border-border p-5 text-muted-foreground transition-colors hover:border-primary/30 hover:text-primary min-h-[140px]"
          >
            <Link to="/lists/create" className="flex items-center gap-2 text-sm font-medium">
              <Plus className="h-4 w-4" />
              New List
            </Link>
          </motion.div>
        </div>
      </motion.section>

    </div>
  );
};

export default MyLists;
