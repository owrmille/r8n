import { beforeEach, describe, expect, it, vi } from "vitest";
import {
  clearSession,
  configureSessionRefresh,
  getSession,
  setSession,
} from "@/lib/auth/session";
import { createHttpClient, HttpError } from "@/lib/http-client";

describe("httpClient", () => {
  beforeEach(() => {
    clearSession();
    configureSessionRefresh(null);
  });

  it("builds requests against the configured base url", async () => {
    const fetchMock = vi.fn().mockResolvedValue(
      new Response(JSON.stringify({ ok: true }), {
        headers: { "Content-Type": "application/json" },
        status: 200,
      }),
    );
    const client = createHttpClient({
      baseUrl: "/api",
      fetchFn: fetchMock,
    });

    await client.get<{ ok: boolean }>("/api/opinions", {
      query: { page: 2, tags: ["safe", "public"], empty: undefined },
    });

    expect(fetchMock).toHaveBeenCalledWith(
      "/api/opinions?page=2&tags=safe&tags=public",
      expect.objectContaining({
        credentials: "same-origin",
        method: "GET",
      }),
    );
  });

  it("serializes object bodies as json and keeps default headers", async () => {
    const fetchMock = vi.fn().mockResolvedValue(
      new Response(JSON.stringify({ id: "1" }), {
        headers: { "Content-Type": "application/json" },
        status: 200,
      }),
    );
    const client = createHttpClient({
      baseUrl: "/api",
      fetchFn: fetchMock,
    });

    await client.post("/api/opinions", {
      body: { title: "Readable review" },
      headers: { "X-Trace-Id": "trace-id" },
    });

    expect(fetchMock).toHaveBeenCalledWith(
      "/api/opinions",
      expect.objectContaining({
        body: JSON.stringify({ title: "Readable review" }),
        method: "POST",
      }),
    );

    const [, requestInit] = fetchMock.mock.calls[0];
    const headers = new Headers(requestInit.headers);

    expect(headers.get("Accept")).toBe("application/json");
    expect(headers.get("Content-Type")).toBe("application/json");
    expect(headers.get("X-Trace-Id")).toBe("trace-id");
  });

  it("rejects absolute request paths to avoid sending data to unexpected hosts", async () => {
    const client = createHttpClient({
      baseUrl: "/api",
      fetchFn: vi.fn(),
    });

    await expect(client.get("https://evil.example/api")).rejects.toThrow(
      "Absolute request URLs are not allowed",
    );
    await expect(client.get("opinions")).rejects.toThrow(
      "HTTP client paths must start with '/'",
    );
    await expect(client.get("//evil.example/api")).rejects.toThrow(
      "Absolute request URLs are not allowed",
    );
  });

  it("throws HttpError with a safe server message on unsuccessful responses", async () => {
    const fetchMock = vi.fn().mockResolvedValue(
      new Response(JSON.stringify({ message: "Validation failed" }), {
        headers: { "Content-Type": "application/json" },
        status: 400,
      }),
    );
    const client = createHttpClient({
      baseUrl: "/api",
      fetchFn: fetchMock,
    });

    await expect(client.get("/api/opinions")).rejects.toEqual(
      expect.objectContaining({
        message: "Validation failed",
        name: "HttpError",
        responseBody: expect.anything(),
        status: 400,
      }),
    );
  });

  it("times out long requests", async () => {
    vi.useFakeTimers();

    let capturedSignal: AbortSignal | null = null;
    const fetchMock = vi.fn(
      (_input: RequestInfo | URL, requestInit?: RequestInit) =>
        new Promise<Response>((resolve, reject) => {
          capturedSignal = requestInit?.signal ?? null;

          if (!capturedSignal) {
            reject(new Error("Expected request signal to be defined."));
            return;
          }

          const abortHandler = (signal: AbortSignal) => {
            signal.addEventListener("abort", () => {
              reject(
                signal.reason ??
                  new DOMException("The operation was aborted.", "AbortError"),
              );
            });
          };

          abortHandler(capturedSignal);

          setTimeout(() => {
            resolve(
              new Response(JSON.stringify({ ok: true }), {
                headers: { "Content-Type": "application/json" },
                status: 200,
              }),
            );
          }, 1000);
        }),
    );

    const client = createHttpClient({
      baseUrl: "/api",
      defaultTimeoutMs: 50,
      fetchFn: fetchMock,
    });

    const request = client.get("/api/opinions");
    const expectation = expect(request).rejects.toEqual(
      expect.objectContaining({
        message: "Request timed out after 50 ms.",
        name: "HttpError",
        status: 0,
      }),
    );

    await vi.advanceTimersByTimeAsync(60);
    await expectation;

    vi.useRealTimers();
  });

  it("adds bearer authorization for authenticated requests", async () => {
    const now = Date.now();
    setSession(
      {
        accessToken: "stub-access-token-123",
        expiresInMilliseconds: 5_000,
      },
      now,
    );
    const fetchMock = vi.fn().mockResolvedValue(
      new Response(JSON.stringify({ ok: true }), {
        headers: { "Content-Type": "application/json" },
        status: 200,
      }),
    );
    const client = createHttpClient({
      baseUrl: "/api",
      fetchFn: fetchMock,
    });

    await client.get("/api/opinions", {
      auth: "required",
    });

    const [, requestInit] = fetchMock.mock.calls[0];
    const headers = new Headers(requestInit.headers);
    expect(headers.get("Authorization")).toBe("Bearer stub-access-token-123");
  });

  it("refreshes the session before an authenticated request when the access token is unavailable", async () => {
    const refreshHandler = vi.fn().mockResolvedValue({
      accessToken: "refreshed-access-token-456",
      expiresInMilliseconds: 5_000,
    });
    configureSessionRefresh(refreshHandler);

    const fetchMock = vi.fn().mockResolvedValue(
      new Response(JSON.stringify({ ok: true }), {
        headers: { "Content-Type": "application/json" },
        status: 200,
      }),
    );
    const client = createHttpClient({
      baseUrl: "/api",
      fetchFn: fetchMock,
    });

    await client.get("/api/opinions", {
      auth: "required",
    });

    expect(refreshHandler).toHaveBeenCalledTimes(1);

    const [, requestInit] = fetchMock.mock.calls[0];
    const headers = new Headers(requestInit.headers);
    expect(headers.get("Authorization")).toBe("Bearer refreshed-access-token-456");
  });

  it("retries an authenticated request once after a 401 response", async () => {
    const now = Date.now();
    setSession(
      {
        accessToken: "stale-access-token-123",
        expiresInMilliseconds: 5_000,
      },
      now,
    );
    const refreshHandler = vi.fn().mockResolvedValue({
      accessToken: "fresh-access-token-456",
      expiresInMilliseconds: 5_000,
    });
    configureSessionRefresh(refreshHandler);

    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce(
        new Response(JSON.stringify({ message: "Expired token" }), {
          headers: { "Content-Type": "application/json" },
          status: 401,
        }),
      )
      .mockResolvedValueOnce(
        new Response(JSON.stringify({ ok: true }), {
          headers: { "Content-Type": "application/json" },
          status: 200,
        }),
      );
    const client = createHttpClient({
      baseUrl: "/api",
      fetchFn: fetchMock,
    });

    await client.get("/api/opinions", {
      auth: "required",
    });

    expect(refreshHandler).toHaveBeenCalledTimes(1);
    expect(fetchMock).toHaveBeenCalledTimes(2);

    const firstHeaders = new Headers(fetchMock.mock.calls[0][1].headers);
    const secondHeaders = new Headers(fetchMock.mock.calls[1][1].headers);
    expect(firstHeaders.get("Authorization")).toBe("Bearer stale-access-token-123");
    expect(secondHeaders.get("Authorization")).toBe("Bearer fresh-access-token-456");
  });

  it("shares one refresh request across concurrent authenticated calls", async () => {
    const refreshHandler = vi.fn().mockResolvedValue({
      accessToken: "fresh-access-token-456",
      expiresInMilliseconds: 5_000,
    });
    configureSessionRefresh(refreshHandler);

    const fetchMock = vi.fn().mockImplementation(() =>
      Promise.resolve(
        new Response(JSON.stringify({ ok: true }), {
          headers: { "Content-Type": "application/json" },
          status: 200,
        }),
      ),
    );
    const client = createHttpClient({
      baseUrl: "/api",
      fetchFn: fetchMock,
    });

    await Promise.all([
      client.get("/api/opinions", { auth: "required" }),
      client.get("/opinion-lists", { auth: "required" }),
    ]);

    expect(refreshHandler).toHaveBeenCalledTimes(1);
    expect(fetchMock).toHaveBeenCalledTimes(2);
  });

  it("clears the session when refresh fails after a 401 response", async () => {
    const now = Date.now();
    setSession(
      {
        accessToken: "stale-access-token-123",
        expiresInMilliseconds: 5_000,
      },
      now,
    );
    configureSessionRefresh(
      vi.fn().mockRejectedValue(new Error("Refresh rejected by server")),
    );

    const fetchMock = vi.fn().mockResolvedValue(
      new Response(JSON.stringify({ message: "Expired token" }), {
        headers: { "Content-Type": "application/json" },
        status: 401,
      }),
    );
    const client = createHttpClient({
      baseUrl: "/api",
      fetchFn: fetchMock,
    });

    await expect(
      client.get("/api/opinions", {
        auth: "required",
      }),
    ).rejects.toThrow("Refresh rejected by server");

    expect(getSession()).toBeNull();
  });
});
