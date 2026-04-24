import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import Messages from "@/pages/Messages";

describe("Messages page", () => {
  it("shows the latest message in a collapsed thread and expands on click", () => {
    render(<Messages />);

    expect(
      screen.getByText("Your export is being prepared. We will notify you here when the archive is ready to download."),
    ).toBeInTheDocument();
    expect(
      screen.queryByText("I requested an export of my account data this morning. Can you confirm when it will be ready?"),
    ).not.toBeInTheDocument();

    fireEvent.click(screen.getByRole("button", { name: "Expand thread with R8N Support" }));

    expect(
      screen.getByText("I requested an export of my account data this morning. Can you confirm when it will be ready?"),
    ).toBeInTheDocument();
  });

  it("does not show incoming and outgoing direction labels", () => {
    render(<Messages />);

    expect(screen.queryByText("To you")).not.toBeInTheDocument();
    expect(screen.queryByText("From you")).not.toBeInTheDocument();
  });

  it("filters support conversations", () => {
    render(<Messages />);

    fireEvent.click(screen.getByRole("button", { name: "Support" }));

    expect(screen.getByText("Export archive request")).toBeInTheDocument();
    expect(screen.queryByText("Question about your coffee grinder review")).not.toBeInTheDocument();
    expect(screen.queryByText("Supplier recommendation follow-up")).not.toBeInTheDocument();
  });
});
