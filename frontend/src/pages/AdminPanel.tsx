import { NavLink, Outlet } from 'react-router-dom'

const tabClass = ({ isActive }: { isActive: boolean }) =>
  `px-4 py-2 text-sm transition-colors border-b-2 -mb-px ${
    isActive
      ? 'font-medium text-foreground border-foreground'
      : 'text-muted-foreground border-transparent hover:text-foreground'
  }`

export default function AdminPanel() {
  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <h1 className="text-xl font-semibold mb-6">Admin Panel</h1>

      <div className="flex gap-1 mb-6 border-b">
        <NavLink to="categories" className={tabClass}>Categories</NavLink>
        <NavLink to="items" className={tabClass}>Items</NavLink>
        <NavLink to="users" className={tabClass}>Users</NavLink>
      </div>

      <Outlet />
    </div>
  )
}
