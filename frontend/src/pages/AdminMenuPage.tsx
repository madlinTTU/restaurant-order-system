import { useState } from 'react'
import * as React from 'react'
import { useCategories, useCreateCategory, useDeleteCategory, useUpdateCategory } from '../hooks/useMenu.ts'
import type { MenuCategory } from '../types/menu.ts'

const emptyForm = { name: '', description: '' }

export default function AdminMenuPage() {
	const [formVisible, setFormVisible] = useState(false)
	const [form, setForm] = useState({ name: '', description: '' })
	const [editingId, setEditingId] = useState<string | null>(null)

	const { data: categories, isLoading, isError } = useCategories()
	const createCategory = useCreateCategory()
	const updateCategory = useUpdateCategory()
	const deleteCategory = useDeleteCategory()

	const closeForm = () => {
		setFormVisible(false)
		setEditingId(null)
		setForm(emptyForm)
	}

	const openEdit = (category: MenuCategory) => {
		setEditingId(category.id)
		setForm({ name: category.name, description: category.description ?? '' })
		setFormVisible(true)
	}

	const handleSubmit = (e: React.FormEvent) => {
		e.preventDefault()
		if (editingId) {
			updateCategory.mutate({ id: editingId, data: form }, {
				onSuccess: closeForm,
				onError: e => alert(e.message)
			})
		} else {
			createCategory.mutate(form, {
				onSuccess: closeForm,
				onError: e => alert(e.message)
			})
		}
	}

	const isPending = createCategory.isPending || updateCategory.isPending

	return (
		<div className="min-h-screen bg-gray-50">
			<div className="max-w-3xl mx-auto px-4 py-8">
				<h1 className="text-xl font-semibold text-gray-900 mb-8">Menu Management</h1>

				<div className="bg-white rounded-lg border border-gray-200">
					<div className="flex items-center justify-between px-5 py-4 border-b border-gray-200">
						<h2 className="font-medium text-gray-900">Categories</h2>
						<button
							onClick={() => setFormVisible(true)}
							className="px-3 py-1.5 bg-gray-900 text-white text-sm rounded-md hover:bg-gray-700 transition-colors"
						>
							+ Add
						</button>
					</div>

					{formVisible && (
						<div className="px-5 py-4 border-b border-gray-200 bg-gray-50">
							<form onSubmit={handleSubmit} className="space-y-3">
								<div className="grid grid-cols-2 gap-3">
									<div>
										<label className="block text-xs font-medium text-gray-600 mb-1">Name *</label>
										<input
											required
											value={form.name}
											onChange={e => setForm(f => ({ ...f, name: e.target.value }))}
											className="w-full border border-gray-300 rounded-md px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-gray-900"
										/>
									</div>
									<div>
										<label className="block text-xs font-medium text-gray-600 mb-1">Description</label>
										<input
											value={form.description}
											onChange={e => setForm(f => ({ ...f, description: e.target.value }))}
											className="w-full border border-gray-300 rounded-md px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-gray-900"
										/>
									</div>
								</div>
								<div className="flex gap-2">
									<button
										type="submit"
										disabled={isPending}
										className="px-3 py-1.5 bg-gray-900 text-white text-sm rounded-md hover:bg-gray-700 disabled:opacity-50 transition-colors"
									>
										{editingId ? 'Save' : 'Create'}
									</button>
									<button
										type="button"
										onClick={closeForm}
										className="px-3 py-1.5 border border-gray-300 text-sm rounded-md hover:bg-gray-100 transition-colors"
									>
										Cancel
									</button>
								</div>
							</form>
						</div>
					)}

					{isLoading && (
						<p className="px-5 py-8 text-sm text-gray-400 text-center">Loading...</p>
					)}
					{isError && (
						<p className="px-5 py-8 text-sm text-red-500 text-center">Something went wrong.</p>
					)}
					{categories && categories.length === 0 && (
						<p className="px-5 py-8 text-sm text-gray-400 text-center">No categories yet.</p>
					)}
					{categories && categories.length > 0 && (
						<table className="w-full text-sm">
							<thead>
								<tr className="text-left text-xs text-gray-500 uppercase tracking-wide border-b border-gray-200">
									<th className="px-5 py-3 font-medium">Name</th>
									<th className="px-5 py-3 font-medium">Description</th>
									<th className="px-5 py-3" />
								</tr>
							</thead>
							<tbody className="divide-y divide-gray-100">
								{categories.map(category => (
									<tr key={category.id} className="hover:bg-gray-50 transition-colors">
										<td className="px-5 py-3 font-medium text-gray-900">{category.name}</td>
										<td className="px-5 py-3 text-gray-500">{category.description}</td>
										<td className="px-5 py-3 text-right space-x-3">
											<button
												onClick={() => openEdit(category)}
												className="text-xs text-gray-500 hover:text-gray-900 transition-colors"
											>
												Edit
											</button>
											<button
												onClick={() => {
													if (confirm('Delete this category?')) {
														deleteCategory.mutate(category.id)
													}
												}}
												className="text-xs text-red-400 hover:text-red-600 transition-colors"
											>
												Delete
											</button>
										</td>
									</tr>
								))}
							</tbody>
						</table>
					)}
				</div>
			</div>
		</div>
	)
}
