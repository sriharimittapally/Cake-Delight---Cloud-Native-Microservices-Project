import { request, ApiError } from './client.js'

// POST /api/baskets  ->  { basketId, status, items, totalAmount }
export async function createBasket() {
  try {
    return await request('/baskets', { method: 'POST' })
  } catch (error) {
    // The API Gateway only forwards paths it has a route for.
    if (error.status === 404) {
      throw new ApiError(
        'The gateway has no /api/baskets route yet. Add it as described in the README ("Backend change").',
        404,
      )
    }
    throw error
  }
}

// GET /api/baskets/{basketId}
export function getBasket(basketId) {
  return request(`/baskets/${basketId}`)
}

// POST /api/baskets/{basketId}/items
export function addItem(basketId, cakeId, quantity) {
  return request(`/baskets/${basketId}/items`, {
    method: 'POST',
    body: JSON.stringify({ cakeId, quantity }),
  })
}

// PUT /api/baskets/{basketId}/items/{itemId}
export function updateItem(basketId, itemId, quantity) {
  return request(`/baskets/${basketId}/items/${itemId}`, {
    method: 'PUT',
    body: JSON.stringify({ quantity }),
  })
}

// DELETE /api/baskets/{basketId}/items/{itemId}
export function removeItem(basketId, itemId) {
  return request(`/baskets/${basketId}/items/${itemId}`, { method: 'DELETE' })
}
