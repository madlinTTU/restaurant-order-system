export interface MenuCategory {
	id: string
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
}