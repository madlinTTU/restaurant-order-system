export interface MenuCategory {
	id: string
	name: string
	description: string
	position: number
}

export interface MenuCategoryRequest {
	name: string
	description: string
}

export interface MenuItem {
	id: string
	categoryId: string
	categoryName: string
	name: string
	description: string
	price: number
	imageUrl: string | null
	available: boolean
	position: number
}

export interface MenuItemRequest {
	categoryId: string
	name: string
	description: string
	price: number
	available: boolean
}