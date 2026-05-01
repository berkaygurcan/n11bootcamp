import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { login, register } from "../services/api";

export default function RegisterPage() {
  const navigate = useNavigate();
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");
    setLoading(true);

    try {
      await register({ username, email, password });
      const data = await login({ username, password });
      const token = data.accessToken || data.access_token;

      if (!token) {
        throw new Error("Registration succeeded but token was not returned.");
      }

      localStorage.setItem("username", username);
      localStorage.setItem("authToken", token);
      window.dispatchEvent(new Event("storage"));
      navigate("/products");
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <section className="auth-card">
      <div className="auth-heading">
        <span>New account</span>
        <h1>Register</h1>
        <p>Create a simple demo account.</p>
      </div>

      <form onSubmit={handleSubmit}>
        <label>
          Username
          <input
            value={username}
            onChange={(event) => setUsername(event.target.value)}
            required
            minLength={3}
            maxLength={30}
          />
        </label>

        <label>
          Email
          <input
            type="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            required
            maxLength={50}
          />
        </label>

        <label>
          Password
          <input
            type="password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            required
            minLength={6}
            maxLength={40}
          />
        </label>

        {error && <div className="error-message">{error}</div>}

        <button type="submit" disabled={loading}>
          {loading ? "Registering..." : "Register"}
        </button>
      </form>

      <p className="switch-link">
        Already registered? <Link to="/login">Login</Link>
      </p>
    </section>
  );
}
