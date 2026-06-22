import { useQuery } from '@tanstack/react-query'
import { getOrders, getOrder } from '../api/orders'

export function useOrders() {
  return useQuery({
    queryKey: ['orders'],
    queryFn: getOrders,
  })
}

export function useOrder(id: string) {
  return useQuery({
    queryKey: ['orders', id],
    queryFn: () => getOrder(id),
  })
}
