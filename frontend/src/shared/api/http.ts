import { API_BASE_URL } from '@/shared/config/api'

type QueryPrimitive = string | number | boolean | null | undefined
type QueryValue = QueryPrimitive | QueryPrimitive[]

interface RequestOptions extends Omit<RequestInit, 'body' | 'headers'> {
  query?: Record<string, QueryValue>
  body?: unknown
  headers?: HeadersInit
  auth?: boolean
}

export class HttpError extends Error {
  status: number
  payload: unknown

  constructor(message: string, status: number, payload: unknown) {
    super(message)
    this.name = 'HttpError'
    this.status = status
    this.payload = payload
  }
}

let accessTokenProvider: (() => string | null) | null = null

export const setAccessTokenProvider = (provider: () => string | null) => {
  accessTokenProvider = provider
}

const appendQueryValue = (url: URL, key: string, value: QueryPrimitive) => {
  if (value !== undefined && value !== null) {
    url.searchParams.append(key, String(value))
  }
}

const buildUrl = (path: string, query?: Record<string, QueryValue>) => {
  const normalizedBase = API_BASE_URL.endsWith('/')
    ? API_BASE_URL.slice(0, -1)
    : API_BASE_URL
  const normalizedPath = path.startsWith('/') ? path : `/${path}`
  const combined = `${normalizedBase}${normalizedPath}`

  const url = combined.startsWith('http://') || combined.startsWith('https://')
    ? new URL(combined)
    : new URL(combined, globalThis.location?.origin ?? 'http://localhost')

  if (query) {
    for (const [key, value] of Object.entries(query)) {
      if (Array.isArray(value)) {
        for (const item of value) {
          appendQueryValue(url, key, item)
        }
      } else {
        appendQueryValue(url, key, value)
      }
    }
  }

  return url.toString()
}

const parseResponse = async (response: Response): Promise<unknown> => {
  const contentType = response.headers.get('content-type') ?? ''

  if (contentType.includes('application/json')) {
    return response.json()
  }

  if (contentType.includes('text/')) {
    return response.text()
  }

  return null
}

const request = async <T>(path: string, options: RequestOptions = {}): Promise<T> => {
  const {
    query,
    body,
    headers,
    auth = true,
    ...rest
  } = options

  const resolvedHeaders = new Headers(headers)

  if (auth && accessTokenProvider) {
    const token = accessTokenProvider()
    if (token) {
      resolvedHeaders.set('Authorization', `Bearer ${token}`)
    }
  }

  let requestBody: BodyInit | undefined
  if (body !== undefined) {
    if (body instanceof FormData || typeof body === 'string') {
      requestBody = body
    } else {
      resolvedHeaders.set('Content-Type', 'application/json')
      requestBody = JSON.stringify(body)
    }
  }

  const response = await fetch(buildUrl(path, query), {
    ...rest,
    headers: resolvedHeaders,
    body: requestBody,
  })

  const payload = await parseResponse(response)

  if (!response.ok) {
    throw new HttpError(`Request failed with status ${response.status}`, response.status, payload)
  }

  return payload as T
}

export const http = {
  get: <T>(path: string, options?: Omit<RequestOptions, 'method' | 'body'>) =>
    request<T>(path, { ...options, method: 'GET' }),

  post: <T>(path: string, options?: Omit<RequestOptions, 'method'>) =>
    request<T>(path, { ...options, method: 'POST' }),

  patch: <T>(path: string, options?: Omit<RequestOptions, 'method'>) =>
    request<T>(path, { ...options, method: 'PATCH' }),

  delete: <T>(path: string, options?: Omit<RequestOptions, 'method' | 'body'>) =>
    request<T>(path, { ...options, method: 'DELETE' }),
}

