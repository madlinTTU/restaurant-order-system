import { Navigate, Routes, Route } from 'react-router-dom'
import MenuPage from './pages/MenuPage'
import KitchenPage from './pages/KitchenPage'
import AdminPanel from './pages/AdminPanel'
import AdminCategoriesPage from './pages/AdminCategoriesPage'
import AdminMenuItemsPage from './pages/AdminMenuItemsPage'
import LoginPage from './pages/LoginPage'

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<MenuPage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/kitchen" element={<KitchenPage />} />
      <Route path="/admin/menu" element={<AdminPanel />}>
        <Route index element={<Navigate to="categories" replace />} />
        <Route path="categories" element={<AdminCategoriesPage />} />
        <Route path="items" element={<AdminMenuItemsPage />} />
      </Route>
    </Routes>
  )
}
