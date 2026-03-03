import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  DndContext,
  DragOverlay,
  PointerSensor,
  useSensor,
  useSensors,
  useDroppable,
  useDraggable,
} from '@dnd-kit/core'
import type { DragEndEvent, DragStartEvent } from '@dnd-kit/core'
import { projectsApi } from '../features/projects/projectsApi'
import { tasksApi } from '../features/tasks/tasksApi'
import { usersApi } from '../features/users/usersApi'
import { useAuth } from '../context/AuthContext'
import { Spinner } from '../components/Spinner'
import { ConfirmDialog } from '../components/ConfirmDialog'
import type { Task, TaskStatus, TaskPriority, User } from '../api/types'

const PRIORITY_COLORS: Record<TaskPriority, string> = {
  LOW: 'bg-gray-100 text-gray-500',
  MEDIUM: 'bg-yellow-100 text-yellow-700',
  HIGH: 'bg-red-100 text-red-700',
}

const COLUMNS: Array<{ status: TaskStatus; label: string; headerColor: string }> = [
  { status: 'TODO',        label: 'To Do',      headerColor: 'text-gray-600'  },
  { status: 'IN_PROGRESS', label: 'In Progress', headerColor: 'text-blue-600'  },
  { status: 'DONE',        label: 'Done',        headerColor: 'text-green-600' },
]

function Avatar({ name }: { name: string }) {
  const initials = name.split(' ').map((w) => w[0]).join('').slice(0, 2).toUpperCase()
  return (
    <span className="inline-flex items-center justify-center w-5 h-5 rounded-full bg-blue-100 text-blue-700 text-xs font-semibold shrink-0">
      {initials}
    </span>
  )
}

function UnassignedBadge() {
  return (
    <div className="flex items-center gap-1.5 mt-1.5">
      <span className="inline-flex items-center justify-center w-5 h-5 rounded-full bg-gray-100 text-gray-500 shrink-0">
        <svg viewBox="-4 -4 24 24" fill="none" className="w-4 h-4" aria-hidden="true">
          <path fill="currentColor" fillRule="evenodd" d="M8 1.5a2.5 2.5 0 1 0 0 5 2.5 2.5 0 0 0 0-5M4 4a4 4 0 1 1 8 0 4 4 0 0 1-8 0m-2 9a3.75 3.75 0 0 1 3.75-3.75h4.5A3.75 3.75 0 0 1 14 13v2h-1.5v-2a2.25 2.25 0 0 0-2.25-2.25h-4.5A2.25 2.25 0 0 0 3.5 13v2H2z" clipRule="evenodd" />
        </svg>
      </span>
      <span className="text-xs text-gray-500">Unassigned</span>
    </div>
  )
}

function TaskCardPreview({ task }: { task: Task }) {
  return (
    <div className="bg-white border border-gray-200 rounded-lg p-3 shadow-2xl">
      <div className="flex items-start gap-2 mb-2">
        <span className="text-gray-300 shrink-0 leading-none">⠿</span>
        <span className="font-medium text-gray-900 text-sm leading-snug flex-1">{task.title}</span>
      </div>

      {task.description && (
        <p className="text-xs text-gray-400 mb-2 line-clamp-2">{task.description}</p>
      )}

      <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${PRIORITY_COLORS[task.priority]}`}>
        {task.priority}
      </span>

      {task.assignee ? (
        <div className="flex items-center gap-1.5 mt-2">
          <Avatar name={task.assignee.name} />
          <span className="text-xs text-gray-500">{task.assignee.name}</span>
        </div>
      ) : (
        <UnassignedBadge />
      )}

      <div className="mt-2 pt-2 border-t border-gray-100" />
    </div>
  )
}

function KanbanColumn({
  col, count, children,
}: {
  col: typeof COLUMNS[0]
  count: number
  children: React.ReactNode
}) {
  const { setNodeRef, isOver } = useDroppable({ id: col.status })
  return (
    <div
      ref={setNodeRef}
      className={`rounded-xl p-3 min-h-[80px] transition-colors ${isOver ? 'bg-blue-50' : 'bg-gray-50'}`}
    >
      <div className="flex items-center gap-2 mb-3 px-1">
        <h3 className={`text-xs font-semibold uppercase tracking-wide ${col.headerColor}`}>{col.label}</h3>
        <span className="text-xs bg-white border border-gray-200 text-gray-500 rounded-full px-1.5 font-medium">
          {count}
        </span>
      </div>
      <div className="space-y-2">{children}</div>
    </div>
  )
}

interface TaskCardProps {
  task: Task
  users: User[] | undefined
  isEditing: boolean
  isSaving: boolean
  onStartEdit: () => void
  onCancelEdit: () => void
  onSave: (data: { title: string; description?: string; priority: TaskPriority; assigneeId: number | null; status: TaskStatus }) => void
  onDelete: () => void
}

function TaskCard({ task, users, isEditing, isSaving, onStartEdit, onCancelEdit, onSave, onDelete }: TaskCardProps) {
  const [editTitle, setEditTitle] = useState(task.title)
  const [editDescription, setEditDescription] = useState(task.description ?? '')
  const [editPriority, setEditPriority] = useState<TaskPriority>(task.priority)
  const [editAssigneeId, setEditAssigneeId] = useState<number | ''>(task.assignee?.id ?? '')

  useEffect(() => {
    if (isEditing) {
      setEditTitle(task.title)
      setEditDescription(task.description ?? '')
      setEditPriority(task.priority)
      setEditAssigneeId(task.assignee?.id ?? '')
    }
  }, [isEditing]) // eslint-disable-line react-hooks/exhaustive-deps

  const { attributes, listeners, setNodeRef, isDragging } = useDraggable({
    id: task.id,
    disabled: isEditing,
  })

  return (
    <div
      ref={setNodeRef}
      className={`bg-white border border-gray-200 rounded-lg p-3 shadow-sm transition-opacity ${isDragging ? 'opacity-30' : ''}`}
    >
      {isEditing ? (
        /* ── Edit mode ── */
        <form
          onSubmit={(e) => {
            e.preventDefault()
            onSave({
              title: editTitle,
              description: editDescription || undefined,
              priority: editPriority,
              assigneeId: editAssigneeId !== '' ? editAssigneeId : null,
              status: task.status,
            })
          }}
          className="space-y-2"
        >
          <input
            type="text"
            value={editTitle}
            onChange={(e) => setEditTitle(e.target.value)}
            required
            className="w-full border border-gray-300 rounded-lg px-2 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          <input
            type="text"
            value={editDescription}
            onChange={(e) => setEditDescription(e.target.value)}
            placeholder="Description (optional)"
            className="w-full border border-gray-300 rounded-lg px-2 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          <select
            value={editPriority}
            onChange={(e) => setEditPriority(e.target.value as TaskPriority)}
            className="w-full border border-gray-300 rounded-lg px-2 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="LOW">Low priority</option>
            <option value="MEDIUM">Medium priority</option>
            <option value="HIGH">High priority</option>
          </select>
          <select
            value={editAssigneeId}
            onChange={(e) => setEditAssigneeId(e.target.value ? Number(e.target.value) : '')}
            className="w-full border border-gray-300 rounded-lg px-2 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="">Unassigned</option>
            {users?.map((u) => <option key={u.id} value={u.id}>{u.name}</option>)}
          </select>
          <div className="flex gap-2">
            <button
              type="submit"
              disabled={isSaving}
              className="bg-blue-600 hover:bg-blue-700 disabled:bg-blue-400 text-white text-xs font-medium px-3 py-1.5 rounded-lg"
            >
              {isSaving ? 'Saving…' : 'Save'}
            </button>
            <button type="button" onClick={onCancelEdit} className="text-xs text-gray-500 hover:text-gray-700 px-3 py-1.5">
              Cancel
            </button>
          </div>
        </form>
      ) : (
        /* ── Read mode ── */
        <>
          <div className="flex items-start gap-2 mb-2">
            <div
              {...attributes}
              {...listeners}
              className="cursor-grab mt-0.5 text-gray-300 hover:text-gray-500 shrink-0 touch-none select-none leading-none"
              title="Drag to move"
            >
              ⠿
            </div>
            <span className="font-medium text-gray-900 text-sm leading-snug flex-1">{task.title}</span>
            <button onClick={onDelete} className="text-gray-300 hover:text-red-500 shrink-0 text-xs mt-0.5">✕</button>
          </div>

          {task.description && (
            <p className="text-xs text-gray-400 mb-2 line-clamp-2">{task.description}</p>
          )}

          <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${PRIORITY_COLORS[task.priority]}`}>
            {task.priority}
          </span>

          {task.assignee ? (
            <div className="flex items-center gap-1.5 mt-2">
              <Avatar name={task.assignee.name} />
              <span className="text-xs text-gray-500">{task.assignee.name}</span>
            </div>
          ) : (
            <UnassignedBadge />
          )}

          <div className="flex justify-end mt-2 pt-2 border-t border-gray-100">
            <button onClick={onStartEdit} className="text-xs text-gray-400 hover:text-blue-600">Edit</button>
          </div>
        </>
      )}
    </div>
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
  const [filterAssignedToMe, setFilterAssignedToMe] = useState(false)

  // Delete confirmation
  const [deletingTaskId, setDeletingTaskId] = useState<number | null>(null)

  // Edit state
  const [editingTaskId, setEditingTaskId] = useState<number | null>(null)

  // Active drag task (for DragOverlay)
  const [activeTask, setActiveTask] = useState<Task | null>(null)

  // DnD sensors — require 8px movement to start drag (prevents accidental drags on click)
  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 8 } })
  )

  // Queries
  const { data: project, isLoading: projectLoading } = useQuery({
    queryKey: ['project', projectId],
    queryFn: () => projectsApi.getById(projectId),
  })

  const { data: tasks, isLoading: tasksLoading } = useQuery({
    queryKey: ['tasks', projectId, filterAssignedToMe],
    queryFn: () => tasksApi.getByProject(projectId, {
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
      queryClient.invalidateQueries({ queryKey: ['projects'] })
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
    mutationFn: ({ taskId, status }: { taskId: number; status: TaskStatus }) => {
      const task = tasks!.content.find((t) => t.id === taskId)!
      return tasksApi.update(taskId, {
        title: task.title,
        description: task.description ?? undefined,
        priority: task.priority,
        assigneeId: task.assignee?.id ?? null,
        status,
      })
    },
    // Move card instantly in UI; roll back if API fails
    onMutate: async ({ taskId, status }) => {
      await queryClient.cancelQueries({ queryKey: ['tasks', projectId] })
      const previousTasks = queryClient.getQueryData(['tasks', projectId, filterAssignedToMe])
      queryClient.setQueryData(
        ['tasks', projectId, filterAssignedToMe],
        (old: typeof tasks) => old
          ? { ...old, content: old.content.map((t) => t.id === taskId ? { ...t, status } : t) }
          : old
      )
      return { previousTasks }
    },
    onError: (_err, _vars, context) => {
      if (context?.previousTasks !== undefined) {
        queryClient.setQueryData(['tasks', projectId, filterAssignedToMe], context.previousTasks)
      }
    },
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: ['tasks', projectId] })
      queryClient.invalidateQueries({ queryKey: ['projects'] })
    },
  })

  const deleteTask = useMutation({
    mutationFn: tasksApi.remove,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tasks', projectId] })
      queryClient.invalidateQueries({ queryKey: ['projects'] })
    },
  })

  const handleDragStart = (event: DragStartEvent) => {
    const task = tasks?.content.find((t) => t.id === event.active.id)
    if (task) setActiveTask(task)
  }

  const handleDragEnd = (event: DragEndEvent) => {
    const { active, over } = event
    setActiveTask(null)
    if (!over) return
    const newStatus = over.id as TaskStatus
    const task = tasks?.content.find((t) => t.id === active.id)
    if (task && task.status !== newStatus) {
      updateStatus.mutate({ taskId: task.id, status: newStatus })
    }
  }

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
      <div className="flex items-center justify-between gap-3 mb-4 flex-wrap">
        <button
          onClick={() => setFilterAssignedToMe((v) => !v)}
          className={`text-sm font-medium px-4 py-2 rounded-lg transition-colors ${
            filterAssignedToMe
              ? 'bg-blue-600 hover:bg-blue-700 text-white'
              : 'bg-gray-100 hover:bg-gray-200 text-gray-600'
          }`}
        >
          Assigned to me
        </button>
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

      {/* Kanban board */}
      <DndContext sensors={sensors} onDragStart={handleDragStart} onDragEnd={handleDragEnd}>
        <div className="overflow-x-auto">
          <div className="grid grid-cols-3 gap-4 min-w-[540px]">
            {COLUMNS.map((col) => {
              const colTasks = (tasks?.content ?? []).filter((t) => t.status === col.status)
              return (
                <KanbanColumn key={col.status} col={col} count={colTasks.length}>
                  {colTasks.length === 0 && !tasksLoading && (
                    <p className="text-xs text-gray-300 text-center py-3">No tasks</p>
                  )}
                  {colTasks.map((task) => (
                    <TaskCard
                      key={task.id}
                      task={task}
                      users={users}
                      isEditing={editingTaskId === task.id}
                      isSaving={updateTask.isPending}
                      onStartEdit={() => setEditingTaskId(task.id)}
                      onCancelEdit={() => setEditingTaskId(null)}
                      onSave={(data) => updateTask.mutate({ taskId: task.id, data })}
                      onDelete={() => setDeletingTaskId(task.id)}
                    />
                  ))}
                </KanbanColumn>
              )
            })}
          </div>
        </div>

        {/* Floating card while dragging */}
        <DragOverlay dropAnimation={null}>
          {activeTask ? <TaskCardPreview task={activeTask} /> : null}
        </DragOverlay>
      </DndContext>

      {deletingTaskId !== null && (
        <ConfirmDialog
          title="Delete task"
          message="This task will be permanently deleted. Are you sure?"
          onConfirm={() => {
            deleteTask.mutate(deletingTaskId)
            setDeletingTaskId(null)
          }}
          onCancel={() => setDeletingTaskId(null)}
        />
      )}
    </div>
  )
}
