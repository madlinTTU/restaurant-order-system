import { useState, useEffect } from 'react'
import type { OrderResponse } from '../types/order'
import { ACTIVE_STATUSES } from '../types/order'
import { useOrderSocket } from '../hooks/useOrderSocket'
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
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40" onClick={onClose}>
      <div
        className="bg-white rounded-2xl shadow-xl w-full max-w-md mx-4 p-6"
        onClick={e => e.stopPropagation()}
      >
        <div className="flex items-start justify-between mb-5">
          <div>
            <p className="text-xs text-gray-400 font-mono mb-1">{order.id}</p>
            <p className="text-xs text-gray-400">
              {new Date(order.createdAt).toLocaleString()}
            </p>
          </div>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 transition-colors text-lg leading-none"
          >
            ✕
          </button>
        </div>

        <div className="mb-5">
          <OrderStatusBadge status={status} />
          {ACTIVE_STATUSES.has(status) && (
            <span className="ml-2 text-xs text-gray-400">live</span>
          )}
        </div>

        <ul className="space-y-2 mb-5">
          {order.items.map(item => (
            <li key={item.id} className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-900">{item.menuItemName}</p>
                <p className="text-xs text-gray-400">× {item.quantity}</p>
              </div>
              <span className="text-sm text-gray-700">
                ${(item.unitPrice * item.quantity).toFixed(2)}
              </span>
            </li>
          ))}
        </ul>

        {order.notes && (
          <p className="text-xs text-gray-400 mb-4 italic">"{order.notes}"</p>
        )}

        <div className="border-t border-gray-100 pt-4 flex justify-between items-center">
          <span className="text-sm text-gray-500">Total</span>
          <span className="font-semibold text-gray-900">${order.totalPrice.toFixed(2)}</span>
        </div>
      </div>
    </div>
  )
}
