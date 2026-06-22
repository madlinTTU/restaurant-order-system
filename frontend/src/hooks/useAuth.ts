export function useAuth() {
  const token = localStorage.getItem('accessToken')
  if (!token) return { isAuthenticated: false, isAdmin: false }

  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    return {
      isAuthenticated: true,
      isAdmin: payload.role === 'ADMIN',
    }
  } catch {
    return { isAuthenticated: false, isAdmin: false }
  }
}
