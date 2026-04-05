import { useState } from "react";
import { Link2, Copy, List } from "lucide-react";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";

interface AccessRequestDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  listTitle: string;
  existingLists: { id: string; title: string }[];
  onSubmit: (choice: { type: "link"; targetListId: string } | { type: "copy" }) => void;
}

const AccessRequestDialog = ({
  open,
  onOpenChange,
  listTitle,
  existingLists,
  onSubmit,
}: AccessRequestDialogProps) => {
  const [mode, setMode] = useState<"choose" | "select-list">("choose");
  const [selectedListId, setSelectedListId] = useState<string | null>(null);

  const handleClose = () => {
    setMode("choose");
    setSelectedListId(null);
    onOpenChange(false);
  };

  const handleLink = () => {
    setMode("select-list");
  };

  const handleCopy = () => {
    onSubmit({ type: "copy" });
    handleClose();
  };

  const handleConfirmLink = () => {
    if (selectedListId) {
      onSubmit({ type: "link", targetListId: selectedListId });
      handleClose();
    }
  };

  return (
    <Dialog open={open} onOpenChange={handleClose}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle className="text-lg">Request Access</DialogTitle>
          <DialogDescription className="text-sm">
            How would you like to add{" "}
            <span className="font-medium text-foreground">{listTitle}</span> to your lists?
          </DialogDescription>
        </DialogHeader>

        {mode === "choose" ? (
          <div className="grid gap-3 pt-2">
            <button
              onClick={handleLink}
              className="flex items-start gap-4 rounded-xl border border-border p-4 text-left transition-colors hover:bg-secondary/50"
            >
              <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-primary/8">
                <Link2 className="h-4 w-4 text-primary" />
              </div>
              <div>
                <p className="text-sm font-medium text-foreground">Merge into existing list</p>
                <p className="text-xs text-muted-foreground mt-0.5">
                  Items and ratings will be added to one of your lists. Their ratings appear as network ratings.
                </p>
              </div>
            </button>

            <button
              onClick={handleCopy}
              className="flex items-start gap-4 rounded-xl border border-border p-4 text-left transition-colors hover:bg-secondary/50"
            >
              <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-primary/8">
                <Copy className="h-4 w-4 text-primary" />
              </div>
              <div>
                <p className="text-sm font-medium text-foreground">Create a copy</p>
                <p className="text-xs text-muted-foreground mt-0.5">
                  A new list will be created with all items. Your ratings start as – and theirs become network ratings.
                </p>
              </div>
            </button>
          </div>
        ) : (
          <div className="pt-2">
            <p className="text-xs text-muted-foreground mb-3">Select a list to merge into:</p>
            <div className="space-y-2 max-h-60 overflow-y-auto">
              {existingLists.map((list) => (
                <button
                  key={list.id}
                  onClick={() => setSelectedListId(list.id)}
                  className={cn(
                    "flex w-full items-center gap-3 rounded-xl border p-3 text-left transition-colors",
                    selectedListId === list.id
                      ? "border-primary bg-primary/5"
                      : "border-border hover:bg-secondary/50"
                  )}
                >
                  <div className="flex h-7 w-7 shrink-0 items-center justify-center rounded-md bg-primary/8">
                    <List className="h-3.5 w-3.5 text-primary" />
                  </div>
                  <span className="text-sm font-medium text-foreground truncate">{list.title}</span>
                </button>
              ))}
            </div>
            <div className="flex gap-2 mt-4">
              <Button variant="outline" size="sm" className="flex-1" onClick={() => setMode("choose")}>
                Back
              </Button>
              <Button size="sm" className="flex-1" disabled={!selectedListId} onClick={handleConfirmLink}>
                Request & Merge
              </Button>
            </div>
          </div>
        )}
      </DialogContent>
    </Dialog>
  );
};

export default AccessRequestDialog;
