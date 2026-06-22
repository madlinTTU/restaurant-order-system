import { useEffect, useRef } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { getActiveOrders, updateOrderStatus } from '../api/orders'

export const useActiveOrders = () =>
  useQuery({
    queryKey: ['activeOrders'],
    queryFn: getActiveOrders,
  })

export const useUpdateOrderStatus = () => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, status }: { id: string; status: string }) =>
      updateOrderStatus(id, status),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['activeOrders'] }),
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
