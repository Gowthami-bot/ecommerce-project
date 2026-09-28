const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "/api";

async function request(path, options = {}) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    credentials: "include",
    headers: {
      Accept: "application/json",
      ...(options.body ? { "Content-Type": "application/json" } : {}),
      ...options.headers,
    },
  });

  const contentType = response.headers.get("content-type") || "";
  const body = contentType.includes("application/json")
    ? await response.json()
    : await response.text();

  if (!response.ok) {
    const error = new Error(body?.message || body || `Request failed (${response.status})`);
    error.status = response.status;
    throw error;
  }

  return body;
}

export const api = {
  getUsers: () => request("/users"),
  getUser: (id) => request(`/users/${encodeURIComponent(id)}`),
  createUser: (user) => request("/users", {
    method: "POST",
    body: JSON.stringify(user),
  }),
  getProducts: () => request("/products"),
  getProduct: (id) => request(`/products/${encodeURIComponent(id)}`),
  getCurrentUser: () => request("/auth/me"),
  login: (credentials) => request("/auth/login", {
    method: "POST",
    body: JSON.stringify(credentials),
  }),
  logout: () => request("/auth/logout", { method: "POST" }),
  getCart: () => request("/cart"),
  setCartItem: (productId, quantity) => request(`/cart/items/${productId}`, {
    method: "PUT",
    body: JSON.stringify({ quantity }),
  }),
  removeCartItem: (productId) => request(`/cart/items/${productId}`, { method: "DELETE" }),
  createOrder: (order) => request("/orders", {
    method: "POST",
    body: JSON.stringify(order),
  }),
};