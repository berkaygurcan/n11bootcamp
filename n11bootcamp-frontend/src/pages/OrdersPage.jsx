import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getOrders } from "../services/api";
import { getCurrentUsername } from "../services/cartStorage";

function getStatusText(order) {
  if (order.status === "COMPLETED") return "Completed";
  if (order.status === "CREATED") return "Processing";
  if (order.failureReason === "STOCK_FAILED") return "Out of stock";
  if (order.failureReason === "PAYMENT_FAILED") return "Payment issue";
  if (order.status === "CANCELLED") return "Cancelled";
  return order.status;
}

function getStatusClass(order) {
  if (order.status === "COMPLETED") return "status-pill success";
  if (order.status === "CREATED") return "status-pill pending";
  return "status-pill failed";
}

function getStatusDescription(order) {
  if (order.status === "COMPLETED") return "Your payment is complete and the stock has been confirmed.";
  if (order.status === "CREATED") return "We are checking stock and payment for this order.";
  if (order.failureReason === "STOCK_FAILED") return "This order was cancelled because one or more items are no longer in stock.";
  if (order.failureReason === "PAYMENT_FAILED") return "This order was cancelled because the payment step could not be completed.";
  return "This order was cancelled during processing.";
}

export default function OrdersPage() {
  const username = getCurrentUsername();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(Boolean(username));
  const [error, setError] = useState("");

  async function loadOrders() {
    if (!username) return;

    setLoading(true);
    setError("");

    try {
      const data = await getOrders();
      const userOrders = data
        .filter((order) => order.username === username)
        .sort((a, b) => b.orderId - a.orderId);

      setOrders(userOrders);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadOrders();
  }, [username]);

  const completedCount = orders.filter((order) => order.status === "COMPLETED").length;
  const processingCount = orders.filter((order) => order.status === "CREATED").length;
  const failedCount = orders.filter((order) => order.status === "CANCELLED").length;

  if (!username) {
    return (
      <section className="content-panel">
        <div className="empty-state">
          <p>Please login to view your orders.</p>
          <Link to="/login">Login</Link>
        </div>
      </section>
    );
  }

  return (
    <section className="content-panel">
      <div className="page-title">
        <div>
          <h1>Orders</h1>
          <p>{orders.length > 0 ? `${orders.length} orders for ${username}` : "Your order history"}</p>
        </div>
        <button className="secondary-button" type="button" onClick={loadOrders} disabled={loading}>
          {loading ? "Refreshing..." : "Refresh"}
        </button>
      </div>

      {loading && (
        <div className="loading-state">
          <span className="spinner" aria-hidden="true"></span>
          <span>Loading orders...</span>
        </div>
      )}
      {error && <div className="error-message">{error}</div>}

      {!loading && !error && orders.length === 0 && (
        <div className="empty-state">
          <p>No orders yet.</p>
          <Link to="/products">Go to products</Link>
        </div>
      )}

      {orders.length > 0 && (
        <div className="order-stats">
          <div>
            <span>Completed</span>
            <strong>{completedCount}</strong>
          </div>
          <div>
            <span>Processing</span>
            <strong>{processingCount}</strong>
          </div>
          <div>
            <span>Failed</span>
            <strong>{failedCount}</strong>
          </div>
        </div>
      )}

      <div className="orders-list">
        {orders.map((order) => (
          <article className="order-card" key={order.orderId}>
            <div className="order-main">
              <div>
                <span>Order #{order.orderId}</span>
                <h2>{order.totalPrice} TL</h2>
              </div>
              <span className={getStatusClass(order)}>{getStatusText(order)}</span>
            </div>
            <p className="order-description">{getStatusDescription(order)}</p>

            {order.items && order.items.length > 0 && (
              <div className="order-items">
                {order.items.map((item) => (
                  <span key={`${order.orderId}-${item.productId}`}>
                    {item.productName} x {item.quantity}
                  </span>
                ))}
              </div>
            )}
          </article>
        ))}
      </div>
    </section>
  );
}
