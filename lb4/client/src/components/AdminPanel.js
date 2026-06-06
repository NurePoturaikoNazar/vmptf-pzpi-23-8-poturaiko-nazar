import { useState, useEffect } from 'react';
import { api } from '../api/api';

const emptyForm = { name: '', price: '', description: '', categoryId: '' };

function AdminPanel() {
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const loadData = async () => {
    const [prods, cats] = await Promise.all([
      api.getProducts(),
      api.getCategories(),
    ]);
    setProducts(prods);
    setCategories(cats);
  };

  useEffect(() => {
    loadData().catch(() => setError('Не вдалося завантажити дані'));
  }, []);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    const payload = {
      name: form.name,
      price: parseFloat(form.price),
      description: form.description,
      categoryId: parseInt(form.categoryId, 10),
    };

    try {
      if (editingId) {
        await api.updateProduct(editingId, payload);
        setSuccess('Товар оновлено');
      } else {
        await api.createProduct(payload);
        setSuccess('Товар додано');
      }
      setForm(emptyForm);
      setEditingId(null);
      await loadData();
    } catch (err) {
      setError(err.response?.data?.error || 'Помилка збереження');
    }
  };

  const handleEdit = (product) => {
    setEditingId(product.id);
    setForm({
      name: product.name,
      price: product.price,
      description: product.description || '',
      categoryId: String(product.categoryId),
    });
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Видалити цей товар?')) return;

    try {
      await api.deleteProduct(id);
      setSuccess('Товар видалено');
      if (editingId === id) {
        setEditingId(null);
        setForm(emptyForm);
      }
      await loadData();
    } catch (err) {
      setError(err.response?.data?.error || 'Помилка видалення');
    }
  };

  const handleCancel = () => {
    setEditingId(null);
    setForm(emptyForm);
  };

  return (
    <div className="admin-panel">
      <h1>Адмін-панель</h1>
      <p className="admin-subtitle">Управління товарами спекулянт.юа</p>

      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      <form className="admin-form" onSubmit={handleSubmit}>
        <h2>{editingId ? 'Редагувати товар' : 'Додати товар'}</h2>

        <div className="form-row">
          <label>
            Назва
            <input
              name="name"
              value={form.name}
              onChange={handleChange}
              required
            />
          </label>
          <label>
            Ціна (₴)
            <input
              name="price"
              type="number"
              step="0.01"
              min="0"
              value={form.price}
              onChange={handleChange}
              required
            />
          </label>
        </div>

        <label>
          Категорія
          <select
            name="categoryId"
            value={form.categoryId}
            onChange={handleChange}
            required
          >
            <option value="">Оберіть категорію</option>
            {categories.map((cat) => (
              <option key={cat.id} value={cat.id}>{cat.name}</option>
            ))}
          </select>
        </label>

        <label>
          Опис
          <textarea
            name="description"
            value={form.description}
            onChange={handleChange}
            rows={3}
          />
        </label>

        <div className="form-actions">
          <button type="submit" className="btn btn-primary">
            {editingId ? 'Зберегти' : 'Додати'}
          </button>
          {editingId && (
            <button type="button" className="btn btn-outline" onClick={handleCancel}>
              Скасувати
            </button>
          )}
        </div>
      </form>

      <div className="admin-table-wrap">
        <h2>Список товарів</h2>
        <table className="admin-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Назва</th>
              <th>Категорія</th>
              <th>Ціна</th>
              <th>Дії</th>
            </tr>
          </thead>
          <tbody>
            {products.map((product) => (
              <tr key={product.id}>
                <td>{product.id}</td>
                <td>{product.name}</td>
                <td>{product.category?.name}</td>
                <td>{parseFloat(product.price).toFixed(2)} ₴</td>
                <td className="actions-cell">
                  <button
                    type="button"
                    className="btn btn-outline btn-sm"
                    onClick={() => handleEdit(product)}
                  >
                    Редагувати
                  </button>
                  <button
                    type="button"
                    className="btn btn-danger btn-sm"
                    onClick={() => handleDelete(product.id)}
                  >
                    Видалити
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

export default AdminPanel;
