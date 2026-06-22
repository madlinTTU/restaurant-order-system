import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { ArrowLeft } from 'lucide-react'
import { type AuthRequest, login, register } from '@/api/auth'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'

interface FormErrors {
  email?: string
  password?: string
}

function validate(email: string, password: string): FormErrors {
  const errors: FormErrors = {}
  if (!email.includes('@')) errors.email = 'Enter a valid email address'
  if (password.length < 8) errors.password = 'Password must be at least 8 characters'
  return errors
}

export default function LoginPage() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [mode, setMode] = useState<'login' | 'register'>('login')
  const [errors, setErrors] = useState<FormErrors>({})
  const navigate = useNavigate()

  const mutation = useMutation({
    mutationFn: () => {
      const credentials: AuthRequest = { email, password }
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
    const formErrors = validate(email, password)
    if (Object.keys(formErrors).length > 0) {
      setErrors(formErrors)
      return
    }
    setErrors({})
    mutation.mutate()
  }

  const switchMode = () => {
    setMode(m => m === 'login' ? 'register' : 'login')
    setErrors({})
    mutation.reset()
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-zinc-200 via-stone-100 to-zinc-300 flex items-center justify-center p-6">
      <div className="w-full max-w-5xl min-h-[600px] bg-zinc-900 rounded-3xl grid lg:grid-cols-2 overflow-hidden relative items-stretch shadow-[0_32px_80px_rgba(0,0,0,0.25)]">

        <div className="flex flex-col justify-between p-10">
          <div className="flex items-center justify-between">
            <span className="text-white text-sm font-semibold">Restaurant</span>
            <button
              onClick={() => navigate(-1)}
              className="flex items-center gap-1.5 text-sm text-zinc-500 hover:text-zinc-300 transition-colors"
            >
              <ArrowLeft className="h-4 w-4" />
              Back
            </button>
          </div>

          <div>
            <p className="text-4xl font-semibold text-white leading-tight tracking-tight">
              Fresh food.<br />Your way.
            </p>
            <p className="mt-3 text-zinc-500 text-sm leading-relaxed max-w-xs">
              Order from our kitchen and track your meal in real time - from prep to your table.
            </p>
          </div>

          <p className="text-xs text-zinc-700">© 2026 Restaurant</p>
        </div>

        <div className="flex">
          <div className="w-full h-full bg-white p-8 space-y-6 flex flex-col justify-center">

            <div>
              <span className="text-sm font-semibold">Restaurant</span>
              <h1 className="text-2xl font-semibold tracking-tight mt-5">
                {mode === 'login' ? 'Sign in' : 'Create account'}
              </h1>
              <p className="text-sm text-zinc-500 mt-1">
                {mode === 'login'
                  ? 'Enter your credentials to continue'
                  : 'Fill in the details below to get started'}
              </p>
            </div>

            <form onSubmit={handleSubmit} className="space-y-4" noValidate>
              <div className="space-y-1.5">
                <Label htmlFor="email" className="text-zinc-700">Email</Label>
                <Input
                  id="email"
                  type="email"
                  value={email}
                  onChange={e => setEmail(e.target.value)}
                  autoComplete="email"
                  placeholder="your@email.com"
                  className={errors.email ? 'border-red-400 focus-visible:ring-red-300' : ''}
                />
                {errors.email && <p className="text-xs text-red-500">{errors.email}</p>}
              </div>

              <div className="space-y-1.5">
                <Label htmlFor="password" className="text-zinc-700">Password</Label>
                <Input
                  id="password"
                  type="password"
                  value={password}
                  onChange={e => setPassword(e.target.value)}
                  autoComplete={mode === 'login' ? 'current-password' : 'new-password'}
                  placeholder="••••••••"
                  className={errors.password ? 'border-red-400 focus-visible:ring-red-300' : ''}
                />
                {errors.password && <p className="text-xs text-red-500">{errors.password}</p>}
              </div>

              {mutation.isError && (
                <p className="text-xs text-red-500">
                  {mode === 'login' ? 'Invalid email or password.' : 'Registration failed. Please try again.'}
                </p>
              )}

              <button
                type="submit"
                disabled={mutation.isPending}
                className="w-full py-2.5 rounded-lg text-sm font-medium text-white bg-zinc-900 hover:bg-zinc-700 disabled:opacity-50 transition-colors"
              >
                {mutation.isPending
                  ? 'Loading...'
                  : mode === 'login' ? 'Sign in' : 'Create account'}
              </button>
            </form>

            <p className="text-sm text-zinc-500 text-center">
              {mode === 'login' ? "Don't have an account?" : 'Already have an account?'}{' '}
              <button
                onClick={switchMode}
                className="text-zinc-900 underline underline-offset-4 hover:text-zinc-500 transition-colors"
              >
                {mode === 'login' ? 'Register' : 'Sign in'}
              </button>
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}
