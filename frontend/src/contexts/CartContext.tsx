import { createContext, useContext, useState } from 'react'

interface CartContextValue {
  cart: Map<string, number>
  totalItems: number
  isOpen: boolean
  setIsOpen: (open: boolean) => void
  addToCart: (itemId: string) => void
  removeFromCart: (itemId: string) => void
  clearCart: () => void
}

const CartContext = createContext<CartContextValue | null>(null)

export function CartProvider({ children }: { children: React.ReactNode }) {
  const [cart, setCart] = useState<Map<string, number>>(new Map())
  const [isOpen, setIsOpen] = useState(false)

  const totalItems = Array.from(cart.values()).reduce((sum, qty) => sum + qty, 0)

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

  const clearCart = () => setCart(new Map())

  return (
    <CartContext.Provider value={{ cart, totalItems, isOpen, setIsOpen, addToCart, removeFromCart, clearCart }}>
      {children}
    </CartContext.Provider>
  )
}

export function useCart() {
  const ctx = useContext(CartContext)
  if (!ctx) throw new Error('useCart must be used within CartProvider')
  return ctx
}
