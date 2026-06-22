import type { MenuItem } from '../types/menu'

interface Props {
  item: MenuItem
  quantity: number
  onAdd: () => void
  onRemove: () => void
}

export default function MenuItemCard({ item, quantity, onAdd, onRemove }: Readonly<Props>) {
  return (
    <div className="bg-white border border-gray-200 rounded-xl overflow-hidden flex flex-col">
      {item.imageUrl ? (
        <img src={item.imageUrl} alt={item.name} className="w-full h-40 object-cover" />
      ) : (
        <div className="w-full h-40 bg-gray-100 flex items-center justify-center text-gray-300 text-sm">
          No image
        </div>
      )}

      <div className="p-4 flex flex-col flex-1 gap-2">
        <div className="flex-1">
          <p className="font-medium text-gray-900 text-sm">{item.name}</p>
          {item.description && (
            <p className="text-xs text-gray-400 mt-0.5 line-clamp-2">{item.description}</p>
          )}
        </div>

        <div className="flex items-center justify-between mt-1">
          <span className="text-sm font-semibold text-gray-900">${item.price.toFixed(2)}</span>

          {!item.available ? (
            <span className="text-xs text-gray-400">Unavailable</span>
          ) : quantity === 0 ? (
            <button
              onClick={onAdd}
              className="px-3 py-1.5 bg-gray-900 text-white text-xs rounded-lg hover:bg-gray-700 transition-colors"
            >
              Add
            </button>
          ) : (
            <div className="flex items-center gap-2">
              <button
                onClick={onRemove}
                className="w-7 h-7 flex items-center justify-center border border-gray-200 rounded-lg text-gray-600 hover:bg-gray-100 transition-colors"
              >
                −
              </button>
              <span className="text-sm font-medium w-4 text-center">{quantity}</span>
              <button
                onClick={onAdd}
                className="w-7 h-7 flex items-center justify-center bg-gray-900 text-white rounded-lg hover:bg-gray-700 transition-colors"
              >
                +
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
