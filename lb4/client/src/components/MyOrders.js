import { useState, useEffect } from 'react';
import { api } from '../api/api';

const statusLabels = {
  pending: 'Очікує',
  processing: 'Обробляється',
  shipped: 'Відправлено',
  delivered: 'Доставлено',
  cancelled: 'Скасовано',
};

function MyOrders() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    api
      .getOrders()
      .then(setOrders)
      .catch(() => setError('Не вдалося завантажити замовлення'))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="loading">Завантаження замовлень...</div>;
  if (error) return <div className="alert alert-error">{error}</div>;

  return (
    <div className="orders-page">
      <h1>Мої замовлення</h1>

      {orders.length === 0 ? (
        <p className="empty-msg">У вас ще немає замовлень</p>
      ) : (
        <div className="orders-list">
          {orders.map((order) => (
            <div key={order.id} className="order-card">
              <div className="order-header">
                <span>Замовлення #{order.id}</span>
                <span className={`status status-${order.status}`}>
                  {statusLabels[order.status] || order.status}
                </span>
              </div>
              <div className="order-meta">
                <span>{new Date(order.createdAt).toLocaleString('uk-UA')}</span>
                <strong>{parseFloat(order.totalAmount).toFixed(2)} ₴</strong>
              </div>
              <ul className="order-items">
                {order.products?.map((product) => (
                  <li key={product.id}>
                    {product.name} × {product.OrderItem?.quantity || 1}
                    <span>
                      {(parseFloat(product.OrderItem?.price || product.price) *
                        (product.OrderItem?.quantity || 1)).toFixed(2)} ₴
                    </span>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default MyOrders;
