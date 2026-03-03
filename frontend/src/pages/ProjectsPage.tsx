import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { projectsApi } from '../features/projects/projectsApi'
import { Spinner } from '../components/Spinner'
import { ConfirmDialog } from '../components/ConfirmDialog'
import type { Project } from '../api/types'

// Completion badge color: gray → blue → green
function DoneBadge({ total, done }: { total: number; done: number }) {
  if (!total) return null   // handles 0, undefined, null
  const pct = Math.round((done / total) * 100)
  const color =
    pct === 100 ? 'bg-green-100 text-green-700' :
    pct > 0     ? 'bg-blue-100 text-blue-700'   :
                  'bg-gray-100 text-gray-500'
  return (
    <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${color}`}>
      {pct}% Done
    </span>
  )
}

export function ProjectsPage() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  // Create form
  const [showCreate, setShowCreate] = useState(false)
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')

  // Delete confirmation
  const [deletingProjectId, setDeletingProjectId] = useState<number | null>(null)

  // Edit form
  const [editingProject, setEditingProject] = useState<Project | null>(null)
  const [editName, setEditName] = useState('')
  const [editDescription, setEditDescription] = useState('')

  const { data, isLoading } = useQuery({
    queryKey: ['projects'],
    queryFn: () => projectsApi.getAll(),
  })

  const createMutation = useMutation({
    mutationFn: projectsApi.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['projects'] })
      setShowCreate(false)
      setName('')
      setDescription('')
    },
  })

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: { name: string; description: string } }) =>
      projectsApi.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['projects'] })
      setEditingProject(null)
    },
  })

  const deleteMutation = useMutation({
    mutationFn: projectsApi.remove,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['projects'] }),
  })

  const startEdit = (e: React.MouseEvent, project: Project) => {
    e.stopPropagation()
    setEditingProject(project)
    setEditName(project.name)
    setEditDescription(project.description ?? '')
  }

  return (
    <div>
      <div className="flex flex-wrap items-center justify-between gap-3 mb-6">
        <h1 className="text-xl sm:text-2xl font-bold text-gray-900">All Projects</h1>
        <button
          onClick={() => setShowCreate(true)}
          className="bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium px-4 py-2 rounded-lg shrink-0"
        >
          + New Project
        </button>
      </div>

      {/* Create project form */}
      {showCreate && (
        <div className="mb-6 bg-white border border-gray-200 rounded-xl p-5 shadow-sm">
          <h2 className="font-semibold text-gray-800 mb-3">New Project</h2>
          <form
            onSubmit={(e) => { e.preventDefault(); createMutation.mutate({ name, description }) }}
            className="space-y-3"
          >
            <input
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
              placeholder="Project name"
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
            <input
              type="text"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Description (optional)"
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
            <div className="flex gap-2">
              <button
                type="submit"
                disabled={createMutation.isPending}
                className="bg-blue-600 hover:bg-blue-700 disabled:bg-blue-400 text-white text-sm font-medium px-4 py-2 rounded-lg"
              >
                {createMutation.isPending ? 'Creating…' : 'Create'}
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

      {isLoading && <Spinner />}

      {data?.content.length === 0 && (
        <p className="text-gray-400 text-center py-12">No projects yet. Create your first one!</p>
      )}

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {data?.content.map((project) => (
          <div
            key={project.id}
            className="bg-white border border-gray-200 rounded-xl p-5 shadow-sm hover:shadow-md transition-shadow"
          >
            {editingProject?.id === project.id ? (
              /* ── Edit mode ── */
              <form
                onSubmit={(e) => {
                  e.preventDefault()
                  updateMutation.mutate({ id: project.id, data: { name: editName, description: editDescription } })
                }}
                className="space-y-3"
                onClick={(e) => e.stopPropagation()}
              >
                <input
                  type="text"
                  value={editName}
                  onChange={(e) => setEditName(e.target.value)}
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
                <div className="flex gap-2">
                  <button
                    type="submit"
                    disabled={updateMutation.isPending}
                    className="bg-blue-600 hover:bg-blue-700 disabled:bg-blue-400 text-white text-xs font-medium px-3 py-1.5 rounded-lg"
                  >
                    {updateMutation.isPending ? 'Saving…' : 'Save'}
                  </button>
                  <button
                    type="button"
                    onClick={() => setEditingProject(null)}
                    className="text-xs text-gray-500 hover:text-gray-700 px-3 py-1.5"
                  >
                    Cancel
                  </button>
                </div>
              </form>
            ) : (
              /* ── Read mode ── */
              <div
                className="cursor-pointer"
                onClick={() => navigate(`/projects/${project.id}`)}
              >
                {/* Top row: name + Done badge */}
                <div className="flex items-start justify-between gap-2 mb-1">
                  <h3 className="font-semibold text-gray-900 leading-tight">{project.name}</h3>
                  <DoneBadge total={project.totalTasks} done={project.doneTasks} />
                </div>

                {project.description && (
                  <p className="text-sm text-gray-500 mb-3 line-clamp-2">{project.description}</p>
                )}

                {/* Progress bar */}
                {project.totalTasks > 0 && (
                  <div className="w-full bg-gray-100 rounded-full h-1.5 mb-3">
                    <div
                      className="bg-blue-500 h-1.5 rounded-full transition-all"
                      style={{ width: `${Math.round((project.doneTasks / project.totalTasks) * 100)}%` }}
                    />
                  </div>
                )}

                <div className="flex items-center justify-between">
                  <span className="text-xs text-gray-400">by {project.owner.name}</span>
                  <div className="flex items-center gap-3" onClick={(e) => e.stopPropagation()}>
                    <button
                      onClick={(e) => startEdit(e, project)}
                      className="text-xs text-gray-400 hover:text-blue-600"
                    >
                      Edit
                    </button>
                    <button
                      onClick={() => setDeletingProjectId(project.id)}
                      className="text-xs text-red-400 hover:text-red-600"
                    >
                      Delete
                    </button>
                  </div>
                </div>
              </div>
            )}
          </div>
        ))}
      </div>

      {deletingProjectId !== null && (
        <ConfirmDialog
          title="Delete project"
          message="All tasks in this project will also be deleted. This cannot be undone."
          onConfirm={() => {
            deleteMutation.mutate(deletingProjectId)
            setDeletingProjectId(null)
          }}
          onCancel={() => setDeletingProjectId(null)}
        />
      )}
    </div>
  )
}
