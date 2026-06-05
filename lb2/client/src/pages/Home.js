import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { api } from '../api/api';
import { useAuth } from '../context/AuthContext';
import CourseCard from '../components/CourseCard';
import styles from './Home.module.css';

function Home() {
  const [courses, setCourses] = useState([]);
  const [teachers, setTeachers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [enrollingId, setEnrollingId] = useState(null);
  const [error, setError] = useState('');
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();

  const loadData = async () => {
    try {
      const [coursesData, teachersData] = await Promise.all([
        api.getCourses(),
        api.getTeachers(),
      ]);
      setCourses(coursesData);
      setTeachers(teachersData);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [isAuthenticated]);

  const handleEnroll = async (courseId) => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }

    setEnrollingId(courseId);
    try {
      await api.enroll(courseId);
      setCourses((prev) =>
        prev.map((c) => (c.id === courseId ? { ...c, is_enrolled: true } : c))
      );
    } catch (err) {
      alert(err.message);
    } finally {
      setEnrollingId(null);
    }
  };

  if (loading) {
    return (
      <div className="page-loading">
        <div className="spinner" />
        <p>Завантаження курсів...</p>
      </div>
    );
  }

  return (
    <div className={styles.home}>
      <section className={styles.hero}>
        <div className={styles.heroContent}>
          <span className={styles.badge}>🎓 Навчайся з комфортом</span>
          <h1>
            Розвивай навички з <span className={styles.highlight}>SkillUp</span>
          </h1>
          <p className={styles.heroText}>
            Онлайн-курси від досвідчених викладачів. Записуйся, навчайся у своєму темпі
            та відстежуй прогрес у особистому кабінеті.
          </p>
          <div className={styles.heroActions}>
            <a href="#courses" className={styles.primaryBtn}>
              Переглянути курси
            </a>
            {!isAuthenticated && (
              <Link to="/register" className={styles.secondaryBtn}>
                Почати безкоштовно
              </Link>
            )}
          </div>
          <div className={styles.stats}>
            <div className={styles.stat}>
              <strong>{courses.length}</strong>
              <span>Курсів</span>
            </div>
            <div className={styles.stat}>
              <strong>{teachers.length}</strong>
              <span>Викладачів</span>
            </div>
            <div className={styles.stat}>
              <strong>24/7</strong>
              <span>Доступ</span>
            </div>
          </div>
        </div>
        <div className={styles.heroVisual}>
          <div className={styles.floatingCard}>
            <span>⭐ 4.8</span>
            <p>Середній рейтинг курсів</p>
          </div>
          <div className={styles.floatingCard2}>
            <span>📈 +120%</span>
            <p>Зростання навичок</p>
          </div>
        </div>
      </section>

      <section className={styles.features}>
        <h2>Чому SkillUp?</h2>
        <div className={styles.featureGrid}>
          <div className={styles.feature}>
            <span className={styles.featureIcon}>🎯</span>
            <h3>Практичні курси</h3>
            <p>Реальні проєкти та завдання для закріплення знань</p>
          </div>
          <div className={styles.feature}>
            <span className={styles.featureIcon}>👨‍🏫</span>
            <h3>Експерти</h3>
            <p>Викладачі з досвідом у провідних IT-компаніях</p>
          </div>
          <div className={styles.feature}>
            <span className={styles.featureIcon}>📊</span>
            <h3>Відстеження прогресу</h3>
            <p>Позначай лекції виконаними та бач свій розвиток</p>
          </div>
        </div>
      </section>

      <section id="courses" className={styles.courses}>
        <div className={styles.sectionHeader}>
          <h2>Каталог курсів</h2>
          <p>Обери курс та почни навчання вже сьогодні</p>
        </div>

        {error && <p className={styles.error}>{error}</p>}

        <div className={styles.courseGrid}>
          {courses.map((course) => (
            <CourseCard
              key={course.id}
              course={course}
              onEnroll={handleEnroll}
              enrolling={enrollingId === course.id}
            />
          ))}
        </div>
      </section>

      <section className={styles.teachers}>
        <div className={styles.sectionHeader}>
          <h2>Наші викладачі</h2>
          <p>Професіонали, які допоможуть тобі досягти цілей</p>
        </div>
        <div className={styles.teacherGrid}>
          {teachers.map((teacher) => (
            <div key={teacher.id} className={styles.teacherCard}>
              <img src={teacher.avatar} alt={teacher.name} className={styles.teacherAvatar} />
              <h3>{teacher.name}</h3>
              <span className={styles.specialty}>{teacher.specialty}</span>
              <p>{teacher.bio}</p>
              <span className={styles.courseCount}>{teacher.course_count} курсів</span>
            </div>
          ))}
        </div>
      </section>
    </div>
  );
}

export default Home;
