import { useState } from 'react'
import './CheckoutForm.css'

// Customer details asked at checkout (there is no login, so this is the customer's identity).
// onSubmit receives { customerName, customerEmail, deliveryAddress }.
export default function CheckoutForm({ submitting, onSubmit }) {
  const [form, setForm] = useState({ customerName: '', customerEmail: '', deliveryAddress: '' })

  function handleChange(event) {
    setForm({ ...form, [event.target.name]: event.target.value })
  }

  function handleSubmit(event) {
    event.preventDefault()
    onSubmit({
      customerName: form.customerName.trim(),
      customerEmail: form.customerEmail.trim(),
      deliveryAddress: form.deliveryAddress.trim(),
    })
  }

  return (
    <form className="checkout-form card" onSubmit={handleSubmit}>
      <h2>Checkout</h2>

      <div className="field">
        <label htmlFor="checkout-name">Your name</label>
        <input id="checkout-name" name="customerName" value={form.customerName} onChange={handleChange} required />
      </div>

      <div className="field">
        <label htmlFor="checkout-email">Email (for the confirmation and to find your orders later)</label>
        <input id="checkout-email" name="customerEmail" type="email" value={form.customerEmail} onChange={handleChange} required />
      </div>

      <div className="field">
        <label htmlFor="checkout-address">Delivery address</label>
        <textarea id="checkout-address" name="deliveryAddress" rows="3" value={form.deliveryAddress} onChange={handleChange} required />
      </div>

      <button type="submit" className="btn btn--primary" disabled={submitting}>
        {submitting ? 'Placing order...' : 'Place order'}
      </button>
    </form>
  )
}
