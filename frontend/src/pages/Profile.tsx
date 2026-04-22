import { motion } from "framer-motion";
import { MapPin, Download, Settings } from "lucide-react";
import { useParams, Link } from "react-router-dom";
import UserAvatar from "@/components/UserAvatar";
import { Button } from "@/components/ui/button";
import { QueryState } from "@/components/server-state/QueryState";
import { useMe, useUserProfile } from "@/lib/server-state/hooks/users";
import { useMyOpinionLists } from "@/lib/server-state/hooks/opinion-lists";

const profileActionButtonClass = "inline-flex h-11 items-center justify-center rounded-xl px-4 py-0 leading-none";
const profileActionIconButtonClass = `${profileActionButtonClass} gap-1.5`;

const Profile = () => {
  const { id } = useParams();
  const { data: me, isLoading: isMeLoading, isError: isMeError } = useMe();
  const isOwnProfile = !id || id === me?.id;
  const targetId = isOwnProfile ? me?.id : id;

  const {
    data: profile,
    isLoading: isProfileLoading,
    isError,
    error,
    refetch,
  } = useUserProfile(targetId ?? "");

  const { data: listsPage } = useMyOpinionLists(
    { pageable: { page: 0, size: 50 } },
    { enabled: isOwnProfile && !!me?.id },
  );

  const lists = listsPage?.items ?? [];

  return (
    <div className="mx-auto max-w-4xl px-4 py-8 md:px-8 md:py-12">
      <QueryState
        isLoading={isProfileLoading || (isOwnProfile && isMeLoading)}
        isError={isError || isMeError}
        error={error}
        isEmpty={false}
        onRetry={refetch}
      >
        <>
          <motion.div
            initial={{ opacity: 0, y: 12 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.4 }}
            className="mb-10"
          >
            <div className="flex flex-col sm:flex-row gap-6 items-start">
              <UserAvatar userId={profile?.id} name={profile?.name ?? "?"} size="lg" />
              <div className="flex-1">
                <h1 className="text-2xl md:text-3xl font-semibold tracking-tight text-foreground mb-1">
                  {profile?.name}
                </h1>
                {profile?.about && (
                  <p className="text-sm text-muted-foreground leading-relaxed mb-4 max-w-lg">
                    {profile.about}
                  </p>
                )}

                {profile?.location && (
                  <div className="flex flex-wrap gap-4 text-xs text-muted-foreground mb-4">
                    <span className="flex items-center gap-1">
                      <MapPin className="h-3 w-3" />
                      {profile.location}
                    </span>
                  </div>
                )}

                {isOwnProfile && (
                  <div className="flex flex-wrap gap-3">
                    <Button asChild variant="outline" className={profileActionButtonClass}>
                      <Link to="/profile/edit">
                        Edit Profile
                      </Link>
                    </Button>
                    <Button asChild variant="outline" className={profileActionIconButtonClass}>
                      <Link to="/settings">
                        <Settings className="h-3.5 w-3.5" />
                        Settings
                      </Link>
                    </Button>
                    <Button
                      variant="outline"
                      className={profileActionIconButtonClass}
                      onClick={() => {
                        const data = {
                          profile,
                          exportedAt: new Date().toISOString(),
                        };
                        const blob = new Blob([JSON.stringify(data, null, 2)], { type: "application/json" });
                        const url = URL.createObjectURL(blob);
                        const a = document.createElement("a");
                        a.href = url;
                        a.download = "my-r8n-data.json";
                        a.click();
                        URL.revokeObjectURL(url);
                      }}
                    >
                      <Download className="h-3.5 w-3.5" />
                      Export my data
                    </Button>
                  </div>
                )}
              </div>
            </div>
          </motion.div>

          {isOwnProfile && (
            <motion.section
              initial={{ opacity: 0, y: 8 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.3, delay: 0.15 }}
            >
              <h2 className="mb-4 text-lg font-semibold tracking-tight text-foreground">Your Lists</h2>
              {lists.length === 0 ? (
                <p className="text-sm text-muted-foreground">No lists yet.</p>
              ) : (
                <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
                  {lists.map((list) => (
                    <Link key={list.id} to={`/list/${list.id}`}>
                      <div className="rounded-2xl border border-border bg-card p-4 hover:bg-muted/30 transition-colors">
                        <p className="font-medium text-foreground text-sm">{list.listName}</p>
                      </div>
                    </Link>
                  ))}
                </div>
              )}
            </motion.section>
          )}
        </>
      </QueryState>
    </div>
  );
};

export default Profile;
