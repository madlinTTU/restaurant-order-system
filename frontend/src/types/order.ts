export interface OrderItemRequest {
  menuItemId: string
  quantity: number
}

export interface CreateOrderRequest {
  items: OrderItemRequest[]
  notes?: string
}

export interface OrderItemResponse {
  id: string
  menuItemId: string
  menuItemName: string
  quantity: number
  unitPrice: number
}

export interface OrderResponse {
  id: string
  userId: string
  status: string
  items: OrderItemResponse[]
  totalPrice: number
  notes: string | null
  createdAt: string
}
