import api from '../../api/axios'
import type { AuthResponse, LoginRequest, RegisterRequest } from '../../api/types'

export const authApi = {
  register: async (data: RegisterRequest): Promise<AuthResponse> => {
    const res = await api.post<AuthResponse>('/api/auth/register', data)
    return res.data
  },

  login: async (data: LoginRequest): Promise<AuthResponse> => {
    const res = await api.post<AuthResponse>('/api/auth/login', data)
    return res.data
  },
}
