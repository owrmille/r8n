import { useState } from "react";
import { motion } from "framer-motion";
import { ArrowLeft, Camera } from "lucide-react";
import { Link, useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import ReviewerAvatar from "@/components/ReviewerAvatar";
import { toast } from "@/hooks/use-toast";

const EditProfile = () => {
  const navigate = useNavigate();
  const [name, setName] = useState("Jane Doe");
  const [bio, setBio] = useState("Curious eater and honest reviewer. I care about quality, atmosphere, and whether a place lives up to the hype.");
  const [location, setLocation] = useState("Berlin, Germany");

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
              <ReviewerAvatar name={name} size="lg" />
              <button
                type="button"
                className="absolute -bottom-1 -right-1 flex h-8 w-8 items-center justify-center rounded-full border-2 border-background bg-primary text-primary-foreground hover:bg-primary/90 transition-colors"
              >
                <Camera className="h-3.5 w-3.5" />
              </button>
            </div>
            <div>
              <p className="text-sm font-medium text-foreground">Profile photo</p>
              <p className="text-xs text-muted-foreground">Click to upload a new photo.</p>
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
