import './Modal.css'

// A simple pop-up window. Clicking the dark background or the x closes it.
export default function Modal({ title, onClose, children }) {
  return (
    <div className="modal__backdrop" onClick={onClose}>
      <div
        className="modal__dialog"
        role="dialog"
        aria-modal="true"
        aria-label={title}
        onClick={(event) => event.stopPropagation()}
      >
        <div className="modal__header">
          <h2>{title}</h2>
          <button type="button" className="modal__close" onClick={onClose} aria-label="Close">
            ×
          </button>
        </div>
        {children}
      </div>
    </div>
  )
}
