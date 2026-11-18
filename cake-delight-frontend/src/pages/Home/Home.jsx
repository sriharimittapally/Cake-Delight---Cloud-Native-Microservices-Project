import { Link } from 'react-router-dom'
import './Home.css'

const steps = [
  { icon: '🔍', title: 'Browse', text: 'Search and filter our cakes by name, category and price.' },
  { icon: '🛒', title: 'Order', text: 'Add cakes to your basket and check out with your email.' },
  { icon: '⭐', title: 'Rate', text: 'Find your orders with your email and rate the cakes you bought.' },
]

export default function Home() {
  return (
    <div className="container">
      <section className="home__hero">
        <h1>Freshly baked cakes for every celebration</h1>
        <p>Pick your favourite, we bake it and bring it to your door.</p>
        <div className="home__actions">
          <Link to="/cakes" className="btn btn--primary">
            Browse cakes
          </Link>
          <Link to="/orders" className="btn btn--outline">
            My orders
          </Link>
        </div>
      </section>

      <section className="home__steps">
        {steps.map((step) => (
          <div key={step.title} className="home__step card">
            <span className="home__step-icon">{step.icon}</span>
            <h3>{step.title}</h3>
            <p>{step.text}</p>
          </div>
        ))}
      </section>
    </div>
  )
}
