import { useState, useEffect } from 'react'
import type { OrderResponse } from '@/types/order'
import { ACTIVE_STATUSES } from '@/types/order'
import { useOrderSocket } from '@/hooks/useOrderSocket'
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import OrderStatusBadge from './OrderStatusBadge'

interface Props {
  order: OrderResponse
  onClose: () => void
}

export default function OrderModal({ order, onClose }: Props) {
  const [status, setStatus] = useState(order.status)

  useEffect(() => { setStatus(order.status) }, [order.status])

  useOrderSocket(ACTIVE_STATUSES.has(status) ? order.id : null, setStatus)

  return (
    <Dialog open onOpenChange={open => { if (!open) onClose() }}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle className="text-sm font-mono text-muted-foreground">{order.id}</DialogTitle>
          <p className="text-xs text-muted-foreground">{new Date(order.createdAt).toLocaleString()}</p>
        </DialogHeader>

        <div className="flex items-center gap-2">
          <OrderStatusBadge status={status} />
          {ACTIVE_STATUSES.has(status) && (
            <span className="text-xs text-muted-foreground">live</span>
          )}
        </div>

        <ul className="space-y-2">
          {order.items.map(item => (
            <li key={item.id} className="flex items-center justify-between">
              <div>
                <p className="text-sm">{item.menuItemName}</p>
                <p className="text-xs text-muted-foreground">× {item.quantity}</p>
              </div>
              <span className="text-sm">{(item.unitPrice * item.quantity).toFixed(2)} €</span>
            </li>
          ))}
        </ul>

        {order.notes && (
          <p className="text-xs text-muted-foreground italic">"{order.notes}"</p>
        )}

        <div className="border-t pt-4 flex justify-between items-center">
          <span className="text-sm text-muted-foreground">Total</span>
          <span className="font-semibold">{order.totalPrice.toFixed(2)} €</span>
        </div>
      </DialogContent>
    </Dialog>
  )
}
