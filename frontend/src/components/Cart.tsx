import type { MenuItem } from '../types/menu'
import type { OrderResponse } from '../types/order'

interface Props {
  items: MenuItem[]
  cart: Map<string, number>
  onAdd: (itemId: string) => void
  onRemove: (itemId: string) => void
  onPlace: () => void
  isPending: boolean
  placedOrder: OrderResponse | null
}

export default function Cart({ items, cart, onAdd, onRemove, onPlace, isPending, placedOrder }: Readonly<Props>) {
  const cartItems = items.filter(i => cart.has(i.id))
  const total = cartItems.reduce((sum, i) => sum + i.price * (cart.get(i.id) ?? 0), 0)

  return (
    <div className="w-72 shrink-0">
      <div className="bg-white border border-gray-200 rounded-xl p-4 sticky top-6">
        <h2 className="font-medium text-gray-900 mb-4">Cart</h2>

        {cartItems.length === 0 ? (
          <p className="text-sm text-gray-400 text-center py-6">Your cart is empty</p>
        ) : (
          <>
            <ul className="space-y-3 mb-4">
              {cartItems.map(item => (
                <li key={item.id} className="flex items-center justify-between gap-2">
                  <div className="flex-1 min-w-0">
                    <p className="text-sm text-gray-900 truncate">{item.name}</p>
                    <p className="text-xs text-gray-400">${item.price.toFixed(2)} each</p>
                  </div>
                  <div className="flex items-center gap-2 shrink-0">
                    <span className="text-sm text-gray-700 w-12 text-right">
                      ${(item.price * (cart.get(item.id) ?? 0)).toFixed(2)}
                    </span>
                    <div className="flex items-center gap-1">
                      <button
                        onClick={() => onRemove(item.id)}
                        className="w-6 h-6 flex items-center justify-center border border-gray-200 rounded-md text-gray-400 hover:text-gray-700 hover:bg-gray-100 transition-colors text-xs"
                      >
                        −
                      </button>
                      <span className="text-sm font-medium w-4 text-center">{cart.get(item.id)}</span>
                      <button
                        onClick={() => onAdd(item.id)}
                        className="w-6 h-6 flex items-center justify-center border border-gray-200 rounded-md text-gray-400 hover:text-gray-700 hover:bg-gray-100 transition-colors text-xs"
                      >
                        +
                      </button>
                    </div>
                  </div>
                </li>
              ))}
            </ul>

            <div className="border-t border-gray-100 pt-3 mb-4 flex justify-between items-center">
              <span className="text-sm text-gray-500">Total</span>
              <span className="font-semibold text-gray-900">${total.toFixed(2)}</span>
            </div>

            <button
              onClick={onPlace}
              disabled={isPending}
              className="w-full py-2 bg-gray-900 text-white text-sm rounded-lg hover:bg-gray-700 disabled:opacity-50 transition-colors"
            >
              {isPending ? 'Placing order...' : 'Place order'}
            </button>
          </>
        )}

        {placedOrder && (
          <div className="mt-4 p-3 bg-green-50 border border-green-200 rounded-lg">
            <p className="text-xs font-medium text-green-700">Order placed!</p>
            <p className="text-xs text-green-600 mt-0.5 font-mono">{placedOrder.id.slice(0, 8)}…</p>
          </div>
        )}
      </div>
    </div>
  )
}
