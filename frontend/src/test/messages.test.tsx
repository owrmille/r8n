import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import Messages from "@/pages/Messages";

describe("Messages page", () => {
  it("expands a thread when the first message is clicked", () => {
    render(<Messages />);

    expect(
      screen.queryByText("Your export is being prepared. We will notify you here when the archive is ready to download."),
    ).not.toBeInTheDocument();

    fireEvent.click(screen.getByRole("button", { name: "Expand thread with R8N Support" }));

    expect(
      screen.getByText("Your export is being prepared. We will notify you here when the archive is ready to download."),
    ).toBeInTheDocument();
  });

  it("shows incoming and outgoing direction labels", () => {
    render(<Messages />);

    expect(screen.getAllByText("To you").length).toBeGreaterThan(0);
    expect(screen.getAllByText("From you").length).toBeGreaterThan(0);
  });

  it("filters support conversations", () => {
    render(<Messages />);

    fireEvent.click(screen.getByRole("button", { name: "Support" }));

    expect(screen.getByText("Export archive request")).toBeInTheDocument();
    expect(screen.queryByText("Question about your coffee grinder review")).not.toBeInTheDocument();
    expect(screen.queryByText("Supplier recommendation follow-up")).not.toBeInTheDocument();
  });
});
