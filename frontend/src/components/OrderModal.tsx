import { useState, useEffect } from 'react'
import type { OrderResponse } from '@/types/order'
import { ACTIVE_STATUSES } from '@/types/order'
import { useOrderSocket } from '@/hooks/useOrderSocket'
import { Dialog, DialogContent, DialogClose } from '@/components/ui/dialog'
import { X } from 'lucide-react'
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

        <div className="bg-zinc-900 px-6 py-5">
          <div className="flex items-start justify-between gap-4">
            <div>
              <p className="text-xs font-mono text-zinc-500">{order.id}</p>
              <p className="text-xs text-zinc-500 mt-0.5">
                {new Date(order.createdAt).toLocaleString('en-GB', {
                  day: 'numeric', month: 'short', year: 'numeric',
                  hour: '2-digit', minute: '2-digit'
                })}
              </p>
            </div>
            <div className="flex items-center gap-3 shrink-0">
              <OrderStatusBadge status={status} />
              <DialogClose className="text-zinc-500 hover:text-white transition-colors">
                <X className="h-4 w-4" />
              </DialogClose>
            </div>
          </div>
        </div>

        <div className="px-6 py-4 space-y-1">
          <div className="grid grid-cols-[1fr_auto_auto_auto] gap-x-4 text-xs text-muted-foreground uppercase tracking-wider mb-2">
            <span>Item</span>
            <span className="text-right">Price</span>
            <span className="text-right">Qty</span>
            <span className="text-right">Total</span>
          </div>
          {order.items.map(item => (
            <div key={item.id} className="grid grid-cols-[1fr_auto_auto_auto] gap-x-4 items-center py-1.5">
              <span className="text-sm">{item.menuItemName}</span>
              <span className="text-sm text-muted-foreground text-right">{item.unitPrice.toFixed(2)} €</span>
              <span className="text-sm text-muted-foreground text-right">×{item.quantity}</span>
              <span className="text-sm font-medium text-right">{(item.unitPrice * item.quantity).toFixed(2)} €</span>
            </div>
          ))}
        </div>

        {order.notes && (
          <div className="px-6 pb-2">
            <p className="text-xs text-muted-foreground italic border-l-2 border-muted pl-3">"{order.notes}"</p>
          </div>
        )}

        <div className="border-t px-6 py-4 flex justify-between items-center bg-muted/30">
          <span className="text-sm text-muted-foreground">Total</span>
          <span className="text-lg font-semibold">{order.totalPrice.toFixed(2)} €</span>
        </div>

      </DialogContent>
    </Dialog>
  )
}
