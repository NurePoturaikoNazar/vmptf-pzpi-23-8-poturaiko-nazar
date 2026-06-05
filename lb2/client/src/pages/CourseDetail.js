import { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { api } from '../api/api';
import { useAuth } from '../context/AuthContext';
import StarRating from '../components/StarRating';
import styles from './CourseDetail.module.css';

function CourseDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();

  const [course, setCourse] = useState(null);
  const [loading, setLoading] = useState(true);
  const [enrolling, setEnrolling] = useState(false);
  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState('');
  const [submittingReview, setSubmittingReview] = useState(false);
  const [error, setError] = useState('');
  const [activeLectureId, setActiveLectureId] = useState(null);

  const loadCourse = useCallback(async () => {
    try {
      const data = await api.getCourse(id);
      setCourse(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    setLoading(true);
    loadCourse();
  }, [loadCourse, isAuthenticated]);

  const handleEnroll = async () => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }

    setEnrolling(true);
    try {
      await api.enroll(course.id);
      await loadCourse();
    } catch (err) {
      alert(err.message);
    } finally {
      setEnrolling(false);
    }
  };

  const handleLectureToggle = async (lectureId) => {
    try {
      const result = await api.toggleLectureComplete(lectureId);
      setCourse((prev) => ({
        ...prev,
        lectureProgress: prev.lectureProgress.map((lp) =>
          lp.lecture_id === lectureId ? { ...lp, completed: result.completed } : lp
        ),
      }));
    } catch (err) {
      alert(err.message);
    }
  };

  const handleReviewSubmit = async (e) => {
    e.preventDefault();
    setSubmittingReview(true);
    try {
      const result = await api.addReview({
        course_id: course.id,
        rating,
        comment,
      });
      setCourse((prev) => ({
        ...prev,
        reviews: [result.review, ...prev.reviews],
        avg_rating: result.avg_rating,
        review_count: result.review_count,
        user_has_reviewed: true,
      }));
      setComment('');
      setRating(5);
    } catch (err) {
      alert(err.message);
    } finally {
      setSubmittingReview(false);
    }
  };

  const getLectureCompleted = (lectureId) => {
    const progress = course.lectureProgress?.find((lp) => lp.lecture_id === lectureId);
    return progress?.completed === 1;
  };

  const getLectureContent = (lecture) => {
    return `У цій лекції "${lecture.title}" коротко розказано основні поняття та практичні поради. Ви знайдете цікаві приклади та корисні підказки для подальшого вивчення.`;
  };

  if (loading) {
    return (
      <div className="page-loading">
        <div className="spinner" />
        <p>Завантаження курсу...</p>
      </div>
    );
  }

  if (error || !course) {
    return (
      <div className={styles.notFound}>
        <h2>Курс не знайдено</h2>
        <p>{error}</p>
        <Link to="/" className={styles.backLink}>← На головну</Link>
      </div>
    );
  }

  const isEnrolled = !!course.enrollment;

  return (
    <div className={styles.detail}>
      <div className={styles.hero}>
        <img src={course.image} alt={course.title} className={styles.heroImage} />
        <div className={styles.heroOverlay}>
          <Link to="/" className={styles.backLink}>← Назад до курсів</Link>
          <h1>{course.title}</h1>
          <div className={styles.heroMeta}>
            <StarRating rating={course.avg_rating || 0} />
            <span>⏱ {course.duration}</span>
            <span className={styles.price}>{course.price} ₴</span>
          </div>
          <p className={styles.description}>{course.description}</p>

          {isEnrolled ? (
            <span className={styles.enrolledBadge}>✓ Ви записані на цей курс</span>
          ) : (
            <button
              type="button"
              className={styles.enrollBtn}
              onClick={handleEnroll}
              disabled={enrolling}
            >
              {enrolling ? 'Запис...' : 'Записатися на курс'}
            </button>
          )}
        </div>
      </div>

      <div className={styles.content}>
        <div className={styles.main}>
          <section className={styles.section}>
            <h2>📖 Програма курсу</h2>
            <ul className={styles.lectureList}>
              {course.lectures.map((lecture, index) => (
                <li key={lecture.id} className={styles.lectureItem}>
                  <span className={styles.lectureNum}>{index + 1}</span>
                  <button
                    type="button"
                    className={styles.lectureTitleBtn}
                    onClick={() => setActiveLectureId(activeLectureId === lecture.id ? null : lecture.id)}
                  >
                    {lecture.title}
                  </button>
                  {isEnrolled && (
                    <button
                      type="button"
                      className={`${styles.completeBtn} ${
                        getLectureCompleted(lecture.id) ? styles.completed : ''
                      }`}
                      onClick={() => handleLectureToggle(lecture.id)}
                    >
                      {getLectureCompleted(lecture.id) ? '✓ Виконано' : 'Позначити'}
                    </button>
                  )}

                  {activeLectureId === lecture.id && (
                    <div className={styles.lectureContent}>
                      <p>{getLectureContent(lecture)}</p>
                    </div>
                  )}
                </li>
              ))}
            </ul>
          </section>

          <section className={styles.section}>
            <h2>💬 Відгуки ({course.review_count || 0})</h2>

            {isEnrolled && !course.user_has_reviewed && (
              <form className={styles.reviewForm} onSubmit={handleReviewSubmit}>
                <label>Ваша оцінка:</label>
                <StarRating rating={rating} onRate={setRating} interactive size="lg" />
                <textarea
                  value={comment}
                  onChange={(e) => setComment(e.target.value)}
                  placeholder="Напишіть відгук про курс..."
                  rows={4}
                />
                <button type="submit" disabled={submittingReview}>
                  {submittingReview ? 'Надсилання...' : 'Залишити відгук'}
                </button>
              </form>
            )}

            {course.reviews.length === 0 ? (
              <p className={styles.noReviews}>Поки що немає відгуків. Будьте першим!</p>
            ) : (
              <div className={styles.reviewList}>
                {course.reviews.map((review) => (
                  <div key={review.id} className={styles.reviewCard}>
                    <div className={styles.reviewHeader}>
                      <strong>{review.user_name}</strong>
                      <StarRating rating={review.rating} />
                    </div>
                    {review.comment && <p>{review.comment}</p>}
                    <span className={styles.reviewDate}>
                      {new Date(review.created_at).toLocaleDateString('uk-UA')}
                    </span>
                  </div>
                ))}
              </div>
            )}
          </section>
        </div>

        <aside className={styles.sidebar}>
          <div className={styles.teacherCard}>
            <h3>Викладач</h3>
            <img
              src={course.teacher_avatar}
              alt={course.teacher_name}
              className={styles.teacherAvatar}
            />
            <h4>{course.teacher_name}</h4>
            <span className={styles.specialty}>{course.teacher_specialty}</span>
            <p>{course.teacher_bio}</p>
          </div>
        </aside>
      </div>
    </div>
  );
}

export default CourseDetail;
