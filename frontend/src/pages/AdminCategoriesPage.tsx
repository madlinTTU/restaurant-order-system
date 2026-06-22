import { useState } from 'react'
import * as React from 'react'
import { useCategories, useCreateCategory, useDeleteCategory, useUpdateCategory } from '../hooks/useMenu.ts'
import type { MenuCategory } from '../types/menu.ts'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Card, CardContent, CardHeader } from '@/components/ui/card'

const emptyForm = { name: '', description: '' }

export default function AdminCategoriesPage() {
  const [formVisible, setFormVisible] = useState(false)
  const [form, setForm] = useState({ name: '', description: '' })
  const [editingId, setEditingId] = useState<string | null>(null)
  const [search, setSearch] = useState('')

  const { data: categories, isLoading, isError } = useCategories()
  const createCategory = useCreateCategory()
  const updateCategory = useUpdateCategory()
  const deleteCategory = useDeleteCategory()

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

  const isPending = createCategory.isPending || updateCategory.isPending
  const filtered = (categories ?? []).filter(c =>
    c.name.toLowerCase().includes(search.toLowerCase())
  )

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between px-5 py-4 border-b space-y-0">
        <span className="font-medium">Categories</span>
        <Button size="sm" onClick={() => setFormVisible(true)}>+ Add</Button>
      </CardHeader>

      {formVisible && (
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
            <div className="px-5 py-3 border-b">
              <Input
                placeholder="Search by category name..."
                value={search}
                onChange={e => setSearch(e.target.value)}
                className="max-w-xs h-8 text-sm"
              />
            </div>
            {filtered.length === 0
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
            }
          </>
        )}
      </CardContent>
    </Card>
  )
}
