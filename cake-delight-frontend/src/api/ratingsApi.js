import { request } from './client.js'

// POST /api/ratings  with { orderId, cakeId, customerName, customerEmail, rating, review }
export function submitRating(rating) {
  return request('/ratings', { method: 'POST', body: JSON.stringify(rating) })
}

// GET /api/ratings/summary  ->  [{ cakeId, averageRating, totalRatings }]
export function getRatingSummaries() {
  return request('/ratings/summary')
}

// GET /api/ratings/customer?email=  ->  ratings this email has already submitted
export function getRatingsByEmail(email) {
  return request(`/ratings/customer?email=${encodeURIComponent(email)}`)
}
