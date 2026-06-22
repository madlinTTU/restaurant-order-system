import { useMutation } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { useCart } from '@/contexts/CartContext'
import { useMenuItems } from '@/hooks/useMenu'
import { createOrder } from '@/api/orders'
import { Sheet, SheetContent, SheetHeader, SheetTitle } from '@/components/ui/sheet'
import { Button } from '@/components/ui/button'

export default function CartSheet() {
  const { cart, isOpen, setIsOpen, addToCart, removeFromCart, clearCart } = useCart()
  const { data: items } = useMenuItems()
  const navigate = useNavigate()

  const { mutate: placeOrder, isPending } = useMutation({
    mutationFn: createOrder,
    onSuccess: (order) => {
      clearCart()
      setIsOpen(false)
      navigate(`/orders/${order.id}/track`)
    },
  })

  const cartItems = (items ?? []).filter(i => cart.has(i.id))
  const total = cartItems.reduce((sum, i) => sum + i.price * (cart.get(i.id) ?? 0), 0)

  const handlePlaceOrder = () => {
    placeOrder({
      items: Array.from(cart.entries()).map(([menuItemId, quantity]) => ({ menuItemId, quantity })),
    })
  }

  return (
    <Sheet open={isOpen} onOpenChange={setIsOpen}>
      <SheetContent className="flex flex-col w-80">
        <SheetHeader>
          <SheetTitle>Your order</SheetTitle>
        </SheetHeader>

        {cartItems.length === 0 ? (
          <p className="text-sm text-muted-foreground text-center py-12">Your cart is empty</p>
        ) : (
          <>
            <ul className="flex-1 overflow-y-auto space-y-4 py-4">
              {cartItems.map(item => (
                <li key={item.id} className="flex items-center justify-between gap-2">
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium truncate">{item.name}</p>
                    <p className="text-xs text-muted-foreground">{item.price.toFixed(2)} € each</p>
                  </div>
                  <div className="flex items-center gap-2 shrink-0">
                    <span className="text-sm w-12 text-right">
                      {(item.price * (cart.get(item.id) ?? 0)).toFixed(2)} €
                    </span>
                    <div className="flex items-center gap-1">
                      <Button variant="outline" size="icon" className="h-6 w-6 text-xs" onClick={() => removeFromCart(item.id)}>−</Button>
                      <span className="text-sm font-medium w-4 text-center">{cart.get(item.id)}</span>
                      <Button variant="outline" size="icon" className="h-6 w-6 text-xs" onClick={() => addToCart(item.id)}>+</Button>
                    </div>
                  </div>
                </li>
              ))}
            </ul>

            <div className="border-t pt-4 space-y-4">
              <div className="flex justify-between items-center">
                <span className="text-sm text-muted-foreground">Total</span>
                <span className="font-semibold">{total.toFixed(2)} €</span>
              </div>
              <Button className="w-full" onClick={handlePlaceOrder} disabled={isPending}>
                {isPending ? 'Placing order...' : 'Place order'}
              </Button>
            </div>
          </>
        )}
      </SheetContent>
    </Sheet>
  )
}
