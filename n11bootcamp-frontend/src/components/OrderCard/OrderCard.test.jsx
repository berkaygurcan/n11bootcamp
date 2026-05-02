import { render, screen } from "@testing-library/react";
import { describe, it, expect } from "vitest";
import OrderCard, { getStatusText, getStatusClass } from "./OrderCard";

describe("OrderCard Component", () => {
  const mockOrder = {
    orderId: 55,
    totalPrice: 150,
    status: "COMPLETED",
    items: [
      { productId: 1, productName: "Keyboard", quantity: 1 }
    ]
  };

  it("renders order details correctly", () => {
    render(<OrderCard order={mockOrder} />);
    expect(screen.getByText("Order #55")).toBeInTheDocument();
    expect(screen.getByText("150 TL")).toBeInTheDocument();
    expect(screen.getByText("Keyboard x 1")).toBeInTheDocument();
    expect(screen.getByText("Completed")).toBeInTheDocument();
  });

  it("helper functions return correct values for CREATED status", () => {
    const processingOrder = { status: "CREATED" };
    expect(getStatusText(processingOrder)).toBe("Processing");
    expect(getStatusClass(processingOrder)).toContain("pending");
  });

  it("helper functions return correct values for FAILED status", () => {
    const failedOrder = { status: "CANCELLED", failureReason: "STOCK_FAILED" };
    expect(getStatusText(failedOrder)).toBe("Out of stock");
    expect(getStatusClass(failedOrder)).toContain("failed");
  });
});
