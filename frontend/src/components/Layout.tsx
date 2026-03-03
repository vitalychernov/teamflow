import { Link, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export function Layout() {
  const { user, logout, isAdmin } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <div className="min-h-screen flex flex-col">
      {/* Navbar */}
      <nav className="bg-white border-b border-gray-200 px-4 sm:px-6 py-3 flex items-center justify-between shadow-sm">
        <Link to="/projects" className="text-xl font-bold text-blue-600 shrink-0">
          TeamFlow
        </Link>
        <div className="flex items-center gap-3 sm:gap-4 min-w-0">
          {isAdmin && (
            <Link to="/admin" className="text-sm text-gray-600 hover:text-blue-600 shrink-0">
              Admin
            </Link>
          )}
          <span className="text-sm text-gray-500 truncate max-w-[120px] sm:max-w-none">
            {user?.name}
          </span>
          <button
            onClick={handleLogout}
            className="text-sm text-red-500 hover:text-red-700 font-medium shrink-0"
          >
            Logout
          </button>
        </div>
      </nav>

      {/* Page content */}
      <main className="flex-1 px-4 py-5 sm:p-6 max-w-5xl mx-auto w-full">
        <Outlet />
      </main>
    </div>
  )
}
