import { createContext, useContext, useState, ReactNode } from 'react'
import type { AuthResponse } from '../api/types'

interface AuthUser {
  email: string
  name: string
  role: string
}

interface AuthContextValue {
  user: AuthUser | null
  token: string | null
  login: (response: AuthResponse) => void
  logout: () => void
  isAdmin: boolean
}

const AuthContext = createContext<AuthContextValue | null>(null)

// Restore user from localStorage on page refresh
function loadUser(): AuthUser | null {
  try {
    const raw = localStorage.getItem('user')
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(loadUser)
  const [token, setToken] = useState<string | null>(localStorage.getItem('token'))

  const login = (response: AuthResponse) => {
    const authUser: AuthUser = {
      email: response.email,
      name: response.name,
      role: response.role,
    }
    localStorage.setItem('token', response.token)
    localStorage.setItem('user', JSON.stringify(authUser))
    setToken(response.token)
    setUser(authUser)
  }

  const logout = () => {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    setToken(null)
    setUser(null)
  }

  return (
    <AuthContext.Provider value={{ user, token, login, logout, isAdmin: user?.role === 'ADMIN' }}>
      {children}
    </AuthContext.Provider>
  )
}

// Custom hook — throws if used outside AuthProvider
export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
