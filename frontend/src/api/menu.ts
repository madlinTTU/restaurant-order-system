import api from './axios'
import type {MenuCategoryRequest, MenuCategory, MenuItem} from '../types/menu.ts'

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