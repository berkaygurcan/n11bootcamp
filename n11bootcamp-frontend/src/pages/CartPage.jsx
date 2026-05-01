import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {
  getCartByName,
  getOrCreateCart,
  removeCartItem,
  updateCartItem
} from "../services/api";
import { getCurrentUsername, notifyCartChanged } from "../services/cartStorage";

export default function CartPage() {
  const username = getCurrentUsername();
  const [cartId, setCartId] = useState(null);
  const [cart, setCart] = useState([]);
  const [cartLoading, setCartLoading] = useState(Boolean(username));
  const [cartError, setCartError] = useState("");

  useEffect(() => {
    async function loadCart() {
      if (!username) return;

      setCartLoading(true);
      setCartError("");

      try {
        const data = await getOrCreateCart(username);
        setCartId(data.id);
        setCart(data.items || []);
      } catch (err) {
        setCartError(err.message);
      } finally {
        setCartLoading(false);
      }
    }

    loadCart();
  }, [username]);

  async function removeItem(productId) {
    try {
      const data = await removeCartItem(cartId, productId);
      setCart(data.items || []);
      notifyCartChanged();
    } catch (err) {
      setCartError(err.message);
    }
  }

  async function clearCart() {
    try {
      let latestCart = cart;

      for (const item of cart) {
        const data = await removeCartItem(cartId, item.productId);
        latestCart = data.items || [];
      }

      setCart(latestCart);
      notifyCartChanged();
    } catch (err) {
      setCartError(err.message);
    }
  }

  async function changeQuantity(productId, quantity) {
    if (quantity < 1) {
      removeItem(productId);
      return;
    }

    try {
      const data = await updateCartItem(cartId, productId, quantity);
      setCart(data.items || []);
      notifyCartChanged();
    } catch (err) {
      setCartError(err.message);
    }
  }

  const total = cart.reduce((sum, item) => sum + item.price * item.quantity, 0);

  return (
    <section className="content-panel">
      <div className="page-title">
        <h1>Cart</h1>
        <p>{username ? `${username}'s cart` : "Login required for cart."}</p>
      </div>

      {!username ? (
        <div className="empty-state">
          <p>Please login to use your cart.</p>
          <Link to="/login">Login</Link>
        </div>
      ) : cart.length === 0 ? (
        <div className="empty-state">
          {cartLoading && <p>Loading cart...</p>}
          {!cartLoading && cartError && <p>{cartError}</p>}
          {!cartLoading && !cartError && <p>Your cart is empty.</p>}
          <Link to="/products">Go to products</Link>
        </div>
      ) : (
        <div className="cart-layout">
          <div className="cart-items">
            {cart.map((item) => (
              <article className="cart-item" key={item.productId}>
                <div>
                  <h2>{item.productName}</h2>
                  <p>{item.price} TL each</p>
                </div>

                <div className="cart-actions">
                  <input
                    type="number"
                    min="1"
                    value={item.quantity}
                    onChange={(event) =>
                      changeQuantity(item.productId, Number(event.target.value))
                    }
                  />
                  <button
                    className="secondary-button"
                    type="button"
                    onClick={() => removeItem(item.productId)}
                  >
                    Remove
                  </button>
                  <strong>{item.price * item.quantity} TL</strong>
                </div>
              </article>
            ))}
          </div>

          <aside className="cart-summary">
            <div className="summary-head">
              <span>Total</span>
              <strong>{total} TL</strong>
            </div>
            <Link className="primary-button summary-link primary-summary-link" to="/checkout">
              Checkout
            </Link>
            <Link className="secondary-button summary-link" to="/products">
              Continue shopping
            </Link>
            <button className="link-button danger-link" type="button" onClick={clearCart}>
              Clear cart
            </button>
          </aside>
        </div>
      )}
    </section>
  );
}
