import { useState } from 'react'
import { ACTIVE_STATUSES } from '../types/order'
import type { OrderResponse } from '../types/order'
import { useOrderSocket } from '../hooks/useOrderSocket'
import OrderStatusBadge from './OrderStatusBadge'

interface Props {
  order: OrderResponse
  onView: (order: OrderResponse) => void
}

export default function OrderRow({ order, onView }: Readonly<Props>) {
  const [status, setStatus] = useState(order.status)

  useOrderSocket(ACTIVE_STATUSES.has(status) ? order.id : null, setStatus)

  return (
    <tr className="border-t border-gray-100 hover:bg-gray-50 transition-colors">
      <td className="py-3 px-4 text-xs font-mono text-gray-400">{order.id.slice(0, 8)}…</td>
      <td className="py-3 px-4 text-sm text-gray-600">
        {new Date(order.createdAt).toLocaleString()}
      </td>
      <td className="py-3 px-4">
        <OrderStatusBadge status={status} />
      </td>
      <td className="py-3 px-4 text-sm font-medium text-gray-900 text-right">
        ${order.totalPrice.toFixed(2)}
      </td>
      <td className="py-3 px-4 text-right">
        <button
          onClick={() => onView({ ...order, status })}
          className="text-xs px-3 py-1.5 border border-gray-200 rounded-lg text-gray-600 hover:bg-gray-100 transition-colors"
        >
          View
        </button>
      </td>
    </tr>
  )
}
