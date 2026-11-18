import './QuantityStepper.css'

// - [ 2 ] +  control used in the basket.
export default function QuantityStepper({ value, onChange, disabled }) {
  return (
    <div className="quantity-stepper">
      <button type="button" onClick={() => onChange(value - 1)} disabled={disabled || value <= 1} aria-label="Decrease quantity">
        −
      </button>
      <span>{value}</span>
      <button type="button" onClick={() => onChange(value + 1)} disabled={disabled} aria-label="Increase quantity">
        +
      </button>
    </div>
  )
}
