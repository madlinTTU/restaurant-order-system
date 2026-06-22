import { useState } from 'react'
import { useOrders } from '../hooks/useOrders'
import type { OrderResponse } from '../types/order'
import OrderRow from '../components/OrderRow'
import OrderModal from '../components/OrderModal'

export default function MyOrdersPage() {
  const { data: orders, isLoading, isError } = useOrders()
  const [selectedOrder, setSelectedOrder] = useState<OrderResponse | null>(null)

  if (isLoading) return <p className="p-6 text-sm text-gray-400">Loading...</p>
  if (isError) return <p className="p-6 text-sm text-red-500">Failed to load orders.</p>

  const sorted = [...(orders ?? [])].sort(
    (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
  )

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-3xl mx-auto px-6 py-8">
        <h1 className="text-xl font-semibold text-gray-900 mb-6">My Orders</h1>

        {sorted.length === 0 ? (
          <p className="text-sm text-gray-400 text-center py-16">No orders yet.</p>
        ) : (
          <div className="bg-white border border-gray-200 rounded-xl overflow-hidden">
            <table className="w-full">
              <thead>
                <tr className="text-left text-xs text-gray-400 uppercase tracking-wider">
                  <th className="py-3 px-4 font-medium">Order</th>
                  <th className="py-3 px-4 font-medium">Date</th>
                  <th className="py-3 px-4 font-medium">Status</th>
                  <th className="py-3 px-4 font-medium text-right">Total</th>
                  <th className="py-3 px-4" />
                </tr>
              </thead>
              <tbody>
                {sorted.map(order => (
                  <OrderRow key={order.id} order={order} onView={setSelectedOrder} />
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {selectedOrder && (
        <OrderModal order={selectedOrder} onClose={() => setSelectedOrder(null)} />
      )}
    </div>
  )
}
