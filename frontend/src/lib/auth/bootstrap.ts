import { authApi } from "@/lib/api/auth";
import { configureSessionRefresh } from "@/lib/auth/session";

configureSessionRefresh(() => authApi.refresh());
