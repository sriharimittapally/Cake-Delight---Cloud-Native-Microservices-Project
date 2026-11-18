import { request } from './client.js'

// POST /api/orders/checkout/{basketId}  with { customerName, customerEmail, deliveryAddress }
export function checkout(basketId, customer) {
  return request(`/orders/checkout/${basketId}`, {
    method: 'POST',
    body: JSON.stringify(customer),
  })
}

// GET /api/orders/customer?email=
export function getOrdersByEmail(email) {
  return request(`/orders/customer?email=${encodeURIComponent(email)}`)
}
