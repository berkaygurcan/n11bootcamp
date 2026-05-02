import { render, screen } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";
import { describe, it, expect } from "vitest";
import EmptyState from "./EmptyState";

describe("EmptyState Component", () => {
  it("renders correctly with title and message", () => {
    render(
      <BrowserRouter>
        <EmptyState title="Test Title" message="Test Message" />
      </BrowserRouter>
    );
    expect(screen.getByText("Test Title")).toBeInTheDocument();
    expect(screen.getByText("Test Message")).toBeInTheDocument();
  });

  it("renders the action button when actionLink and actionText are provided", () => {
    render(
      <BrowserRouter>
        <EmptyState title="Test" message="Test" actionLink="/home" actionText="Go Home" />
      </BrowserRouter>
    );
    const linkElement = screen.getByRole("link", { name: /Go Home/i });
    expect(linkElement).toBeInTheDocument();
    expect(linkElement).toHaveAttribute("href", "/home");
  });

  it("does not render the action button when missing action props", () => {
    render(
      <BrowserRouter>
        <EmptyState title="Test" message="Test" />
      </BrowserRouter>
    );
    const linkElement = screen.queryByRole("link");
    expect(linkElement).not.toBeInTheDocument();
  });
});
