import { formatPrice } from "../../utils/format.js";
import "./AdminCakeTable.css";

// Table of all cakes with a Delete button (admin dashboard).
export default function AdminCakeTable({
  cakes,
  deletingId,
  onDelete,
  onToggleAvailability
}) {
  if (cakes.length === 0) {
    return (
      <p className="empty-state">There are no cakes yet. Add the first one.</p>
    );
  }

  return (
    <div className="admin-cake-table__wrapper">
      <table className="admin-cake-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Name</th>
            <th>Category</th>
            <th>Price</th>
            <th>Status</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {cakes.map((cake) => (
            <tr key={cake.id}>
              <td>{cake.id}</td>
              <td>{cake.name}</td>
              <td>{cake.category}</td>
              <td>{formatPrice(cake.price)}</td>
              <td>
                <label>
                  <input
                    type="checkbox"
                    checked={cake.available}
                    onChange={() => onToggleAvailability(cake)}
                  />
                  {cake.available ? "Available" : "Unavailable"}
                </label>
              </td>
              <td>
                <button
                  type="button"
                  className="btn btn--danger btn--small"
                  disabled={deletingId === cake.id}
                  onClick={() => onDelete(cake)}
                >
                  {deletingId === cake.id ? "Deleting..." : "Delete"}
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
