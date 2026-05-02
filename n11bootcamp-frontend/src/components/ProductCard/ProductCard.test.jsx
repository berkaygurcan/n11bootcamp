import { render, screen } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";
import { describe, it, expect } from "vitest";
import ProductCard from "./ProductCard";

const mockProduct = {
  id: 1,
  title: "Test Product",
  brand: "Test Brand",
  price: 99.99,
  img: "test-image.jpg"
};

describe("ProductCard Component", () => {
  it("renders product details correctly", () => {
    render(
      <BrowserRouter>
        <ProductCard product={mockProduct} />
      </BrowserRouter>
    );
    expect(screen.getByText("Test Product")).toBeInTheDocument();
    expect(screen.getByText("Test Brand")).toBeInTheDocument();
    expect(screen.getByText("99.99 TL")).toBeInTheDocument();
    
    const img = screen.getByRole("img");
    expect(img).toHaveAttribute("src", "test-image.jpg");
    expect(img).toHaveAttribute("alt", "Test Product");
  });

  it("renders placeholder when image is missing", () => {
    const noImgProduct = { ...mockProduct, img: null };
    render(
      <BrowserRouter>
        <ProductCard product={noImgProduct} />
      </BrowserRouter>
    );
    expect(screen.getByText("No image")).toBeInTheDocument();
    expect(screen.queryByRole("img")).not.toBeInTheDocument();
  });

  it("renders correct link", () => {
    render(
      <BrowserRouter>
        <ProductCard product={mockProduct} />
      </BrowserRouter>
    );
    const link = screen.getByRole("link");
    expect(link).toHaveAttribute("href", "/products/1");
  });
});
