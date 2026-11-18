// Small display helpers shared by the pages.

const money = new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' })

export function formatPrice(value) {
  return money.format(Number(value))
}

// The backend sends dates like 2026-11-15T10:20:00.123456 (no time zone).
export function formatDate(value) {
  return new Date(String(value).slice(0, 19)).toLocaleString('en-IN', {
    dateStyle: 'medium',
    timeStyle: 'short',
  })
}
