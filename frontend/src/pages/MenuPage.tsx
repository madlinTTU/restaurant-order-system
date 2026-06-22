import { useState } from 'react'
import { useMenuItems } from '@/hooks/useMenu'
import { useCart } from '@/contexts/CartContext'
import MenuItemCard from '@/components/MenuItemCard'

export default function MenuPage() {
  const { data: items, isLoading, isError } = useMenuItems()
  const { cart, addToCart, removeFromCart } = useCart()
  const [activeCategory, setActiveCategory] = useState<string | null>(null)

  if (isLoading) return <p className="p-6 text-sm text-muted-foreground">Loading...</p>
  if (isError) return <p className="p-6 text-sm text-destructive">Failed to load menu.</p>

  const grouped = (items ?? []).reduce<Record<string, typeof items>>((acc, item) => {
    acc[item.categoryName] = [...(acc[item.categoryName] ?? []), item]
    return acc
  }, {})

  const categories = Object.keys(grouped)
  const filtered = activeCategory ? { [activeCategory]: grouped[activeCategory] } : grouped

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
        {categories.map(cat => (
          <button
            key={cat}
            onClick={() => setActiveCategory(cat === activeCategory ? null : cat)}
            className={`px-4 py-1.5 rounded-full text-sm font-medium transition-colors ${
              activeCategory === cat
                ? 'bg-primary text-primary-foreground'
                : 'bg-muted text-muted-foreground hover:bg-muted/80'
            }`}
          >
            {cat}
          </button>
        ))}
      </div>

      <div className="space-y-10">
        {Object.entries(filtered).map(([category, categoryItems]) => (
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
