import { useEffect, useState } from "react";

export function useObjectUrl(blob: Blob | null | undefined): string | undefined {
  const [objectUrl, setObjectUrl] = useState<string>();

  useEffect(() => {
    if (!blob) {
      setObjectUrl(undefined);
      return;
    }

    const nextObjectUrl = URL.createObjectURL(blob);
    setObjectUrl(nextObjectUrl);

    return () => {
      URL.revokeObjectURL(nextObjectUrl);
    };
  }, [blob]);

  return objectUrl;
}
