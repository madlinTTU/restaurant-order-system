import { NavLink } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'

const navLink = ({ isActive }: { isActive: boolean }) =>
  `text-sm transition-colors ${isActive ? 'text-gray-900 font-medium' : 'text-gray-400 hover:text-gray-700'}`

export default function Navbar() {
  const { isAuthenticated, isAdmin } = useAuth()

  return (
    <nav className="bg-white border-b border-gray-200 px-6 py-3 flex items-center justify-between">
      <span className="font-semibold text-gray-900 text-sm">Restaurant</span>
      <div className="flex items-center gap-4">
        <NavLink to="/" className={navLink}>Menu</NavLink>
        {isAuthenticated && (
          <NavLink to="/orders" className={navLink}>My Orders</NavLink>
        )}
        {isAdmin && (
          <NavLink to="/admin/menu" className={navLink}>Admin</NavLink>
        )}
        <NavLink to="/login" className={navLink}>Login</NavLink>
      </div>
    </nav>
  )
}
