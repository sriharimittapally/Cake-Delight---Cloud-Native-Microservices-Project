import { NavLink, useLocation } from 'react-router-dom'
import { useBasket } from '../../context/BasketContext.jsx'
import './Navbar.css'

// Top bar. Inside the admin dashboard it switches to a minimal version
// with a "Back to shop" button (demo only: there is no login).
export default function Navbar() {
  const { itemCount } = useBasket()
  const { pathname } = useLocation()
  const inAdmin = pathname.startsWith('/admin')

  return (
    <header className="navbar">
      <div className="navbar__inner">
        <NavLink to="/" className="navbar__brand">
          🎂 Cake Delight
        </NavLink>

        {inAdmin ? (
          <div className="navbar__right">
            <span className="navbar__badge">Admin demo</span>
            <NavLink to="/" className="btn btn--outline btn--small">
              ← Back to shop
            </NavLink>
          </div>
        ) : (
          <>
            <nav className="navbar__links">
              <NavLink to="/" end>
                Home
              </NavLink>
              <NavLink to="/cakes">Cakes</NavLink>
              <NavLink to="/orders">Orders</NavLink>
              <NavLink to="/basket">
                Basket
                {itemCount > 0 && <span className="navbar__count">{itemCount}</span>}
              </NavLink>
            </nav>
            <NavLink to="/admin" className="btn btn--outline btn--small">
              Admin Dashboard
            </NavLink>
          </>
        )}
      </div>
    </header>
  )
}
