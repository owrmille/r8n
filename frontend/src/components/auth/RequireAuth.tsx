import { useEffect, useState } from "react";
import { Navigate, Outlet } from "react-router-dom";
import {
  clearSession,
  getAccessToken,
  refreshSession,
} from "@/lib/auth/session";

type AuthStatus = "checking" | "authorized" | "unauthorized";
const IS_E2E_AUTH_BYPASS_ENABLED = import.meta.env.VITE_E2E_BYPASS_AUTH === "true";

const RequireAuth = () => {
  const [status, setStatus] = useState<AuthStatus>("checking");

  useEffect(() => {
    if (IS_E2E_AUTH_BYPASS_ENABLED) {
      setStatus("authorized");
      return;
    }

    if (getAccessToken()) {
      setStatus("authorized");
      return;
    }

    let isCancelled = false;

    void refreshSession()
      .then(() => {
        if (!isCancelled) {
          setStatus("authorized");
        }
      })
      .catch(() => {
        clearSession();

        if (!isCancelled) {
          setStatus("unauthorized");
        }
      });

    return () => {
      isCancelled = true;
    };
  }, []);

  if (status === "authorized") {
    return <Outlet />;
  }

  if (status === "unauthorized") {
    return <Navigate to="/login" replace />;
  }

  return null;
};

export default RequireAuth;
