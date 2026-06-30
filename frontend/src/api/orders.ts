import api from './axios'
import type { CreateOrderRequest, OrderResponse } from '../types/order'

export const createOrder = async (request: CreateOrderRequest): Promise<OrderResponse> => {
  const res = await api.post('/orders', request)
  return res.data
}

export const getOrders = async (): Promise<OrderResponse[]> => {
  const res = await api.get('/orders')
  return res.data
}

export const getOrder = async (id: string): Promise<OrderResponse> => {
  const res = await api.get(`/orders/${id}`)
  return res.data
}

export const getActiveOrders = async (): Promise<OrderResponse[]> => {
  const res = await api.get('/orders/active')
  return res.data
}

export const updateOrderStatus = async (id: string, status: string): Promise<OrderResponse> => {
  const res = await api.patch(`/orders/${id}/status`, { status })
  return res.data
}

export interface AdminOrderResponse {
  orderData: OrderResponse
  customerEmail: string
}

export interface OrderFilter {
  statuses?: string[]
  userEmailSearch?: string
  dateFrom?: string
  dateTill?: string
  sortBy?: string
  sortDir?: string
}

export const getAdminOrders = async (filter: OrderFilter): Promise<AdminOrderResponse[]> => {
  const params = new URLSearchParams()
  filter.statuses?.forEach(s => params.append('statuses', s))
  if (filter.userEmailSearch) params.set('userEmailSearch', filter.userEmailSearch)
  if (filter.dateFrom) params.set('dateFrom', filter.dateFrom)
  if (filter.dateTill) params.set('dateTill', filter.dateTill)
  if (filter.sortBy) params.set('sortBy', filter.sortBy)
  if (filter.sortDir) params.set('sortDir', filter.sortDir)
  const res = await api.get('/orders/admin', { params })
  return res.data
}
