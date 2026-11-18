import { useCallback, useEffect, useState } from 'react'
import { addCake, deleteCake,toggleCakeAvailability ,getAdminCakes } from '../../api/cakesApi.js'
import AddCakeForm from '../../components/AddCakeForm/AddCakeForm.jsx'
import AdminCakeTable from '../../components/AdminCakeTable/AdminCakeTable.jsx'
import Loader from '../../components/Loader/Loader.jsx'
import Message from '../../components/Message/Message.jsx'
import './AdminDashboard.css'

// Demo admin page: add and delete cakes. There is no login on purpose (simulation).
export default function AdminDashboard() {
  const [cakes, setCakes] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const [deletingId, setDeletingId] = useState(null)

  const loadCakes = useCallback(async () => {
    try {
      setCakes(await getAdminCakes())
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    loadCakes()
  }, [loadCakes])

  // Returns true when the cake was saved (the form uses it to clear itself).
  async function handleAdd(cake) {
    setError('')
    setNotice('')
    try {
      const saved = await addCake(cake)
      setNotice(`"${saved.name}" was added.`)
      await loadCakes()
      return true
    } catch (err) {
      setError(err.message)
      return false
    }
  }

  async function handleToggleAvailability(cake) {
  setError('')
  setNotice('')

  try {
    await toggleCakeAvailability(cake.id)

    setNotice(
      `"${cake.name}" is now ${cake.available ? 'unavailable' : 'available'}.`
    )

    await loadCakes()
  } catch (err) {
    setError(err.message)
  }
}

  async function handleDelete(cake) {
    if (!window.confirm(`Delete "${cake.name}"?`)) {
      return
    }
    setDeletingId(cake.id)
    setError('')
    setNotice('')
    try {
      await deleteCake(cake.id)
      setNotice(`"${cake.name}" was deleted.`)
      await loadCakes()
    } catch (err) {
      setError(err.message)
    } finally {
      setDeletingId(null)
    }
  }

  return (
    <div className="container">
      <h1 className="page-title">Admin Dashboard</h1>
      <p className="page-subtitle">Manage the cake catalog. (Demo: anyone can open this page.)</p>

      <Message type="error">{error}</Message>
      <Message type="success">{notice}</Message>

      <div className="admin__layout">
        <AddCakeForm onAdd={handleAdd} />

        <section>
          <h2>All cakes ({cakes.length})</h2>
          {loading ? (
            <Loader text="Loading cakes..." />
          ) : (
            <AdminCakeTable cakes={cakes} deletingId={deletingId} onDelete={handleDelete} onToggleAvailability={handleToggleAvailability} />
          )}
        </section>
      </div>
    </div>
  )
}
