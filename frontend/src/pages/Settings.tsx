import { useState } from "react";
import { motion } from "framer-motion";
import { Shield, Eye, Bell, ChevronRight, AlertTriangle } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Switch } from "@/components/ui/switch";
import { Input } from "@/components/ui/input";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { toast } from "@/hooks/use-toast";
import { cn } from "@/lib/utils";
import { usersApi } from "@/lib/api/users";

const TABS = [
  { id: "account", label: "Account & Security", icon: Shield },
  { id: "privacy", label: "Privacy & Visibility", icon: Eye },
  { id: "notifications", label: "Notifications", icon: Bell },
] as const;

type Tab = typeof TABS[number]["id"];

const Settings = () => {
  const [activeTab, setActiveTab] = useState<Tab>("account");

  // Account state
  const [email] = useState("jane@example.com");

  // Privacy state
  const [profileSearchable, setProfileSearchable] = useState(true);
  const [defaultListVisibility, setDefaultListVisibility] = useState<"private" | "searchable">("private");

  // Notification state
  const [notifyAccessRequests, setNotifyAccessRequests] = useState(true);
  const [notifyNewReviews, setNotifyNewReviews] = useState(true);
  const [notifyAccessApproved, setNotifyAccessApproved] = useState(true);

  // Account deletion state
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [deleteEmail, setDeleteEmail] = useState("");
  const [isDeleting, setIsDeleting] = useState(false);

  const handleSave = () => {
    toast({ title: "Settings saved", description: "Your preferences have been updated." });
  };

  const handleAccountDeletion = async () => {
    if (deleteEmail.toLowerCase() !== email.toLowerCase()) {
      toast({
        title: "Email mismatch",
        description: "Please enter your email address exactly as shown.",
        variant: "destructive",
      });
      return;
    }

    setIsDeleting(true);
    try {
      await usersApi.requestAccountDeletion({ email: deleteEmail });
      toast({
        title: "Account deleted",
        description: "Your account and all associated data have been permanently deleted.",
      });
      setDeleteDialogOpen(false);
      setDeleteEmail("");
      // Redirect to home or logout would happen here
    } catch (error) {
      toast({
        title: "Deletion failed",
        description: "There was an error deleting your account. Please try again.",
        variant: "destructive",
      });
    } finally {
      setIsDeleting(false);
    }
  };

  return (
    <div className="mx-auto max-w-3xl px-4 py-8 md:px-8 md:py-12">
      <motion.div
        initial={{ opacity: 0, y: 12 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.4 }}
      >
        <h1 className="text-2xl md:text-3xl font-semibold tracking-tight text-foreground mb-1">Settings</h1>
        <p className="text-sm text-muted-foreground mb-8">Manage your account, privacy, and notifications.</p>

        <div className="flex flex-col md:flex-row gap-8">
          {/* Tab nav */}
          <nav className="flex md:flex-col gap-1 md:w-56 shrink-0">
            {TABS.map(({ id, label, icon: Icon }) => (
              <button
                key={id}
                onClick={() => setActiveTab(id)}
                className={cn(
                  "flex items-center gap-2.5 rounded-xl px-4 py-2.5 text-sm text-left transition-all",
                  activeTab === id
                    ? "bg-primary/5 text-foreground font-medium border border-primary/20"
                    : "text-muted-foreground hover:text-foreground hover:bg-muted/50"
                )}
              >
                <Icon className="h-4 w-4 shrink-0" />
                <span className="flex-1">{label}</span>
                <ChevronRight className={cn("h-3.5 w-3.5 shrink-0 md:block hidden", activeTab === id ? "text-primary" : "text-muted-foreground/30")} />
              </button>
            ))}
          </nav>

          {/* Content */}
          <div className="flex-1 min-w-0">
            {activeTab === "account" && (
              <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="space-y-6">
                <div className="rounded-2xl border border-border bg-card p-5 space-y-4">
                  <h3 className="text-sm font-medium text-foreground">Email address</h3>
                  <p className="text-sm text-muted-foreground">{email}</p>
                </div>

                <div className="rounded-2xl border border-border bg-card p-5 space-y-4">
                  <h3 className="text-sm font-medium text-foreground">Password</h3>
                  <p className="text-xs text-muted-foreground">Change your password to keep your account secure.</p>
                  <Button variant="outline" size="sm" className="rounded-xl">
                    Change password
                  </Button>
                </div>

                <div className="rounded-2xl border border-border bg-card p-5 space-y-4">
                  <h3 className="text-sm font-medium text-foreground">Two-factor authentication</h3>
                  <p className="text-xs text-muted-foreground">Add an extra layer of security to your account.</p>
                  <Button variant="outline" size="sm" className="rounded-xl">
                    Enable 2FA
                  </Button>
                </div>

                <div className="rounded-2xl border border-destructive/20 bg-card p-5 space-y-4">
                  <h3 className="text-sm font-medium text-destructive">Danger zone</h3>
                  <p className="text-xs text-muted-foreground">Permanently delete your account and all associated data.</p>
                  <Dialog open={deleteDialogOpen} onOpenChange={setDeleteDialogOpen}>
                    <DialogTrigger asChild>
                      <Button variant="destructive" size="sm" className="rounded-xl">
                        Delete account
                      </Button>
                    </DialogTrigger>
                    <DialogContent className="rounded-2xl">
                      <DialogHeader>
                        <DialogTitle className="flex items-center gap-2">
                          <AlertTriangle className="h-5 w-5 text-destructive" />
                          Delete Account
                        </DialogTitle>
                        <DialogDescription>
                          This action cannot be undone. This will permanently delete your account and remove all your data from our servers.
                        </DialogDescription>
                      </DialogHeader>
                      <div className="space-y-4 py-4">
                        <div className="space-y-2">
                          <label htmlFor="delete-email" className="text-sm font-medium">
                            Type your email to confirm
                          </label>
                          <Input
                            id="delete-email"
                            type="email"
                            placeholder={email}
                            value={deleteEmail}
                            onChange={(e) => setDeleteEmail(e.target.value)}
                            className="rounded-xl"
                          />
                        </div>
                        <p className="text-xs text-muted-foreground">
                          Your email: <span className="font-medium">{email}</span>
                        </p>
                      </div>
                      <DialogFooter>
                        <Button
                          variant="outline"
                          onClick={() => {
                            setDeleteDialogOpen(false);
                            setDeleteEmail("");
                          }}
                          className="rounded-xl"
                        >
                          Cancel
                        </Button>
                        <Button
                          variant="destructive"
                          onClick={handleAccountDeletion}
                          disabled={isDeleting || !deleteEmail}
                          className="rounded-xl"
                        >
                          {isDeleting ? "Deleting..." : "Delete Account"}
                        </Button>
                      </DialogFooter>
                    </DialogContent>
                  </Dialog>
                </div>
              </motion.div>
            )}

            {activeTab === "privacy" && (
              <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="space-y-6">
                <div className="rounded-2xl border border-border bg-card p-5 space-y-5">
                  <div className="flex items-center justify-between">
                    <div>
                      <h3 className="text-sm font-medium text-foreground">Profile searchable</h3>
                      <p className="text-xs text-muted-foreground mt-0.5">Allow others to find your profile by name.</p>
                    </div>
                    <Switch checked={profileSearchable} onCheckedChange={setProfileSearchable} />
                  </div>

                  <div className="border-t border-border pt-5">
                    <h3 className="text-sm font-medium text-foreground mb-2">Default list visibility</h3>
                    <p className="text-xs text-muted-foreground mb-3">New lists will use this visibility by default.</p>
                    <div className="grid grid-cols-2 gap-3">
                      {([
                        { value: "private" as const, label: "Private", desc: "Only you can see" },
                        { value: "searchable" as const, label: "Searchable", desc: "Others can discover & request access" },
                      ]).map(({ value, label, desc }) => (
                        <button
                          key={value}
                          type="button"
                          onClick={() => setDefaultListVisibility(value)}
                          className={cn(
                            "flex flex-col items-start rounded-xl border p-3 text-left transition-all",
                            defaultListVisibility === value
                              ? "border-primary bg-primary/5 ring-1 ring-primary/20"
                              : "border-border hover:border-primary/30"
                          )}
                        >
                          <p className={cn("text-xs font-medium", defaultListVisibility === value ? "text-foreground" : "text-muted-foreground")}>{label}</p>
                          <p className="text-[10px] text-muted-foreground/70 mt-0.5">{desc}</p>
                        </button>
                      ))}
                    </div>
                  </div>
                </div>

                <Button onClick={handleSave} className="rounded-xl px-8">Save changes</Button>
              </motion.div>
            )}

            {activeTab === "notifications" && (
              <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="space-y-6">
                <div className="rounded-2xl border border-border bg-card p-5 space-y-5">
                  <div className="flex items-center justify-between">
                    <div>
                      <h3 className="text-sm font-medium text-foreground">Access requests</h3>
                      <p className="text-xs text-muted-foreground mt-0.5">When someone requests access to your lists.</p>
                    </div>
                    <Switch checked={notifyAccessRequests} onCheckedChange={setNotifyAccessRequests} />
                  </div>

                  <div className="flex items-center justify-between border-t border-border pt-5">
                    <div>
                      <h3 className="text-sm font-medium text-foreground">New reviews in network</h3>
                      <p className="text-xs text-muted-foreground mt-0.5">When someone in your network posts a new review.</p>
                    </div>
                    <Switch checked={notifyNewReviews} onCheckedChange={setNotifyNewReviews} />
                  </div>

                  <div className="flex items-center justify-between border-t border-border pt-5">
                    <div>
                      <h3 className="text-sm font-medium text-foreground">Access approved</h3>
                      <p className="text-xs text-muted-foreground mt-0.5">When your access request to a list is approved.</p>
                    </div>
                    <Switch checked={notifyAccessApproved} onCheckedChange={setNotifyAccessApproved} />
                  </div>
                </div>

                <Button onClick={handleSave} className="rounded-xl px-8">Save changes</Button>
              </motion.div>
            )}
          </div>
        </div>
      </motion.div>
    </div>
  );
};

export default Settings;
