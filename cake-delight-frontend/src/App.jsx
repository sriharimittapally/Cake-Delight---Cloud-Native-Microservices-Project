import { Navigate, Route, Routes } from 'react-router-dom'
import Navbar from './components/Navbar/Navbar.jsx'
import AdminDashboard from './pages/AdminDashboard/AdminDashboard.jsx'
import Basket from './pages/Basket/Basket.jsx'
import Cakes from './pages/Cakes/Cakes.jsx'
import Home from './pages/Home/Home.jsx'
import Orders from './pages/Orders/Orders.jsx'

// The page layout and the list of pages (routes).
export default function App() {
  return (
    <>
      <Navbar />
      <main>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/cakes" element={<Cakes />} />
          <Route path="/basket" element={<Basket />} />
          <Route path="/orders" element={<Orders />} />
          <Route path="/admin" element={<AdminDashboard />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
    </>
  )
}
