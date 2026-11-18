import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { checkout } from '../../api/ordersApi.js'
import { useBasket } from '../../context/BasketContext.jsx'
import CheckoutForm from '../../components/CheckoutForm/CheckoutForm.jsx'
import Loader from '../../components/Loader/Loader.jsx'
import Message from '../../components/Message/Message.jsx'
import QuantityStepper from '../../components/QuantityStepper/QuantityStepper.jsx'
import { formatPrice } from '../../utils/format.js'
import './Basket.css'

// Basket contents, quantity changes and the checkout form.
export default function Basket() {
  const navigate = useNavigate()
  const { basket, loading, updateQuantity, removeItem, clearBasket } = useBasket()

  const [busyItemId, setBusyItemId] = useState(null)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')

  if (loading) {
    return (
      <div className="container">
        <Loader text="Loading your basket..." />
      </div>
    )
  }

  const items = basket?.items || []

  if (items.length === 0) {
    return (
      <div className="container">
        <h1 className="page-title">Your basket</h1>
        <Message type="error">{error}</Message>
        <div className="empty-state card">
          <p>Your basket is empty.</p>
          <Link to="/cakes" className="btn btn--primary">
            Browse cakes
          </Link>
        </div>
      </div>
    )
  }

  // Runs one basket action while disabling that row's buttons.
  async function runItemAction(itemId, action) {
    setBusyItemId(itemId)
    setError('')
    try {
      await action()
    } catch (err) {
      setError(err.message)
    } finally {
      setBusyItemId(null)
    }
  }

  async function handleCheckout(customer) {
    setSubmitting(true)
    setError('')
    try {
      const order = await checkout(basket.basketId, customer)
      clearBasket()
      // Go to the orders page, which shows the new order straight away.
      navigate('/orders', { state: { email: order.customerEmail, placedOrderId: order.orderId } })
    } catch (err) {
      setError(err.message)
      setSubmitting(false)
    }
  }

  return (
    <div className="container">
      <h1 className="page-title">Your basket</h1>
      <Message type="error">{error}</Message>

      <div className="basket__layout">
        <section className="basket__items card">
          {items.map((item) => (
            <div key={item.itemId} className="basket__row">
              <div className="basket__info">
                <strong>{item.cakeName}</strong>
                <span>{formatPrice(item.unitPrice)} each</span>
              </div>

              <QuantityStepper
                value={item.quantity}
                disabled={busyItemId === item.itemId}
                onChange={(quantity) => runItemAction(item.itemId, () => updateQuantity(item.itemId, quantity))}
              />

              <span className="basket__subtotal">{formatPrice(item.subtotal)}</span>

              <button
                type="button"
                className="btn btn--danger btn--small"
                disabled={busyItemId === item.itemId}
                onClick={() => runItemAction(item.itemId, () => removeItem(item.itemId))}
              >
                Remove
              </button>
            </div>
          ))}

          <div className="basket__total">
            <span>Total</span>
            <strong>{formatPrice(basket.totalAmount)}</strong>
          </div>
        </section>

        <CheckoutForm submitting={submitting} onSubmit={handleCheckout} />
      </div>
    </div>
  )
}
