import { useEffect, useState } from "react";
import { Link, Outlet } from "react-router-dom";
import Topbar from "./components/Topbar/Topbar";
import { getCartByName } from "./services/api";
import { getCurrentUsername } from "./services/cartStorage";

export default function App() {
  const [username, setUsername] = useState(() => getCurrentUsername());
  const [cartCount, setCartCount] = useState(0);
  const [theme, setTheme] = useState(() => localStorage.getItem("theme") || "light");

  useEffect(() => {
    document.documentElement.setAttribute("data-theme", theme);
    localStorage.setItem("theme", theme);
  }, [theme]);

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

  function toggleTheme() {
    setTheme((prev) => (prev === "light" ? "dark" : "light"));
  }

  return (
    <div className="app-shell">
      <Topbar 
        username={username} 
        cartCount={cartCount} 
        theme={theme} 
        toggleTheme={toggleTheme} 
        handleLogout={handleLogout} 
      />
      <main className="page">
        <Outlet />
      </main>
    </div>
  );
}
