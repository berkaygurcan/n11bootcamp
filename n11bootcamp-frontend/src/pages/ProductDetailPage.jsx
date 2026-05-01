import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { getCurrentUsername, notifyCartChanged } from "../services/cartStorage";
import { addCartItem, getOrCreateCart, getProduct } from "../services/api";

export default function ProductDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [added, setAdded] = useState(false);
  const [adding, setAdding] = useState(false);

  useEffect(() => {
    async function loadProduct() {
      try {
        const data = await getProduct(id);
        setProduct(data);
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    }

    loadProduct();
  }, [id]);

  async function handleAddToCart() {
    const username = getCurrentUsername();

    if (!username) {
      navigate("/login");
      return;
    }

    setAdding(true);
    setError("");

    try {
      const cart = await getOrCreateCart(username);
      await addCartItem(cart.id, {
        productId: product.id,
        productName: product.title || "Untitled product",
        price: product.price,
        quantity: 1
      });
      notifyCartChanged();
      setAdded(true);
    } catch (err) {
      setError(err.message);
    } finally {
      setAdding(false);
    }
  }

  return (
    <section className="content-panel">
      <Link className="back-link" to="/products">Back to products</Link>

      {loading && (
        <div className="loading-state">
          <span className="spinner" aria-hidden="true"></span>
          <span>Loading product...</span>
        </div>
      )}
      {error && <div className="error-message">{error}</div>}

      {!loading && !error && product && (
        <article className="detail-layout">
          <div className="detail-image">
            {product.img ? (
              <img src={product.img} alt={product.title || "Product"} />
            ) : (
              <span>No image</span>
            )}
          </div>

          <div className="detail-info">
            <p className="detail-meta">{product.brand || product.category || "Product"}</p>
            <h1>{product.title || "Untitled product"}</h1>
            {product.description && <p>{product.description}</p>}

            <div className="purchase-panel">
              <div>
                <span>Price</span>
                <strong>{product.price} TL</strong>
              </div>
              <button className="primary-button" type="button" onClick={handleAddToCart} disabled={adding}>
                {adding ? "Adding..." : "Add to cart"}
              </button>
              {!getCurrentUsername() && (
                <p className="help-text">Login required to add products to cart.</p>
              )}
              {added && <p className="success-text">Added to cart.</p>}
            </div>

            <dl className="detail-list">
              <div>
                <dt>Category</dt>
                <dd>{product.category || "-"}</dd>
              </div>
              <div>
                <dt>Color</dt>
                <dd>{product.color || "-"}</dd>
              </div>
              <div>
                <dt>Labels</dt>
                <dd>{product.labels || "-"}</dd>
              </div>
            </dl>
          </div>
        </article>
      )}
    </section>
  );
}
