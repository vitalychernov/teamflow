import { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { projectsApi } from '../features/projects/projectsApi'
import { tasksApi } from '../features/tasks/tasksApi'
import { usersApi } from '../features/users/usersApi'
import { useAuth } from '../context/AuthContext'
import { Spinner } from '../components/Spinner'
import type { Task, TaskStatus, TaskPriority } from '../api/types'

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

// First letter avatar for assignee
function Avatar({ name }: { name: string }) {
  return (
    <span className="inline-flex items-center justify-center w-5 h-5 rounded-full bg-blue-100 text-blue-700 text-xs font-semibold shrink-0">
      {name.charAt(0).toUpperCase()}
    </span>
  )
}

export function ProjectDetailPage() {
  const { id } = useParams<{ id: string }>()
  const projectId = Number(id)
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const { user } = useAuth()

  // Create form state
  const [showCreate, setShowCreate] = useState(false)
  const [title, setTitle] = useState('')
  const [taskDescription, setTaskDescription] = useState('')
  const [priority, setPriority] = useState<TaskPriority>('MEDIUM')
  const [assigneeId, setAssigneeId] = useState<number | ''>('')

  // Filter state
  const [filterStatus, setFilterStatus] = useState<TaskStatus | ''>('')
  const [filterAssignedToMe, setFilterAssignedToMe] = useState(false)

  // Edit state
  const [editingTaskId, setEditingTaskId] = useState<number | null>(null)
  const [editTitle, setEditTitle] = useState('')
  const [editDescription, setEditDescription] = useState('')
  const [editPriority, setEditPriority] = useState<TaskPriority>('MEDIUM')
  const [editAssigneeId, setEditAssigneeId] = useState<number | ''>('')

  const startEdit = (task: Task) => {
    setEditingTaskId(task.id)
    setEditTitle(task.title)
    setEditDescription(task.description ?? '')
    setEditPriority(task.priority)
    setEditAssigneeId(task.assignee?.id ?? '')
  }

  const cancelEdit = () => setEditingTaskId(null)

  // Queries
  const { data: project, isLoading: projectLoading } = useQuery({
    queryKey: ['project', projectId],
    queryFn: () => projectsApi.getById(projectId),
  })

  const { data: tasks, isLoading: tasksLoading } = useQuery({
    queryKey: ['tasks', projectId, filterStatus, filterAssignedToMe],
    queryFn: () => tasksApi.getByProject(projectId, {
      ...(filterStatus ? { status: filterStatus } : {}),
      ...(filterAssignedToMe && user ? { assigneeId: user.id } : {}),
    }),
  })

  const { data: users } = useQuery({
    queryKey: ['users'],
    queryFn: usersApi.getAll,
  })

  // Mutations
  const createTask = useMutation({
    mutationFn: (data: { title: string; description: string; priority: TaskPriority; assigneeId?: number }) =>
      tasksApi.create(projectId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tasks', projectId] })
      setShowCreate(false)
      setTitle('')
      setTaskDescription('')
      setPriority('MEDIUM')
      setAssigneeId('')
    },
  })

  const updateTask = useMutation({
    mutationFn: ({ taskId, data }: { taskId: number; data: Parameters<typeof tasksApi.update>[1] }) =>
      tasksApi.update(taskId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tasks', projectId] })
      setEditingTaskId(null)
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
      <div className="mb-5">
        <button
          onClick={() => navigate('/projects')}
          className="text-sm text-gray-500 hover:text-blue-600 mb-2 flex items-center gap-1"
        >
          ← Projects
        </button>
        <h1 className="text-xl sm:text-2xl font-bold text-gray-900 break-words">{project?.name}</h1>
        {project?.description && (
          <p className="text-gray-500 mt-1 text-sm">{project.description}</p>
        )}
      </div>

      {/* Toolbar */}
      <div className="flex flex-col gap-3 mb-4 sm:flex-row sm:items-center sm:justify-between">
        <div className="flex flex-wrap gap-2">
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
              {s === '' ? 'All' : s.replace('_', ' ')}
            </button>
          ))}
          <button
            onClick={() => setFilterAssignedToMe((v) => !v)}
            className={`text-xs px-3 py-1 rounded-full border transition-colors ${
              filterAssignedToMe
                ? 'bg-purple-600 text-white border-purple-600'
                : 'bg-white text-gray-600 border-gray-300 hover:border-purple-400'
            }`}
          >
            Assigned to me
          </button>
        </div>
        <button
          onClick={() => setShowCreate(true)}
          className="bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium px-4 py-2 rounded-lg w-full sm:w-auto"
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
              createTask.mutate({
                title,
                description: taskDescription,
                priority,
                assigneeId: assigneeId !== '' ? assigneeId : undefined,
              })
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
            <div className="flex flex-col sm:flex-row gap-3">
              <select
                value={priority}
                onChange={(e) => setPriority(e.target.value as TaskPriority)}
                className="flex-1 border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="LOW">Low priority</option>
                <option value="MEDIUM">Medium priority</option>
                <option value="HIGH">High priority</option>
              </select>
              <select
                value={assigneeId}
                onChange={(e) => setAssigneeId(e.target.value ? Number(e.target.value) : '')}
                className="flex-1 border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="">Unassigned</option>
                {users?.map((u) => (
                  <option key={u.id} value={u.id}>{u.name}</option>
                ))}
              </select>
            </div>
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
          <div key={task.id} className="bg-white border border-gray-200 rounded-xl p-4 shadow-sm">

            {editingTaskId === task.id ? (
              /* ── Edit mode ── */
              <form
                onSubmit={(e) => {
                  e.preventDefault()
                  updateTask.mutate({
                    taskId: task.id,
                    data: {
                      title: editTitle,
                      description: editDescription || undefined,
                      priority: editPriority,
                      assigneeId: editAssigneeId !== '' ? editAssigneeId : null,
                      status: task.status,
                    },
                  })
                }}
                className="space-y-3"
              >
                <input
                  type="text"
                  value={editTitle}
                  onChange={(e) => setEditTitle(e.target.value)}
                  required
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
                <input
                  type="text"
                  value={editDescription}
                  onChange={(e) => setEditDescription(e.target.value)}
                  placeholder="Description (optional)"
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
                <div className="flex flex-col sm:flex-row gap-3">
                  <select
                    value={editPriority}
                    onChange={(e) => setEditPriority(e.target.value as TaskPriority)}
                    className="flex-1 border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                  >
                    <option value="LOW">Low priority</option>
                    <option value="MEDIUM">Medium priority</option>
                    <option value="HIGH">High priority</option>
                  </select>
                  <select
                    value={editAssigneeId}
                    onChange={(e) => setEditAssigneeId(e.target.value ? Number(e.target.value) : '')}
                    className="flex-1 border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                  >
                    <option value="">Unassigned</option>
                    {users?.map((u) => (
                      <option key={u.id} value={u.id}>{u.name}</option>
                    ))}
                  </select>
                </div>
                <div className="flex gap-2">
                  <button
                    type="submit"
                    disabled={updateTask.isPending}
                    className="bg-blue-600 hover:bg-blue-700 disabled:bg-blue-400 text-white text-sm font-medium px-4 py-2 rounded-lg"
                  >
                    {updateTask.isPending ? 'Saving…' : 'Save'}
                  </button>
                  <button
                    type="button"
                    onClick={cancelEdit}
                    className="text-sm text-gray-500 hover:text-gray-700 px-4 py-2"
                  >
                    Cancel
                  </button>
                </div>
              </form>
            ) : (
              /* ── Read mode ── */
              <div className="flex items-start justify-between gap-3">
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2 flex-wrap">
                    <span className="font-medium text-gray-900 text-sm">{task.title}</span>
                    <span className={`text-xs px-2 py-0.5 rounded-full font-medium shrink-0 ${STATUS_COLORS[task.status]}`}>
                      {task.status.replace('_', ' ')}
                    </span>
                    <span className={`text-xs px-2 py-0.5 rounded-full font-medium shrink-0 ${PRIORITY_COLORS[task.priority]}`}>
                      {task.priority}
                    </span>
                  </div>
                  {task.description && (
                    <p className="text-xs text-gray-400 mt-1">{task.description}</p>
                  )}
                  {task.assignee && (
                    <div className="flex items-center gap-1.5 mt-1.5">
                      <Avatar name={task.assignee.name} />
                      <span className="text-xs text-gray-500">{task.assignee.name}</span>
                    </div>
                  )}
                </div>
                <div className="flex items-center gap-2 shrink-0">
                  <button
                    onClick={() => startEdit(task)}
                    className="text-xs text-gray-400 hover:text-blue-600"
                  >
                    Edit
                  </button>
                  <button
                    onClick={() => { if (confirm('Delete task?')) deleteTask.mutate(task.id) }}
                    className="text-xs text-red-400 hover:text-red-600"
                  >
                    ✕
                  </button>
                </div>
              </div>
            )}

            {/* Status select — always visible at the bottom */}
            <div className="mt-3 pt-3 border-t border-gray-100">
              <select
                value={task.status}
                onChange={(e) => updateStatus.mutate({ taskId: task.id, status: e.target.value as TaskStatus })}
                className="text-xs border border-gray-200 rounded-lg px-2 py-1.5 focus:outline-none w-full sm:w-auto bg-white"
              >
                <option value="TODO">TODO</option>
                <option value="IN_PROGRESS">IN PROGRESS</option>
                <option value="DONE">DONE</option>
              </select>
            </div>

          </div>
        ))}
      </div>
    </div>
  )
}
