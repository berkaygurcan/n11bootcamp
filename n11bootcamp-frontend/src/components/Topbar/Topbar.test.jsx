import { render, screen, fireEvent } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";
import { describe, it, expect, vi } from "vitest";
import Topbar from "./Topbar";

describe("Topbar Component", () => {
  it("renders brand and common links", () => {
    render(
      <BrowserRouter>
        <Topbar cartCount={2} theme="light" />
      </BrowserRouter>
    );
    expect(screen.getByText("n11 Demo")).toBeInTheDocument();
    expect(screen.getByText("Products")).toBeInTheDocument();
    expect(screen.getByText("Cart (2)")).toBeInTheDocument();
  });

  it("shows login/register when no user is logged in", () => {
    render(
      <BrowserRouter>
        <Topbar cartCount={0} theme="light" />
      </BrowserRouter>
    );
    expect(screen.getByText("Login")).toBeInTheDocument();
    expect(screen.getByText("Register")).toBeInTheDocument();
    expect(screen.queryByText("Orders")).not.toBeInTheDocument();
  });

  it("shows username, orders and logout when user is logged in", () => {
    render(
      <BrowserRouter>
        <Topbar username="testuser" cartCount={0} theme="light" />
      </BrowserRouter>
    );
    expect(screen.getByText("testuser")).toBeInTheDocument();
    expect(screen.getByText("Orders")).toBeInTheDocument();
    expect(screen.getByText("Logout")).toBeInTheDocument();
    expect(screen.queryByText("Login")).not.toBeInTheDocument();
  });

  it("calls toggleTheme when theme button is clicked", () => {
    const toggleThemeMock = vi.fn();
    render(
      <BrowserRouter>
        <Topbar username="testuser" cartCount={0} theme="light" toggleTheme={toggleThemeMock} />
      </BrowserRouter>
    );
    const themeButton = screen.getByRole("button", { name: /Toggle Theme/i });
    fireEvent.click(themeButton);
    expect(toggleThemeMock).toHaveBeenCalledTimes(1);
  });
});
