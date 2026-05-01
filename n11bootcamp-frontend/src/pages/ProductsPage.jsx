import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getCategories, getPagedProducts, getProducts } from "../services/api";

const PAGE_SIZE = 8;

export default function ProductsPage() {
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [categoryKey, setCategoryKey] = useState("");
  const [searchTerm, setSearchTerm] = useState("");
  const [pageInfo, setPageInfo] = useState({
    page: 0,
    totalPages: 0,
    totalElements: 0,
    isFirst: true,
    isLast: true
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    async function loadCategories() {
      try {
        const data = await getCategories();
        setCategories(Array.isArray(data) ? data : []);
      } catch {
        setCategories([]);
      }
    }

    loadCategories();
  }, []);

  useEffect(() => {
    async function loadProducts() {
      setLoading(true);
      setError("");

      try {
        if (categoryKey || searchTerm.trim()) {
          const data = await getProducts();
          const normalizedSearch = searchTerm.trim().toLowerCase();
          const filteredProducts = (Array.isArray(data) ? data : [])
            .filter((product) => !categoryKey || product.categoryKey === categoryKey)
            .filter((product) => {
              if (!normalizedSearch) return true;

              return (product.title || "").toLowerCase().includes(normalizedSearch);
            });
          const totalPages = Math.ceil(filteredProducts.length / PAGE_SIZE);
          const startIndex = pageInfo.page * PAGE_SIZE;

          setProducts(filteredProducts.slice(startIndex, startIndex + PAGE_SIZE));
          setPageInfo({
            page: pageInfo.page,
            totalPages,
            totalElements: filteredProducts.length,
            isFirst: pageInfo.page === 0,
            isLast: pageInfo.page >= totalPages - 1
          });
        } else {
          const data = await getPagedProducts(pageInfo.page, PAGE_SIZE);
          setProducts(Array.isArray(data.items) ? data.items : []);
          setPageInfo({
            page: data.page,
            totalPages: data.totalPages,
            totalElements: data.totalElements,
            isFirst: data.isFirst,
            isLast: data.isLast
          });
        }
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    }

    loadProducts();
  }, [pageInfo.page, categoryKey, searchTerm]);

  function goToPage(nextPage) {
    setPageInfo((current) => ({
      ...current,
      page: nextPage
    }));
  }

  function handleCategoryChange(event) {
    setCategoryKey(event.target.value);
    setPageInfo((current) => ({
      ...current,
      page: 0
    }));
  }

  function handleSearchChange(event) {
    setSearchTerm(event.target.value);
    setPageInfo((current) => ({
      ...current,
      page: 0
    }));
  }

  return (
    <section className="content-panel">
      <div className="page-title">
        <div>
          <h1>Products</h1>
          <p>
            {pageInfo.totalElements > 0
              ? `${pageInfo.totalElements} products`
              : "Products from the backend."}
          </p>
        </div>

        <div className="product-tools">
          <label>
            Search
            <input
              type="search"
              value={searchTerm}
              onChange={handleSearchChange}
              placeholder="Search by name"
            />
          </label>

          <label>
            Category
            <select value={categoryKey} onChange={handleCategoryChange}>
              <option value="">All categories</option>
              {categories.map((category) => (
                <option key={category.id || category.key} value={category.key}>
                  {category.name}
                </option>
              ))}
            </select>
          </label>
        </div>
      </div>

      {loading && (
        <div className="loading-state">
          <span className="spinner" aria-hidden="true"></span>
          <span>Loading products...</span>
        </div>
      )}
      {error && <div className="error-message">{error}</div>}

      {!loading && !error && products.length === 0 && (
        <p className="status-text">No products found.</p>
      )}

      <div className="product-grid">
        {products.map((product) => (
          <Link className="product-card" key={product.id} to={`/products/${product.id}`}>
            {product.img ? (
              <img src={product.img} alt={product.title || product.brand || "Product"} />
            ) : (
              <div className="product-image-placeholder">No image</div>
            )}
            <div className="product-info">
              <h2>{product.title || "Untitled product"}</h2>
              <p>{product.brand || product.category || "Product"}</p>
              <strong>{product.price} TL</strong>
            </div>
          </Link>
        ))}
      </div>

      {!loading && !error && pageInfo.totalPages > 1 && (
        <div className="pagination">
          <button
            className="secondary-button"
            type="button"
            disabled={pageInfo.isFirst}
            onClick={() => goToPage(pageInfo.page - 1)}
          >
            Previous
          </button>
          <span>
            Page {pageInfo.page + 1} of {pageInfo.totalPages}
          </span>
          <button
            className="secondary-button"
            type="button"
            disabled={pageInfo.isLast}
            onClick={() => goToPage(pageInfo.page + 1)}
          >
            Next
          </button>
        </div>
      )}
    </section>
  );
}
