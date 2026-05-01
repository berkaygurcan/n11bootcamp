const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "";

async function request(path, options = {}) {
  const token = localStorage.getItem("authToken");
  const skipAuth = options.skipAuth;
  const fetchOptions = { ...options };
  delete fetchOptions.skipAuth;

  const headers = {
    "Content-Type": "application/json",
    ...options.headers
  };

  if (token && !skipAuth) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers,
    ...fetchOptions
  });

  const text = await response.text();
  const data = text ? JSON.parse(text) : null;

  if (!response.ok) {
    const message = data?.message || data?.error || "Request failed";
    const error = new Error(message);
    error.status = response.status;
    throw error;
  }

  return data;
}

export function login(payload) {
  return request("/api/user/signin", {
    method: "POST",
    body: JSON.stringify(payload),
    skipAuth: true
  });
}

export function register(payload) {
  return request("/api/user/signup", {
    method: "POST",
    body: JSON.stringify(payload),
    skipAuth: true
  });
}

export function getProducts() {
  return request("/api/product", {
    skipAuth: true
  });
}

export function getCategories() {
  return request("/api/categories", {
    skipAuth: true
  });
}

export function getPagedProducts(page = 0, size = 8) {
  return request(`/api/product/paged?page=${page}&size=${size}&sortBy=id&direction=desc`, {
    skipAuth: true
  });
}

export function getProduct(id) {
  return request(`/api/product/${id}`, {
    skipAuth: true
  });
}

export function getOrders() {
  return request("/api/orders");
}

export function getCartByName(username) {
  return request(`/api/shopping-cart/by-name/${encodeURIComponent(username)}`);
}

export function createCart(username) {
  return request(`/api/shopping-cart?name=${encodeURIComponent(username)}`, {
    method: "POST"
  });
}

export async function getOrCreateCart(username) {
  try {
    return await getCartByName(username);
  } catch {
    return createCart(username);
  }
}

export function addCartItem(cartId, item) {
  return request(`/api/shopping-cart/${cartId}/items`, {
    method: "POST",
    body: JSON.stringify(item)
  });
}

export function updateCartItem(cartId, productId, quantity) {
  return request(`/api/shopping-cart/${cartId}/items/${productId}`, {
    method: "PUT",
    body: JSON.stringify({ quantity })
  });
}

export function removeCartItem(cartId, productId) {
  return request(`/api/shopping-cart/${cartId}/items/${productId}`, {
    method: "DELETE"
  });
}

export function createOrder(payload) {
  return request("/api/orders", {
    method: "POST",
    body: JSON.stringify(payload)
  });
}
