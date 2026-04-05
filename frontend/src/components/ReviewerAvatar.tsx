import { cn } from "@/lib/utils";

interface ReviewerAvatarProps {
  name: string;
  image?: string;
  size?: "sm" | "md" | "lg";
  className?: string;
}

const sizeClasses = {
  sm: "h-8 w-8 text-xs",
  md: "h-10 w-10 text-sm",
  lg: "h-14 w-14 text-lg",
};

const ReviewerAvatar = ({ name, image, size = "md", className }: ReviewerAvatarProps) => {
  const initials = name
    .split(" ")
    .map((n) => n[0])
    .join("")
    .slice(0, 2)
    .toUpperCase();

  return (
    <div
      className={cn(
        "relative flex items-center justify-center rounded-full bg-primary/10 text-primary font-medium shrink-0",
        sizeClasses[size],
        className
      )}
    >
      {image ? (
        <img src={image} alt={name} className="h-full w-full rounded-full object-cover" />
      ) : (
        initials
      )}
    </div>
  );
};

export default ReviewerAvatar;
