import { useState } from "react";
import { motion } from "framer-motion";
import { Camera, MapPin } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";

const CreateProfile = () => {
  const [name, setName] = useState("");
  const [bio, setBio] = useState("");
  const [location, setLocation] = useState("");

  return (
    <div className="flex min-h-screen items-center justify-center bg-background px-4">
      <motion.div
        initial={{ opacity: 0, y: 16 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.4 }}
        className="w-full max-w-md"
      >
        <div className="mb-8 text-center">
          <h1 className="font-display text-2xl font-semibold text-foreground">Set up your profile</h1>
          <p className="mt-1 text-sm text-muted-foreground">Tell people a little about yourself.</p>
        </div>

        <div className="rounded-2xl border border-border bg-card p-6">
          {/* Avatar upload */}
          <div className="mb-6 flex justify-center">
            <button className="group relative flex h-20 w-20 items-center justify-center rounded-full bg-secondary transition-colors hover:bg-muted">
              <Camera className="h-6 w-6 text-muted-foreground group-hover:text-foreground transition-colors" />
              <div className="absolute -bottom-1 -right-1 flex h-6 w-6 items-center justify-center rounded-full bg-primary text-primary-foreground text-xs font-bold">
                +
              </div>
            </button>
          </div>

          <form onSubmit={(e) => e.preventDefault()} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="name" className="text-xs">Display name</Label>
              <Input
                id="name"
                placeholder="Jane Doe"
                value={name}
                onChange={(e) => setName(e.target.value)}
                className="rounded-xl"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="bio" className="text-xs">Bio</Label>
              <Textarea
                id="bio"
                placeholder="Curious eater and honest reviewer..."
                value={bio}
                onChange={(e) => setBio(e.target.value)}
                className="rounded-xl resize-none"
                rows={3}
              />
              <p className="text-[10px] text-muted-foreground text-right">{bio.length}/160</p>
            </div>

            <div className="space-y-2">
              <Label htmlFor="location" className="text-xs">Location</Label>
              <div className="relative">
                <MapPin className="absolute left-3 top-1/2 -translate-y-1/2 h-3.5 w-3.5 text-muted-foreground" />
                <Input
                  id="location"
                  placeholder="Berlin, Germany"
                  value={location}
                  onChange={(e) => setLocation(e.target.value)}
                  className="rounded-xl pl-9"
                />
              </div>
            </div>

            <div className="pt-2">
              <Button type="submit" className="w-full rounded-xl">
                Complete profile
              </Button>
              <button
                type="button"
                className="mt-3 w-full text-center text-xs text-muted-foreground hover:text-foreground transition-colors"
              >
                Skip for now
              </button>
            </div>
          </form>
        </div>
      </motion.div>
    </div>
  );
};

export default CreateProfile;
