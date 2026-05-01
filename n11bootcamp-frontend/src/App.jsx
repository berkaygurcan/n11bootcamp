import { useEffect, useState } from "react";
import { Link, Outlet } from "react-router-dom";
import { getCartByName } from "./services/api";
import { getCurrentUsername } from "./services/cartStorage";

export default function App() {
  const [username, setUsername] = useState(() => getCurrentUsername());
  const [cartCount, setCartCount] = useState(0);

  useEffect(() => {
    async function syncAppState() {
      const currentUsername = getCurrentUsername();
      const token = localStorage.getItem("authToken");
      setUsername(currentUsername);

      if (!currentUsername || !token) {
        setCartCount(0);
        return;
      }

      try {
        const cart = await getCartByName(currentUsername);
        const count = (cart.items || []).reduce((total, item) => total + item.quantity, 0);
        setCartCount(count);
      } catch (err) {
        if (err.status === 401) {
          localStorage.removeItem("username");
          localStorage.removeItem("authToken");
          setUsername(null);
        }
        setCartCount(0);
      }
    }

    syncAppState();
    window.addEventListener("storage", syncAppState);
    return () => window.removeEventListener("storage", syncAppState);
  }, []);

  function handleLogout() {
    localStorage.removeItem("username");
    localStorage.removeItem("authToken");
    window.dispatchEvent(new Event("storage"));
  }

  return (
    <div className="app-shell">
      <header className="topbar">
        <Link className="brand" to="/products">n11 Demo</Link>
        <nav className="nav-links">
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
      <main className="page">
        <Outlet />
      </main>
    </div>
  );
}
