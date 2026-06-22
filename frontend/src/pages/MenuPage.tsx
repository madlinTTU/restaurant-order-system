import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { useMenuItems } from '../hooks/useMenu'
import { createOrder } from '../api/orders'
import type { OrderResponse } from '../types/order'
import MenuItemCard from '../components/MenuItemCard'
import Cart from '../components/Cart'

export default function MenuPage() {
  const { data: items, isLoading, isError } = useMenuItems()
  const [cart, setCart] = useState<Map<string, number>>(new Map())
  const [placedOrder, setPlacedOrder] = useState<OrderResponse | null>(null)

  const { mutate: placeOrder, isPending } = useMutation({
    mutationFn: createOrder,
    onSuccess: (order) => {
      setPlacedOrder(order)
      setCart(new Map())
    },
  })

  const addToCart = (itemId: string) => {
    setCart(prev => new Map(prev).set(itemId, (prev.get(itemId) ?? 0) + 1))
  }

  const removeFromCart = (itemId: string) => {
    setCart(prev => {
      const next = new Map(prev)
      const qty = next.get(itemId) ?? 0
      if (qty <= 1) next.delete(itemId)
      else next.set(itemId, qty - 1)
      return next
    })
  }

  const handlePlaceOrder = () => {
    placeOrder({
      items: Array.from(cart.entries()).map(([menuItemId, quantity]) => ({ menuItemId, quantity })),
    })
  }

  if (isLoading) return <p className="p-6 text-sm text-gray-400">Loading...</p>
  if (isError) return <p className="p-6 text-sm text-red-500">Failed to load menu.</p>

  const grouped = (items ?? []).reduce<Record<string, typeof items>>((acc, item) => {
    const key = item.categoryName
    acc[key] = [...(acc[key] ?? []), item]
    return acc
  }, {})

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-5xl mx-auto px-6 py-8 flex gap-8 items-start">
        <div className="flex-1">
          <h1 className="text-xl font-semibold text-gray-900 mb-8">Menu</h1>
          <div className="space-y-10">
            {Object.entries(grouped).map(([category, categoryItems]) => (
              <section key={category}>
                <h2 className="text-sm font-semibold text-gray-500 uppercase tracking-widest mb-4">
                  {category}
                </h2>
                <div className="grid grid-cols-2 gap-4 sm:grid-cols-3">
                  {categoryItems?.map(item => (
                    <MenuItemCard
                      key={item.id}
                      item={item}
                      quantity={cart.get(item.id) ?? 0}
                      onAdd={() => addToCart(item.id)}
                      onRemove={() => removeFromCart(item.id)}
                    />
                  ))}
                </div>
              </section>
            ))}
          </div>
        </div>

        <Cart
          items={items ?? []}
          cart={cart}
          onAdd={addToCart}
          onRemove={removeFromCart}
          onPlace={handlePlaceOrder}
          isPending={isPending}
          placedOrder={placedOrder}
        />
      </div>
    </div>
  )
}
