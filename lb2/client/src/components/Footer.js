import styles from './Footer.module.css';

function Footer() {
  return (
    <footer className={styles.footer}>
      <div className={styles.container}>
        <div className={styles.brand}>
          <span className={styles.logo}>📚 SkillUp</span>
          <p>Онлайн-платформа для навчання новим навичкам</p>
        </div>
        <div className={styles.info}>
          <p className={styles.copy}>© 2026 SkillUp. Всі права захищені.</p>
        </div>
      </div>
    </footer>
  );
}

export default Footer;
