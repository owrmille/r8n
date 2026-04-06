import { cn } from "@/lib/utils";

interface RatingBadgeProps {
  value: number;
  locked?: boolean;
  className?: string;
}

const RatingBadge = ({ value, locked = false, className }: RatingBadgeProps) => {
  const getBadgeColor = (val: number) => {
    if (val >= 8) return "bg-primary/10 text-primary border-primary/20";
    if (val >= 6) return "bg-accent/10 text-accent border-accent/20";
    return "bg-muted text-muted-foreground border-border";
  };

  return (
    <div
      className={cn(
        "inline-flex items-center justify-center rounded-lg border px-2.5 py-1 font-mono text-sm font-semibold tabular-nums",
        locked ? "bg-muted text-muted-foreground/40 border-border" : getBadgeColor(value),
        className
      )}
    >
      {locked ? "•••" : `${value}/10`}
    </div>
  );
};

export default RatingBadge;
