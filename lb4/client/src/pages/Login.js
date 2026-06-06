import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      await login(email, password);
      navigate('/');
    } catch (err) {
      setError(err.response?.data?.error || 'Помилка входу');
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <h1>Вхід</h1>
        <p className="auth-subtitle">спекулянт.юа</p>

        {error && <div className="alert alert-error">{error}</div>}

        <form onSubmit={handleSubmit}>
          <label>
            Email
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </label>
          <label>
            Пароль
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </label>
          <button type="submit" className="btn btn-primary btn-block">
            Увійти
          </button>
        </form>

        <p className="auth-footer">
          Немає акаунту? <Link to="/register">Зареєструватися</Link>
        </p>

        <div className="demo-credentials">
          <small>Демо-адмін: admin@spekulant.ua / admin123</small>
        </div>
      </div>
    </div>
  );
}

export default Login;
