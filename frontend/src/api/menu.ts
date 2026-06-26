import api from './axios'
import type {MenuCategoryRequest, MenuCategory, MenuItem, MenuItemRequest} from '../types/menu.ts'

export const getMenuItems = async (): Promise<MenuItem[]> => {
	const res = await api.get('/menu/items')
	return res.data
}

export const getCategories = async (): Promise<MenuCategory[]> => {
	const res = await api.get('/menu/categories')
	return res.data
}

export const createCategory = async (request: MenuCategoryRequest): Promise<MenuCategory> => {
	const res = await api.post('/menu/categories', request)
	return res.data
}

export const updateCategory = async (id: string, request: MenuCategoryRequest): Promise<MenuCategory> => {
	const res = await api.put(`/menu/categories/${id}`, request)
	return res.data
}

export const deleteCategory = async (id: string): Promise<void> => {
	await api.delete(`/menu/categories/${id}`)
}

export const createMenuItem = async (request: MenuItemRequest): Promise<MenuItem> => {
	const res = await api.post('/menu/items', request)
	return res.data
}

export const updateMenuItem = async (id: string, request: MenuItemRequest): Promise<MenuItem> => {
	const res = await api.put(`/menu/items/${id}`, request)
	return res.data
}

export const deleteMenuItem = async (id: string): Promise<void> => {
	await api.delete(`/menu/items/${id}`)
}

export const getImageUploadUrl = async (id: string, contentType: string): Promise<{ uploadUrl: string }> => {
	const res = await api.post(`/menu/items/${id}/image-upload-url`, null, {
		params: { contentType }
	})
	return res.data
}

export const uploadImageToS3 = async (uploadUrl: string, file: File): Promise<void> => {
	await fetch(uploadUrl, {
		method: 'PUT',
		body: file,
		headers: { 'Content-Type': file.type || 'application/octet-stream' },
	})
}

export const updateCategoryPositions = async (entries: { id: string; position: number }[]): Promise<void> => {
	await api.patch('/menu/categories/positions', entries)
}

export const updateItemPositions = async (entries: { id: string; position: number }[]): Promise<void> => {
	await api.patch('/menu/items/positions', entries)
}