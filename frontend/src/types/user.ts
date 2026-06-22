export type Role = 'ADMIN' | 'KITCHEN' | 'CUSTOMER'

export interface User {
  id: string
  email: string
  role: Role
  createdAt: string
}

export interface CreateUserRequest {
  email: string
  password: string
  role: Role
}
