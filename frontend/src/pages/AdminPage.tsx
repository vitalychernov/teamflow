import { useQuery } from '@tanstack/react-query'
import axiosInstance from '../api/axios'
import type { PageResponse, User } from '../api/types'
import { Spinner } from '../components/Spinner'

const ROLE_COLORS: Record<string, string> = {
  ROLE_ADMIN: 'bg-purple-100 text-purple-700',
  ROLE_USER: 'bg-gray-100 text-gray-600',
}

export function AdminPage() {
  const { data, isLoading } = useQuery<PageResponse<User>>({
    queryKey: ['admin', 'users'],
    queryFn: () =>
      axiosInstance.get<PageResponse<User>>('/api/admin/users').then((r) => r.data),
  })

  if (isLoading) return <Spinner />

  return (
    <div>
      <h1 className="text-xl sm:text-2xl font-bold text-gray-900 mb-5">All Users</h1>

      {/* overflow-x-auto prevents table from breaking the layout on narrow screens */}
      <div className="bg-white border border-gray-200 rounded-xl shadow-sm overflow-hidden overflow-x-auto">
        <table className="w-full text-sm min-w-[480px]">
          <thead className="bg-gray-50 border-b border-gray-200">
            <tr>
              <th className="text-left px-4 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wide">
                Name
              </th>
              <th className="text-left px-4 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wide">
                Email
              </th>
              <th className="text-left px-4 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wide">
                Role
              </th>
              <th className="text-left px-4 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wide">
                Joined
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {data?.content.map((user) => (
              <tr key={user.id} className="hover:bg-gray-50 transition-colors">
                <td className="px-4 py-3 font-medium text-gray-900 whitespace-nowrap">{user.name}</td>
                <td className="px-4 py-3 text-gray-500">{user.email}</td>
                <td className="px-4 py-3 whitespace-nowrap">
                  <span
                    className={`text-xs px-2 py-0.5 rounded-full font-medium ${
                      ROLE_COLORS[user.role] ?? 'bg-gray-100 text-gray-600'
                    }`}
                  >
                    {user.role.replace('ROLE_', '')}
                  </span>
                </td>
                <td className="px-4 py-3 text-gray-400 whitespace-nowrap">
                  {new Date(user.createdAt).toLocaleDateString()}
                </td>
              </tr>
            ))}
          </tbody>
        </table>

        {data?.content.length === 0 && (
          <p className="text-center text-gray-400 py-12">No users found.</p>
        )}
      </div>

      {data && (
        <p className="text-xs text-gray-400 mt-3 text-right">
          Total: {data.totalElements} user{data.totalElements !== 1 ? 's' : ''}
        </p>
      )}
    </div>
  )
}
