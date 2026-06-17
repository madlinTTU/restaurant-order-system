import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getMenuItems, getCategories, createCategory, updateCategory, deleteCategory, createMenuItem, updateMenuItem, deleteMenuItem } from '../api/menu.ts'
import type { MenuCategoryRequest, MenuItemRequest } from "../types/menu.ts";

export const useMenuItems = () =>
	useQuery({
		queryKey: ['menuItems'],
		queryFn: getMenuItems,
	})

export const useCategories = () =>
	useQuery({
		queryKey: ['categories'],
		queryFn: getCategories,
	})

export const useCreateCategory = () => {
	const queryClient = useQueryClient()
	return useMutation({
		mutationFn: createCategory,
		onSuccess: () => queryClient.invalidateQueries({ queryKey: ['categories'] }),
	})
}

export const useUpdateCategory = () => {
	const queryClient = useQueryClient()
	return useMutation({
		mutationFn: ({ id, data }: { id: string; data: MenuCategoryRequest }) =>
			updateCategory(id, data),
		onSuccess: () => queryClient.invalidateQueries({ queryKey: ['categories'] }),
	})
}

export const useDeleteCategory = () => {
	const queryClient = useQueryClient()
	return useMutation({
		mutationFn: deleteCategory,
		onSuccess: () => queryClient.invalidateQueries({ queryKey: ['categories'] }),
	})
}

export const useCreateMenuItem = () => {
	const queryClient = useQueryClient()
	return useMutation({
		mutationFn: createMenuItem,
		onSuccess: () => queryClient.invalidateQueries({ queryKey: ['menuItems'] }),
	})
}

export const useUpdateMenuItem = () => {
	const queryClient = useQueryClient()
	return useMutation({
		mutationFn: ({ id, data }: { id: string; data: MenuItemRequest }) =>
			updateMenuItem(id, data),
		onSuccess: () => queryClient.invalidateQueries({ queryKey: ['menuItems'] }),
	})
}

export const useDeleteMenuItem = () => {
	const queryClient = useQueryClient()
	return useMutation({
		mutationFn: deleteMenuItem,
		onSuccess: () => queryClient.invalidateQueries({ queryKey: ['menuItems'] }),
	})
}