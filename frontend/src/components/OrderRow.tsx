import { useState } from 'react'
import { ACTIVE_STATUSES } from '@/types/order'
import type { OrderResponse } from '@/types/order'
import { useOrderSocket } from '@/hooks/useOrderSocket'
import { Button } from '@/components/ui/button'
import OrderStatusBadge from './OrderStatusBadge'

interface Props {
  order: OrderResponse
  onView: (order: OrderResponse) => void
}

export default function OrderRow({ order, onView }: Readonly<Props>) {
  const [status, setStatus] = useState(order.status)

  useOrderSocket(ACTIVE_STATUSES.has(status) ? order.id : null, setStatus)

  return (
    <tr className="border-t hover:bg-muted/50 transition-colors">
      <td className="py-3 px-4 text-xs font-mono text-muted-foreground">{order.id.slice(0, 8)}…</td>
      <td className="py-3 px-4 text-sm text-muted-foreground">
        {new Date(order.createdAt).toLocaleString()}
      </td>
      <td className="py-3 px-4">
        <OrderStatusBadge status={status} />
      </td>
      <td className="py-3 px-4 text-sm font-medium text-right">
        {order.totalPrice.toFixed(2)} €
      </td>
      <td className="py-3 px-4 text-right">
        <Button variant="outline" size="sm" onClick={() => onView({ ...order, status })}>
          View
        </Button>
      </td>
    </tr>
  )
}
