import { render, screen } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import UserAvatar from "@/components/UserAvatar";

const { useUserAvatarMock } = vi.hoisted(() => ({
  useUserAvatarMock: vi.fn(),
}));

vi.mock("@/lib/server-state/hooks/users", () => ({
  useUserAvatar: useUserAvatarMock,
}));

const USER_ID = "11111111-1111-1111-1111-111111111111";

describe("UserAvatar", () => {
  beforeEach(() => {
    useUserAvatarMock.mockReset();
    Object.defineProperty(URL, "createObjectURL", {
      configurable: true,
      value: vi.fn(),
    });
    Object.defineProperty(URL, "revokeObjectURL", {
      configurable: true,
      value: vi.fn(),
    });
    vi.spyOn(URL, "createObjectURL").mockReturnValue("blob:avatar-url");
    vi.spyOn(URL, "revokeObjectURL").mockImplementation(() => {});
  });

  it("shows initials when the user has no avatar", () => {
    useUserAvatarMock.mockReturnValue({ data: null });

    render(<UserAvatar userId={USER_ID} name="Jane Doe" />);

    expect(screen.getByText("JD")).toBeInTheDocument();
    expect(screen.queryByRole("img")).not.toBeInTheDocument();
  });

  it("renders the avatar image when the backend returns a blob", () => {
    useUserAvatarMock.mockReturnValue({
      data: new Blob(["avatar"], { type: "image/png" }),
    });

    render(<UserAvatar userId={USER_ID} name="Jane Doe" />);

    expect(screen.getByRole("img", { name: "Jane Doe" })).toHaveAttribute(
      "src",
      "blob:avatar-url",
    );
  });
});
