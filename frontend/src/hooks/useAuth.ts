export function useAuth() {
  const token = localStorage.getItem('accessToken')
  if (!token) return { isAuthenticated: false, isAdmin: false, role: null }

  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    return {
      isAuthenticated: true,
      isAdmin: payload.role === 'ADMIN',
      role: payload.role as string,
    }
  } catch {
    return { isAuthenticated: false, isAdmin: false, role: null }
  }
}
