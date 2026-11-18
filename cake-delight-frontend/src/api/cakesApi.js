import { request } from './client.js'

// GET /api/cakes?name=&category=&minPrice=&maxPrice=   (empty filters are left out)
export function getCakes(filters = {}) {
  const params = new URLSearchParams()
  Object.entries(filters).forEach(([key, value]) => {
    if (value !== '' && value !== null && value !== undefined) {
      params.set(key, value)
    }
  })
  const query = params.toString()
  return request(`/cakes${query ? `?${query}` : ''}`)
}

export function getAdminCakes() {
  return request('/cakes/admin')
}

// POST /api/cakes   (admin dashboard)
export function addCake(cake) {
  return request('/cakes', { method: 'POST', body: JSON.stringify(cake) })
}

// PATCH /api/cakes/{id}/availability
export function toggleCakeAvailability(id) {
  return request(`/cakes/${id}/availability`, {
    method: 'PATCH'
  })
}

// DELETE /api/cakes/{id}   (admin dashboard)
export function deleteCake(id) {
  return request(`/cakes/${id}`, { method: 'DELETE' })
}
