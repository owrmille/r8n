import { motion } from "framer-motion";
import { MapPin, Download, Settings, Upload } from "lucide-react";
import { useParams, Link } from "react-router-dom";
import { useRef } from "react";
import UserAvatar from "@/components/UserAvatar";
import { Button } from "@/components/ui/button";
import { QueryState } from "@/components/server-state/QueryState";
import { useMe, useUserProfile, useUpdateMyPublicProfileMutation } from "@/lib/server-state/hooks/users";
import { useMyOpinionLists } from "@/lib/server-state/hooks/opinion-lists";
import { useExportStatus, useStartExportMutation, useImportDataMutation } from "@/lib/server-state/hooks/migration";
import { migrationApi } from "@/lib/api/migration";
import { toast } from "sonner";

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

  const updateProfileMutation = useUpdateMyPublicProfileMutation();
  const startExportMutation = useStartExportMutation();
  const importDataMutation = useImportDataMutation();

  const { data: exportStatus } = useExportStatus({
    enabled: isOwnProfile && !!me?.id,
    refetchInterval: (query) => {
      const status = query.state.data?.status;
      return status === "PENDING" || status === "IN_PROGRESS" ? 2000 : false;
    },
  });

  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleImportData = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;

    importDataMutation.mutate(file, {
      onSuccess: () => {
        toast.success("Data imported successfully");
        refetch();
      },
      onSettled: () => {
        if (fileInputRef.current) {
          fileInputRef.current.value = "";
        }
      },
    });
  };

  const handleExportData = async () => {
    if (exportStatus?.status === "COMPLETED") {
      try {
        const blob = await migrationApi.downloadExport();
        const url = URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = `r8n-export-${new Date().toISOString().split("T")[0]}.json`;
        a.click();
        URL.revokeObjectURL(url);
      } catch (err) {
        console.error("Download failed:", err);
        toast.error("Failed to download export");
      }
    } else {
      startExportMutation.mutate(undefined, {
        onSuccess: () => {
          toast.info("Export started. Please wait while we prepare your data.");
        },
      });
    }
  };

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
                      onClick={handleExportData}
                      disabled={
                        startExportMutation.isPending ||
                        exportStatus?.status === "PENDING" ||
                        exportStatus?.status === "IN_PROGRESS"
                      }
                    >
                      <Download className="h-3.5 w-3.5" />
                      {exportStatus?.status === "PENDING" || exportStatus?.status === "IN_PROGRESS"
                        ? "Preparing..."
                        : exportStatus?.status === "COMPLETED"
                        ? "Download data"
                        : "Export my data"}
                    </Button>
                    <input
                      type="file"
                      ref={fileInputRef}
                      onChange={handleImportData}
                      accept=".json"
                      className="hidden"
                    />
                    <Button
                      variant="outline"
                      className={profileActionIconButtonClass}
                      onClick={() => fileInputRef.current?.click()}
                      disabled={importDataMutation.isPending}
                    >
                      <Upload className="h-3.5 w-3.5" />
                      {importDataMutation.isPending ? "Importing..." : "Import my data"}
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
                    <Link key={list.listId} to={`/list/${list.listId}`}>
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
