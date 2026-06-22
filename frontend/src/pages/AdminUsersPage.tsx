import { useState } from 'react'
import * as React from 'react'
import { useUsers, useCreateUser } from '../hooks/useUsers'
import type { CreateUserRequest, Role } from '../types/user'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardHeader } from '@/components/ui/card'

const emptyForm: CreateUserRequest = { email: '', password: '', role: 'KITCHEN' }

export default function AdminUsersPage() {
  const [formVisible, setFormVisible] = useState(false)
  const [form, setForm] = useState<CreateUserRequest>(emptyForm)
  const [search, setSearch] = useState('')
  const [roleFilter, setRoleFilter] = useState('')

  const { data: users, isLoading, isError } = useUsers()
  const createUser = useCreateUser()

  const closeForm = () => {
    setFormVisible(false)
    setForm(emptyForm)
  }

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    createUser.mutate(form, {
      onSuccess: closeForm,
      onError: (e) => alert(e.message),
    })
  }

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between px-5 py-4 border-b space-y-0">
        <span className="font-medium">Users</span>
        <Button size="sm" onClick={() => setFormVisible(true)}>+ Add</Button>
      </CardHeader>

      {formVisible && (
        <div className="px-5 py-4 border-b bg-muted/30">
          <form onSubmit={handleSubmit} className="space-y-3">
            <div className="grid grid-cols-3 gap-3">
              <div className="space-y-1">
                <Label>Email *</Label>
                <Input
                  required
                  type="email"
                  value={form.email}
                  onChange={e => setForm(f => ({ ...f, email: e.target.value }))}
                />
              </div>
              <div className="space-y-1">
                <Label>Password *</Label>
                <Input
                  required
                  type="password"
                  minLength={8}
                  value={form.password}
                  onChange={e => setForm(f => ({ ...f, password: e.target.value }))}
                />
              </div>
              <div className="space-y-1">
                <Label>Role *</Label>
                <select
                  value={form.role}
                  onChange={e => setForm(f => ({ ...f, role: e.target.value as Role }))}
                  className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
                >
                  <option value="KITCHEN">Kitchen</option>
                  <option value="ADMIN">Admin</option>
                </select>
              </div>
            </div>
            <div className="flex gap-2">
              <Button type="submit" size="sm" disabled={createUser.isPending}>
                {createUser.isPending ? 'Creating...' : 'Create'}
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
        {!isLoading && !isError && users && (() => {
          const filtered = users.filter(u =>
            u.email.toLowerCase().includes(search.toLowerCase()) &&
            (!roleFilter || u.role === roleFilter)
          )
          return (
            <>
              <div className="px-5 py-3 border-b flex gap-3">
                <Input
                  placeholder="Search by email..."
                  value={search}
                  onChange={e => setSearch(e.target.value)}
                  className="max-w-xs h-8 text-sm"
                />
                <select
                  value={roleFilter}
                  onChange={e => setRoleFilter(e.target.value)}
                  className="h-8 rounded-md border border-input bg-background px-3 text-sm"
                >
                  <option value="">All roles</option>
                  <option value="CUSTOMER">Customer</option>
                  <option value="ADMIN">Admin</option>
                  <option value="KITCHEN">Kitchen</option>
                </select>
              </div>
              {filtered.length === 0
                ? <p className="px-5 py-8 text-sm text-muted-foreground text-center">No users found.</p>
                : (
                  <table className="w-full text-sm">
                    <thead>
                      <tr className="text-left text-xs text-muted-foreground uppercase tracking-wide border-b">
                        <th className="px-5 py-3 font-medium">Email</th>
                        <th className="px-5 py-3 font-medium">Role</th>
                        <th className="px-5 py-3 font-medium">Created</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y">
                      {filtered.map(user => (
                        <tr key={user.id} className="hover:bg-muted/30 transition-colors">
                          <td className="px-5 py-3">{user.email}</td>
                          <td className="px-5 py-3">
                            <Badge variant={user.role === 'ADMIN' ? 'default' : 'secondary'}>
                              {user.role}
                            </Badge>
                          </td>
                          <td className="px-5 py-3 text-muted-foreground">
                            {new Date(user.createdAt).toLocaleDateString()}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                )
              }
            </>
          )
        })()}
        {!isLoading && !isError && users?.length === 0 && (
          <p className="px-5 py-8 text-sm text-muted-foreground text-center">No users yet.</p>
        )}
      </CardContent>
    </Card>
  )
}
