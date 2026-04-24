import { describe, expect, it } from "vitest";
import { getUnreadMessagesCount, MOCK_MESSAGE_THREADS } from "@/lib/messages";

describe("messages helpers", () => {
  it("sums unread incoming messages across threads", () => {
    expect(getUnreadMessagesCount(MOCK_MESSAGE_THREADS)).toBe(3);
  });
});
