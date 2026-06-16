import { useMenuItems } from '../hooks/useMenu'

export default function MenuPage() {
  const { data: items, isLoading, isError } = useMenuItems()

  if (isLoading) return <div>Loading...</div>
  if (isError) return <div>Failed to load menu</div>

  return (
    <div>
      <h1>Menu</h1>
      <ul>
        {items?.map(item => (
          <li key={item.id}>
            {item.imageUrl && <img src={item.imageUrl} alt={item.name} width={80} />}
            <span>{item.name}</span>
            <span> — ${item.price}</span>
            {!item.available && <span> (unavailable)</span>}
          </li>
        ))}
      </ul>
    </div>
  )
}
