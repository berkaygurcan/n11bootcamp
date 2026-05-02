import "./CartItem.css";

export default function CartItem({ item, onQuantityChange, onRemove }) {
  return (
    <article className="cart-item">
      <div>
        <h2>{item.productName}</h2>
        <p>{item.price} TL each</p>
      </div>

      <div className="cart-actions">
        <input
          type="number"
          min="1"
          value={item.quantity}
          onChange={(event) => onQuantityChange(item.productId, Number(event.target.value))}
        />
        <button
          className="secondary-button"
          type="button"
          onClick={() => onRemove(item.productId)}
        >
          Remove
        </button>
        <strong>{item.price * item.quantity} TL</strong>
      </div>
    </article>
  );
}
