import { Link } from 'react-router-dom';
import StarRating from './StarRating';
import styles from './CourseCard.module.css';

function CourseCard({ course, onEnroll, enrolling }) {
  const handleEnroll = (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (onEnroll) onEnroll(course.id);
  };

  return (
    <article className={styles.card}>
      <Link to={`/course/${course.id}`} className={styles.imageLink}>
        <img src={course.image} alt={course.title} className={styles.image} />
        <span className={styles.price}>{course.price} ₴</span>
      </Link>

      <div className={styles.body}>
        <Link to={`/course/${course.id}`} className={styles.title}>
          {course.title}
        </Link>

        <p className={styles.description}>{course.description}</p>

        <div className={styles.meta}>
          <span className={styles.duration}>⏱ {course.duration}</span>
          {course.teacher_name && (
            <span className={styles.teacher}>👤 {course.teacher_name}</span>
          )}
        </div>

        <div className={styles.footer}>
          <div className={styles.ratingRow}>
            <StarRating rating={course.avg_rating || 0} />
            {course.review_count > 0 && (
              <span className={styles.reviewCount}>({course.review_count})</span>
            )}
          </div>

          {course.is_enrolled ? (
            <span className={styles.enrolledBadge}>✓ Записано</span>
          ) : (
            <button
              type="button"
              className={styles.enrollBtn}
              onClick={handleEnroll}
              disabled={enrolling}
            >
              {enrolling ? '...' : 'Записатися'}
            </button>
          )}
        </div>
      </div>
    </article>
  );
}

export default CourseCard;
