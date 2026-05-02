import { Link } from "react-router-dom";
import "./ProductCard.css";

export default function ProductCard({ product }) {
  return (
    <Link className="product-card" to={`/products/${product.id}`}>
      {product.img ? (
        <img src={product.img} alt={product.title || product.brand || "Product"} />
      ) : (
        <div className="product-image-placeholder">No image</div>
      )}
      <div className="product-info">
        <h2>{product.title || "Untitled product"}</h2>
        <p>{product.brand || product.category || "Product"}</p>
        <strong>{product.price} TL</strong>
      </div>
    </Link>
  );
}
