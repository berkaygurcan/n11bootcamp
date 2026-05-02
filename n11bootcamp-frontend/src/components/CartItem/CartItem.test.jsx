import { render, screen, fireEvent } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import CartItem from "./CartItem";

const mockItem = {
  productId: 101,
  productName: "Test Mouse",
  price: 50,
  quantity: 2
};

describe("CartItem Component", () => {
  it("renders item details and total correctly", () => {
    render(<CartItem item={mockItem} onQuantityChange={() => {}} onRemove={() => {}} />);
    expect(screen.getByText("Test Mouse")).toBeInTheDocument();
    expect(screen.getByText("50 TL each")).toBeInTheDocument();
    expect(screen.getByText("100 TL")).toBeInTheDocument(); // 50 * 2
    expect(screen.getByDisplayValue("2")).toBeInTheDocument();
  });

  it("calls onQuantityChange when input changes", () => {
    const onQuantityChangeMock = vi.fn();
    render(<CartItem item={mockItem} onQuantityChange={onQuantityChangeMock} onRemove={() => {}} />);
    const input = screen.getByDisplayValue("2");
    fireEvent.change(input, { target: { value: "3" } });
    expect(onQuantityChangeMock).toHaveBeenCalledWith(101, 3);
  });

  it("calls onRemove when remove button is clicked", () => {
    const onRemoveMock = vi.fn();
    render(<CartItem item={mockItem} onQuantityChange={() => {}} onRemove={onRemoveMock} />);
    const button = screen.getByRole("button", { name: /Remove/i });
    fireEvent.click(button);
    expect(onRemoveMock).toHaveBeenCalledWith(101);
  });
});
