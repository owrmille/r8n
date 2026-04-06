import { describe, expect, it, vi } from "vitest";
import { createHttpClient } from "@/lib/http-client";
import { createAuthApi } from "@/lib/api/auth";
import { createOpinionsApi } from "@/lib/api/opinions";
import { createOpinionListsApi } from "@/lib/api/opinion-lists";

describe("API modules", () => {
  it("posts login credentials to the auth endpoint", async () => {
    const fetchMock = vi.fn().mockResolvedValue(
      new Response(
        JSON.stringify({
          accessToken: "access-token",
          expiresInMilliseconds: 1000,
          refreshToken: "refresh-token",
        }),
        {
          headers: { "Content-Type": "application/json" },
          status: 200,
        },
      ),
    );
    const client = createHttpClient({
      baseUrl: "/api",
      fetchFn: fetchMock,
    });
    const authApi = createAuthApi(client);

    await authApi.login({
      login: "test",
      password: "1234",
    });

    expect(fetchMock).toHaveBeenCalledWith(
      "/api/auth/login",
      expect.objectContaining({
        body: JSON.stringify({
          login: "test",
          password: "1234",
        }),
        method: "POST",
      }),
    );
  });

  it("adds bearer authorization for protected opinions endpoints", async () => {
    const fetchMock = vi.fn().mockResolvedValue(
      new Response(
        JSON.stringify({
          componentMark: null,
          components: [],
          id: "opinion-id",
          mark: 8,
          objective: [],
          owner: "owner-id",
          ownerName: "Jane Doe",
          status: "PUBLISHED",
          subject: "subject-id",
          subjective: [],
          subjectName: "Bonanza Coffee",
          timestamp: "2026-03-28T10:00:00Z",
        }),
        {
          headers: { "Content-Type": "application/json" },
          status: 200,
        },
      ),
    );
    const client = createHttpClient({
      baseUrl: "/api",
      fetchFn: fetchMock,
    });
    const opinionsApi = createOpinionsApi(client);

    await opinionsApi.getForSubject({
      accessToken: "stub-access-token-123",
      subjectId: "00000000-0000-0000-0000-000000000000",
    });

    expect(fetchMock).toHaveBeenCalledWith(
      "/api/opinions/for?subjectId=00000000-0000-0000-0000-000000000000",
      expect.objectContaining({
        method: "GET",
      }),
    );

    const [, requestInit] = fetchMock.mock.calls[0];
    const headers = new Headers(requestInit.headers);
    expect(headers.get("Authorization")).toBe("Bearer stub-access-token-123");
  });

  it("merges pagination and filters for opinion list searches", async () => {
    const fetchMock = vi.fn().mockResolvedValue(
      new Response(
        JSON.stringify({
          items: [],
          page: 0,
          size: 20,
          total: 0,
        }),
        {
          headers: { "Content-Type": "application/json" },
          status: 200,
        },
      ),
    );
    const client = createHttpClient({
      baseUrl: "/api",
      fetchFn: fetchMock,
    });
    const opinionListsApi = createOpinionListsApi(client);

    await opinionListsApi.search({
      accessToken: "stub-access-token-123",
      filters: {
        authorNameSubstring: "Jane",
        nameSubstring: "coffee",
      },
      pageable: {
        page: 0,
        size: 20,
      },
    });

    expect(fetchMock).toHaveBeenCalledWith(
      "/api/opinionLists/search?page=0&size=20&authorNameSubstring=Jane&nameSubstring=coffee",
      expect.objectContaining({
        method: "GET",
      }),
    );
  });
});
