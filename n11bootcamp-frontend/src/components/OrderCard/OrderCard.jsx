import "./OrderCard.css";

export function getStatusText(order) {
  if (order.status === "COMPLETED") return "Completed";
  if (order.status === "CREATED") return "Processing";
  if (order.failureReason === "STOCK_FAILED") return "Out of stock";
  if (order.failureReason === "PAYMENT_FAILED") return "Payment issue";
  if (order.status === "CANCELLED") return "Cancelled";
  return order.status;
}

export function getStatusClass(order) {
  if (order.status === "COMPLETED") return "status-pill success";
  if (order.status === "CREATED") return "status-pill pending";
  return "status-pill failed";
}

export function getStatusDescription(order) {
  if (order.status === "COMPLETED") return "Your payment is complete and the stock has been confirmed.";
  if (order.status === "CREATED") return "We are checking stock and payment for this order.";
  if (order.failureReason === "STOCK_FAILED") return "This order was cancelled because one or more items are no longer in stock.";
  if (order.failureReason === "PAYMENT_FAILED") return "This order was cancelled because the payment step could not be completed.";
  return "This order was cancelled during processing.";
}

export default function OrderCard({ order }) {
  return (
    <article className="order-card">
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
  );
}
