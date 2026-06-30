import { lazy, Suspense } from 'react'
import { Navigate, Routes, Route, useLocation } from 'react-router-dom'
import { CartProvider } from '@/contexts/CartContext'
import { Toaster } from '@/components/ui/sonner'
import Navbar from './components/Navbar'
import CartSheet from './components/Cart'

const MenuPage = lazy(() => import('./pages/MenuPage'))
const KitchenPage = lazy(() => import('./pages/KitchenPage'))
const AdminPanel = lazy(() => import('./pages/AdminPanel'))
const AdminCategoriesPage = lazy(() => import('./pages/AdminCategoriesPage'))
const AdminMenuItemsPage = lazy(() => import('./pages/AdminMenuItemsPage'))
const AdminUsersPage = lazy(() => import('./pages/AdminUsersPage'))
const LoginPage = lazy(() => import('./pages/LoginPage'))
const MyOrdersPage = lazy(() => import('./pages/MyOrdersPage'))
const MenuItemPage = lazy(() => import('./pages/MenuItemPage'))

export default function App() {
  const location = useLocation()
  const hideNav = location.pathname === '/login'

  return (
    <CartProvider>
      {!hideNav && <Navbar />}
      {!hideNav && <CartSheet />}
      <Suspense fallback={null}>
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
          </Route>
        </Routes>
      </Suspense>
      <Toaster position="bottom-right" />
    </CartProvider>
  )
}
