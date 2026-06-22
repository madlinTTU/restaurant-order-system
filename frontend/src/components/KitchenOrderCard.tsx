import type { OrderResponse } from '../types/order'

const NEXT_STATUS: Record<string, string> = {
  PLACED: 'CONFIRMED',
  CONFIRMED: 'PREPARING',
  PREPARING: 'READY',
  READY: 'PICKED_UP',
}

const STATUS_LABEL: Record<string, string> = {
  PLACED: 'New',
  CONFIRMED: 'Confirmed',
  PREPARING: 'Preparing',
  READY: 'Ready',
}

const STATUS_COLOR: Record<string, string> = {
  PLACED: 'bg-yellow-100 text-yellow-800',
  CONFIRMED: 'bg-blue-100 text-blue-800',
  PREPARING: 'bg-orange-100 text-orange-800',
  READY: 'bg-green-100 text-green-800',
}

export default function KitchenOrderCard(
	{order, onAdvance}: Readonly<{ order: OrderResponse, onAdvance: (id: string, status: string) => void }>) {
  const nextStatus = NEXT_STATUS[order.status]

  return (
    <div className="bg-white border border-gray-200 rounded-lg px-5 py-4">
      <div className="flex items-start justify-between gap-4">
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-3 mb-2">
            <span className="text-sm font-medium text-gray-900">
              #{order.id.slice(0, 8)}
            </span>
            <span className={`inline-block px-2 py-0.5 rounded text-xs font-medium ${STATUS_COLOR[order.status] ?? 'bg-gray-100 text-gray-700'}`}>
              {STATUS_LABEL[order.status] ?? order.status}
            </span>
            <span className="text-xs text-gray-400">
              {new Date(order.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
            </span>
          </div>

          <ul className="space-y-0.5">
            {order.items.map(item => (
              <li key={item.id} className="text-sm text-gray-700">
                <span className="font-medium">{item.quantity}×</span> {item.menuItemName}
              </li>
            ))}
          </ul>

          {order.notes && (
            <p className="mt-2 text-xs text-gray-500 italic">Note: {order.notes}</p>
          )}
        </div>

        {nextStatus && (
          <button
            onClick={() => onAdvance(order.id, nextStatus)}
            className="shrink-0 px-3 py-1.5 bg-gray-900 text-white text-sm rounded-md hover:bg-gray-700 transition-colors"
          >
            → {nextStatus.charAt(0) + nextStatus.slice(1).toLowerCase()}
          </button>
        )}
      </div>
    </div>
  )
}
