import { NavLink, Outlet } from 'react-router-dom'

export default function AdminPanel() {
	return (
		<div className="min-h-screen bg-gray-50">
			<div className="max-w-4xl mx-auto px-4 py-8">
				<h1 className="text-xl font-semibold text-gray-900 mb-6">Menu Management</h1>

				<div className="flex gap-1 mb-6 border-b border-gray-200">
					<NavLink
						to="categories"
						className={({ isActive }) =>
							`px-4 py-2 text-sm transition-colors ${
								isActive
									? 'font-medium text-gray-900 border-b-2 border-gray-900 -mb-px'
									: 'text-gray-500 hover:text-gray-900'
							}`
						}
					>
						Categories
					</NavLink>
					<NavLink
						to="items"
						className={({ isActive }) =>
							`px-4 py-2 text-sm transition-colors ${
								isActive
									? 'font-medium text-gray-900 border-b-2 border-gray-900 -mb-px'
									: 'text-gray-500 hover:text-gray-900'
							}`
						}
					>
						Items
					</NavLink>
				</div>

				<Outlet />
			</div>
		</div>
	)
}
