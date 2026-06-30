import { X } from 'lucide-react'
import { Dialog, DialogContent, DialogClose, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'
import OrderStatusBadge from './OrderStatusBadge'
import type { AdminOrderResponse } from '../api/orders'

function formatDateTime(iso: string) {
  const d = new Date(iso)
  return `${d.toLocaleDateString()} ${d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`
}

interface Props {
  order: AdminOrderResponse | null
  onClose: () => void
  onCustomerInfoOpen: () => void
}

export default function OrderDetailsModal({ order, onClose, onCustomerInfoOpen }: Readonly<Props>) {
  if (!order) return null

  const { orderData, customerEmail } = order

  return (
    <Dialog open onOpenChange={open => { if (!open) onClose() }}>
      <DialogContent className="max-w-md p-6">
        <DialogHeader className="flex flex-row items-center justify-between space-y-0">
          <DialogTitle className="text-base">Order details</DialogTitle>
          <DialogClose className="rounded-sm opacity-70 hover:opacity-100 transition-opacity">
            <X className="h-4 w-4" />
          </DialogClose>
        </DialogHeader>

        <div className="space-y-4 text-sm">
          <div className="grid grid-cols-[auto_1fr] gap-x-6 gap-y-2">
            <span className="text-muted-foreground">Order ID</span>
            <span className="font-mono text-xs break-all">{orderData.id}</span>

            <span className="text-muted-foreground">Customer</span>
            <div className="flex items-center gap-2">
              <span>{customerEmail}</span>
              <Button size="sm" variant="outline" className="h-6 text-xs px-2" onClick={onCustomerInfoOpen}>
                Info
              </Button>
            </div>

            <span className="text-muted-foreground">Placed</span>
            <span>{formatDateTime(orderData.createdAt)}</span>

            <span className="text-muted-foreground">Modified</span>
            <span>{formatDateTime(orderData.modifiedAt)}</span>

            <span className="text-muted-foreground">Status</span>
            <span><OrderStatusBadge status={orderData.status} /></span>
          </div>

          <div>
            <p className="text-muted-foreground mb-2">Items</p>
            <div className="border rounded-md divide-y">
              {orderData.items.map(item => (
                <div key={item.id} className="grid grid-cols-[1fr_auto_auto] gap-4 px-3 py-2">
                  <span>{item.menuItemName}</span>
                  <span className="text-muted-foreground">× {item.quantity}</span>
                  <span className="tabular-nums">{(item.unitPrice * item.quantity).toFixed(2)} €</span>
                </div>
              ))}
              <div className="flex justify-between px-3 py-2 font-medium">
                <span>Total</span>
                <span className="tabular-nums">{orderData.totalPrice.toFixed(2)} €</span>
              </div>
            </div>
          </div>

          {orderData.notes && (
            <p className="text-muted-foreground text-xs italic">Note: {orderData.notes}</p>
          )}
        </div>
      </DialogContent>
    </Dialog>
  )
}
