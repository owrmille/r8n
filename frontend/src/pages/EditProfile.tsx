import { useEffect, useRef, useState } from "react";
import { motion } from "framer-motion";
import { ArrowLeft, CloudUpload, Loader2, Trash2 } from "lucide-react";
import { Link, useNavigate } from "react-router-dom";
import UserAvatar from "@/components/UserAvatar";
import { Button } from "@/components/ui/button";
import { toast } from "@/hooks/use-toast";
import {
  useDeleteMyAvatarMutation,
  useMe,
  useUploadMyAvatarMutation,
  useUserAvatar,
} from "@/lib/server-state/hooks/users";

const MAX_AVATAR_SIZE_BYTES = readPositiveIntegerEnv(
  "VITE_AVATAR_MAX_SIZE_BYTES",
  import.meta.env.VITE_AVATAR_MAX_SIZE_BYTES,
);
const MAX_AVATAR_SIZE_LABEL = formatFileSize(MAX_AVATAR_SIZE_BYTES);
const ALLOWED_AVATAR_TYPES = new Set([
  "image/jpeg",
  "image/png",
  "image/webp",
]);

function readPositiveIntegerEnv(name: string, value: string | undefined): number {
  const parsedValue = Number(value);

  if (!Number.isInteger(parsedValue) || parsedValue <= 0) {
    throw new Error(`${name} must be a positive integer.`);
  }

  return parsedValue;
}

function formatFileSize(bytes: number): string {
  const megabytes = bytes / (1024 * 1024);
  if (Number.isInteger(megabytes)) {
    return `${megabytes}MB`;
  }

  const kilobytes = bytes / 1024;
  if (Number.isInteger(kilobytes)) {
    return `${kilobytes}KB`;
  }

  return `${bytes}B`;
}

function validateAvatarFile(file: File): string | undefined {
  if (!ALLOWED_AVATAR_TYPES.has(file.type)) {
    return "Please choose a PNG, JPEG, or WebP image.";
  }

  if (file.size > MAX_AVATAR_SIZE_BYTES) {
    return `Profile image must be ${MAX_AVATAR_SIZE_LABEL} or smaller.`;
  }

  return undefined;
}

const EditProfile = () => {
  const navigate = useNavigate();
  const fileInputRef = useRef<HTMLInputElement>(null);
  const isNameInitializedRef = useRef(false);
  const [name, setName] = useState("");
  const [bio, setBio] = useState("Curious eater and honest reviewer. I care about quality, atmosphere, and whether a place lives up to the hype.");
  const [location, setLocation] = useState("Berlin, Germany");
  const { data: me } = useMe();
  const { data: avatar } = useUserAvatar(me?.id ?? "", {
    enabled: !!me?.id,
    retry: false,
  });
  const uploadAvatarMutation = useUploadMyAvatarMutation({
    onSuccess: () => {
      toast({
        title: "Profile image updated",
        description: "Your new image has been saved.",
      });
    },
  });
  const deleteAvatarMutation = useDeleteMyAvatarMutation({
    onSuccess: () => {
      toast({
        title: "Profile image removed",
        description: "Your profile now shows initials again.",
      });
    },
  });
  const isAvatarMutating =
    uploadAvatarMutation.isPending || deleteAvatarMutation.isPending;
  const displayName = name.trim() || me?.name || "?";

  useEffect(() => {
    if (!me?.name || isNameInitializedRef.current) {
      return;
    }

    setName(me.name);
    isNameInitializedRef.current = true;
  }, [me?.name]);

  const handleAvatarChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.currentTarget.files?.[0];
    event.currentTarget.value = "";

    if (!file) {
      return;
    }

    const validationError = validateAvatarFile(file);
    if (validationError) {
      toast({
        title: "Image not uploaded",
        description: validationError,
      });
      return;
    }

    uploadAvatarMutation.mutate({ file });
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) {
      toast({ title: "Name required", description: "Please enter your display name." });
      return;
    }
    toast({ title: "Profile updated", description: "Your changes have been saved." });
    navigate("/profile");
  };

  return (
    <div className="mx-auto max-w-2xl px-4 py-8 md:px-8 md:py-12">
      <Link
        to="/profile"
        className="mb-6 inline-flex items-center gap-1.5 text-xs text-muted-foreground hover:text-foreground transition-colors"
      >
        <ArrowLeft className="h-3 w-3" />
        Back to profile
      </Link>

      <motion.div
        initial={{ opacity: 0, y: 12 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.4 }}
      >
        <h1 className="text-2xl md:text-3xl font-semibold tracking-tight text-foreground mb-1">Edit Profile</h1>
        <p className="text-sm text-muted-foreground mb-8">Update your public profile information.</p>

        <form onSubmit={handleSubmit} className="space-y-8">
          {/* Avatar */}
          <div className="flex items-center gap-5">
            <div className="relative">
              <UserAvatar userId={me?.id} name={displayName} size="lg" />
              <input
                ref={fileInputRef}
                type="file"
                accept="image/png,image/jpeg,image/webp"
                className="hidden"
                onChange={handleAvatarChange}
              />
              <button
                type="button"
                aria-label="Upload profile image"
                disabled={!me?.id || isAvatarMutating}
                onClick={() => fileInputRef.current?.click()}
                className="absolute -bottom-1 -right-1 flex h-8 w-8 items-center justify-center rounded-full border-2 border-background bg-primary text-primary-foreground hover:bg-primary/90 transition-colors"
              >
                {uploadAvatarMutation.isPending ? (
                  <Loader2 className="h-3.5 w-3.5 animate-spin" />
                ) : (
                  <CloudUpload className="h-3.5 w-3.5" />
                )}
              </button>
            </div>
            <div>
              <p className="text-sm font-medium text-foreground">Profile image</p>
              <p className="text-xs text-muted-foreground">
                PNG, JPEG, or WebP. Max {MAX_AVATAR_SIZE_LABEL}.
              </p>
              {avatar && (
                <button
                  type="button"
                  disabled={isAvatarMutating}
                  onClick={() => deleteAvatarMutation.mutate()}
                  className="mt-2 inline-flex items-center gap-1.5 text-xs font-medium text-destructive hover:text-destructive/80 disabled:cursor-not-allowed disabled:opacity-60"
                >
                  {deleteAvatarMutation.isPending ? (
                    <Loader2 className="h-3 w-3 animate-spin" />
                  ) : (
                    <Trash2 className="h-3 w-3" />
                  )}
                  Remove image
                </button>
              )}
            </div>
          </div>

          {/* Name */}
          <div>
            <label className="mb-1.5 block text-sm font-medium text-foreground">Display name</label>
            <input
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="Your name"
              className="w-full rounded-xl border border-border bg-card px-4 py-3 text-sm text-foreground placeholder:text-muted-foreground/60 focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary/40 transition-all"
            />
          </div>

          {/* Bio */}
          <div>
            <label className="mb-1.5 block text-sm font-medium text-foreground">Bio</label>
            <textarea
              value={bio}
              onChange={(e) => setBio(e.target.value)}
              placeholder="Tell others what you care about in your reviews..."
              rows={3}
              className="w-full rounded-xl border border-border bg-card px-4 py-3 text-sm text-foreground placeholder:text-muted-foreground/60 focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary/40 transition-all resize-none leading-relaxed"
            />
            <p className="mt-1 text-[11px] text-muted-foreground/60">{bio.length}/200 characters</p>
          </div>

          {/* Location */}
          <div>
            <label className="mb-1.5 block text-sm font-medium text-foreground">Location</label>
            <input
              type="text"
              value={location}
              onChange={(e) => setLocation(e.target.value)}
              placeholder="e.g., Berlin, Germany"
              className="w-full rounded-xl border border-border bg-card px-4 py-3 text-sm text-foreground placeholder:text-muted-foreground/60 focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary/40 transition-all"
            />
          </div>

          {/* Submit */}
          <div className="flex gap-3 pt-2">
            <Button type="submit" className="rounded-xl px-8">
              Save Profile
            </Button>
            <Button type="button" variant="ghost" onClick={() => navigate("/profile")} className="rounded-xl">
              Cancel
            </Button>
          </div>
        </form>
      </motion.div>
    </div>
  );
};

export default EditProfile;
