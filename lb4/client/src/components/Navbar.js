import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

function Navbar() {
  const { user, isAuthenticated, isAdmin, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <nav className="navbar">
      <div className="navbar-inner">
        <Link to="/" className="logo">
          спекулянт<span className="logo-domain">.юа</span>
        </Link>

        <div className="nav-links">
          <Link to="/">Каталог</Link>
          {isAuthenticated && <Link to="/orders">Мої замовлення</Link>}
          {isAdmin && <Link to="/admin">Адмін-панель</Link>}
        </div>

        <div className="nav-auth">
          {isAuthenticated ? (
            <>
              <span className="user-greeting">{user.username}</span>
              <button type="button" className="btn btn-outline" onClick={handleLogout}>
                Вийти
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="btn btn-outline">Увійти</Link>
              <Link to="/register" className="btn btn-primary">Реєстрація</Link>
            </>
          )}
        </div>
      </div>
    </nav>
  );
}

export default Navbar;
