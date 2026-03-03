import axios from 'axios'

// Single Axios instance for the entire app.
// Reads the backend URL from environment variable — differs per environment:
//   .env              → http://localhost:8080  (local dev)
//   .env.production   → https://teamflow-backend.onrender.com  (Vercel deploy)
const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  headers: { 'Content-Type': 'application/json' },
})

// Request interceptor — attach JWT token to every request automatically.
// Token is stored in localStorage after login.
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Response interceptor — redirect to login when an authenticated session expires.
// IMPORTANT: only redirect if a token exists in localStorage.
// Without this guard, the /api/auth/login endpoint also returns 401 on wrong
// credentials, which would cause an infinite reload loop on the login page.
api.interceptors.response.use(
  (response) => response,
  (error) => {
    const hasToken = Boolean(localStorage.getItem('token'))
    if (error.response?.status === 401 && hasToken) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default api
