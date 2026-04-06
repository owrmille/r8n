import { motion } from "framer-motion";
import { MapPin, Globe, UtensilsCrossed } from "lucide-react";
import ReviewCard from "@/components/ReviewCard";

const supplier = {
  name: "Coda Dessert Bar",
  location: "Neukölln, Berlin",
  category: "Fine Dining · Dessert",
  description: "A unique dessert-focused restaurant offering a multi-course tasting experience. One Michelin star.",
};

const reviews = [
  {
    restaurantName: "Coda Dessert Bar",
    reviewerName: "Alex Krüger",
    timeAgo: "2h ago",
    rating: 9.5,
    content: "Extraordinary tasting menu. The black sesame course was revelatory — bitter, sweet, and textural in ways I've never experienced.",
  },
  {
    restaurantName: "Coda Dessert Bar",
    reviewerName: "Sophie Chen",
    timeAgo: "1w ago",
    rating: 8,
    content: "Creative and delicious. Every dish feels like art. The wine pairing adds another dimension to the experience.",
  },
];

const Supplier = () => {
  return (
    <div className="mx-auto max-w-4xl px-4 py-8 md:px-8 md:py-12">
      <motion.div
        initial={{ opacity: 0, y: 12 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.4 }}
        className="mb-10"
      >
        <div className="flex items-start gap-4 mb-4">
          <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-primary/8 shrink-0">
            <UtensilsCrossed className="h-6 w-6 text-primary" />
          </div>
          <div>
            <h1 className="text-2xl md:text-3xl font-semibold tracking-tight text-foreground mb-1">
              {supplier.name}
            </h1>
            <div className="flex flex-wrap gap-3 text-xs text-muted-foreground">
              <span className="flex items-center gap-1">
                <MapPin className="h-3 w-3" />
                {supplier.location}
              </span>
              <span className="flex items-center gap-1">
                <Globe className="h-3 w-3" />
                {supplier.category}
              </span>
            </div>
          </div>
        </div>
        <p className="text-sm text-muted-foreground leading-relaxed max-w-lg">{supplier.description}</p>

        <div className="mt-6 rounded-xl bg-secondary/50 px-4 py-3">
          <p className="text-xs text-muted-foreground">
            <span className="font-mono font-semibold text-foreground">2</span> reviews from your network
          </p>
        </div>
      </motion.div>

      <motion.section
        initial={{ opacity: 0, y: 8 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.3, delay: 0.15 }}
      >
        <h2 className="mb-4 text-lg font-semibold tracking-tight text-foreground">Reviews from your network</h2>
        <div className="space-y-4">
          {reviews.map((r, i) => (
            <ReviewCard key={i} {...r} />
          ))}
        </div>
      </motion.section>
    </div>
  );
};

export default Supplier;
