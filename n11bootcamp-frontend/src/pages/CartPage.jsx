import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import EmptyState from "../components/EmptyState/EmptyState";
import CartItem from "../components/CartItem/CartItem";
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
        <EmptyState type="auth" title="Login Required" message="Please login to use your cart." actionLink="/login" actionText="Login" />
      ) : cart.length === 0 ? (
        cartLoading ? (
          <div className="empty-state"><p>Loading cart...</p></div>
        ) : cartError ? (
          <div className="empty-state"><p>{cartError}</p></div>
        ) : (
          <EmptyState type="cart" title="Cart Empty" message="Your cart is empty." actionLink="/products" actionText="Go to products" />
        )
      ) : (
        <div className="cart-layout">
          <div className="cart-items">
            {cart.map((item) => (
              <CartItem 
                key={item.productId} 
                item={item} 
                onQuantityChange={changeQuantity} 
                onRemove={removeItem} 
              />
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
