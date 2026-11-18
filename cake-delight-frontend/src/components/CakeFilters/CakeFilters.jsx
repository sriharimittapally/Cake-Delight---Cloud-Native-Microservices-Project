import { useState } from 'react'
import './CakeFilters.css'

const emptyForm = { name: '', category: '', minPrice: '', maxPrice: '' }

// Search box + category + price range. Sends only the filled-in values to the page.
export default function CakeFilters({ categories, onApply, onClear }) {
  const [form, setForm] = useState(emptyForm)

  function handleChange(event) {
    setForm({ ...form, [event.target.name]: event.target.value })
  }

  function handleSubmit(event) {
    event.preventDefault()
    const filters = {}
    Object.entries(form).forEach(([key, value]) => {
      if (value.trim() !== '') {
        filters[key] = value.trim()
      }
    })
    onApply(filters)
  }

  function handleClear() {
    setForm(emptyForm)
    onClear()
  }

  return (
    <form className="cake-filters card" onSubmit={handleSubmit}>
      <div className="field">
        <label htmlFor="filter-name">Name</label>
        <input id="filter-name" name="name" value={form.name} onChange={handleChange} placeholder="e.g. chocolate" />
      </div>

      <div className="field">
        <label htmlFor="filter-category">Category</label>
        <select id="filter-category" name="category" value={form.category} onChange={handleChange}>
          <option value="">All categories</option>
          {categories.map((category) => (
            <option key={category} value={category}>
              {category}
            </option>
          ))}
        </select>
      </div>

      <div className="field">
        <label htmlFor="filter-min">Min price</label>
        <input id="filter-min" name="minPrice" type="number" min="0" value={form.minPrice} onChange={handleChange} />
      </div>

      <div className="field">
        <label htmlFor="filter-max">Max price</label>
        <input id="filter-max" name="maxPrice" type="number" min="0" value={form.maxPrice} onChange={handleChange} />
      </div>

      <div className="cake-filters__actions">
        <button type="submit" className="btn btn--primary">
          Search
        </button>
        <button type="button" className="btn btn--outline" onClick={handleClear}>
          Clear
        </button>
      </div>
    </form>
  )
}
