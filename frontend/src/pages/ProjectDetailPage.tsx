import { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { projectsApi } from '../features/projects/projectsApi'
import { tasksApi } from '../features/tasks/tasksApi'
import { Spinner } from '../components/Spinner'
import type { TaskStatus, TaskPriority } from '../api/types'

const STATUS_COLORS: Record<TaskStatus, string> = {
  TODO: 'bg-gray-100 text-gray-700',
  IN_PROGRESS: 'bg-blue-100 text-blue-700',
  DONE: 'bg-green-100 text-green-700',
}

const PRIORITY_COLORS: Record<TaskPriority, string> = {
  LOW: 'bg-gray-100 text-gray-500',
  MEDIUM: 'bg-yellow-100 text-yellow-700',
  HIGH: 'bg-red-100 text-red-700',
}

export function ProjectDetailPage() {
  const { id } = useParams<{ id: string }>()
  const projectId = Number(id)
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const [showCreate, setShowCreate] = useState(false)
  const [title, setTitle] = useState('')
  const [taskDescription, setTaskDescription] = useState('')
  const [priority, setPriority] = useState<TaskPriority>('MEDIUM')
  const [filterStatus, setFilterStatus] = useState<TaskStatus | ''>('')

  const { data: project, isLoading: projectLoading } = useQuery({
    queryKey: ['project', projectId],
    queryFn: () => projectsApi.getById(projectId),
  })

  const { data: tasks, isLoading: tasksLoading } = useQuery({
    queryKey: ['tasks', projectId, filterStatus],
    queryFn: () => tasksApi.getByProject(projectId, filterStatus ? { status: filterStatus } : {}),
  })

  const createTask = useMutation({
    mutationFn: (data: { title: string; description: string; priority: TaskPriority }) =>
      tasksApi.create(projectId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tasks', projectId] })
      setShowCreate(false)
      setTitle('')
      setTaskDescription('')
      setPriority('MEDIUM')
    },
  })

  const updateStatus = useMutation({
    mutationFn: ({ taskId, status }: { taskId: number; status: TaskStatus }) =>
      tasksApi.update(taskId, {
        title: tasks!.content.find((t) => t.id === taskId)!.title,
        status,
      }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['tasks', projectId] }),
  })

  const deleteTask = useMutation({
    mutationFn: tasksApi.remove,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['tasks', projectId] }),
  })

  if (projectLoading) return <Spinner />

  return (
    <div>
      {/* Header */}
      <div className="mb-6">
        <button
          onClick={() => navigate('/projects')}
          className="text-sm text-gray-500 hover:text-blue-600 mb-2 flex items-center gap-1"
        >
          ← Projects
        </button>
        <h1 className="text-2xl font-bold text-gray-900">{project?.name}</h1>
        {project?.description && <p className="text-gray-500 mt-1">{project.description}</p>}
      </div>

      {/* Toolbar */}
      <div className="flex items-center justify-between mb-4">
        <div className="flex gap-2">
          {(['', 'TODO', 'IN_PROGRESS', 'DONE'] as const).map((s) => (
            <button
              key={s}
              onClick={() => setFilterStatus(s)}
              className={`text-xs px-3 py-1 rounded-full border transition-colors ${
                filterStatus === s
                  ? 'bg-blue-600 text-white border-blue-600'
                  : 'bg-white text-gray-600 border-gray-300 hover:border-blue-400'
              }`}
            >
              {s || 'All'}
            </button>
          ))}
        </div>
        <button
          onClick={() => setShowCreate(true)}
          className="bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium px-4 py-2 rounded-lg"
        >
          + Add Task
        </button>
      </div>

      {/* Create task form */}
      {showCreate && (
        <div className="mb-4 bg-white border border-gray-200 rounded-xl p-4 shadow-sm">
          <form
            onSubmit={(e) => {
              e.preventDefault()
              createTask.mutate({ title, description: taskDescription, priority })
            }}
            className="space-y-3"
          >
            <input
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              required
              placeholder="Task title"
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
            <input
              type="text"
              value={taskDescription}
              onChange={(e) => setTaskDescription(e.target.value)}
              placeholder="Description (optional)"
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
            <select
              value={priority}
              onChange={(e) => setPriority(e.target.value as TaskPriority)}
              className="border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="LOW">Low priority</option>
              <option value="MEDIUM">Medium priority</option>
              <option value="HIGH">High priority</option>
            </select>
            <div className="flex gap-2">
              <button
                type="submit"
                disabled={createTask.isPending}
                className="bg-blue-600 hover:bg-blue-700 disabled:bg-blue-400 text-white text-sm font-medium px-4 py-2 rounded-lg"
              >
                {createTask.isPending ? 'Adding…' : 'Add Task'}
              </button>
              <button
                type="button"
                onClick={() => setShowCreate(false)}
                className="text-sm text-gray-500 hover:text-gray-700 px-4 py-2"
              >
                Cancel
              </button>
            </div>
          </form>
        </div>
      )}

      {tasksLoading && <Spinner />}

      {tasks?.content.length === 0 && (
        <p className="text-gray-400 text-center py-12">No tasks yet.</p>
      )}

      {/* Task list */}
      <div className="space-y-2">
        {tasks?.content.map((task) => (
          <div
            key={task.id}
            className="bg-white border border-gray-200 rounded-xl p-4 shadow-sm flex items-start justify-between"
          >
            <div className="flex-1 min-w-0">
              <div className="flex items-center gap-2 flex-wrap">
                <span className="font-medium text-gray-900 text-sm">{task.title}</span>
                <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${STATUS_COLORS[task.status]}`}>
                  {task.status.replace('_', ' ')}
                </span>
                <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${PRIORITY_COLORS[task.priority]}`}>
                  {task.priority}
                </span>
              </div>
              {task.description && (
                <p className="text-xs text-gray-400 mt-1">{task.description}</p>
              )}
              {task.assignee && (
                <p className="text-xs text-gray-400 mt-1">→ {task.assignee.name}</p>
              )}
            </div>
            <div className="flex items-center gap-2 ml-4 shrink-0">
              {/* Quick status change */}
              <select
                value={task.status}
                onChange={(e) => updateStatus.mutate({ taskId: task.id, status: e.target.value as TaskStatus })}
                className="text-xs border border-gray-200 rounded px-2 py-1 focus:outline-none"
              >
                <option value="TODO">TODO</option>
                <option value="IN_PROGRESS">IN PROGRESS</option>
                <option value="DONE">DONE</option>
              </select>
              <button
                onClick={() => { if (confirm('Delete task?')) deleteTask.mutate(task.id) }}
                className="text-xs text-red-400 hover:text-red-600"
              >
                ✕
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
