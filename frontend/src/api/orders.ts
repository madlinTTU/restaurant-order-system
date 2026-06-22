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
