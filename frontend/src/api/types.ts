export interface RegisterRequest {
  name: string
  email: string
  password: string
}

export interface LoginRequest {
  email: string
  password: string
}

export interface AuthResponse {
  id: number
  token: string
  email: string
  name: string
  role: string
}

export interface ProjectOwner {
  id: number
  name: string
  email: string
}

export interface Project {
  id: number
  name: string
  description: string | null
  owner: ProjectOwner
  createdAt: string
  updatedAt: string
  totalTasks: number
  doneTasks: number
}

export interface CreateProjectRequest {
  name: string
  description?: string
}

export interface UpdateProjectRequest {
  name: string
  description?: string
}

export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'DONE'
export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH'

export interface Task {
  id: number
  title: string
  description: string | null
  status: TaskStatus
  priority: TaskPriority
  projectId: number
  assignee: { id: number; name: string; email: string } | null
  createdAt: string
  updatedAt: string
}

export interface CreateTaskRequest {
  title: string
  description?: string
  status?: TaskStatus
  priority?: TaskPriority
  assigneeId?: number
}

export interface UpdateTaskRequest {
  title: string
  description?: string
  status?: TaskStatus
  priority?: TaskPriority
  assigneeId?: number | null
}

export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  last: boolean
}

export interface User {
  id: number
  name: string
  email: string
  role: string
  createdAt: string
}

export interface ApiError {
  status: number
  message: string
  errors?: Record<string, string>
}
