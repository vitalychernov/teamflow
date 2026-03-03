import api from '../../api/axios'
import type { User } from '../../api/types'

export const usersApi = {
  getAll: async (): Promise<User[]> => {
    const res = await api.get<User[]>('/api/users')
    return res.data
  },
}
