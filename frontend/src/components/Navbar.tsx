import { NavLink, useNavigate } from 'react-router-dom'
import { ShoppingCart } from 'lucide-react'
import { useAuth } from '@/hooks/useAuth'
import { useCart } from '@/contexts/CartContext'
import { Button } from '@/components/ui/button'

const navLinkClass = ({ isActive }: { isActive: boolean }) =>
  `text-sm transition-colors ${isActive ? 'text-foreground font-medium' : 'text-muted-foreground hover:text-foreground'}`

export default function Navbar() {
  const { isAuthenticated, isAdmin } = useAuth()
  const { totalItems, setIsOpen } = useCart()
  const navigate = useNavigate()

  const logout = () => {
    localStorage.removeItem('accessToken')
    navigate('/login')
  }

  return (
    <nav className="border-b bg-background px-6 py-3 flex items-center justify-between">
      <span className="font-semibold text-sm">Restaurant</span>

      <div className="flex items-center gap-4">
        <NavLink to="/" className={navLinkClass}>Menu</NavLink>
        {isAuthenticated && (
          <NavLink to="/orders" className={navLinkClass}>My Orders</NavLink>
        )}
        {isAdmin && (
          <NavLink to="/admin" className={navLinkClass}>Admin</NavLink>
        )}
        {isAuthenticated ? (
          <Button variant="ghost" size="sm" onClick={logout}>Logout</Button>
        ) : (
          <NavLink to="/login" className={navLinkClass}>Login</NavLink>
        )}

        <Button variant="ghost" size="icon" className="relative" onClick={() => setIsOpen(true)}>
          <ShoppingCart className="h-5 w-5" />
          {totalItems > 0 && (
            <span className="absolute -top-1 -right-1 bg-primary text-primary-foreground text-xs rounded-full h-4 w-4 flex items-center justify-center leading-none">
              {totalItems}
            </span>
          )}
        </Button>
      </div>
    </nav>
  )
}
