import { useState } from "react";
import { motion } from "framer-motion";
import { Shield, ShieldOff, ShieldCheck } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import UserAvatar from "@/components/UserAvatar";
import {
  useUsersWithRoles,
  useAssignModeratorMutation,
  useRevokeModeratorMutation,
  useAssignAdminMutation,
  useRevokeAdminMutation,
} from "@/lib/server-state/hooks/users";
import type { UserWithRolesDto } from "@/lib/api/users";

const RoleAssignment = () => {
  const { data: users = [], isLoading, isError } = useUsersWithRoles();
  const [pendingUserId, setPendingUserId] = useState<string | null>(null);

  const assignModerator = useAssignModeratorMutation({ onSettled: () => setPendingUserId(null) });
  const revokeModerator = useRevokeModeratorMutation({ onSettled: () => setPendingUserId(null) });
  const assignAdmin = useAssignAdminMutation({ onSettled: () => setPendingUserId(null) });
  const revokeAdmin = useRevokeAdminMutation({ onSettled: () => setPendingUserId(null) });

  const handleModeratorToggle = (user: UserWithRolesDto) => {
    setPendingUserId(user.id);
    if (user.isModerator) {
      revokeModerator.mutate(user.id);
    } else {
      assignModerator.mutate(user.id);
    }
  };

  const handleAdminToggle = (user: UserWithRolesDto) => {
    setPendingUserId(user.id);
    if (user.isAdmin) {
      revokeAdmin.mutate(user.id);
    } else {
      assignAdmin.mutate(user.id);
    }
  };

  return (
    <div className="mx-auto max-w-4xl px-4 py-8 md:px-8 md:py-12">
      <motion.div
        initial={{ opacity: 0, y: 12 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.4 }}
        className="mb-8"
      >
        <h1 className="mb-2 text-3xl font-semibold tracking-tight text-foreground md:text-4xl">
          User Roles
        </h1>
        <p className="max-w-2xl text-sm text-muted-foreground">
          Assign or remove roles for users. Roles are inherited. Admin includes all Moderator permissions, and Moderator includes all regular User permissions.
        </p>
      </motion.div>

      {/* Role inheritance explanation */}
      <motion.div
        initial={{ opacity: 0, y: 8 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.3, delay: 0.05 }}
        className="mb-8 grid grid-cols-1 gap-3 sm:grid-cols-3"
      >
        {[
          {
            title: "User",
            description: "Can write reviews, manage their own lists, and send access requests.",
            color: "bg-muted/50 border-border",
          },
          {
            title: "Moderator",
            description: "Inherits User. Can also review and action opinions in the moderation queue.",
            color: "bg-primary/5 border-primary/20",
          },
          {
            title: "Admin",
            description: "Inherits Moderator. Can also assign roles and manage platform settings.",
            color: "bg-amber-500/5 border-amber-500/20",
          },
        ].map((role) => (
          <div key={role.title} className={`rounded-xl border p-4 ${role.color}`}>
            <p className="mb-1 text-sm font-semibold text-foreground">{role.title}</p>
            <p className="text-xs text-muted-foreground">{role.description}</p>
          </div>
        ))}
      </motion.div>

      {isLoading ? (
        <div className="py-16 text-center text-sm text-muted-foreground">Loading users…</div>
      ) : isError ? (
        <div className="py-16 text-center text-sm text-destructive">
          Failed to load users. You may not have permission to view this page.
        </div>
      ) : (
        <motion.div
          initial={{ opacity: 0, y: 8 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.3, delay: 0.1 }}
          className="space-y-3"
        >
          {users.map((user) => (
            <Card key={user.id} className="rounded-2xl border-border">
              <CardContent className="flex flex-wrap items-center gap-4 px-5 py-4 md:px-6">
                <UserAvatar userId={user.id} name={user.name} size="md" />

                <div className="min-w-0 flex-1">
                  <div className="flex flex-wrap items-center gap-2">
                    <p className="text-sm font-medium text-foreground">{user.name}</p>
                    {user.isAdmin && (
                      <Badge className="bg-amber-500/10 text-amber-600 hover:bg-amber-500/10 text-[10px] px-2">
                        Admin
                      </Badge>
                    )}
                    {user.isModerator && !user.isAdmin && (
                      <Badge className="bg-primary/10 text-primary hover:bg-primary/10 text-[10px] px-2">
                        Moderator
                      </Badge>
                    )}
                  </div>
                  <p className="text-xs text-muted-foreground">{user.email}</p>
                </div>

                <div className="flex shrink-0 gap-2">
                  <Button
                    variant={user.isModerator ? "outline" : "default"}
                    size="sm"
                    className="rounded-xl"
                    disabled={pendingUserId === user.id || user.isAdmin}
                    title={user.isAdmin ? "Admins already have Moderator permissions" : undefined}
                    onClick={() => handleModeratorToggle(user)}
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

                  <Button
                    variant={user.isAdmin ? "outline" : "secondary"}
                    size="sm"
                    className="rounded-xl"
                    disabled={pendingUserId === user.id}
                    onClick={() => handleAdminToggle(user)}
                  >
                    {user.isAdmin ? (
                      <>
                        <ShieldOff className="h-3.5 w-3.5" />
                        Remove admin
                      </>
                    ) : (
                      <>
                        <ShieldCheck className="h-3.5 w-3.5" />
                        Make admin
                      </>
                    )}
                  </Button>
                </div>
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
