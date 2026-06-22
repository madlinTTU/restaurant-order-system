import {useState} from 'react'
import * as React from 'react'
import {useQueryClient} from '@tanstack/react-query'
import {useCategories, useMenuItems, useCreateMenuItem, useUpdateMenuItem, useDeleteMenuItem} from '../hooks/useMenu.ts'
import type {MenuItem, MenuItemRequest} from '../types/menu.ts'
import {getImageUploadUrl, uploadImageToS3} from '../api/menu.ts'

const emptyForm = {
	categoryId: '',
	name: '',
	description: '',
	price: '',
	available: true,
}


export default function AdminMenuItemsPage() {
	const [formVisible, setFormVisible] = useState(false)
	const [form, setForm] = useState(emptyForm)
	const [editingId, setEditingId] = useState<string | null>(null)
	const [imageFile, setImageFile] = useState<File | null>(null)


	const queryClient = useQueryClient()
	const {data: categories} = useCategories()
	const {data: items, isLoading, isError} = useMenuItems()
	const createItem = useCreateMenuItem()
	const updateItem = useUpdateMenuItem()
	const deleteItem = useDeleteMenuItem()

	const closeForm = () => {
		setFormVisible(false)
		setEditingId(null)
		setForm(emptyForm)
		setImageFile(null)
	}

	const uploadImageIfSelected = async (itemId: string) => {
		if (imageFile) {
			const {uploadUrl} = await getImageUploadUrl(itemId, imageFile.type)
			await uploadImageToS3(uploadUrl, imageFile)
			await queryClient.invalidateQueries({ queryKey: ['menuItems'] })
		}
	}
	const renderImage = (item: MenuItem) => {
		if (item.imageUrl) {
			return <img src={item.imageUrl} alt={item.name} className="w-10 h-10 object-cover rounded"/>
		}
		return (
			<div className="w-10 h-10 bg-gray-100 rounded flex items-center justify-center">
				<span className="text-gray-300 text-xs">—</span>
			</div>
		)
	}


	const openEdit = (item: MenuItem) => {
		setEditingId(item.id)
		setForm({
			categoryId: item.categoryId,
			name: item.name,
			description: item.description ?? '',
			price: item.price.toString(),
			available: item.available,
		})
		setFormVisible(true)
	}

	const handleSubmit = (e: React.FormEvent) => {
		e.preventDefault()
		const itemData: MenuItemRequest = {
			categoryId: form.categoryId,
			name: form.name,
			description: form.description,
			price: parseFloat(form.price),
			available: form.available,
		}

		if (editingId) {
			updateItem.mutate({id: editingId, data: itemData}, {
				onSuccess: async () => {
					await uploadImageIfSelected(editingId)
					closeForm()
				},
				onError: e => alert(e.message),
			})
		} else {
			createItem.mutate(itemData, {
				onSuccess: async (created) => {
					await uploadImageIfSelected(created.id)
					closeForm()
				},
				onError: e => alert(e.message),
			})
		}
	}

	return (
		<div className="bg-white rounded-lg border border-gray-200">
			<div className="flex items-center justify-between px-5 py-4 border-b border-gray-200">
				<h2 className="font-medium text-gray-900">Menu Items</h2>
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
								<label className="block text-xs font-medium text-gray-600 mb-1">Category *</label>
								<select
									required
									value={form.categoryId}
									onChange={e => setForm(f => ({...f, categoryId: e.target.value}))}
									className="w-full border border-gray-300 rounded-md px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-gray-900 bg-white"
								>
									<option value="">Select category...</option>
									{categories?.map(c => (
										<option key={c.id} value={c.id}>{c.name}</option>
									))}
								</select>
							</div>
							<div>
								<label className="block text-xs font-medium text-gray-600 mb-1">Name *</label>
								<input
									required
									value={form.name}
									onChange={e => setForm(f => ({...f, name: e.target.value}))}
									className="w-full border border-gray-300 rounded-md px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-gray-900"
								/>
							</div>
							<div>
								<label className="block text-xs font-medium text-gray-600 mb-1">Price *</label>
								<input
									required
									type="number"
									min="0"
									step="0.01"
									value={form.price}
									onChange={e => setForm(f => ({...f, price: e.target.value}))}
									className="w-full border border-gray-300 rounded-md px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-gray-900"
								/>
							</div>
							<div>
								<label className="block text-xs font-medium text-gray-600 mb-1">Description</label>
								<input
									value={form.description}
									onChange={e => setForm(f => ({...f, description: e.target.value}))}
									className="w-full border border-gray-300 rounded-md px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-gray-900"
								/>
							</div>
							<div>
								<label className="block text-xs font-medium text-gray-600 mb-1">Image</label>
								<input
									type='file'
									accept='image/*'
									onChange={e => setImageFile(e.target.files?.[0] ?? null)}
									className="w-full text-sm text-gray-500 file:mr-3 file:py-1 file:px-3 file:rounded file:border-0 file:text-xs file:font-medium file:bg-gray-900 file:text-white
								  hover:file:bg-gray-700 file:cursor-pointer"/>
							</div>
							<div className="flex items-center gap-2 pt-4">
								<input
									type="checkbox"
									id="available"
									checked={form.available}
									onChange={e => setForm(f => ({...f, available: e.target.checked}))}
									className="rounded border-gray-300"
								/>
								<label htmlFor="available" className="text-xs font-medium text-gray-600">Available</label>
							</div>
						</div>
						<div className="flex gap-2">
							<button
								type="submit"
								disabled={createItem.isPending || updateItem.isPending}
								className="px-3 py-1.5 bg-gray-900 text-white text-sm rounded-md hover:bg-gray-700 disabled:opacity-50 transition-colors"
							>
								{createItem.isPending || updateItem.isPending ? 'Saving...' : editingId ? 'Save' : 'Create'}
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

			{isLoading && <p className="px-5 py-8 text-sm text-gray-400 text-center">Loading...</p>}
			{isError && <p className="px-5 py-8 text-sm text-red-500 text-center">Something went wrong.</p>}
			{items?.length === 0 && <p className="px-5 py-8 text-sm text-gray-400 text-center">No items yet.</p>}
			{items && items.length > 0 && (
				<table className="w-full text-sm">
					<thead>
					<tr className="text-left text-xs text-gray-500 uppercase tracking-wide border-b border-gray-200">
						<th className="px-5 py-3 font-medium">Image</th>
						<th className="px-5 py-3 font-medium">Name</th>
						<th className="px-5 py-3 font-medium">Category</th>
						<th className="px-5 py-3 font-medium">Description</th>
						<th className="px-5 py-3 font-medium">Price</th>
						<th className="px-5 py-3 font-medium">Available</th>
						<th className="px-5 py-3"/>
					</tr>
					</thead>
					<tbody className="divide-y divide-gray-100">
					{items.map(item => (
						<tr key={item.id} className="hover:bg-gray-50 transition-colors">
							<td className="px-5 py-3">
								{renderImage(item)}
							</td>
							<td className="px-5 py-3 font-medium text-gray-900">{item.name}</td>
							<td className="px-5 py-3 text-gray-500">{item.categoryName}</td>
							<td className="px-5 py-3 text-gray-500">{item.description}</td>
							<td className="px-5 py-3 text-gray-900">${item.price.toFixed(2)}</td>
							<td className="px-5 py-3">
									<span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-medium ${
										item.available ? 'bg-green-50 text-green-700' : 'bg-gray-100 text-gray-500'
									}`}>
										{item.available ? 'Yes' : 'No'}
									</span>
							</td>
							<td className="px-5 py-3 text-right space-x-3">
								<button
									onClick={() => openEdit(item)}
									className="text-xs text-gray-500 hover:text-gray-900 transition-colors"
								>
									Edit
								</button>
								<button
									onClick={() => {
										if (confirm('Delete this item?')) {
											deleteItem.mutate(item.id)
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
	)
}
