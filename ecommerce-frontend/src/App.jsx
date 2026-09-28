import { useEffect, useState } from "react";
import { api } from "./services/api.js";
import SiteHeader from "./components/SiteHeader.jsx";
import ProductCard from "./components/ProductCard.jsx";
import { ErrorState, LoadingState } from "./components/PageFeedback.jsx";
import { Link } from "./components/SiteLink.jsx";
import { formatPrice } from "./utils/formatPrice.js";
import "./App.css";

function readCart() {
  try {
    return JSON.parse(localStorage.getItem("common-goods-cart") || "[]");
  } catch {
    return [];
  }
}

function readRoute() {
  return window.location.hash.slice(1) || "/";
}

function useRoute() {
  const [route, setRoute] = useState(readRoute);
  useEffect(() => {
    const update = () => setRoute(readRoute());
    window.addEventListener("hashchange", update);
    return () => window.removeEventListener("hashchange", update);
  }, []);
  return route.replace(/\/$/, "") || "/";
}

function useProducts() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [refresh, setRefresh] = useState(0);

  useEffect(() => {
    let active = true;
    api.getProducts().then((data) => {
      if (active) setProducts(data);
    }).catch((requestError) => {
      if (active) setError(requestError.message);
    }).finally(() => {
      if (active) setLoading(false);
    });
    return () => { active = false; };
  }, [refresh]);

  function reload() {
    setError("");
    setLoading(true);
    setRefresh((value) => value + 1);
  }

  return { products, loading, error, reload };
}

function App() {
  const route = useRoute();
  const [cart, setCart] = useState(readCart);
  const [user, setUser] = useState(null);
  const [authLoading, setAuthLoading] = useState(true);
  const [notice, setNotice] = useState("");

  useEffect(() => {
    let active = true;
    api.getCurrentUser().then((account) => {
      if (active) setUser(account);
    }).catch(() => {
      if (active) setUser(null);
    }).finally(() => {
      if (active) setAuthLoading(false);
    });
    return () => { active = false; };
  }, []);

  useEffect(() => {
    if (authLoading) return undefined;
    if (!user) return undefined;
    let active = true;
    api.getCart().then((items) => {
      if (active) setCart(items.map(({ productId, quantity }) => ({ productId, quantity })));
    }).catch((error) => {
      if (active) setNotice(error.message);
    });
    return () => { active = false; };
  }, [authLoading, user]);

  useEffect(() => {
    if (authLoading || user) return;
    try {
      localStorage.setItem("common-goods-cart", JSON.stringify(cart));
    } catch { /* Storage may be unavailable in private browsing. */ }
  }, [authLoading, cart, user]);

  useEffect(() => {
    if (!notice) return undefined;
    const timeout = window.setTimeout(() => setNotice(""), 3200);
    return () => window.clearTimeout(timeout);
  }, [notice]);

  async function addToCart(product) {
    if (product.stock < 1) return;
    const existing = cart.find((item) => item.productId === product.id);
    const quantity = Math.min((existing?.quantity || 0) + 1, product.stock);
    try {
      if (user) {
        setCart((await api.setCartItem(product.id, quantity)).map(({ productId, quantity: itemQuantity }) => ({ productId, quantity: itemQuantity })));
      } else {
        setCart((current) => existing
          ? current.map((item) => item.productId === product.id ? { ...item, quantity } : item)
          : [...current, { productId: product.id, quantity }]);
      }
      setNotice(user ? `${product.name} saved to your account bag` : `${product.name} added. Sign in to sync your bag across devices.`);
    } catch (error) {
      setNotice(error.message);
    }
  }

  async function setQuantity(productId, quantity, stock) {
    try {
      if (user) {
        const items = quantity <= 0
          ? await api.removeCartItem(productId)
          : await api.setCartItem(productId, Math.min(quantity, stock));
        setCart(items.map(({ productId: itemId, quantity: itemQuantity }) => ({ productId: itemId, quantity: itemQuantity })));
        return;
      }
      if (quantity <= 0) {
        setCart((current) => current.filter((item) => item.productId !== productId));
        return;
      }
      setCart((current) => current.map((item) => item.productId === productId
        ? { ...item, quantity: Math.min(quantity, stock) }
        : item));
    } catch (error) {
      setNotice(error.message);
    }
  }

  async function acceptLogin(account) {
    const guestCart = cart;
    if (guestCart.length) {
      const saved = await api.getCart();
      const quantities = new Map(saved.map((item) => [item.productId, item.quantity]));
      for (const item of guestCart) {
        const product = await api.getProduct(item.productId);
        quantities.set(item.productId, Math.min((quantities.get(item.productId) || 0) + item.quantity, product.stock));
      }
      for (const [productId, quantity] of quantities) {
        await api.setCartItem(productId, quantity);
      }
      localStorage.removeItem("common-goods-cart");
    }
    setUser(account);
    setCart((await api.getCart()).map(({ productId, quantity }) => ({ productId, quantity })));
  }

  async function signOut() {
    try {
      await api.logout();
      setUser(null);
      setCart([]);
      setNotice("You have signed out.");
    } catch (error) {
      setNotice(error.message);
    }
  }

  const cartCount = cart.reduce((sum, item) => sum + item.quantity, 0);
  const productDetail = route.match(/^\/products\/(.+)$/);
  const userDetail = route.match(/^\/users\/(.+)$/);

  let page;
  if (route === "/") page = <HomePage onAdd={addToCart} />;
  else if (route === "/products") page = <ProductsPage onAdd={addToCart} />;
  else if (productDetail) page = <ProductDetailsPage id={decodeURIComponent(productDetail[1])} onAdd={addToCart} />;
  else if (route === "/cart") page = <CartPage cart={cart} setQuantity={setQuantity} />;
  else if (route === "/login") page = <LoginPage onAuthenticated={acceptLogin} />;
  else if (route === "/checkout" && authLoading) page = <LoadingState label="Checking your account" />;
  else if (route === "/checkout" && !user) page = <SignInRequired />;
  else if (route === "/checkout") page = <CheckoutPage cart={cart} user={user} onOrderCreated={(order) => {
    setCart([]);
    sessionStorage.setItem("common-goods-order", JSON.stringify(order));
    window.location.hash = "/confirmation";
  }} />;
  else if (route === "/confirmation") page = <ConfirmationPage />;
  else if (route === "/users") page = <UsersPage />;
  else if (route === "/users/new") page = <AddUserPage onAuthenticated={acceptLogin} />;
  else if (userDetail) page = <UserDetailsPage id={decodeURIComponent(userDetail[1])} />;
  else page = <NotFoundPage />;

  return (
    <div className="app-shell">
      <SiteHeader cartCount={cartCount} user={user} onLogout={signOut} />
      <main>{page}</main>
      <footer className="site-footer">
        <Link href="/" className="footer-brand">common goods</Link>
        <span>Useful things, chosen with care.</span>
        <Link href="/users">Our community</Link>
      </footer>
      {notice && <div className="toast" role="status">{notice}</div>}
    </div>
  );
}

function HomePage({ onAdd }) {
  const { products, loading, error, reload } = useProducts();
  return (
    <>
      <section className="home-hero" style={{ "--hero-image": `url("${products[0]?.imageUrl || "https://images.unsplash.com/photo-1490481651871-ab68de25d43d?auto=format&fit=crop&w=2000&q=90"}")` }}>
        <div className="hero-copy">
          <p className="eyebrow">A little less, a little better</p>
          <h1>Good things.<br />Made to stay.</h1>
          <p>Everyday pieces from thoughtful makers, selected for the way you really live.</p>
          <Link href="/products" className="button button-light">Explore the collection <span aria-hidden="true">↗</span></Link>
        </div>
        <span className="hero-caption">Objects for the everyday, and all the days after.</span>
      </section>
      <section className="content-section featured-section">
        <div className="section-heading">
          <div><p className="eyebrow">The edit</p><h2>Good finds, lately</h2></div>
          <Link href="/products" className="text-link">See everything <span aria-hidden="true">↗</span></Link>
        </div>
        {loading ? <LoadingState label="Finding the good stuff" />
          : error ? <ErrorState message={error} onRetry={reload} />
            : <div className="product-grid">{products.slice(0, 3).map((product) => <ProductCard key={product.id} product={product} onAdd={onAdd} />)}</div>}
      </section>
      <section className="home-note">
        <p className="eyebrow">The common goods promise</p>
        <p>Useful by design. Easy to love.<br />A little more considered.</p>
        <Link href="/users" className="text-link">Meet the community <span aria-hidden="true">↗</span></Link>
      </section>
    </>
  );
}

function ProductsPage({ onAdd }) {
  const { products, loading, error, reload } = useProducts();
  const [category, setCategory] = useState("Everything");
  const categories = ["Everything", ...new Set(products.map((product) => product.category))];
  const visibleProducts = category === "Everything" ? products : products.filter((product) => product.category === category);
  return (
    <section className="content-section catalog-section">
      <div className="page-intro"><p className="eyebrow">Considered everyday</p><h1>The collection</h1><p>Useful pieces, chosen to be reached for again and again.</p></div>
      {loading ? <LoadingState label="Loading the collection" /> : error ? <ErrorState message={error} onRetry={reload} /> : (
        <>
          <div className="catalog-toolbar"><span>{visibleProducts.length} pieces</span><label className="category-filter">Browse <select value={category} onChange={(event) => setCategory(event.target.value)}>{categories.map((item) => <option key={item}>{item}</option>)}</select></label></div>
          {visibleProducts.length ? <div className="product-grid">{visibleProducts.map((product) => <ProductCard key={product.id} product={product} onAdd={onAdd} />)}</div> : <p className="empty-state">There are no products in this collection yet.</p>}
        </>
      )}
    </section>
  );
}

function ProductDetailsPage({ id, onAdd }) {
  const [result, setResult] = useState({ id: null, product: null, error: "" });

  useEffect(() => {
    let active = true;
    api.getProduct(id).then((product) => { if (active) setResult({ id, product, error: "" }); })
      .catch((requestError) => { if (active) setResult({ id, product: null, error: requestError.message }); });
    return () => { active = false; };
  }, [id]);

  if (result.id !== id) return <LoadingState label="Loading product" />;
  if (result.error) return <ErrorState message={result.error} />;
  const product = result.product;
  return (
    <section className="content-section product-detail">
      <Link href="/products" className="back-link">← Back to the collection</Link>
      <div className="product-detail-layout">
        <div className="detail-image-wrap"><img src={product.imageUrl} alt={product.name} /></div>
        <div className="detail-copy">
          <p className="eyebrow">{product.category} · ★ {Number(product.rating || 0).toFixed(1)}</p>
          <h1>{product.name}</h1>
          <p className="detail-price">{formatPrice(product.price)}</p>
          <p className="detail-description">{product.description}</p>
          <p className="stock-note">{product.stock > 0 ? `${product.stock} available` : "Currently sold out"}</p>
          <button className="button button-dark detail-add" onClick={() => onAdd(product)} disabled={product.stock < 1}>Add to bag</button>
          <p className="detail-meta">Color <strong>{product.color || "Natural"}</strong></p>
          <p className="detail-meta">A useful piece, selected for everyday life and made to be kept.</p>
        </div>
      </div>
    </section>
  );
}

function CartPage({ cart, setQuantity }) {
  const { products, loading, error, reload } = useProducts();
  if (!cart.length) return <section className="content-section"><div className="empty-page"><p className="eyebrow">Your bag</p><h1>It is taking a breather.</h1><p>Your bag is empty for now.</p><Link href="/products" className="button button-dark">Browse the collection</Link></div></section>;
  if (loading) return <LoadingState label="Loading your bag" />;
  if (error) return <ErrorState message={error} onRetry={reload} />;

  const lines = cart.map((item) => ({ ...item, product: products.find((product) => product.id === item.productId) })).filter((item) => item.product);
  const subtotal = lines.reduce((sum, item) => sum + Number(item.product.price) * item.quantity, 0);
  return (
    <section className="content-section cart-section">
      <div className="page-intro"><p className="eyebrow">Your bag</p><h1>The good stuff.</h1></div>
      <div className="cart-layout">
        <div className="cart-lines">{lines.map(({ product, quantity }) => (
          <article className="cart-line" key={product.id}>
            <img src={product.imageUrl} alt={product.name} />
            <div className="cart-line-info"><Link href={`/products/${product.id}`} className="product-name">{product.name}</Link><p>{product.category} · {formatPrice(product.price)}</p>
              <div className="quantity-control" aria-label={`Quantity for ${product.name}`}>
                <button aria-label="Decrease quantity" onClick={() => setQuantity(product.id, quantity - 1, product.stock)}>−</button><span>{quantity}</span><button aria-label="Increase quantity" disabled={quantity >= product.stock} onClick={() => setQuantity(product.id, quantity + 1, product.stock)}>+</button>
              </div>
            </div>
            <div className="cart-line-end"><strong>{formatPrice(Number(product.price) * quantity)}</strong><button className="remove-button" onClick={() => setQuantity(product.id, 0, product.stock)}>Remove</button></div>
          </article>
        ))}</div>
        <aside className="order-summary"><p className="eyebrow">Order summary</p><div><span>Subtotal</span><strong>{formatPrice(subtotal)}</strong></div><div><span>Shipping</span><span>Calculated at checkout</span></div><div className="summary-total"><span>Total</span><strong>{formatPrice(subtotal)}</strong></div><Link href="/checkout" className="button button-dark checkout-button">Continue to checkout <span aria-hidden="true">→</span></Link><Link href="/products" className="text-link">Keep browsing</Link></aside>
      </div>
    </section>
  );
}

function CheckoutPage({ cart, user, onOrderCreated }) {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [form, setForm] = useState({ shippingAddress: "", contactPhone: "" });

  useEffect(() => {
    api.getProducts().then((productData) => {
      setProducts(productData);
    }).catch((requestError) => setError(requestError.message)).finally(() => setLoading(false));
  }, []);

  if (!cart.length) return <section className="content-section"><div className="empty-page"><p className="eyebrow">Checkout</p><h1>Your bag is empty.</h1><Link href="/products" className="button button-dark">Browse the collection</Link></div></section>;
  if (loading) return <LoadingState label="Preparing checkout" />;
  if (error) return <ErrorState message={error} />;

  const orderLines = cart.map((item) => ({ ...item, product: products.find((product) => product.id === item.productId) })).filter((item) => item.product);
  const total = orderLines.reduce((sum, item) => sum + Number(item.product.price) * item.quantity, 0);

  async function submitOrder(event) {
    event.preventDefault();
    setMessage("");
    setSubmitting(true);
    try {
      const order = await api.createOrder({
        shippingAddress: form.shippingAddress,
        contactPhone: form.contactPhone,
        items: cart.map(({ productId, quantity }) => ({ productId, quantity })),
      });
      onOrderCreated(order);
    } catch (requestError) {
      setMessage(requestError.message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="content-section checkout-section">
      <div className="page-intro"><p className="eyebrow">Almost yours</p><h1>Make it yours.</h1></div>
      <div className="checkout-layout">
        <form className="checkout-form" onSubmit={submitOrder}>
          <h2>Delivery details</h2>
          <p className="signed-in-note">Ordering as <strong>{user.username}</strong> · {user.email}</p>
          <label htmlFor="checkout-address">Shipping address</label>
          <textarea id="checkout-address" rows="3" maxLength="500" value={form.shippingAddress} onChange={(event) => setForm({ ...form, shippingAddress: event.target.value })} placeholder="Street, city, postal code" required />
          <label htmlFor="checkout-phone">Phone number</label>
          <input id="checkout-phone" type="tel" minLength="7" maxLength="30" value={form.contactPhone} onChange={(event) => setForm({ ...form, contactPhone: event.target.value })} required />
          {message && <p className="form-message error-message" role="alert">{message}</p>}
          <button className="button button-dark" type="submit" disabled={submitting}>{submitting ? "Confirming demo payment…" : "Confirm demo payment"}</button>
          <p className="field-note">Demo payment is simulated; no card details are collected. Fulfillment remains pending.</p>
        </form>
        <aside className="order-summary checkout-summary"><p className="eyebrow">In your bag</p>{orderLines.map(({ product, quantity }) => <div key={product.id}><span>{product.name} × {quantity}</span><strong>{formatPrice(Number(product.price) * quantity)}</strong></div>)}<div className="summary-total"><span>Total</span><strong>{formatPrice(total)}</strong></div></aside>
      </div>
    </section>
  );
}

function ConfirmationPage() {
  let order;
  try { order = JSON.parse(sessionStorage.getItem("common-goods-order") || "null"); } catch { order = null; }
  if (!order) return <section className="content-section"><div className="empty-page"><p className="eyebrow">Order complete</p><h1>Thank you.</h1><Link href="/products" className="button button-dark">Back to the collection</Link></div></section>;
  return <section className="content-section"><div className="confirmation-page"><span className="confirmation-mark">✓</span><p className="eyebrow">Order {order.id}</p><h1>Good things are on their way.</h1><p>Demo payment: {order.paymentStatus?.replaceAll("_", " ") || "SIMULATED PAID"}. Fulfillment: {order.fulfillmentStatus || "PENDING"}.</p><div className="confirmation-total"><span>Order total</span><strong>{formatPrice(order.totalAmount)}</strong></div><Link href="/products" className="button button-dark">Keep exploring</Link></div></section>;
}

function UsersPage() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [refresh, setRefresh] = useState(0);
  useEffect(() => {
    let active = true;
    api.getUsers().then((data) => { if (active) setUsers(data); })
      .catch((requestError) => { if (active) setError(requestError.message); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [refresh]);
  return (
    <section className="content-section users-section">
      <div className="section-heading"><div><p className="eyebrow">People make a place</p><h1>Our community</h1></div><Link href="/users/new" className="button button-dark">Add a person <span aria-hidden="true">+</span></Link></div>
      {loading ? <LoadingState label="Loading community" /> : error ? <ErrorState message={error} onRetry={() => { setLoading(true); setRefresh((value) => value + 1); }} /> : users.length ? (
        <div className="user-grid">{users.map((user) => <article className="user-card" key={user.id}><span className="user-avatar">{user.username.charAt(0).toUpperCase()}</span><div><h2>{user.username}</h2><p>{user.email}</p></div><Link href={`/users/${user.id}`} className="text-link">View profile <span aria-hidden="true">↗</span></Link></article>)}</div>
      ) : <p className="empty-state">No community members yet.</p>}
    </section>
  );
}

function UserDetailsPage({ id }) {
  const [result, setResult] = useState({ id: null, user: null, error: "" });
  useEffect(() => {
    let active = true;
    api.getUser(id).then((user) => { if (active) setResult({ id, user, error: "" }); })
      .catch((requestError) => { if (active) setResult({ id, user: null, error: requestError.message }); });
    return () => { active = false; };
  }, [id]);
  if (result.id !== id) return <LoadingState label="Finding profile" />;
  if (result.error) return <section className="content-section"><ErrorState message={result.error} /><Link href="/users" className="text-link">Back to community</Link></section>;
  const user = result.user;
  return <section className="content-section"><Link href="/users" className="back-link">← Back to community</Link><div className="profile-page"><span className="user-avatar profile-avatar">{user.username.charAt(0).toUpperCase()}</span><p className="eyebrow">Community member</p><h1>{user.username}</h1><p>{user.email}</p><p className="field-note">Member since {new Date(user.createdAt).toLocaleDateString()}</p></div></section>;
}

function AddUserPage({ onAuthenticated }) {
  const [form, setForm] = useState({ username: "", email: "", password: "" });
  const [message, setMessage] = useState("");
  const [error, setError] = useState(false);
  const [duplicateAccount, setDuplicateAccount] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  async function submitUser(event) {
    event.preventDefault();
    setMessage("");
    setSubmitting(true);
    try {
      await api.createUser(form);
      const account = await api.login({ email: form.email, password: form.password });
      await onAuthenticated(account);
      setForm({ username: "", email: "", password: "" });
      setError(false);
      setDuplicateAccount(false);
      setMessage("Your account is ready, and you are signed in.");
    } catch (requestError) {
      setError(true);
      setDuplicateAccount(requestError.status === 409);
      setMessage(requestError.message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="content-section account-section">
      <div className="page-intro"><p className="eyebrow">Good to have you</p><h1>Join the community.</h1><p>Create an account to make a little space for good things.</p></div>
      <form className="account-form" onSubmit={submitUser}>
        <label htmlFor="username">Name</label><input id="username" name="username" autoComplete="name" minLength="2" maxLength="50" value={form.username} onChange={(event) => setForm({ ...form, username: event.target.value })} required />
        <label htmlFor="email">Email</label><input id="email" name="email" type="email" autoComplete="email" maxLength="100" value={form.email} onChange={(event) => setForm({ ...form, email: event.target.value })} required />
        <label htmlFor="password">Password</label>
        <div className="password-field">
          <input id="password" name="password" type={showPassword ? "text" : "password"} autoComplete="new-password" minLength="8" maxLength="100" value={form.password} onChange={(event) => setForm({ ...form, password: event.target.value })} required />
          <button type="button" className="password-toggle" aria-label={showPassword ? "Hide password" : "Show password"} onClick={() => setShowPassword((visible) => !visible)}>{showPassword ? "Hide" : "Show"}</button>
        </div>
        <p className="field-note">At least 8 characters. Your password is never shown in profile or list responses.</p>
        {message && <p className={`form-message ${error ? "error-message" : "success-message"}`} role={error ? "alert" : "status"}>{message}</p>}
        {duplicateAccount && <Link href="/login" className="text-link duplicate-login-link">Sign in to your existing account <span aria-hidden="true">→</span></Link>}
        <button className="button button-dark" type="submit" disabled={submitting}>{submitting ? "Creating account…" : "Create account"}</button>
      </form>
    </section>
  );
}

function LoginPage({ onAuthenticated }) {
  const [form, setForm] = useState({ email: "", password: "" });
  const [message, setMessage] = useState("");
  const [submitting, setSubmitting] = useState(false);

  async function submitLogin(event) {
    event.preventDefault();
    setMessage("");
    setSubmitting(true);
    try {
      const account = await api.login(form);
      await onAuthenticated(account);
      window.location.hash = "/products";
    } catch (error) {
      setMessage(error.message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="content-section account-section">
      <div className="page-intro"><p className="eyebrow">Welcome back</p><h1>Sign in.</h1><p>Your bag and orders are tied to your account.</p></div>
      <form className="account-form" onSubmit={submitLogin}>
        <label htmlFor="login-email">Email</label><input id="login-email" type="email" autoComplete="email" value={form.email} onChange={(event) => setForm({ ...form, email: event.target.value })} required />
        <label htmlFor="login-password">Password</label><input id="login-password" type="password" autoComplete="current-password" value={form.password} onChange={(event) => setForm({ ...form, password: event.target.value })} required />
        {message && <p className="form-message error-message" role="alert">{message}</p>}
        <button className="button button-dark" type="submit" disabled={submitting}>{submitting ? "Signing in…" : "Sign in"}</button>
        <p className="field-note">New here? <Link href="/users/new" className="text-link">Create an account</Link></p>
      </form>
    </section>
  );
}

function SignInRequired() {
  return (
    <section className="content-section">
      <div className="empty-page"><p className="eyebrow">Account needed</p><h1>Keep your order yours.</h1><p>Sign in to continue to checkout. Your saved bag will be ready when you return.</p><Link href="/login" className="button button-dark">Sign in to checkout</Link></div>
    </section>
  );
}

function NotFoundPage() {
  return <section className="content-section"><div className="empty-page"><p className="eyebrow">Nothing here</p><h1>That page wandered off.</h1><Link href="/" className="button button-dark">Go home</Link></div></section>;
}

export default App;