import { Users } from "lucide-react";
import { motion } from "framer-motion";
import RatingBadge from "@/components/RatingBadge";
import ReviewerAvatar from "@/components/ReviewerAvatar";
import { cn } from "@/lib/utils";
import { Badge } from "@/components/ui/badge";

export interface ReviewCardProps {
  restaurantName: string;
  reviewerName: string;
  reviewerImage?: string;
  timeAgo: string;
  rating: number;
  weightedRating?: number;
  networkCount?: number;
  content: string;
  category?: string;
  className?: string;
}

const ReviewCard = ({
  restaurantName,
  reviewerName,
  reviewerImage,
  timeAgo,
  rating,
  weightedRating,
  networkCount,
  content,
  category,
  className,
}: ReviewCardProps) => {
  return (
    <motion.div
      initial={{ opacity: 0, y: 8 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.3 }}
      className={cn(
        "group relative rounded-2xl border border-border bg-card p-6 transition-shadow hover:shadow-premium",
        className
      )}
    >
      {/* Header */}
      <div className="mb-4 flex items-start justify-between">
        <div className="flex gap-3">
          <ReviewerAvatar name={reviewerName} image={reviewerImage} size="md" />
          <div>
            <div className="flex items-center gap-2">
              <h4 className="font-medium text-card-foreground font-body">{restaurantName}</h4>
              {category && (
                <Badge variant="outline" className="text-[10px] px-1.5 py-0 font-normal text-muted-foreground border-border">
                  {category}
                </Badge>
              )}
            </div>
            <p className="text-xs text-muted-foreground">
              Reviewed by {reviewerName} · {timeAgo}
            </p>
          </div>
        </div>
        <div className="flex flex-col items-end gap-1.5">
          <RatingBadge value={rating} />
          {weightedRating !== undefined && (
            <div className="flex items-center gap-1 text-[11px] text-muted-foreground" title={`Weighted average from ${networkCount ?? 0} network reviews`}>
              <Users className="h-3 w-3" />
              <span className="font-mono font-medium">{weightedRating.toFixed(1)}</span>
              {networkCount !== undefined && (
                <span className="text-muted-foreground/60">({networkCount})</span>
              )}
            </div>
          )}
        </div>
      </div>

      {/* Content */}
      <p className="text-sm leading-relaxed text-card-foreground/80">
        {content}
      </p>
    </motion.div>
  );
};

export default ReviewCard;
