import { useQueryClient } from '@tanstack/react-query'
import { useActiveOrders, useUpdateOrderStatus, useKitchenSocket } from '../hooks/useKitchen'
import KitchenOrderCard from '../components/KitchenOrderCard'

export default function KitchenPage() {
  const queryClient = useQueryClient()
  const { data: orders, isLoading, isError } = useActiveOrders()
  const updateStatus = useUpdateOrderStatus()

  useKitchenSocket(() => {
    queryClient.invalidateQueries({ queryKey: ['activeOrders'] })
  })

  if (isLoading) return <p className="p-6 text-sm text-gray-400">Loading...</p>
  if (isError) return <p className="p-6 text-sm text-red-500">Failed to load orders.</p>

  const sorted = [...(orders ?? [])].sort(
    (a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()
  )

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-4xl mx-auto px-6 py-8">
        <h1 className="text-xl font-semibold text-gray-900 mb-6">Kitchen Dashboard</h1>

        {sorted.length === 0 ? (
          <p className="text-sm text-gray-400 text-center py-16">No active orders.</p>
        ) : (
          <div className="space-y-3">
            {sorted.map(order => (
              <KitchenOrderCard
                key={order.id}
                order={order}
                onAdvance={(id, status) => updateStatus.mutate({ id, status })}
              />
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
