import styles from './StarRating.module.css';

function StarRating({ rating, onRate, interactive = false, size = 'md' }) {
  const stars = [1, 2, 3, 4, 5];
  const displayRating = Math.round(rating * 10) / 10;

  return (
    <div className={`${styles.rating} ${styles[size]}`}>
      {stars.map((star) => (
        <button
          key={star}
          type="button"
          className={`${styles.star} ${star <= Math.round(rating) ? styles.filled : ''} ${
            interactive ? styles.interactive : ''
          }`}
          onClick={interactive && onRate ? () => onRate(star) : undefined}
          disabled={!interactive}
          aria-label={`${star} зірок`}
        >
          ★
        </button>
      ))}
      {rating > 0 && (
        <span className={styles.value}>{interactive ? `${rating} зірок` : displayRating}</span>
      )}
    </div>
  );
}

export default StarRating;
