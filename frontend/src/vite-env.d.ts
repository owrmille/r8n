/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_E2E_BYPASS_AUTH?: string;
  readonly VITE_API_BASE_URL?: string;
  readonly VITE_API_TIMEOUT_MS?: string;
  readonly VITE_AVATAR_MAX_SIZE_BYTES?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
