import { toast } from "@/hooks/use-toast";
import { HttpError } from "@/lib/http-client";

export interface ApiErrorToastOptions {
  description?: string;
  title?: string;
  variant?: "default" | "destructive";
}

export function getApiErrorMessage(
  error: unknown,
  fallback = "Something went wrong. Please try again.",
): string {
  if (error instanceof HttpError) {
    return error.message || fallback;
  }

  if (error instanceof Error && error.message.trim() !== "") {
    return error.message;
  }

  return fallback;
}

export function notifyApiError(
  error: unknown,
  { title, description, variant = "destructive" }: ApiErrorToastOptions = {},
): void {
  const resolvedDescription =
    description ?? getApiErrorMessage(error, "Request failed. Please try again.");

  toast({
    title: title ?? "Request failed",
    description: resolvedDescription,
    variant,
  });
}
