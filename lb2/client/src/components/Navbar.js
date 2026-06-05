import { Link, NavLink } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import styles from './Navbar.module.css';

function Navbar() {
  const { user, isAuthenticated, logout } = useAuth();

  return (
    <header className={styles.navbar}>
      <div className={styles.container}>
        <Link to="/" className={styles.logo}>
          <span className={styles.logoIcon}>📚</span>
          SkillUp
        </Link>

        <nav className={styles.nav}>
          <NavLink to="/" end className={({ isActive }) => (isActive ? styles.active : '')}>
            Курси
          </NavLink>
          {isAuthenticated && (
            <NavLink to="/profile" className={({ isActive }) => (isActive ? styles.active : '')}>
              Мої курси
            </NavLink>
          )}
        </nav>

        <div className={styles.actions}>
          {isAuthenticated ? (
            <>
              <span className={styles.userName}>Привіт, {user.name.split(' ')[0]}!</span>
              <button type="button" className={styles.logoutBtn} onClick={logout}>
                Вийти
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className={styles.loginLink}>
                Увійти
              </Link>
              <Link to="/register" className={styles.registerBtn}>
                Реєстрація
              </Link>
            </>
          )}
        </div>
      </div>
    </header>
  );
}

export default Navbar;
