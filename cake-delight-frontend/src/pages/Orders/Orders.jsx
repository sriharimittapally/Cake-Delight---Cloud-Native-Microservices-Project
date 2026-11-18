import { useEffect, useState } from 'react'
import { useLocation } from 'react-router-dom'
import { getOrdersByEmail } from '../../api/ordersApi.js'
import { getRatingsByEmail } from '../../api/ratingsApi.js'
import Loader from '../../components/Loader/Loader.jsx'
import Message from '../../components/Message/Message.jsx'
import OrderCard from '../../components/OrderCard/OrderCard.jsx'
import RatingModal from '../../components/RatingModal/RatingModal.jsx'
import './Orders.css'

const EMAIL_KEY = 'cakeDelightEmail'

// Orders of a customer. There is no login: the customer types the email used at checkout,
// sees the orders, and can rate each purchased cake.
export default function Orders() {
  const location = useLocation()
  const placedEmail = location.state?.email // set by the basket page after checkout
  const placedOrderId = location.state?.placedOrderId

  const [email, setEmail] = useState(placedEmail || localStorage.getItem(EMAIL_KEY) || '')
  const [orders, setOrders] = useState(null) // null = nobody searched yet
  const [rated, setRated] = useState({}) // "orderId-cakeId" -> rating number
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const [ratingTarget, setRatingTarget] = useState(null) // { order, item }

  async function loadOrders(emailToSearch) {
    setLoading(true)
    setError('')
    setNotice('')
    try {
      const [orderList, ratingList] = await Promise.all([
        getOrdersByEmail(emailToSearch),
        getRatingsByEmail(emailToSearch),
      ])
      // newest order first
      setOrders([...orderList].sort((a, b) => b.orderId - a.orderId))
      setRated(Object.fromEntries(ratingList.map((r) => [`${r.orderId}-${r.cakeId}`, r.rating])))
      localStorage.setItem(EMAIL_KEY, emailToSearch)
    } catch (err) {
      setOrders(null)
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  // Right after checkout, show the customer's orders without asking again.
  useEffect(() => {
    if (placedEmail) {
      loadOrders(placedEmail)
    }
  }, [placedEmail])

  function handleSubmit(event) {
    event.preventDefault()
    loadOrders(email.trim())
  }

  function handleRated(saved) {
    setRated({ ...rated, [`${saved.orderId}-${saved.cakeId}`]: saved.rating })
    setRatingTarget(null)
    setNotice('Thank you! Your rating was saved.')
  }

  return (
    <div className="container">
      <h1 className="page-title">My orders</h1>
      <p className="page-subtitle">Enter the email you used at checkout to see your orders and rate your cakes.</p>

      {placedOrderId && (
        <Message type="success">
          Order #{placedOrderId} was placed. A confirmation email is on its way.
        </Message>
      )}

      <form className="orders__search card" onSubmit={handleSubmit}>
        <div className="field">
          <label htmlFor="orders-email">Email</label>
          <input id="orders-email" type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
        </div>
        <button type="submit" className="btn btn--primary" disabled={loading}>
          Find my orders
        </button>
      </form>

      <Message type="error">{error}</Message>
      <Message type="success">{notice}</Message>

      {loading && <Loader text="Looking for your orders..." />}

      {!loading && orders && orders.length === 0 && (
        <p className="empty-state">No orders found for this email.</p>
      )}

      {!loading &&
        orders &&
        orders.map((order) => (
          <OrderCard
            key={order.orderId}
            order={order}
            rated={rated}
            onRate={(selectedOrder, item) => setRatingTarget({ order: selectedOrder, item })}
          />
        ))}

      {ratingTarget && (
        <RatingModal
          order={ratingTarget.order}
          item={ratingTarget.item}
          onClose={() => setRatingTarget(null)}
          onSubmitted={handleRated}
        />
      )}
    </div>
  )
}
