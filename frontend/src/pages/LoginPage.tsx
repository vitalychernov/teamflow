import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useMutation } from '@tanstack/react-query'
import { authApi } from '../features/auth/authApi'
import { useAuth } from '../context/AuthContext'
import type { ApiError } from '../api/types'

// Demo accounts for quick access
const DEMO_ACCOUNTS = [
  { label: 'Alice (user)', email: 'alice@teamflow.com', password: 'demo123' },
  { label: 'Admin', email: 'admin@teamflow.com', password: 'admin123' },
]

export function LoginPage() {
  const navigate = useNavigate()
  const { login } = useAuth()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')

  const mutation = useMutation({
    mutationFn: authApi.login,
    onSuccess: (data) => {
      login(data)
      navigate('/projects')
    },
    onError: (err: { response?: { data?: ApiError } }) => {
      setError(err.response?.data?.message ?? 'Login failed')
    },
  })

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    mutation.mutate({ email, password })
  }

  const fillDemo = (demoEmail: string, demoPassword: string) => {
    setEmail(demoEmail)
    setPassword(demoPassword)
    setError('')
  }

  return (
    // py-8 allows the page to scroll when the mobile keyboard is open
    <div className="min-h-screen flex flex-col justify-center bg-gray-50 px-4 py-8">
      <div className="bg-white rounded-xl shadow-md p-6 sm:p-8 w-full max-w-md mx-auto">
        <h1 className="text-2xl font-bold text-gray-900 mb-6 text-center">Sign in to TeamFlow</h1>

        {/* Demo credentials block */}
        <div className="mb-5 p-3 bg-blue-50 border border-blue-100 rounded-lg">
          <p className="text-xs font-semibold text-blue-700 mb-2">Try a demo account:</p>
          <div className="flex flex-wrap gap-2">
            {DEMO_ACCOUNTS.map((acc) => (
              <button
                key={acc.email}
                type="button"
                onClick={() => fillDemo(acc.email, acc.password)}
                className="text-xs px-3 py-1 bg-white border border-blue-200 text-blue-600 rounded-full hover:bg-blue-100 transition-colors"
              >
                {acc.label}
              </button>
            ))}
          </div>
        </div>

        {error && (
          <div className="mb-4 p-3 bg-red-50 border border-red-200 text-red-700 rounded-lg text-sm">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="you@example.com"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Password</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="••••••"
            />
          </div>
          <button
            type="submit"
            disabled={mutation.isPending}
            className="w-full bg-blue-600 hover:bg-blue-700 disabled:bg-blue-400 text-white font-semibold py-2.5 rounded-lg transition-colors"
          >
            {mutation.isPending ? 'Signing in…' : 'Sign in'}
          </button>
        </form>

        <p className="mt-4 text-center text-sm text-gray-500">
          No account?{' '}
          <Link to="/register" className="text-blue-600 hover:underline font-medium">
            Register
          </Link>
        </p>
      </div>
    </div>
  )
}
