import { Navigate, Routes, Route, useLocation } from 'react-router-dom'
import { CartProvider } from '@/contexts/CartContext'
import { Toaster } from '@/components/ui/sonner'
import MenuPage from './pages/MenuPage'
import KitchenPage from './pages/KitchenPage'
import AdminPanel from './pages/AdminPanel'
import AdminCategoriesPage from './pages/AdminCategoriesPage'
import AdminMenuItemsPage from './pages/AdminMenuItemsPage'
import AdminUsersPage from './pages/AdminUsersPage'
import AdminOrdersPage from './pages/AdminOrdersPage'
import LoginPage from './pages/LoginPage'
import MyOrdersPage from './pages/MyOrdersPage'
import MenuItemPage from './pages/MenuItemPage'
import Navbar from './components/Navbar'
import CartSheet from './components/Cart'

export default function App() {
  const location = useLocation()
  const hideNav = location.pathname === '/login'

  return (
    <CartProvider>
      {!hideNav && <Navbar />}
      {!hideNav && <CartSheet />}
      <Routes>
        <Route path="/" element={<MenuPage />} />
        <Route path="/menu/:id" element={<MenuItemPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/orders" element={<MyOrdersPage />} />
        <Route path="/kitchen" element={<KitchenPage />} />
        <Route path="/admin" element={<AdminPanel />}>
          <Route index element={<Navigate to="categories" replace />} />
          <Route path="categories" element={<AdminCategoriesPage />} />
          <Route path="items" element={<AdminMenuItemsPage />} />
          <Route path="users" element={<AdminUsersPage />} />
          <Route path="orders" element={<AdminOrdersPage />} />
        </Route>
      </Routes>
      <Toaster position="bottom-right" />
    </CartProvider>
  )
}
