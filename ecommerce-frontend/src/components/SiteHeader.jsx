import { Link } from "./SiteLink.jsx";

export default function SiteHeader({ cartCount, user, onLogout }) {
  return (
    <header className="site-header">
      <Link href="/" className="brand" aria-label="Common Goods home">
        <span className="brand-mark">CG</span>
        <span>common goods</span>
      </Link>
      <nav className="main-nav" aria-label="Main navigation">
        <Link href="/products">Shop</Link>
        <Link href="/users">Community</Link>
        {user ? <button className="nav-action" onClick={onLogout}>Sign out</button> : <Link href="/login" className="auth-link">Sign in</Link>}
        {!user && <Link href="/users/new" className="join-link">Join</Link>}
        <Link href="/cart" className="cart-link">Bag <span>{cartCount}</span></Link>
      </nav>
    </header>
  );
}