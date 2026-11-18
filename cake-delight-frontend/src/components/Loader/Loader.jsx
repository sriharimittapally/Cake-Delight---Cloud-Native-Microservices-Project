import './Loader.css'

export default function Loader({ text = 'Loading...' }) {
  return (
    <p className="loader" role="status">
      {text}
    </p>
  )
}
