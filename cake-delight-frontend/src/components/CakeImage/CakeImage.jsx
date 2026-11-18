import { useState } from 'react'
import './CakeImage.css'

// Shows the cake picture. A soft placeholder is always behind it, so if the
// image link is missing or broken the card still looks fine.
export default function CakeImage({ src, alt }) {
  const [failed, setFailed] = useState(false)

  return (
    <div className="cake-image">
      <span className="cake-image__placeholder" aria-hidden="true">
        🍰
      </span>
      {src && !failed && (
        <img className="cake-image__img" src={src} alt={alt} loading="lazy" onError={() => setFailed(true)} />
      )}
    </div>
  )
}
