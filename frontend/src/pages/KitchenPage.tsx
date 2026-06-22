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

  if (isLoading) return (
    <div className="flex items-center justify-center min-h-screen">
      <p className="text-sm text-muted-foreground">Loading orders...</p>
    </div>
  )

  if (isError) return (
    <div className="flex items-center justify-center min-h-screen">
      <p className="text-sm text-destructive">Failed to load orders.</p>
    </div>
  )

  const sorted = [...(orders ?? [])].sort(
    (a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()
  )

  return (
    <div className="max-w-2xl mx-auto px-4 py-8">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-xl font-semibold">Kitchen Dashboard</h1>
        <span className="text-sm text-muted-foreground">
          {sorted.length} active {sorted.length === 1 ? 'order' : 'orders'}
        </span>
      </div>

      {sorted.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-24 text-center">
          <p className="text-muted-foreground text-sm">No active orders right now.</p>
        </div>
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
  )
}
