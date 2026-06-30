import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getOrders, getOrder, getAdminOrders, updateOrderStatus } from '../api/orders'
import type { OrderFilter } from '../api/orders'

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

export function useAdminOrders(filter: OrderFilter) {
  return useQuery({
    queryKey: ['orders', 'admin', filter],
    queryFn: () => getAdminOrders(filter),
  })
}

export function useUpdateOrderStatus() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, status }: { id: string; status: string }) => updateOrderStatus(id, status),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['orders', 'admin'] }),
  })
}
