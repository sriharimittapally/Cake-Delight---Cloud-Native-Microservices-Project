import { formatDate, formatPrice } from '../../utils/format.js'
import StarRating from '../StarRating/StarRating.jsx'
import './OrderCard.css'

// One order with its items. Each item has a "Rate" button, or the rating already given.
// rated = { "orderId-cakeId": ratingNumber }
export default function OrderCard({ order, rated, onRate }) {
  return (
    <article className="order-card card">
      <header className="order-card__header">
        <div>
          <h3>Order #{order.orderId}</h3>
          <span className="order-card__date">{formatDate(order.createdAt)}</span>
        </div>
        <span className="order-card__status">{order.status}</span>
      </header>

      <ul className="order-card__items">
        {order.items.map((item) => {
          const givenRating = rated[`${order.orderId}-${item.cakeId}`]
          return (
            <li key={item.orderItemId} className="order-card__item">
              <div className="order-card__item-info">
                <strong>{item.cakeName}</strong>
                <span>
                  {item.quantity} × {formatPrice(item.unitPrice)}
                </span>
              </div>
              <span className="order-card__item-subtotal">{formatPrice(item.subtotal)}</span>
              <div className="order-card__item-rate">
                {givenRating ? (
                  <span className="order-card__rated">
                    <StarRating value={givenRating} /> Rated
                  </span>
                ) : (
                  <button type="button" className="btn btn--outline btn--small" onClick={() => onRate(order, item)}>
                    Rate
                  </button>
                )}
              </div>
            </li>
          )
        })}
      </ul>

      <footer className="order-card__footer">
        <span>Delivery: {order.deliveryAddress}</span>
        <strong>Total {formatPrice(order.totalAmount)}</strong>
      </footer>
    </article>
  )
}
