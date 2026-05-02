import { Link } from "react-router-dom";
import "./Topbar.css";

export default function Topbar({ username, cartCount, theme, toggleTheme, handleLogout }) {
  return (
    <header className="topbar">
      <Link className="brand" to="/products">n11 Demo</Link>
      <nav className="nav-links">
        <button className="theme-toggle" onClick={toggleTheme} aria-label="Toggle Theme">
          {theme === "light" ? "🌙" : "☀️"}
        </button>
        <Link to="/products">Products</Link>
        <Link to="/cart">Cart ({cartCount})</Link>
        {username && <Link to="/orders">Orders</Link>}
        {username ? (
          <>
            <span className="username">{username}</span>
            <button className="link-button" onClick={handleLogout}>Logout</button>
          </>
        ) : (
          <>
            <Link to="/login">Login</Link>
            <Link className="nav-cta" to="/register">Register</Link>
          </>
        )}
      </nav>
    </header>
  );
}
