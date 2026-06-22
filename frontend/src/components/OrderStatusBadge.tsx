const config: Record<string, { label: string; className: string }> = {
  PLACED:    { label: 'Placed',     className: 'bg-yellow-100 text-yellow-700' },
  CONFIRMED: { label: 'Confirmed',  className: 'bg-blue-100 text-blue-700' },
  PREPARING: { label: 'Preparing',  className: 'bg-orange-100 text-orange-700' },
  READY:     { label: 'Ready',      className: 'bg-green-100 text-green-700' },
  PICKED_UP: { label: 'Picked up',  className: 'bg-teal-100 text-teal-700' },
  DELIVERED: { label: 'Delivered',  className: 'bg-green-100 text-green-800' },
  CANCELLED: { label: 'Cancelled',  className: 'bg-red-100 text-red-700' },
}

export default function OrderStatusBadge({ status }: Readonly<{ status: string }>) {
  const { label, className } = config[status] ?? { label: status, className: 'bg-gray-100 text-gray-600' }
  return (
    <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium ${className}`}>
      {label}
    </span>
  )
}
