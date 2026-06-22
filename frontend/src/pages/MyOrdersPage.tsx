import { useState, useEffect } from 'react'
import { useOrders } from '@/hooks/useOrders'
import type { OrderResponse } from '@/types/order'
import { ACTIVE_STATUSES } from '@/types/order'
import { useOrderSocket } from '@/hooks/useOrderSocket'
import OrderStatusBadge from '@/components/OrderStatusBadge'
import OrderModal from '@/components/OrderModal'
import { Button } from '@/components/ui/button'

function OrderCard({ order, onView }: { order: OrderResponse; onView: (o: OrderResponse) => void }) {
  const [status, setStatus] = useState(order.status)
  useEffect(() => { setStatus(order.status) }, [order.status])
  useOrderSocket(ACTIVE_STATUSES.has(status) ? order.id : null, setStatus)

  return (
    <div className="border rounded-xl p-4 flex items-center justify-between gap-4 hover:bg-muted/30 transition-colors">
      <div className="flex items-center gap-4 min-w-0">
        <div className="w-10 h-10 rounded-lg bg-muted flex items-center justify-center shrink-0">
          <span className="text-xs font-mono text-muted-foreground">{order.id.slice(0, 4)}</span>
        </div>
        <div className="min-w-0">
          <p className="text-sm font-medium truncate">
            {order.items.map(i => `${i.menuItemName} ×${i.quantity}`).join(', ')}
          </p>
          <p className="text-xs text-muted-foreground mt-0.5">
            {new Date(order.createdAt).toLocaleString('en-GB', {
              day: 'numeric', month: 'short', year: 'numeric',
              hour: '2-digit', minute: '2-digit'
            })}
          </p>
        </div>
      </div>

      <div className="flex items-center gap-3 shrink-0">
        <OrderStatusBadge status={status} />
        <span className="text-sm font-medium">{order.totalPrice.toFixed(2)} €</span>
        <Button variant="outline" size="sm" onClick={() => onView({ ...order, status })}>
          View
        </Button>
      </div>
    </div>
  )
}

export default function MyOrdersPage() {
  const { data: orders, isLoading, isError } = useOrders()
  const [selectedOrder, setSelectedOrder] = useState<OrderResponse | null>(null)

  if (isLoading) return <p className="p-6 text-sm text-muted-foreground">Loading...</p>
  if (isError) return <p className="p-6 text-sm text-destructive">Failed to load orders.</p>

  const sorted = [...(orders ?? [])].sort(
    (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
  )

  return (
    <div className="max-w-3xl mx-auto px-6 py-8">
      <div className="mb-6">
        <h1 className="text-2xl font-semibold tracking-tight">My Orders</h1>
        <p className="text-sm text-muted-foreground mt-1">
          {sorted.length > 0 ? `${sorted.length} order${sorted.length > 1 ? 's' : ''}` : 'No orders yet'}
        </p>
      </div>

      {sorted.length === 0 ? (
        <div className="border rounded-xl py-20 flex flex-col items-center gap-2">
          <p className="text-sm font-medium">Nothing here yet</p>
          <p className="text-xs text-muted-foreground">Your orders will appear here once you place one</p>
        </div>
      ) : (
        <div className="space-y-3">
          {sorted.map(order => (
            <OrderCard key={order.id} order={order} onView={setSelectedOrder} />
          ))}
        </div>
      )}

      {selectedOrder && (
        <OrderModal order={selectedOrder} onClose={() => setSelectedOrder(null)} />
      )}
    </div>
  )
}
