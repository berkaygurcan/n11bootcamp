import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import EmptyState from "../components/EmptyState/EmptyState";
import { createOrder, getOrCreateCart, getOrders, removeCartItem } from "../services/api";
import { getCurrentUsername, notifyCartChanged } from "../services/cartStorage";

const demoCard = {
  cardHolderName: "John Doe",
  cardNumber: "5528790000000008",
  expireMonth: "12",
  expireYear: "2030",
  cvc: "123"
};

function getOrderResultMessage(order) {
  if (order.status === "COMPLETED") {
    return {
      type: "success",
      title: "Payment approved",
      body: "Your order has been created successfully and your cart has been cleared.",
      completed: true
    };
  }

  if (order.failureReason === "STOCK_FAILED") {
    return {
      type: "error",
      title: "We could not complete your order",
      body: "One or more items are no longer in stock. Please update your cart and try again."
    };
  }

  if (order.failureReason === "PAYMENT_FAILED") {
    return {
      type: "error",
      title: "Payment could not be completed",
      body: "The payment step could not be completed. Please try checkout again."
    };
  }

  return {
    type: "error",
    title: "Order could not be completed",
    body: "Your order was cancelled during processing. Please try again in a moment."
  };
}

export default function CheckoutPage() {
  const username = getCurrentUsername();
  const [cartId, setCartId] = useState(null);
  const [cart, setCart] = useState([]);
  const [card, setCard] = useState(demoCard);
  const [loading, setLoading] = useState(Boolean(username));
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState(null);
  const [trackingOrderId, setTrackingOrderId] = useState(null);

  useEffect(() => {
    async function loadCart() {
      if (!username) return;

      setLoading(true);
      setError("");

      try {
        const data = await getOrCreateCart(username);
        setCartId(data.id);
        setCart(data.items || []);
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    }

    loadCart();
  }, [username]);

  useEffect(() => {
    if (!trackingOrderId) return;

    const intervalId = window.setInterval(async () => {
      try {
        const orders = await getOrders();
        const order = orders.find((item) => item.orderId === trackingOrderId);

        if (!order || order.status === "CREATED") return;

        if (order.status === "COMPLETED") {
          await Promise.all(cart.map((item) => removeCartItem(cartId, item.productId)));
          setCart([]);
          notifyCartChanged();
        }

        setMessage(getOrderResultMessage(order));
        setTrackingOrderId(null);
      } catch (err) {
        setError(`Could not check order status: ${err.message}`);
        setTrackingOrderId(null);
      }
    }, 3000);

    return () => window.clearInterval(intervalId);
  }, [trackingOrderId, cart, cartId]);

  function updateCard(field, value) {
    setCard((current) => ({ ...current, [field]: value }));
  }

  async function handleSubmit(event) {
    event.preventDefault();

    if (cart.length === 0) {
      setError("Your cart is empty.");
      return;
    }

    setSubmitting(true);
    setError("");
    setMessage(null);

    try {
      const order = await createOrder({
        username,
        items: cart,
        paymentCard: card
      });

      setMessage({
        type: "success",
        title: "Order processing...",
        body: "Your order was received. We are checking stock and payment now."
      });
      setTrackingOrderId(order.orderId);
    } catch (err) {
      setError(
        err.message === "Request failed"
          ? "Checkout failed. Please check the backend services and try again."
          : err.message
      );
    } finally {
      setSubmitting(false);
    }
  }

  const total = cart.reduce((sum, item) => sum + item.price * item.quantity, 0);

  if (!username) {
    return (
      <section className="content-panel">
        <EmptyState type="auth" title="Login Required" message="Please login to checkout." actionLink="/login" actionText="Login" />
      </section>
    );
  }

  return (
    <section className="content-panel">
      <Link className="back-link" to="/cart">Back to cart</Link>

      <div className="page-title">
        <div>
          <h1>Checkout</h1>
          <p>Review your cart and confirm the demo payment.</p>
        </div>
      </div>

      {loading && (
        <div className="loading-state">
          <span className="spinner" aria-hidden="true"></span>
          <span>Loading checkout...</span>
        </div>
      )}

      {!loading && message?.completed && (
        <div className="checkout-result success-message">
          <strong>{message.title}</strong>
          <p>{message.body}</p>
          <div className="success-actions">
            <Link className="primary-button" to="/orders">View orders</Link>
            <Link className="secondary-button" to="/products">Continue shopping</Link>
          </div>
        </div>
      )}

      {!loading && cart.length === 0 && (
        <EmptyState 
          type="cart" 
          title="Cart Empty" 
          message={message?.completed ? "Your cart has been cleared." : "Your cart is empty."} 
          actionLink={message?.completed ? undefined : "/products"} 
          actionText={message?.completed ? undefined : "Go to products"} 
        />
      )}

      {!loading && cart.length > 0 && (
        <form className="checkout-layout" onSubmit={handleSubmit}>
          <div className="checkout-card">
            <h2>Payment card</h2>
            <p className="status-text">Demo Iyzico sandbox card is filled in for testing.</p>

            <label>
              Card holder
              <input
                value={card.cardHolderName}
                onChange={(event) => updateCard("cardHolderName", event.target.value)}
              />
            </label>

            <label>
              Card number
              <input
                value={card.cardNumber}
                onChange={(event) => updateCard("cardNumber", event.target.value)}
              />
            </label>

            <div className="checkout-card-row">
              <label>
                Month
                <input
                  value={card.expireMonth}
                  onChange={(event) => updateCard("expireMonth", event.target.value)}
                />
              </label>
              <label>
                Year
                <input
                  value={card.expireYear}
                  onChange={(event) => updateCard("expireYear", event.target.value)}
                />
              </label>
              <label>
                CVC
                <input value={card.cvc} onChange={(event) => updateCard("cvc", event.target.value)} />
              </label>
            </div>
          </div>

          <aside className="checkout-summary">
            <h2>Summary</h2>
            <div className="checkout-items">
              {cart.map((item) => (
                <div key={item.productId}>
                  <span>{item.productName} x {item.quantity}</span>
                  <strong>{item.price * item.quantity} TL</strong>
                </div>
              ))}
            </div>

            <div className="summary-head">
              <span>Total</span>
              <strong>{total} TL</strong>
            </div>

            <button className="primary-button" type="submit" disabled={submitting || Boolean(trackingOrderId)}>
              {submitting ? "Submitting..." : trackingOrderId ? "Processing..." : "Confirm payment"}
            </button>

            {message && (
              <div className={message.type === "error" ? "error-message" : "success-message"}>
                <strong>{message.title}</strong>
                <p>{message.body}</p>
              </div>
            )}
            {error && <div className="error-message">{error}</div>}
          </aside>
        </form>
      )}
    </section>
  );
}
