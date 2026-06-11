import { Routes, Route } from 'react-router-dom'
import MenuPage from './pages/MenuPage'
import KitchenPage from './pages/KitchenPage'
import AdminMenuPage from './pages/AdminMenuPage'
import LoginPage from './pages/LoginPage'

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<MenuPage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/kitchen" element={<KitchenPage />} />
      <Route path="/admin/menu" element={<AdminMenuPage />} />
    </Routes>
  )
}
