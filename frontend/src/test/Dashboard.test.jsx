import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import Dashboard from '../pages/Dashboard'
import { AuthProvider } from '../context/AuthContext'
import api from '../services/api'

vi.mock('../services/api', () => ({
  default: {
    get: vi.fn(() => Promise.resolve({ data: {} })),
    post: vi.fn(() => Promise.resolve({ data: {} })),
    interceptors: {
      request: { handlers: [] },
      response: { handlers: [] },
    },
  },
}))

const renderDashboard = () => {
  return render(
    <MemoryRouter initialEntries={['/']}>
      <AuthProvider>
        <Dashboard />
      </AuthProvider>
    </MemoryRouter>
  )
}

describe('Dashboard Page', () => {
  beforeEach(() => {
    localStorage.clear()
    vi.clearAllMocks()
    api.get.mockReset()
    api.get.mockImplementation(() => Promise.resolve({ data: {} }))
  })

  it('shows loading spinner initially', () => {
    api.get.mockReturnValueOnce(new Promise(() => {}))
    renderDashboard()
    expect(screen.getByRole('progressbar')).toBeInTheDocument()
  })

  it('displays dashboard cards with stats', async () => {
    api.get.mockImplementation((url) => {
      if (url.includes('employees')) return Promise.resolve({ data: { total: 25 } })
      if (url.includes('departments')) return Promise.resolve({ data: [{}, {}, {}] })
      if (url.includes('leaves')) return Promise.resolve({ data: [{}, {}] })
      if (url.includes('attendance')) return Promise.resolve({ data: [{ check_in: '09:00' }, { check_in: '08:30' }] })
      if (url.includes('payroll')) return Promise.resolve({ data: { paidCount: 10, pendingCount: 5 } })
      return Promise.resolve({ data: {} })
    })

    renderDashboard()

    await waitFor(() => {
      expect(screen.getByText('Dashboard')).toBeInTheDocument()
      expect(screen.getByText('Total Employees')).toBeInTheDocument()
      expect(screen.getByText('Departments')).toBeInTheDocument()
      expect(screen.getByText('Pending Leaves')).toBeInTheDocument()
    })
  })

  it('shows error alert when data fetch fails', async () => {
    api.get.mockRejectedValue(new Error('Network error'))

    renderDashboard()

    await waitFor(() => {
      expect(screen.getByText(/failed to load dashboard data/i)).toBeInTheDocument()
    })
  })

  it('renders all 6 stat cards on successful data load', async () => {
    api.get.mockImplementation((url) => {
      if (url.includes('employees')) return Promise.resolve({ data: { total: 25 } })
      if (url.includes('departments')) return Promise.resolve({ data: [{}] })
      if (url.includes('leaves')) return Promise.resolve({ data: [] })
      if (url.includes('attendance')) return Promise.resolve({ data: [{ check_in: '09:00' }] })
      if (url.includes('payroll')) return Promise.resolve({ data: { paidCount: 10, pendingCount: 5 } })
      return Promise.resolve({ data: {} })
    })

    renderDashboard()

    await waitFor(() => {
      expect(screen.getByText('Total Employees')).toBeInTheDocument()
      expect(screen.getByText('Departments')).toBeInTheDocument()
      expect(screen.getByText('Pending Leaves')).toBeInTheDocument()
      expect(screen.getByText("Today's Check-ins")).toBeInTheDocument()
      expect(screen.getByText('Paid Payrolls')).toBeInTheDocument()
      expect(screen.getByText('Pending Payrolls')).toBeInTheDocument()
    })
  })
})
