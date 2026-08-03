import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import Departments from '../pages/Departments'
import { AuthProvider } from '../context/AuthContext'
import api from '../services/api'

vi.mock('../services/api', () => ({
  default: {
    get: vi.fn(() => Promise.resolve({ data: [] })),
    post: vi.fn(() => Promise.resolve({ data: {} })),
    put: vi.fn(() => Promise.resolve({ data: {} })),
    delete: vi.fn(() => Promise.resolve({ data: {} })),
    interceptors: {
      request: { handlers: [] },
      response: { handlers: [] },
    },
  },
}))

const mockDepartments = [
  { id: 'd1', name: 'Engineering', description: 'Eng team', employee_count: 10 },
  { id: 'd2', name: 'Marketing', description: 'Marketing team', employee_count: 5 },
]

const renderDepartments = (userRole = 'admin') => {
  localStorage.setItem('user', JSON.stringify({ id: '1', name: 'Admin', role: userRole }))

  return render(
    <MemoryRouter initialEntries={['/departments']}>
      <AuthProvider>
        <Departments />
      </AuthProvider>
    </MemoryRouter>
  )
}

describe('Departments Page', () => {
  beforeEach(() => {
    localStorage.clear()
    vi.clearAllMocks()
    api.get.mockReset()
    api.get.mockImplementation(() => Promise.resolve({ data: [] }))
  })

  it('renders departments heading', async () => {
    api.get.mockResolvedValueOnce({ data: mockDepartments })

    renderDepartments()

    expect(screen.getByText('Departments')).toBeInTheDocument()
  })

  it('displays department cards', async () => {
    api.get.mockResolvedValueOnce({ data: mockDepartments })

    renderDepartments()

    await waitFor(() => {
      expect(screen.getByText('Engineering')).toBeInTheDocument()
      expect(screen.getByText('Marketing')).toBeInTheDocument()
    })
  })

  it('shows employee count for departments', async () => {
    api.get.mockResolvedValueOnce({ data: mockDepartments })

    renderDepartments()

    await waitFor(() => {
      expect(screen.getByText('Employees: 10')).toBeInTheDocument()
      expect(screen.getByText('Employees: 5')).toBeInTheDocument()
    })
  })

  it('shows Add Department button for admin users', async () => {
    api.get.mockResolvedValueOnce({ data: mockDepartments })

    renderDepartments('admin')

    await waitFor(() => {
      expect(screen.getByText('Add Department')).toBeInTheDocument()
    })
  })

  it('hides Add Department button for non-admin users', async () => {
    api.get.mockResolvedValueOnce({ data: mockDepartments })

    renderDepartments('employee')

    await waitFor(() => {
      expect(screen.queryByText('Add Department')).not.toBeInTheDocument()
    })
  })

  it('opens add dialog when Add Department is clicked', async () => {
    api.get.mockResolvedValueOnce({ data: mockDepartments })

    renderDepartments('admin')

    await waitFor(() => {
      fireEvent.click(screen.getByText('Add Department'))
    })

    await waitFor(() => {
      expect(screen.getByText('Add Department', { selector: 'h2' })).toBeInTheDocument()
    })
  })

  it('shows no description text when description is empty', async () => {
    api.get.mockResolvedValueOnce({ data: [{ id: 'd3', name: 'HR', description: null, employee_count: 0 }] })

    renderDepartments()

    await waitFor(() => {
      expect(screen.getByText('No description')).toBeInTheDocument()
    })
  })

  it('shows edit and delete buttons for admin', async () => {
    api.get.mockResolvedValueOnce({ data: mockDepartments })

    renderDepartments('admin')

    await waitFor(() => {
      const editButtons = screen.getAllByText('Edit')
      const deleteButtons = screen.getAllByText('Delete')
      expect(editButtons.length).toBeGreaterThan(0)
      expect(deleteButtons.length).toBeGreaterThan(0)
    })
  })
})
