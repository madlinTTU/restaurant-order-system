import { useNavigate } from 'react-router-dom'
import type { MenuItem } from '@/types/menu'
import { Button } from '@/components/ui/button'

interface Props {
  item: MenuItem
  quantity: number
  onAdd: () => void
  onRemove: () => void
}

export default function MenuItemCard({ item, quantity, onAdd, onRemove }: Readonly<Props>) {
  const navigate = useNavigate()

  return (
    <div className="group border rounded-xl overflow-hidden flex flex-col bg-background hover:shadow-md transition-shadow">
      <div
        className="relative cursor-pointer overflow-hidden"
        onClick={() => navigate(`/menu/${item.id}`)}
      >
        {item.imageUrl ? (
          <img
            src={item.imageUrl}
            alt={item.name}
            className="w-full h-44 object-cover group-hover:scale-105 transition-transform duration-300"
          />
        ) : (
          <div className="w-full h-44 bg-muted flex items-center justify-center text-muted-foreground text-sm">
            No image
          </div>
        )}
      </div>

      <div className="p-3 flex flex-col gap-2 flex-1">
        <div
          className="cursor-pointer"
          onClick={() => navigate(`/menu/${item.id}`)}
        >
          <p className="font-medium text-sm leading-tight">{item.name}</p>
        </div>

        <div className="flex items-center justify-between mt-auto">
          <span className="text-sm font-semibold">{item.price.toFixed(2)} €</span>

          {!item.available ? (
            <span className="text-xs text-muted-foreground">Unavailable</span>
          ) : quantity === 0 ? (
            <Button size="sm" onClick={onAdd}>Add</Button>
          ) : (
            <div className="flex items-center gap-1.5">
              <Button variant="outline" size="icon" className="h-7 w-7" onClick={onRemove}>−</Button>
              <span className="text-sm font-medium w-4 text-center">{quantity}</span>
              <Button size="icon" className="h-7 w-7" onClick={onAdd}>+</Button>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
