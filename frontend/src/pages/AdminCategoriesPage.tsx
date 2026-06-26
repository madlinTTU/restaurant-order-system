import { useState } from 'react'
import * as React from 'react'
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
import { useCategories, useCreateCategory, useDeleteCategory, useUpdateCategory, useUpdateCategoryPositions } from '../hooks/useMenu.ts'
import type { MenuCategory } from '../types/menu.ts'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Card, CardContent, CardHeader } from '@/components/ui/card'

const emptyForm = { name: '', description: '' }

function SortableCategoryRow({ category }: { category: MenuCategory }) {
  const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({ id: category.id })
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
      <td className="px-5 py-3 font-medium">{category.name}</td>
      <td className="px-5 py-3 text-muted-foreground">{category.description}</td>
    </tr>
  )
}

export default function AdminCategoriesPage() {
  const [formVisible, setFormVisible] = useState(false)
  const [form, setForm] = useState({ name: '', description: '' })
  const [editingId, setEditingId] = useState<string | null>(null)
  const [search, setSearch] = useState('')
  const [reorderMode, setReorderMode] = useState(false)
  const [reorderedList, setReorderedList] = useState<MenuCategory[]>([])

  const { data: categories, isLoading, isError } = useCategories()
  const createCategory = useCreateCategory()
  const updateCategory = useUpdateCategory()
  const deleteCategory = useDeleteCategory()
  const updatePositions = useUpdateCategoryPositions()

  const sensors = useSensors(useSensor(PointerSensor))

  const closeForm = () => {
    setFormVisible(false)
    setEditingId(null)
    setForm(emptyForm)
  }

  const openEdit = (category: MenuCategory) => {
    setEditingId(category.id)
    setForm({ name: category.name, description: category.description ?? '' })
    setFormVisible(true)
  }

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (editingId) {
      updateCategory.mutate({ id: editingId, data: form }, {
        onSuccess: closeForm,
        onError: e => alert(e.message),
      })
    } else {
      createCategory.mutate(form, {
        onSuccess: closeForm,
        onError: e => alert(e.message),
      })
    }
  }

  const enterReorderMode = () => {
    setReorderedList([...(categories ?? [])])
    setSearch('')
    setFormVisible(false)
    setReorderMode(true)
  }

  const exitReorderMode = () => {
    setReorderMode(false)
    setReorderedList([])
  }

  const handleDragEnd = (event: DragEndEvent) => {
    const { active, over } = event
    if (over && active.id !== over.id) {
      setReorderedList(list => {
        const oldIndex = list.findIndex(c => c.id === active.id)
        const newIndex = list.findIndex(c => c.id === over.id)
        return arrayMove(list, oldIndex, newIndex)
      })
    }
  }

  const handleSaveOrder = () => {
    updatePositions.mutate(
      reorderedList.map((cat, i) => ({ id: cat.id, position: i })),
      { onSuccess: exitReorderMode }
    )
  }

  const isPending = createCategory.isPending || updateCategory.isPending
  const filtered = (categories ?? []).filter(c =>
    c.name.toLowerCase().includes(search.toLowerCase())
  )

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between px-5 py-4 border-b space-y-0">
        <span className="font-medium">Categories</span>
        <div className="flex gap-2">
          {!reorderMode && (
            <>
              <Button size="sm" variant="outline" onClick={enterReorderMode} disabled={!categories?.length}>
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
                <Label>Name *</Label>
                <Input
                  required
                  value={form.name}
                  onChange={e => setForm(f => ({ ...f, name: e.target.value }))}
                />
              </div>
              <div className="space-y-1">
                <Label>Description</Label>
                <Input
                  value={form.description}
                  onChange={e => setForm(f => ({ ...f, description: e.target.value }))}
                />
              </div>
            </div>
            <div className="flex gap-2">
              <Button type="submit" size="sm" disabled={isPending}>
                {editingId ? 'Save' : 'Create'}
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
        {!isLoading && !isError && categories && (
          <>
            {!reorderMode && (
              <div className="px-5 py-3 border-b">
                <Input
                  placeholder="Search by category name..."
                  value={search}
                  onChange={e => setSearch(e.target.value)}
                  className="max-w-xs h-8 text-sm"
                />
              </div>
            )}

            {!reorderMode && (
              filtered.length === 0
                ? <p className="px-5 py-8 text-sm text-muted-foreground text-center">No categories found.</p>
                : (
                  <table className="w-full text-sm">
                    <thead>
                      <tr className="text-left text-xs text-muted-foreground uppercase tracking-wide border-b">
                        <th className="px-5 py-3 font-medium">Name</th>
                        <th className="px-5 py-3 font-medium">Description</th>
                        <th className="px-5 py-3" />
                      </tr>
                    </thead>
                    <tbody className="divide-y">
                      {filtered.map(category => (
                        <tr key={category.id} className="hover:bg-muted/30 transition-colors">
                          <td className="px-5 py-3 font-medium">{category.name}</td>
                          <td className="px-5 py-3 text-muted-foreground">{category.description}</td>
                          <td className="px-5 py-3">
                            <div className="flex items-center justify-end gap-2">
                              <Button variant="outline" size="sm" onClick={() => openEdit(category)}>
                                Edit
                              </Button>
                              <Button
                                variant="destructive"
                                size="sm"
                                onClick={() => { if (confirm('Delete this category?')) deleteCategory.mutate(category.id) }}
                              >
                                Delete
                              </Button>
                            </div>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                )
            )}

            {reorderMode && (
              <DndContext sensors={sensors} collisionDetection={closestCenter} onDragEnd={handleDragEnd}>
                <SortableContext items={reorderedList.map(c => c.id)} strategy={verticalListSortingStrategy}>
                  <table className="w-full text-sm">
                    <thead>
                      <tr className="text-left text-xs text-muted-foreground uppercase tracking-wide border-b">
                        <th className="px-3 py-3 w-8" />
                        <th className="px-5 py-3 font-medium">Name</th>
                        <th className="px-5 py-3 font-medium">Description</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y">
                      {reorderedList.map(category => (
                        <SortableCategoryRow key={category.id} category={category} />
                      ))}
                    </tbody>
                  </table>
                </SortableContext>
              </DndContext>
            )}
          </>
        )}
      </CardContent>
    </Card>
  )
}
