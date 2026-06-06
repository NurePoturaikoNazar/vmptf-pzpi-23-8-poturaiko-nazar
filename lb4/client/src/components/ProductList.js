import { useState, useEffect } from 'react';
import { api } from '../api/api';
import { useAuth } from '../context/AuthContext';

function ProductList() {
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [search, setSearch] = useState('');
  const [categoryId, setCategoryId] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [cart, setCart] = useState([]);
  const [orderMsg, setOrderMsg] = useState('');
  const { isAuthenticated } = useAuth();

  useEffect(() => {
    api.getCategories().then(setCategories).catch(() => {});
  }, []);

  useEffect(() => {
    const timer = setTimeout(() => {
      setLoading(true);
      const params = {};
      if (search.trim()) params.search = search.trim();
      if (categoryId) params.categoryId = categoryId;

      api
        .getProducts(params)
        .then(setProducts)
        .catch(() => setError('Не вдалося завантажити товари'))
        .finally(() => setLoading(false));
    }, 300);

    return () => clearTimeout(timer);
  }, [search, categoryId]);

  const addToCart = (product) => {
    setCart((prev) => {
      const existing = prev.find((item) => item.productId === product.id);
      if (existing) {
        return prev.map((item) =>
          item.productId === product.id
            ? { ...item, quantity: item.quantity + 1 }
            : item
        );
      }
      return [...prev, { productId: product.id, quantity: 1, product }];
    });
    setOrderMsg('');
  };

  const removeFromCart = (productId) => {
    setCart((prev) => prev.filter((item) => item.productId !== productId));
  };

  const cartTotal = cart.reduce(
    (sum, item) => sum + parseFloat(item.product.price) * item.quantity,
    0
  );

  const handleOrder = async () => {
    if (!isAuthenticated) {
      setOrderMsg('Увійдіть, щоб оформити замовлення');
      return;
    }
    if (cart.length === 0) return;

    try {
      await api.createOrder(
        cart.map(({ productId, quantity }) => ({ productId, quantity }))
      );
      setCart([]);
      setOrderMsg('Замовлення успішно оформлено!');
    } catch (err) {
      setOrderMsg(err.response?.data?.error || 'Помилка оформлення замовлення');
    }
  };

  return (
    <div className="catalog">
      <div className="catalog-header">
        <h1>Каталог товарів</h1>
        <div className="filters">
          <input
            type="text"
            className="search-input"
            placeholder="Пошук товарів..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
          <select
            className="filter-select"
            value={categoryId}
            onChange={(e) => setCategoryId(e.target.value)}
          >
            <option value="">Усі категорії</option>
            {categories.map((cat) => (
              <option key={cat.id} value={cat.id}>{cat.name}</option>
            ))}
          </select>
        </div>
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {loading ? (
        <div className="loading">Завантаження товарів...</div>
      ) : (
        <div className="product-grid">
          {products.map((product) => (
            <div key={product.id} className="product-card">
              <div className="product-category">{product.category?.name}</div>
              <h3>{product.name}</h3>
              <p className="product-desc">{product.description}</p>
              <div className="product-footer">
                <span className="product-price">{parseFloat(product.price).toFixed(2)} ₴</span>
                <button
                  type="button"
                  className="btn btn-primary btn-sm"
                  onClick={() => addToCart(product)}
                >
                  У кошик
                </button>
              </div>
            </div>
          ))}
          {products.length === 0 && (
            <p className="empty-msg">Товарів не знайдено</p>
          )}
        </div>
      )}

      {cart.length > 0 && (
        <div className="cart-panel">
          <h3>Кошик ({cart.length})</h3>
          <ul className="cart-list">
            {cart.map((item) => (
              <li key={item.productId}>
                <span>{item.product.name} × {item.quantity}</span>
                <span>{(parseFloat(item.product.price) * item.quantity).toFixed(2)} ₴</span>
                <button
                  type="button"
                  className="btn-icon"
                  onClick={() => removeFromCart(item.productId)}
                  title="Видалити"
                >
                  ×
                </button>
              </li>
            ))}
          </ul>
          <div className="cart-total">
            <strong>Разом: {cartTotal.toFixed(2)} ₴</strong>
            <button type="button" className="btn btn-primary" onClick={handleOrder}>
              Оформити замовлення
            </button>
          </div>
          {orderMsg && (
            <div className={`alert ${orderMsg.includes('успішно') ? 'alert-success' : 'alert-error'}`}>
              {orderMsg}
            </div>
          )}
        </div>
      )}
    </div>
  );
}

export default ProductList;
