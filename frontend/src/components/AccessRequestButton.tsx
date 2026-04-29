import { Lock, Clock, Check, XCircle } from "lucide-react";
import { motion } from "framer-motion";
import { useState } from "react";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { useCreateOutgoingAccessRequestMutation } from "@/lib/server-state/hooks/access-requests";
import type { Uuid } from "@/lib/api/shared";

interface AccessRequestButtonProps {
  listId: Uuid;
  status?: "none" | "pending" | "approved" | "declined";
  listTitle?: string;
  className?: string;
}

const AccessRequestButton = ({
  listId,
  status = "none",
  listTitle = "this list",
  className,
}: AccessRequestButtonProps) => {
  const [currentStatus, setCurrentStatus] = useState(status);
  const [confirmOpen, setConfirmOpen] = useState(false);
  const createRequest = useCreateOutgoingAccessRequestMutation({
    onSuccess: () => {
      setCurrentStatus("pending");
      setConfirmOpen(false);
    },
  });

  if (currentStatus === "approved") {
    return (
      <Button variant="ghost" size="sm" className={className} disabled>
        <Check className="mr-1.5 h-3 w-3" />
        Access Granted
      </Button>
    );
  }

  if (currentStatus === "declined") {
    return (
      <Button variant="ghost" size="sm" className={className} disabled>
        <XCircle className="mr-1.5 h-3 w-3 text-destructive" />
        <span className="text-destructive">Declined</span>
      </Button>
    );
  }

  if (currentStatus === "pending") {
    return (
      <motion.div initial={{ scale: 0.95 }} animate={{ scale: 1 }}>
        <Button variant="secondary" size="sm" className={className} disabled>
          <Clock className="mr-1.5 h-3 w-3" />
          Request Sent
        </Button>
      </motion.div>
    );
  }

  return (
    <>
      <motion.div whileTap={{ scale: 0.98 }}>
        <Button
          variant="access"
          size="sm"
          className={className}
          onClick={(e) => {
            e.preventDefault();
            e.stopPropagation();
            setConfirmOpen(true);
          }}
        >
          <Lock className="mr-1.5 h-3 w-3" />
          Request Access
        </Button>
      </motion.div>
      <Dialog open={confirmOpen} onOpenChange={(v) => { if (!v) setConfirmOpen(false); }}>
        <DialogContent
          className="sm:max-w-sm"
          onClick={(e) => e.stopPropagation()}
        >
          <DialogHeader>
            <DialogTitle>Request access?</DialogTitle>
            <DialogDescription>
              The owner of <span className="font-medium text-foreground">{listTitle}</span> will be asked to accept or decline. You'll see the result on your Requests page.
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button
              variant="ghost"
              size="sm"
              onClick={(e) => { e.stopPropagation(); setConfirmOpen(false); }}
              disabled={createRequest.isPending}
            >
              Cancel
            </Button>
            <Button
              size="sm"
              onClick={(e) => { e.stopPropagation(); createRequest.mutate({ listId }); }}
              disabled={createRequest.isPending}
            >
              Send request
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
};

export default AccessRequestButton;
