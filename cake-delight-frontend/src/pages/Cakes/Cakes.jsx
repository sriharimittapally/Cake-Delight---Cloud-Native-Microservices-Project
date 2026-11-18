import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getCakes } from '../../api/cakesApi.js'
import { getRatingSummaries } from '../../api/ratingsApi.js'
import { useBasket } from '../../context/BasketContext.jsx'
import CakeCard from '../../components/CakeCard/CakeCard.jsx'
import CakeFilters from '../../components/CakeFilters/CakeFilters.jsx'
import Loader from '../../components/Loader/Loader.jsx'
import Message from '../../components/Message/Message.jsx'
import './Cakes.css'

// Browse and filter cakes, add them to the basket.
export default function Cakes() {
  const { addToBasket } = useBasket()

  const [cakes, setCakes] = useState([])
  const [categories, setCategories] = useState([])
  const [summaries, setSummaries] = useState({}) // cakeId -> { averageRating, totalRatings }
  const [filters, setFilters] = useState({})
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const [addingId, setAddingId] = useState(null)

  // Average ratings for the cards (if this fails the cards simply show "No ratings yet").
  useEffect(() => {
    getRatingSummaries()
      .then((list) => setSummaries(Object.fromEntries(list.map((item) => [item.cakeId, item]))))
      .catch(() => {})
  }, [])

  // Load cakes at start and every time the filters change.
  useEffect(() => {
    let ignore = false
    setLoading(true)
    setError('')

    getCakes(filters)
      .then((data) => {
        if (ignore) return
        setCakes(data)
        // Without filters we see every cake, so this is the full category list.
        if (Object.keys(filters).length === 0) {
          setCategories([...new Set(data.map((cake) => cake.category))].sort())
        }
      })
      .catch((err) => {
        if (!ignore) setError(err.message)
      })
      .finally(() => {
        if (!ignore) setLoading(false)
      })

    return () => {
      ignore = true
    }
  }, [filters])

  async function handleAdd(cake) {
    setAddingId(cake.id)
    setNotice('')
    setError('')
    try {
      await addToBasket(cake.id, 1)
      setNotice(`${cake.name} was added to your basket.`)
    } catch (err) {
      setError(err.message)
    } finally {
      setAddingId(null)
    }
  }

  return (
    <div className="container">
      <h1 className="page-title">Our cakes</h1>
      <p className="page-subtitle">Search, filter and add your favourites to the basket.</p>

      <CakeFilters categories={categories} onApply={setFilters} onClear={() => setFilters({})} />

      <Message type="error">{error}</Message>
      <Message type="success">
        {notice && (
          <>
            {notice} <Link to="/basket">View basket</Link>
          </>
        )}
      </Message>

      {loading ? (
        <Loader text="Loading cakes..." />
      ) : cakes.length === 0 && !error ? (
        <p className="empty-state">No cakes match your search.</p>
      ) : (
        <div className="cakes__grid">
          {cakes.map((cake) => (
            <CakeCard
              key={cake.id}
              cake={cake}
              summary={summaries[cake.id]}
              adding={addingId === cake.id}
              onAdd={handleAdd}
            />
          ))}
        </div>
      )}
    </div>
  )
}
