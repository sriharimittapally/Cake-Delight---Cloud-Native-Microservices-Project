import './Message.css'

// A coloured banner for errors, success and information.
// Usage: <Message type="error">Something went wrong</Message>
// Renders nothing when there is no content.
export default function Message({ type = 'info', children }) {
  if (!children) {
    return null
  }
  return (
    <div className={`message message--${type}`} role={type === 'error' ? 'alert' : 'status'}>
      {children}
    </div>
  )
}
