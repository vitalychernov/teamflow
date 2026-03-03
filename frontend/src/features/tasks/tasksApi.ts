import api from '../../api/axios'
import type { CreateTaskRequest, PageResponse, Task, TaskPriority, TaskStatus, UpdateTaskRequest } from '../../api/types'

export const tasksApi = {
  getByProject: async (
    projectId: number,
    params?: { status?: TaskStatus; priority?: TaskPriority; page?: number; size?: number }
  ): Promise<PageResponse<Task>> => {
    const res = await api.get<PageResponse<Task>>(`/api/projects/${projectId}/tasks`, {
      params: { page: 0, size: 50, ...params },
    })
    return res.data
  },

  getById: async (id: number): Promise<Task> => {
    const res = await api.get<Task>(`/api/tasks/${id}`)
    return res.data
  },

  create: async (projectId: number, data: CreateTaskRequest): Promise<Task> => {
    const res = await api.post<Task>(`/api/projects/${projectId}/tasks`, data)
    return res.data
  },

  update: async (id: number, data: UpdateTaskRequest): Promise<Task> => {
    const res = await api.put<Task>(`/api/tasks/${id}`, data)
    return res.data
  },

  remove: async (id: number): Promise<void> => {
    await api.delete(`/api/tasks/${id}`)
  },
}
