import { useEffect, useRef } from 'react'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

export function useOrderSocket(orderId: string | null, onStatus: (status: string) => void) {
  const onStatusRef = useRef(onStatus)
  useEffect(() => { onStatusRef.current = onStatus })

  useEffect(() => {
    if (!orderId) return

    const client = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      onConnect: () => {
        client.subscribe(`/topic/orders/${orderId}`, (message) => {
          const event = JSON.parse(message.body)
          onStatusRef.current(event.status)
        })
      },
    })

    client.activate()
    return () => { client.deactivate() }
  }, [orderId])
}
