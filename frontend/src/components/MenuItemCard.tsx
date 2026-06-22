import type { MenuItem } from '@/types/menu'
import { Card, CardContent } from '@/components/ui/card'
import { Button } from '@/components/ui/button'

interface Props {
  item: MenuItem
  quantity: number
  onAdd: () => void
  onRemove: () => void
}

export default function MenuItemCard({ item, quantity, onAdd, onRemove }: Readonly<Props>) {
  return (
    <Card className="overflow-hidden flex flex-col p-0">
      {item.imageUrl ? (
        <img src={item.imageUrl} alt={item.name} className="w-full h-40 object-cover" />
      ) : (
        <div className="w-full h-40 bg-muted flex items-center justify-center text-muted-foreground text-sm">
          No image
        </div>
      )}

      <CardContent className="flex flex-col flex-1 gap-2 p-4">
        <div className="flex-1">
          <p className="font-medium text-sm">{item.name}</p>
          {item.description && (
            <p className="text-xs text-muted-foreground mt-0.5 line-clamp-2">{item.description}</p>
          )}
        </div>

        <div className="flex items-center justify-between mt-1">
          <span className="text-sm font-semibold">{item.price.toFixed(2)} €</span>

          {!item.available ? (
            <span className="text-xs text-muted-foreground">Unavailable</span>
          ) : quantity === 0 ? (
            <Button size="sm" onClick={onAdd}>Add</Button>
          ) : (
            <div className="flex items-center gap-2">
              <Button variant="outline" size="icon" className="h-7 w-7" onClick={onRemove}>−</Button>
              <span className="text-sm font-medium w-4 text-center">{quantity}</span>
              <Button size="icon" className="h-7 w-7" onClick={onAdd}>+</Button>
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  )
}
