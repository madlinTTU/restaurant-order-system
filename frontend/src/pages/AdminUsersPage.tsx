import { useState } from 'react'
import * as React from 'react'
import { useUsers, useCreateUser } from '../hooks/useUsers'
import type { CreateUserRequest, Role } from '../types/user'

const emptyForm: CreateUserRequest = { email: '', password: '', role: 'KITCHEN' }

export default function AdminUsersPage() {
  const [formVisible, setFormVisible] = useState(false)
  const [form, setForm] = useState<CreateUserRequest>(emptyForm)

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
    <div className="bg-white rounded-lg border border-gray-200">
      <div className="flex items-center justify-between px-5 py-4 border-b border-gray-200">
        <h2 className="font-medium text-gray-900">Users</h2>
        <button
          onClick={() => setFormVisible(true)}
          className="px-3 py-1.5 bg-gray-900 text-white text-sm rounded-md hover:bg-gray-700 transition-colors"
        >
          + Add
        </button>
      </div>

      {formVisible && (
        <div className="px-5 py-4 border-b border-gray-200 bg-gray-50">
          <form onSubmit={handleSubmit} className="space-y-3">
            <div className="grid grid-cols-3 gap-3">
              <div>
                <label className="block text-xs font-medium text-gray-600 mb-1">Email *</label>
                <input
                  required
                  type="email"
                  value={form.email}
                  onChange={(e) => setForm((f) => ({ ...f, email: e.target.value }))}
                  className="w-full border border-gray-300 rounded-md px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-gray-900"
                />
              </div>
              <div>
                <label className="block text-xs font-medium text-gray-600 mb-1">Password *</label>
                <input
                  required
                  type="password"
                  minLength={6}
                  value={form.password}
                  onChange={(e) => setForm((f) => ({ ...f, password: e.target.value }))}
                  className="w-full border border-gray-300 rounded-md px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-gray-900"
                />
              </div>
              <div>
                <label className="block text-xs font-medium text-gray-600 mb-1">Role *</label>
                <select
                  value={form.role}
                  onChange={(e) => setForm((f) => ({ ...f, role: e.target.value as Role }))}
                  className="w-full border border-gray-300 rounded-md px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-gray-900"
                >
                  <option value="KITCHEN">Kitchen</option>
                  <option value="ADMIN">Admin</option>
                </select>
              </div>
            </div>
            <div className="flex gap-2">
              <button
                type="submit"
                disabled={createUser.isPending}
                className="px-3 py-1.5 bg-gray-900 text-white text-sm rounded-md hover:bg-gray-700 disabled:opacity-50 transition-colors"
              >
                Create
              </button>
              <button
                type="button"
                onClick={closeForm}
                className="px-3 py-1.5 border border-gray-300 text-sm rounded-md hover:bg-gray-100 transition-colors"
              >
                Cancel
              </button>
            </div>
          </form>
        </div>
      )}

      {isLoading && (
        <p className="px-5 py-8 text-sm text-gray-400 text-center">Loading...</p>
      )}
      {isError && (
        <p className="px-5 py-8 text-sm text-red-500 text-center">Something went wrong.</p>
      )}
      {users && users.length === 0 && (
        <p className="px-5 py-8 text-sm text-gray-400 text-center">No users yet.</p>
      )}
      {users && users.length > 0 && (
        <table className="w-full text-sm">
          <thead>
            <tr className="text-left text-xs text-gray-500 uppercase tracking-wide border-b border-gray-200">
              <th className="px-5 py-3 font-medium">Email</th>
              <th className="px-5 py-3 font-medium">Role</th>
              <th className="px-5 py-3 font-medium">Created</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {users.map((user) => (
              <tr key={user.id} className="hover:bg-gray-50 transition-colors">
                <td className="px-5 py-3 text-gray-900">{user.email}</td>
                <td className="px-5 py-3">
                  <span className={`inline-block px-2 py-0.5 rounded text-xs font-medium ${
                    user.role === 'ADMIN'
                      ? 'bg-gray-900 text-white'
                      : 'bg-gray-100 text-gray-700'
                  }`}>
                    {user.role}
                  </span>
                </td>
                <td className="px-5 py-3 text-gray-400">
                  {new Date(user.createdAt).toLocaleDateString()}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  )
}
