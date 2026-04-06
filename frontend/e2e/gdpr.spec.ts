import { test, expect } from "playwright/test";

test.describe("GDPR / Users API", () => {
  const GDPR_EXPORT_PATH = "/users/export";

  test("should return 401 Unauthorized when no token is provided", async ({ request }) => {
    const response = await request.get(GDPR_EXPORT_PATH);
    expect(response.status()).toBe(401);
  });

  test("should return GDPR data when authenticated with stub token", async ({ request }) => {
    // Note: This matches the stub token pattern from the Makefile
    // In a real environment, this might be a dynamic token or handled by a setup/login fixture
    const response = await request.get(GDPR_EXPORT_PATH, {
      headers: {
        Authorization: "Bearer stub-access-token-123",
      },
    });

    // If the backend is running with stub auth enabled, we expect 200
    // If it's not configured, this might still return 401/403 which would fail the test,
    // indicating a need for environment-specific configuration.
    expect(response.status()).toBe(200);

    const data = await response.json();
    expect(data).toHaveProperty("id");
    expect(data).toHaveProperty("status");
    expect(data).toHaveProperty("personalIdentifiableInformation");
    expect(data).toHaveProperty("consents");
  });
});
