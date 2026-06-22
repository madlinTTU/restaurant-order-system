import { useMenuItems } from '@/hooks/useMenu'
import { useCart } from '@/contexts/CartContext'
import MenuItemCard from '@/components/MenuItemCard'

export default function MenuPage() {
  const { data: items, isLoading, isError } = useMenuItems()
  const { cart, addToCart, removeFromCart } = useCart()

  if (isLoading) return <p className="p-6 text-sm text-muted-foreground">Loading...</p>
  if (isError) return <p className="p-6 text-sm text-destructive">Failed to load menu.</p>

  const grouped = (items ?? []).reduce<Record<string, typeof items>>((acc, item) => {
    const key = item.categoryName
    acc[key] = [...(acc[key] ?? []), item]
    return acc
  }, {})

  return (
    <div className="max-w-4xl mx-auto px-6 py-8">
      <h1 className="text-xl font-semibold mb-8">Menu</h1>
      <div className="space-y-10">
        {Object.entries(grouped).map(([category, categoryItems]) => (
          <section key={category}>
            <h2 className="text-xs font-semibold text-muted-foreground uppercase tracking-widest mb-4">
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
  )
}
