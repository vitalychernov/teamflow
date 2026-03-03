import api from '../../api/axios'
import type { CreateProjectRequest, PageResponse, Project, UpdateProjectRequest } from '../../api/types'

export const projectsApi = {
  getAll: async (page = 0, size = 10): Promise<PageResponse<Project>> => {
    const res = await api.get<PageResponse<Project>>('/api/projects', {
      params: { page, size, sort: 'createdAt', direction: 'DESC' },
    })
    return res.data
  },

  getById: async (id: number): Promise<Project> => {
    const res = await api.get<Project>(`/api/projects/${id}`)
    return res.data
  },

  create: async (data: CreateProjectRequest): Promise<Project> => {
    const res = await api.post<Project>('/api/projects', data)
    return res.data
  },

  update: async (id: number, data: UpdateProjectRequest): Promise<Project> => {
    const res = await api.put<Project>(`/api/projects/${id}`, data)
    return res.data
  },

  remove: async (id: number): Promise<void> => {
    await api.delete(`/api/projects/${id}`)
  },
}
