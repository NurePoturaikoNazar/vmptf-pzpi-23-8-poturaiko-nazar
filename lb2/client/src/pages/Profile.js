import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/api';
import { useAuth } from '../context/AuthContext';
import StarRating from '../components/StarRating';
import styles from './Profile.module.css';

function Profile() {
  const { user } = useAuth();
  const [enrollments, setEnrollments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    api
      .getEnrollments()
      .then(setEnrollments)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  const handleToggleComplete = async (enrollmentId) => {
    try {
      const result = await api.toggleCourseComplete(enrollmentId);
      setEnrollments((prev) =>
        prev.map((e) =>
          e.enrollment_id === enrollmentId ? { ...e, course_completed: result.completed } : e
        )
      );
    } catch (err) {
      alert(err.message);
    }
  };

  const getProgress = (enrollment) => {
    if (!enrollment.total_lectures) return 0;
    return Math.round((enrollment.completed_lectures / enrollment.total_lectures) * 100);
  };

  if (loading) {
    return (
      <div className="page-loading">
        <div className="spinner" />
        <p>Завантаження профілю...</p>
      </div>
    );
  }

  return (
    <div className={styles.profile}>
      <div className={styles.header}>
        <div className={styles.avatar}>
          {user.name.charAt(0).toUpperCase()}
        </div>
        <div>
          <h1>{user.name}</h1>
          <p>{user.email}</p>
        </div>
      </div>

      <section className={styles.section}>
        <h2>📚 Мої курси</h2>

        {error && <p className={styles.error}>{error}</p>}

        {enrollments.length === 0 ? (
          <div className={styles.empty}>
            <p>Ви ще не записані на жоден курс</p>
            <Link to="/" className={styles.browseBtn}>
              Переглянути каталог
            </Link>
          </div>
        ) : (
          <div className={styles.courseList}>
            {enrollments.map((enrollment) => {
              const progress = getProgress(enrollment);
              return (
                <div key={enrollment.enrollment_id} className={styles.courseItem}>
                  <img src={enrollment.image} alt={enrollment.title} className={styles.thumb} />
                  <div className={styles.courseInfo}>
                    <Link to={`/course/${enrollment.id}`} className={styles.courseTitle}>
                      {enrollment.title}
                    </Link>
                    <div className={styles.meta}>
                      <StarRating rating={enrollment.avg_rating || 0} />
                      <span>👤 {enrollment.teacher_name}</span>
                    </div>
                    <div className={styles.progressBar}>
                      <div
                        className={styles.progressFill}
                        style={{ width: `${progress}%` }}
                      />
                    </div>
                    <span className={styles.progressText}>
                      {enrollment.completed_lectures} / {enrollment.total_lectures} лекцій ({progress}%)
                    </span>
                  </div>
                  <div className={styles.actions}>
                    <Link to={`/course/${enrollment.id}`} className={styles.continueBtn}>
                      Продовжити
                    </Link>
                    <button
                      type="button"
                      className={`${styles.completeBtn} ${
                        enrollment.course_completed ? styles.completed : ''
                      }`}
                      onClick={() => handleToggleComplete(enrollment.enrollment_id)}
                    >
                      {enrollment.course_completed ? '✓ Завершено' : 'Завершити курс'}
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </section>
    </div>
  );
}

export default Profile;
