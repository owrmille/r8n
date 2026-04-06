import { Lock, Clock, Check, XCircle } from "lucide-react";
import { Button } from "@/components/ui/button";
import { motion } from "framer-motion";
import { useState } from "react";
import AccessRequestDialog from "@/components/AccessRequestDialog";

interface AccessRequestButtonProps {
  status?: "none" | "pending" | "approved" | "declined";
  listTitle?: string;
  onRequest?: (choice: { type: "link"; targetListId: string } | { type: "copy" }) => void;
  className?: string;
}

const existingLists = [
  { id: "best-espresso", title: "Best espresso in Berlin" },
  { id: "date-night", title: "Date night restaurants" },
  { id: "cheap-lunch", title: "Cheap lunch under €10" },
  { id: "best-cocktails", title: "Best cocktail bars" },
];

const AccessRequestButton = ({ status = "none", listTitle = "this list", onRequest, className }: AccessRequestButtonProps) => {
  const [currentStatus, setCurrentStatus] = useState(status);
  const [dialogOpen, setDialogOpen] = useState(false);

  const handleSubmit = (choice: { type: "link"; targetListId: string } | { type: "copy" }) => {
    setCurrentStatus("pending");
    onRequest?.(choice);
  };

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
            setDialogOpen(true);
          }}
        >
          <Lock className="mr-1.5 h-3 w-3" />
          Request Access
        </Button>
      </motion.div>
      <AccessRequestDialog
        open={dialogOpen}
        onOpenChange={setDialogOpen}
        listTitle={listTitle}
        existingLists={existingLists}
        onSubmit={handleSubmit}
      />
    </>
  );
};

export default AccessRequestButton;
