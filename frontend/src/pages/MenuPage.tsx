import { useState } from 'react'
import { useMenuItems, useCategories } from '@/hooks/useMenu'
import { useCart } from '@/contexts/CartContext'
import MenuItemCard from '@/components/MenuItemCard'

export default function MenuPage() {
  const { data: items, isLoading, isError } = useMenuItems()
  const { data: categories } = useCategories()
  const { cart, addToCart, removeFromCart } = useCart()
  const [activeCategory, setActiveCategory] = useState<string | null>(null)

  if (isLoading) return <p className="p-6 text-sm text-muted-foreground">Loading...</p>
  if (isError) return <p className="p-6 text-sm text-destructive">Failed to load menu.</p>

  const grouped = (categories ?? []).map(cat => ({
    id: cat.id,
    name: cat.name,
    items: (items ?? []).filter(item => item.categoryId === cat.id),
  })).filter(g => g.items.length > 0)

  const visibleGroups = activeCategory ? grouped.filter(g => g.id === activeCategory) : grouped

  return (
    <div className="max-w-4xl mx-auto px-6 py-8 space-y-8">
      <div>
        <h1 className="text-2xl font-semibold tracking-tight">Menu</h1>
        <p className="text-sm text-muted-foreground mt-1">Fresh ingredients, every order</p>
      </div>

      <div className="flex gap-2 flex-wrap">
        <button
          onClick={() => setActiveCategory(null)}
          className={`px-4 py-1.5 rounded-full text-sm font-medium transition-colors ${
            activeCategory === null
              ? 'bg-primary text-primary-foreground'
              : 'bg-muted text-muted-foreground hover:bg-muted/80'
          }`}
        >
          All
        </button>
        {grouped.map(g => (
          <button
            key={g.id}
            onClick={() => setActiveCategory(g.id === activeCategory ? null : g.id)}
            className={`px-4 py-1.5 rounded-full text-sm font-medium transition-colors ${
              activeCategory === g.id
                ? 'bg-primary text-primary-foreground'
                : 'bg-muted text-muted-foreground hover:bg-muted/80'
            }`}
          >
            {g.name}
          </button>
        ))}
      </div>

      <div className="space-y-10">
        {visibleGroups.map(g => (
          <section key={g.id}>
            <h2 className="text-xs font-semibold text-muted-foreground uppercase tracking-widest mb-4">
              {g.name}
            </h2>
            <div className="grid grid-cols-2 gap-4 sm:grid-cols-3">
              {g.items.map(item => (
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
