import CakeImage from '../CakeImage/CakeImage.jsx'
import StarRating from '../StarRating/StarRating.jsx'
import { formatPrice } from '../../utils/format.js'
import './CakeCard.css'

// One cake in the grid: picture, name, rating, price and the "Add to basket" button.
// cake    = response of GET /api/cakes
// summary = { averageRating, totalRatings } from GET /api/ratings/summary (may be missing)
export default function CakeCard({ cake, summary, adding, onAdd }) {
  const hasRatings = summary && summary.totalRatings > 0

  return (
    <article className="cake-card">
      <CakeImage src={cake.imageReference} alt={cake.name} />

      <div className="cake-card__body">
        <span className="cake-card__category">{cake.category}</span>
        <h3 className="cake-card__name">{cake.name}</h3>
        <p className="cake-card__description">{cake.description}</p>

        <div className="cake-card__rating">
          {hasRatings ? (
            <>
              <StarRating value={summary.averageRating} />
              <span>
                {Number(summary.averageRating).toFixed(1)} ({summary.totalRatings})
              </span>
            </>
          ) : (
            <span className="cake-card__no-rating">No ratings yet</span>
          )}
        </div>

        <div className="cake-card__footer">
          <span className="cake-card__price">{formatPrice(cake.price)}</span>
          <button
            type="button"
            className="btn btn--primary btn--small"
            disabled={!cake.available || adding}
            onClick={() => onAdd(cake)}
          >
            {!cake.available ? 'Unavailable' : adding ? 'Adding...' : 'Add to basket'}
          </button>
        </div>
      </div>
    </article>
  )
}
