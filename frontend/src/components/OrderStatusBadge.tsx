import { Badge } from '@/components/ui/badge'
import { cn } from '@/lib/utils'

const config: Record<string, { label: string; className: string }> = {
  PLACED:    { label: 'Placed',     className: 'bg-yellow-100 text-yellow-700 hover:bg-yellow-100' },
  CONFIRMED: { label: 'Confirmed',  className: 'bg-blue-100 text-blue-700 hover:bg-blue-100' },
  PREPARING: { label: 'Preparing',  className: 'bg-orange-100 text-orange-700 hover:bg-orange-100' },
  READY:     { label: 'Ready',      className: 'bg-green-100 text-green-700 hover:bg-green-100' },
  PICKED_UP: { label: 'Picked up',  className: 'bg-teal-100 text-teal-700 hover:bg-teal-100' },
  DELIVERED: { label: 'Delivered',  className: 'bg-green-100 text-green-800 hover:bg-green-100' },
  CANCELLED: { label: 'Cancelled',  className: 'bg-red-100 text-red-700 hover:bg-red-100' },
}

export default function OrderStatusBadge({ status }: Readonly<{ status: string }>) {
  const { label, className } = config[status] ?? { label: status, className: 'bg-muted text-muted-foreground hover:bg-muted' }
  return (
    <Badge variant="secondary" className={cn(className)}>
      {label}
    </Badge>
  )
}
