import { useState } from 'react'
import Modal from '../Modal/Modal.jsx'
import Message from '../Message/Message.jsx'
import StarRating from '../StarRating/StarRating.jsx'
import { submitRating } from '../../api/ratingsApi.js'
import './RatingModal.css'

// Pop-up to rate one cake of one order.
// The backend checks that this order (and email) really contains this cake.
export default function RatingModal({ order, item, onClose, onSubmitted }) {
  const [rating, setRating] = useState(0)
  const [review, setReview] = useState('')
  const [name, setName] = useState(order.customerName)
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(event) {
    event.preventDefault()
    if (rating === 0) {
      setError('Please choose a star rating.')
      return
    }

    setSubmitting(true)
    setError('')
    try {
      const saved = await submitRating({
        orderId: order.orderId,
        cakeId: item.cakeId,
        customerName: name.trim(),
        customerEmail: order.customerEmail,
        rating,
        review: review.trim() || undefined,
      })
      onSubmitted(saved)
    } catch (err) {
      setError(err.message)
      setSubmitting(false)
    }
  }

  return (
    <Modal title={`Rate ${item.cakeName}`} onClose={onClose}>
      <form className="rating-modal" onSubmit={handleSubmit}>
        <Message type="error">{error}</Message>

        <div className="rating-modal__stars">
          <StarRating value={rating} onChange={setRating} />
        </div>

        <div className="field">
          <label htmlFor="rating-name">Your name</label>
          <input id="rating-name" value={name} onChange={(e) => setName(e.target.value)} required />
        </div>

        <div className="field">
          <label htmlFor="rating-review">Review (optional)</label>
          <textarea id="rating-review" rows="3" maxLength="1000" value={review} onChange={(e) => setReview(e.target.value)} />
        </div>

        <button type="submit" className="btn btn--primary" disabled={submitting}>
          {submitting ? 'Saving...' : 'Submit rating'}
        </button>
      </form>
    </Modal>
  )
}
