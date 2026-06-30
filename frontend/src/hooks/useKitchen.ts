import { useEffect, useRef } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { getActiveOrders, updateOrderStatus } from '../api/orders'
import type { OrderResponse } from '../types/order'

export const useActiveOrders = () =>
  useQuery({
    queryKey: ['activeOrders'],
    queryFn: getActiveOrders,
  })

export const useUpdateOrderStatus = () => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, status }: { id: string; status: string }) => updateOrderStatus(id, status),
    onMutate: async ({ id, status }) => {
      await queryClient.cancelQueries({ queryKey: ['activeOrders'] })
      const previous = queryClient.getQueryData<OrderResponse[]>(['activeOrders'])
      queryClient.setQueryData<OrderResponse[]>(['activeOrders'], old =>
        old?.map(order => order.id === id ? { ...order, status } : order)
      )
      return { previous }
    },
    onError: (_err, _vars, context) => {
      if (context?.previous) {
        queryClient.setQueryData(['activeOrders'], context.previous)
      }
    },
    onSettled: () => queryClient.invalidateQueries({ queryKey: ['activeOrders'] }),
  })
}

export function useKitchenSocket(onEvent: () => void) {
  const onEventRef = useRef(onEvent)
  useEffect(() => { onEventRef.current = onEvent })

  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      onConnect: () => {
        client.subscribe('/topic/kitchen', () => {
          onEventRef.current()
        })
      },
    })

    client.activate()
    return () => { client.deactivate() }
  }, [])
}
