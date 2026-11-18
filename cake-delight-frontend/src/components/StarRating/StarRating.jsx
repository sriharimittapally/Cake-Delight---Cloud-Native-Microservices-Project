import './StarRating.css'

// Shows stars. Without onChange it is read-only (average rating);
// with onChange the stars are buttons the user can click (rating form).
export default function StarRating({ value = 0, onChange }) {
  const rounded = Math.round(value)

  return (
    <span className="star-rating" aria-label={`${value} out of 5 stars`}>
      {[1, 2, 3, 4, 5].map((star) => {
        const filled = star <= rounded
        const symbol = filled ? '★' : '☆'

        if (!onChange) {
          return (
            <span key={star} className={filled ? 'star-rating__star is-filled' : 'star-rating__star'}>
              {symbol}
            </span>
          )
        }

        return (
          <button
            key={star}
            type="button"
            className={filled ? 'star-rating__star star-rating__star--button is-filled' : 'star-rating__star star-rating__star--button'}
            onClick={() => onChange(star)}
            aria-label={`${star} star${star > 1 ? 's' : ''}`}
          >
            {symbol}
          </button>
        )
      })}
    </span>
  )
}
