import { useParams, useNavigate } from 'react-router-dom'
import { ArrowLeft, ShoppingCart } from 'lucide-react'
import { useMenuItems } from '@/hooks/useMenu'
import { useCart } from '@/contexts/CartContext'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'

export default function MenuItemPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { data: items, isLoading } = useMenuItems()
  const { cart, addToCart, removeFromCart, setIsOpen } = useCart()

  const item = items?.find(i => i.id === id)
  const quantity = cart.get(id ?? '') ?? 0

  if (isLoading) return <p className="p-6 text-sm text-muted-foreground">Loading...</p>
  if (!item) return <p className="p-6 text-sm text-destructive">Item not found.</p>

  return (
    <div className="max-w-4xl mx-auto px-6 py-8">
      <button
        onClick={() => navigate(-1)}
        className="flex items-center gap-1.5 text-sm text-muted-foreground hover:text-foreground transition-colors mb-8"
      >
        <ArrowLeft className="h-4 w-4" />
        Back to menu
      </button>

      <div className="grid lg:grid-cols-2 gap-10 items-start">
        <div className="rounded-2xl overflow-hidden bg-muted aspect-square">
          {item.imageUrl ? (
            <img src={item.imageUrl} alt={item.name} className="w-full h-full object-cover" />
          ) : (
            <div className="w-full h-full flex items-center justify-center text-muted-foreground text-sm">
              No image
            </div>
          )}
        </div>

        <div className="space-y-6">
          <div>
            <Badge variant="secondary" className="mb-3">{item.categoryName}</Badge>
            <h1 className="text-2xl font-semibold tracking-tight">{item.name}</h1>
            {item.description && (
              <p className="text-muted-foreground mt-3 leading-relaxed">{item.description}</p>
            )}
          </div>

          {!item.available && (
            <p className="text-sm text-destructive">Currently unavailable</p>
          )}

          <div className="border-t pt-6 space-y-4">
            <div className="flex items-baseline justify-between">
              <span className="text-sm text-muted-foreground">Price</span>
              <span className="text-2xl font-semibold">{item.price.toFixed(2)} €</span>
            </div>

            {item.available && (
              quantity === 0 ? (
                <Button className="w-full" size="lg" onClick={onAdd}>
                  Add to order
                </Button>
              ) : (
                <div className="space-y-3">
                  <div className="flex items-center justify-between border rounded-lg p-1">
                    <Button variant="ghost" size="icon" onClick={() => removeFromCart(item.id)}>−</Button>
                    <span className="text-sm font-medium">{quantity} in order</span>
                    <Button variant="ghost" size="icon" onClick={() => addToCart(item.id)}>+</Button>
                  </div>
                  <Button className="w-full" variant="outline" onClick={() => setIsOpen(true)}>
                    <ShoppingCart className="h-4 w-4 mr-2" />
                    View order · {(item.price * quantity).toFixed(2)} €
                  </Button>
                </div>
              )
            )}
          </div>
        </div>
      </div>
    </div>
  )

  function onAdd() {
    addToCart(item!.id)
  }
}
