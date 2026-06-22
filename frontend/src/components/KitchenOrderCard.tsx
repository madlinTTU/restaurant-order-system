import { Card, CardContent } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import type { OrderResponse } from '../types/order'

const NEXT_STATUS: Record<string, string> = {
  CONFIRMED: 'PREPARING',
  PREPARING: 'READY',
  READY: 'PICKED_UP',
}

const NEXT_LABEL: Record<string, string> = {
  CONFIRMED: 'Start preparing',
  PREPARING: 'Mark ready',
  READY: 'Picked up',
}

const STATUS_LABEL: Record<string, string> = {
  PLACED: 'New',
  CONFIRMED: 'Confirmed',
  PREPARING: 'Preparing',
  READY: 'Ready',
}

type BadgeVariant = 'default' | 'secondary' | 'destructive' | 'outline'

const STATUS_VARIANT: Record<string, BadgeVariant> = {
  PLACED: 'destructive',
  CONFIRMED: 'secondary',
  PREPARING: 'default',
  READY: 'outline',
}

export default function KitchenOrderCard(
  { order, onAdvance }: Readonly<{ order: OrderResponse; onAdvance: (id: string, status: string) => void }>
) {
  const nextStatus = NEXT_STATUS[order.status]

  return (
    <Card>
      <CardContent className="pt-5 pb-4">
        <div className="flex items-start justify-between gap-4">
          <div className="flex-1 min-w-0">
            <div className="flex items-center gap-2 mb-3">
              <span className="text-sm font-semibold">#{order.id.slice(0, 8)}</span>
              <Badge variant={STATUS_VARIANT[order.status] ?? 'secondary'}>
                {STATUS_LABEL[order.status] ?? order.status}
              </Badge>
              <span className="text-xs text-muted-foreground">
                {new Date(order.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
              </span>
            </div>

            <ul className="space-y-1">
              {order.items.map(item => (
                <li key={item.id} className="text-sm">
                  <span className="font-medium">{item.quantity}×</span>{' '}
                  <span className="text-foreground">{item.menuItemName}</span>
                </li>
              ))}
            </ul>

            {order.notes && (
              <p className="mt-2 text-xs text-muted-foreground italic">Note: {order.notes}</p>
            )}
          </div>

          <div className="flex flex-col gap-2 shrink-0">
            {order.status === 'PLACED' ? (
              <>
                <Button size="sm" onClick={() => onAdvance(order.id, 'CONFIRMED')}>
                  Confirm
                </Button>
                <Button
                  size="sm"
                  variant="outline"
                  className="text-destructive hover:text-destructive"
                  onClick={() => onAdvance(order.id, 'CANCELLED')}
                >
                  Deny
                </Button>
              </>
            ) : (
              nextStatus && (
                <Button size="sm" onClick={() => onAdvance(order.id, nextStatus)}>
                  {NEXT_LABEL[order.status]}
                </Button>
              )
            )}
          </div>
        </div>
      </CardContent>
    </Card>
  )
}
