import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import {type AuthRequest, login, register} from '../api/auth'

export default function LoginPage() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [mode, setMode] = useState<'login' | 'register'>('login')
  const navigate = useNavigate()

  const mutation = useMutation({
    mutationFn: () => {
			const credentials: AuthRequest = {email, password}
			return mode === 'login' ? login(credentials) : register(credentials)
		},
    onSuccess: (data) => {
      localStorage.setItem('accessToken', data.accessToken)
      localStorage.setItem('refreshToken', data.refreshToken)
      navigate('/')
    },
  })

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    mutation.mutate()
  }

  return (
    <div>
      <h1>{mode === 'login' ? 'Login' : 'Register'}</h1>
      <form onSubmit={handleSubmit}>
        <div>
          <label htmlFor="email">Email</label>
          <input
						id="email"
            type="email"
            value={email}
            onChange={e => setEmail(e.target.value)}
						autoComplete="email"
						placeholder="your@email.com"
            required
          />
        </div>
        <div>
          <label htmlFor="password">Password</label>
          <input
						id="password"
            type="password"
            value={password}
            onChange={e => setPassword(e.target.value)}
						autoComplete={mode === 'login' ? 'current-password' : 'new-password'}
						placeholder="••••••••"
            required
          />
        </div>
        {mutation.isError && <div>Something went wrong. Try again.</div>}
        <button type="submit" disabled={mutation.isPending}>
          {mutation.isPending ? 'Loading...' : mode === 'login' ? 'Login' : 'Register'}
        </button>
      </form>
      <button onClick={() => setMode(mode === 'login' ? 'register' : 'login')}>
        {mode === 'login' ? 'No account? Register' : 'Already hacve an account? Login'}
      </button>
    </div>
  )
}
