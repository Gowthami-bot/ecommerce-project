import { Link } from "./SiteLink.jsx";
import { formatPrice } from "../utils/formatPrice.js";

export default function ProductCard({ product, onAdd }) {
  return (
    <article className="product-card">
      <Link href={`/products/${product.id}`} className="product-image-link" aria-label={`View ${product.name}`}>
        <img className="product-image" src={product.imageUrl} alt={product.name} loading="lazy" />
        <span className="product-category">{product.category}</span>
      </Link>
      <div className="product-card-copy">
        <div>
          <Link href={`/products/${product.id}`} className="product-name">{product.name}</Link>
          <p className="product-meta">{product.color || product.category} · ★ {Number(product.rating || 0).toFixed(1)}</p>
        </div>
        <strong>{formatPrice(product.price)}</strong>
      </div>
      <button className="button button-outline product-add" onClick={() => onAdd(product)} disabled={product.stock < 1}>
        {product.stock < 1 ? "Sold out" : "Add to bag"}
      </button>
    </article>
  );
}