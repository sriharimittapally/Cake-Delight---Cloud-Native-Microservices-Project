// One small helper used by every API file.
// Every call goes straight to the API Gateway. Its address comes from VITE_API_BASE_URL
// (see .env.example); if that is not set, the default below is used.

const BASE_URL = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api').replace(/\/$/, '')

// Error with the HTTP status and the message sent by the backend.
export class ApiError extends Error {
  constructor(message, status) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

export async function request(path, options = {}) {
  let response

  try {
    response = await fetch(`${BASE_URL}${path}`, {
      ...options,
      headers: options.body ? { 'Content-Type': 'application/json' } : undefined,
    })
  } catch {
    // The request never got an answer: backend stopped, wrong address, or the browser
    // blocked it because the gateway does not allow this site yet (CORS).
    throw new ApiError(
      'Cannot reach the server. Check that the backend is running and that the gateway allows this site (CORS, see BACKEND_CHANGES.md).',
      0,
    )
  }

  // 204 No Content (for example after deleting something): nothing to read.
  if (response.status === 204) {
    return null
  }

  // Read the body safely: it is JSON for the backend, but could be empty or plain text.
  let data = null
  const text = await response.text()
  if (text) {
    try {
      data = JSON.parse(text)
    } catch {
      data = null
    }
  }

  if (!response.ok) {
    // The backend sends errors as { timestamp, status, error, message }.
    throw new ApiError(data?.message || `Request failed (${response.status})`, response.status)
  }

  return data
}
