import {
  MutationCache,
  QueryCache,
  QueryClient,
  type QueryClientConfig,
} from "@tanstack/react-query";
import { notifyApiError } from "@/lib/server-state/errors";

export interface ApiErrorMeta {
  errorMessage?: string;
  errorTitle?: string;
  showErrorToast?: boolean;
}

const baseDefaultOptions = {
  queries: {
    refetchOnWindowFocus: false,
    retry: 1,
  },
  mutations: {
    retry: false,
  },
} as const;

function shouldNotifyQuery(meta?: ApiErrorMeta): boolean {
  return Boolean(meta?.showErrorToast);
}

function shouldNotifyMutation(meta?: ApiErrorMeta): boolean {
  return meta?.showErrorToast !== false;
}

export function createQueryClient(config: QueryClientConfig = {}): QueryClient {
  const {
    defaultOptions,
    queryCache: _queryCache,
    mutationCache: _mutationCache,
    ...rest
  } = config;

  const mergedDefaultOptions = {
    ...baseDefaultOptions,
    ...defaultOptions,
    queries: {
      ...baseDefaultOptions.queries,
      ...defaultOptions?.queries,
    },
    mutations: {
      ...baseDefaultOptions.mutations,
      ...defaultOptions?.mutations,
    },
  };

  return new QueryClient({
    ...rest,
    defaultOptions: mergedDefaultOptions,
    queryCache: new QueryCache({
      onError: (error, query) => {
        const meta = query.meta as ApiErrorMeta | undefined;
        if (!shouldNotifyQuery(meta)) {
          return;
        }
        notifyApiError(error, {
          title: meta?.errorTitle,
          description: meta?.errorMessage,
        });
      },
    }),
    mutationCache: new MutationCache({
      onError: (error, _variables, _context, mutation) => {
        const meta = mutation.meta as ApiErrorMeta | undefined;
        if (!shouldNotifyMutation(meta)) {
          return;
        }
        notifyApiError(error, {
          title: meta?.errorTitle,
          description: meta?.errorMessage,
        });
      },
    }),
  });
}
