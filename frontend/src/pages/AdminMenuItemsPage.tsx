import { useState } from 'react'
import * as React from 'react'
import { useQueryClient } from '@tanstack/react-query'
import {
  DndContext,
  closestCenter,
  PointerSensor,
  useSensor,
  useSensors,
  type DragEndEvent,
} from '@dnd-kit/core'
import {
  SortableContext,
  verticalListSortingStrategy,
  useSortable,
  arrayMove,
} from '@dnd-kit/sortable'
import { CSS } from '@dnd-kit/utilities'
import { toast } from 'sonner'
import { useCategories, useMenuItems, useCreateMenuItem, useUpdateMenuItem, useDeleteMenuItem, useUpdateItemPositions } from '../hooks/useMenu.ts'
import type { MenuItem, MenuItemRequest } from '../types/menu.ts'
import ConfirmDialog from '../components/ConfirmDialog.tsx'
import { getImageUploadUrl, uploadImageToS3 } from '../api/menu.ts'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardHeader } from '@/components/ui/card'

const emptyForm = { categoryId: '', name: '', description: '', price: '', available: true }

const selectClass = "flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"

function SortableItemRow({ item }: { item: MenuItem }) {
  const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({ id: item.id })
  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
    opacity: isDragging ? 0.4 : 1,
  }

  return (
    <tr ref={setNodeRef} style={style} className="hover:bg-muted/30 transition-colors">
      <td className="px-3 py-3 w-8">
        <button
          {...attributes}
          {...listeners}
          className="cursor-grab text-muted-foreground hover:text-foreground select-none touch-none text-lg leading-none"
          aria-label="Drag to reorder"
        >
          ⠿
        </button>
      </td>
      <td className="px-5 py-3">
        {item.imageUrl
          ? <img src={item.imageUrl} alt={item.name} className="w-10 h-10 object-cover rounded-md" />
          : <div className="w-10 h-10 bg-muted rounded-md flex items-center justify-center">
              <span className="text-muted-foreground text-xs">-</span>
            </div>
        }
      </td>
      <td className="px-5 py-3 font-medium">{item.name}</td>
      <td className="px-5 py-3 text-muted-foreground">{item.description}</td>
      <td className="px-5 py-3 whitespace-nowrap">{item.price.toFixed(2)} €</td>
      <td className="px-5 py-3">
        <Badge variant={item.available ? 'default' : 'secondary'}>
          {item.available ? 'Available' : 'Hidden'}
        </Badge>
      </td>
    </tr>
  )
}

export default function AdminMenuItemsPage() {
  const [formVisible, setFormVisible] = useState(false)
  const [form, setForm] = useState(emptyForm)
  const [editingId, setEditingId] = useState<string | null>(null)
  const [imageFile, setImageFile] = useState<File | null>(null)
  const [currentImageUrl, setCurrentImageUrl] = useState<string | null>(null)
  const [search, setSearch] = useState('')
  const [availableFilter, setAvailableFilter] = useState<'all' | 'available' | 'hidden'>('all')
  const [reorderMode, setReorderMode] = useState(false)
  const [reorderedGroups, setReorderedGroups] = useState<Record<string, MenuItem[]>>({})
  const [deletingId, setDeletingId] = useState<string | null>(null)

  const queryClient = useQueryClient()
  const { data: categories } = useCategories()
  const { data: items, isLoading, isError } = useMenuItems()
  const createItem = useCreateMenuItem()
  const updateItem = useUpdateMenuItem()
  const deleteItem = useDeleteMenuItem()
  const updatePositions = useUpdateItemPositions()

  const sensors = useSensors(useSensor(PointerSensor))

  const closeForm = () => {
    setFormVisible(false)
    setEditingId(null)
    setForm(emptyForm)
    setImageFile(null)
    setCurrentImageUrl(null)
  }

  const uploadImageIfSelected = async (itemId: string) => {
    if (imageFile) {
      const { uploadUrl } = await getImageUploadUrl(itemId, imageFile.type)
      await uploadImageToS3(uploadUrl, imageFile)
      await queryClient.invalidateQueries({ queryKey: ['menuItems'] })
    }
  }

  const openEdit = (item: MenuItem) => {
    setEditingId(item.id)
    setForm({
      categoryId: item.categoryId,
      name: item.name,
      description: item.description ?? '',
      price: item.price.toString(),
      available: item.available,
    })
    setCurrentImageUrl(item.imageUrl ?? null)
    setFormVisible(true)
  }

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    const itemData: MenuItemRequest = {
      categoryId: form.categoryId,
      name: form.name,
      description: form.description,
      price: parseFloat(form.price),
      available: form.available,
    }

    if (editingId) {
      updateItem.mutate({ id: editingId, data: itemData }, {
        onSuccess: async () => { await uploadImageIfSelected(editingId); closeForm(); toast.success('Item updated') },
        onError: e => toast.error(e.message),
      })
    } else {
      createItem.mutate(itemData, {
        onSuccess: async (created) => { await uploadImageIfSelected(created.id); closeForm(); toast.success('Item created') },
        onError: e => toast.error(e.message),
      })
    }
  }

  const enterReorderMode = () => {
    const groups: Record<string, MenuItem[]> = {}
    for (const cat of categories ?? []) {
      groups[cat.id] = (items ?? []).filter(i => i.categoryId === cat.id)
    }
    setReorderedGroups(groups)
    setSearch('')
    setAvailableFilter('all')
    setFormVisible(false)
    setReorderMode(true)
  }

  const exitReorderMode = () => {
    setReorderMode(false)
    setReorderedGroups({})
  }

  const handleDragEnd = (categoryId: string) => (event: DragEndEvent) => {
    const { active, over } = event
    if (over && active.id !== over.id) {
      setReorderedGroups(groups => {
        const catItems = groups[categoryId] ?? []
        const oldIndex = catItems.findIndex(i => i.id === active.id)
        const newIndex = catItems.findIndex(i => i.id === over.id)
        return { ...groups, [categoryId]: arrayMove(catItems, oldIndex, newIndex) }
      })
    }
  }

  const handleSaveOrder = () => {
    const entries = Object.values(reorderedGroups)
      .flatMap(catItems => catItems.map((item, i) => ({ id: item.id, position: i })))
    updatePositions.mutate(entries, {
      onSuccess: () => { exitReorderMode(); toast.success('Order saved') },
      onError: e => toast.error(e.message),
    })
  }

  const isPending = createItem.isPending || updateItem.isPending
  const filtered = (items ?? []).filter(item => {
    const matchesSearch = item.name.toLowerCase().includes(search.toLowerCase())
    const matchesAvailable =
      availableFilter === 'all' ||
      (availableFilter === 'available' && item.available) ||
      (availableFilter === 'hidden' && !item.available)
    return matchesSearch && matchesAvailable
  })

  const grouped = (categories ?? []).map(cat => ({
    category: cat,
    items: filtered.filter(item => item.categoryId === cat.id),
  }))

  const reorderGrouped = (categories ?? []).map(cat => ({
    category: cat,
    items: reorderedGroups[cat.id] ?? [],
  })).filter(g => g.items.length > 0)

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between px-5 py-4 border-b space-y-0">
        <span className="font-medium">Menu Items</span>
        <div className="flex gap-2">
          {!reorderMode && (
            <>
              <Button size="sm" variant="outline" onClick={enterReorderMode} disabled={!items?.length}>
                Reorder
              </Button>
              <Button size="sm" onClick={() => setFormVisible(true)}>+ Add</Button>
            </>
          )}
          {reorderMode && (
            <>
              <Button size="sm" variant="outline" onClick={exitReorderMode}>Cancel</Button>
              <Button size="sm" onClick={handleSaveOrder} disabled={updatePositions.isPending}>
                {updatePositions.isPending ? 'Saving...' : 'Save order'}
              </Button>
            </>
          )}
        </div>
      </CardHeader>

      {formVisible && !reorderMode && (
        <div className="px-5 py-4 border-b bg-muted/30">
          <form onSubmit={handleSubmit} className="space-y-3">
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-1">
                <Label>Category *</Label>
                <select
                  required
                  value={form.categoryId}
                  onChange={e => setForm(f => ({ ...f, categoryId: e.target.value }))}
                  className={selectClass}
                >
                  <option value="">Select category...</option>
                  {categories?.map(c => (
                    <option key={c.id} value={c.id}>{c.name}</option>
                  ))}
                </select>
              </div>
              <div className="space-y-1">
                <Label>Name *</Label>
                <Input
                  required
                  value={form.name}
                  onChange={e => setForm(f => ({ ...f, name: e.target.value }))}
                />
              </div>
              <div className="space-y-1">
                <Label>Price *</Label>
                <Input
                  required
                  type="number"
                  min="0"
                  step="0.01"
                  value={form.price}
                  onChange={e => setForm(f => ({ ...f, price: e.target.value }))}
                />
              </div>
              <div className="space-y-1">
                <Label>Description</Label>
                <Input
                  value={form.description}
                  onChange={e => setForm(f => ({ ...f, description: e.target.value }))}
                />
              </div>
              <div className="space-y-1">
                <Label>Image</Label>
                {currentImageUrl && !imageFile && (
                  <div className="flex items-center gap-2 mb-1">
                    <img src={currentImageUrl} alt="current" className="w-10 h-10 object-cover rounded-md" />
                    <span className="text-xs text-muted-foreground">Current image - select file to replace</span>
                  </div>
                )}
                <Input
                  type="file"
                  accept="image/*"
                  onChange={e => setImageFile(e.target.files?.[0] ?? null)}
                  className="cursor-pointer"
                />
              </div>
              <div className="flex items-center gap-2 pt-5">
                <input
                  type="checkbox"
                  id="available"
                  checked={form.available}
                  onChange={e => setForm(f => ({ ...f, available: e.target.checked }))}
                  className="rounded border-input"
                />
                <Label htmlFor="available">Available</Label>
              </div>
            </div>
            <div className="flex gap-2">
              <Button type="submit" size="sm" disabled={isPending}>
                {isPending ? 'Saving...' : editingId ? 'Save' : 'Create'}
              </Button>
              <Button type="button" size="sm" variant="outline" onClick={closeForm}>
                Cancel
              </Button>
            </div>
          </form>
        </div>
      )}

      <CardContent className="p-0">
        {isLoading && <p className="px-5 py-8 text-sm text-muted-foreground text-center">Loading...</p>}
        {isError && <p className="px-5 py-8 text-sm text-destructive text-center">Something went wrong.</p>}
        {!isLoading && !isError && items && (
          <>
            {!reorderMode && (
              <div className="px-5 py-3 border-b flex gap-3">
                <Input
                  placeholder="Search items..."
                  value={search}
                  onChange={e => setSearch(e.target.value)}
                  className="max-w-xs h-8 text-sm"
                />
                <select
                  value={availableFilter}
                  onChange={e => setAvailableFilter(e.target.value as 'all' | 'available' | 'hidden')}
                  className="h-8 rounded-md border border-input bg-background px-3 text-sm"
                >
                  <option value="all">All</option>
                  <option value="available">Available</option>
                  <option value="hidden">Hidden</option>
                </select>
              </div>
            )}

            {!reorderMode && (
              grouped.every(g => g.items.length === 0)
                ? <p className="px-5 py-8 text-sm text-muted-foreground text-center">No items found.</p>
                : grouped.map(({ category, items: groupItems }) =>
                    groupItems.length === 0 ? null : (
                      <div key={category.id} className="border-b last:border-b-0">
                        <div className="px-5 py-2 bg-muted/40 border-b">
                          <span className="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                            {category.name}
                          </span>
                        </div>
                        <table className="w-full text-sm">
                          <thead>
                            <tr className="text-left text-xs text-muted-foreground uppercase tracking-wide border-b">
                              <th className="px-5 py-2 font-medium">Image</th>
                              <th className="px-5 py-2 font-medium">Name</th>
                              <th className="px-5 py-2 font-medium">Description</th>
                              <th className="px-5 py-2 font-medium w-24">Price</th>
                              <th className="px-5 py-2 font-medium">Available</th>
                              <th className="px-5 py-2" />
                            </tr>
                          </thead>
                          <tbody className="divide-y">
                            {groupItems.map(item => (
                              <tr key={item.id} className="hover:bg-muted/30 transition-colors">
                                <td className="px-5 py-3">
                                  {item.imageUrl
                                    ? <img src={item.imageUrl} alt={item.name} className="w-10 h-10 object-cover rounded-md" />
                                    : <div className="w-10 h-10 bg-muted rounded-md flex items-center justify-center">
                                        <span className="text-muted-foreground text-xs">-</span>
                                      </div>
                                  }
                                </td>
                                <td className="px-5 py-3 font-medium">{item.name}</td>
                                <td className="px-5 py-3 text-muted-foreground">{item.description}</td>
                                <td className="px-5 py-3 whitespace-nowrap">{item.price.toFixed(2)} €</td>
                                <td className="px-5 py-3">
                                  <Badge variant={item.available ? 'default' : 'secondary'}>
                                    {item.available ? 'Available' : 'Hidden'}
                                  </Badge>
                                </td>
                                <td className="px-5 py-3">
                                  <div className="flex items-center justify-end gap-2">
                                    <Button variant="outline" size="sm" onClick={() => openEdit(item)}>Edit</Button>
                                    <Button
                                      variant="destructive"
                                      size="sm"
                                      onClick={() => setDeletingId(item.id)}
                                    >
                                      Delete
                                    </Button>
                                  </div>
                                </td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      </div>
                    )
                  )
            )}

            {reorderMode && (
              reorderGrouped.length === 0
                ? <p className="px-5 py-8 text-sm text-muted-foreground text-center">No items to reorder.</p>
                : reorderGrouped.map(({ category, items: groupItems }) => (
                    <div key={category.id} className="border-b last:border-b-0">
                      <div className="px-5 py-2 bg-muted/40 border-b">
                        <span className="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                          {category.name}
                        </span>
                      </div>
                      <DndContext
                        sensors={sensors}
                        collisionDetection={closestCenter}
                        onDragEnd={handleDragEnd(category.id)}
                      >
                        <SortableContext items={groupItems.map(i => i.id)} strategy={verticalListSortingStrategy}>
                          <table className="w-full text-sm">
                            <thead>
                              <tr className="text-left text-xs text-muted-foreground uppercase tracking-wide border-b">
                                <th className="px-3 py-2 w-8" />
                                <th className="px-5 py-2 font-medium">Image</th>
                                <th className="px-5 py-2 font-medium">Name</th>
                                <th className="px-5 py-2 font-medium">Description</th>
                                <th className="px-5 py-2 font-medium w-24">Price</th>
                                <th className="px-5 py-2 font-medium">Available</th>
                              </tr>
                            </thead>
                            <tbody className="divide-y">
                              {groupItems.map(item => (
                                <SortableItemRow key={item.id} item={item} />
                              ))}
                            </tbody>
                          </table>
                        </SortableContext>
                      </DndContext>
                    </div>
                  ))
            )}
          </>
        )}
      </CardContent>

      <ConfirmDialog
        open={deletingId !== null}
        title="Delete item?"
        description="This action cannot be undone."
        isPending={deleteItem.isPending}
        onCancel={() => setDeletingId(null)}
        onConfirm={() => {
          if (!deletingId) return
          deleteItem.mutate(deletingId, {
            onSuccess: () => { setDeletingId(null); toast.success('Item deleted') },
            onError: e => { setDeletingId(null); toast.error(e.message) },
          })
        }}
      />
    </Card>
  )
}
