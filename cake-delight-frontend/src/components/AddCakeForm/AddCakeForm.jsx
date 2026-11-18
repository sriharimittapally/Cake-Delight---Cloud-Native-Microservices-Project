import { useState } from 'react'
import './AddCakeForm.css'

const emptyForm = { name: '', category: '', price: '', description: '', imageReference: '', available: true }

// Admin form to add a cake. onAdd(payload) must return true when the cake was saved,
// so the form can clear itself.
export default function AddCakeForm({ onAdd }) {
  const [form, setForm] = useState(emptyForm)
  const [saving, setSaving] = useState(false)

  function handleChange(event) {
    const { name, value, type, checked } = event.target
    setForm({ ...form, [name]: type === 'checkbox' ? checked : value })
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setSaving(true)
    const saved = await onAdd({
      name: form.name.trim(),
      category: form.category.trim(),
      price: Number(form.price),
      description: form.description.trim() || undefined,
      imageReference: form.imageReference.trim() || undefined,
      available: form.available,
    })
    setSaving(false)
    if (saved) {
      setForm(emptyForm)
    }
  }

  return (
    <form className="add-cake-form card" onSubmit={handleSubmit}>
      <h2>Add a cake</h2>

      <div className="field">
        <label htmlFor="cake-name">Name</label>
        <input id="cake-name" name="name" value={form.name} onChange={handleChange} required />
      </div>

      <div className="field">
        <label htmlFor="cake-category">Category</label>
        <input id="cake-category" name="category" value={form.category} onChange={handleChange} required />
      </div>

      <div className="field">
        <label htmlFor="cake-price">Price</label>
        <input id="cake-price" name="price" type="number" min="0.01" step="0.01" value={form.price} onChange={handleChange} required />
      </div>

      <div className="field">
        <label htmlFor="cake-description">Description</label>
        <textarea id="cake-description" name="description" rows="3" value={form.description} onChange={handleChange} />
      </div>

      <div className="field">
        <label htmlFor="cake-image">Image link (optional)</label>
        <input id="cake-image" name="imageReference" value={form.imageReference} onChange={handleChange} />
      </div>

      <div className="field field--inline">
        <input id="cake-available" name="available" type="checkbox" checked={form.available} onChange={handleChange} />
        <label htmlFor="cake-available">Available</label>
      </div>

      <button type="submit" className="btn btn--primary" disabled={saving}>
        {saving ? 'Saving...' : 'Add cake'}
      </button>
    </form>
  )
}
