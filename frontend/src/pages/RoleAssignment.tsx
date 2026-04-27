import { motion } from "framer-motion";
import { Shield, ShieldOff } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import UserAvatar from "@/components/UserAvatar";
import {
  useUsersWithRoles,
  useAssignModeratorMutation,
  useRevokeModeratorMutation,
} from "@/lib/server-state/hooks/users";
import type { UserWithRolesDto } from "@/lib/api/users";

const RoleAssignment = () => {
  const { data: users = [], isLoading } = useUsersWithRoles();
  const assignModerator = useAssignModeratorMutation();
  const revokeModerator = useRevokeModeratorMutation();

  const isBusy = assignModerator.isPending || revokeModerator.isPending;

  const handleToggle = (user: UserWithRolesDto) => {
    if (user.isModerator) {
      revokeModerator.mutate(user.id);
    } else {
      assignModerator.mutate(user.id);
    }
  };

  return (
    <div className="mx-auto max-w-4xl px-4 py-8 md:px-8 md:py-12">
      <motion.div
        initial={{ opacity: 0, y: 12 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.4 }}
        className="mb-10"
      >
        <h1 className="mb-2 text-3xl font-semibold tracking-tight text-foreground md:text-4xl">
          User Roles
        </h1>
        <p className="max-w-2xl text-sm text-muted-foreground">
          Assign or remove the moderator role. Moderators can review and action opinions in the moderation queue.
        </p>
      </motion.div>

      {isLoading ? (
        <div className="py-16 text-center text-sm text-muted-foreground">Loading users…</div>
      ) : (
        <motion.div
          initial={{ opacity: 0, y: 8 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.3, delay: 0.1 }}
          className="space-y-3"
        >
          {users.map((user) => (
            <Card key={user.id} className="rounded-2xl border-border">
              <CardContent className="flex items-center gap-4 px-5 py-4 md:px-6">
                <UserAvatar userId={user.id} name={user.name} size="md" />

                <div className="min-w-0 flex-1">
                  <div className="flex flex-wrap items-center gap-2">
                    <p className="text-sm font-medium text-foreground">{user.name}</p>
                    {user.isModerator && (
                      <Badge className="bg-primary/10 text-primary hover:bg-primary/10 text-[10px] px-2">
                        Moderator
                      </Badge>
                    )}
                  </div>
                  <p className="text-xs text-muted-foreground">{user.email}</p>
                </div>

                <Button
                  variant={user.isModerator ? "outline" : "default"}
                  size="sm"
                  className="shrink-0 rounded-xl"
                  disabled={isBusy}
                  onClick={() => handleToggle(user)}
                >
                  {user.isModerator ? (
                    <>
                      <ShieldOff className="h-3.5 w-3.5" />
                      Remove moderator
                    </>
                  ) : (
                    <>
                      <Shield className="h-3.5 w-3.5" />
                      Make moderator
                    </>
                  )}
                </Button>
              </CardContent>
            </Card>
          ))}

          {users.length === 0 && (
            <Card className="rounded-2xl border-dashed border-border">
              <CardContent className="py-14 text-center">
                <p className="text-base font-medium text-foreground">No users found.</p>
              </CardContent>
            </Card>
          )}
        </motion.div>
      )}
    </div>
  );
};

export default RoleAssignment;
