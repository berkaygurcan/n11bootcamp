import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import EmptyState from "../components/EmptyState/EmptyState";
import OrderCard from "../components/OrderCard/OrderCard";
import { getOrders } from "../services/api";
import { getCurrentUsername } from "../services/cartStorage";



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
        <EmptyState type="auth" title="Login Required" message="Please login to view your orders." actionLink="/login" actionText="Login" />
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
        <EmptyState type="orders" title="No Orders" message="No orders yet." actionLink="/products" actionText="Go to products" />
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
          <OrderCard key={order.orderId} order={order} />
        ))}
      </div>
    </section>
  );
}
