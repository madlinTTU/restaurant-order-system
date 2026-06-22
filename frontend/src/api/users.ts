import api from './axios'
import type { User, CreateUserRequest } from '../types/user'

export const getUsers = async (): Promise<User[]> => {
  const res = await api.get('/admin/users')
  return res.data
}

export const createUser = async (request: CreateUserRequest): Promise<User> => {
  const res = await api.post('/admin/users', request)
  return res.data
}
