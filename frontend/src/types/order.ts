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

export interface OrderStatusEvent {
  eventId: string
  customerId: string
  status: string
  timestamp: string
  payload: {
    orderId: string
    totalPrice: number
    notes: string | null
  }
}

export const ACTIVE_STATUSES = new Set(['PLACED', 'CONFIRMED', 'PREPARING', 'READY', 'PICKED_UP'])
