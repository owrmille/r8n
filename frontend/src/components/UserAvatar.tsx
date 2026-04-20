import ReviewerAvatar from "@/components/ReviewerAvatar";
import { useObjectUrl } from "@/hooks/use-object-url";
import type { Uuid } from "@/lib/api/shared";
import { useUserAvatar } from "@/lib/server-state/hooks/users";

interface UserAvatarProps {
  userId?: Uuid | null;
  name: string;
  size?: "sm" | "md" | "lg";
  className?: string;
}

const UserAvatar = ({ userId, name, size = "md", className }: UserAvatarProps) => {
  const { data: avatar } = useUserAvatar(userId ?? "", {
    enabled: !!userId,
    retry: false,
  });
  const avatarUrl = useObjectUrl(avatar);

  return (
    <ReviewerAvatar
      name={name}
      image={avatarUrl}
      size={size}
      className={className}
    />
  );
};

export default UserAvatar;
