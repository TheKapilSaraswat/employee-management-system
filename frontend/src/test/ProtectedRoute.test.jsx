import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import ProtectedRoute from '../components/ProtectedRoute'
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

const ProtectedContent = () => <div>Protected Content</div>

const renderWithAuth = (initialEntries = ['/'], token = null) => {
  if (token) localStorage.setItem('token', token)

  return render(
    <MemoryRouter initialEntries={initialEntries}>
      <AuthProvider>
        <Routes>
          <Route element={<ProtectedRoute />}>
            <Route path="/" element={<ProtectedContent />} />
          </Route>
        </Routes>
      </AuthProvider>
    </MemoryRouter>
  )
}

describe('ProtectedRoute', () => {
  beforeEach(() => {
    localStorage.clear()
    vi.clearAllMocks()
  })

  it('redirects to /login when no user is authenticated', async () => {
    renderWithAuth()

    await waitFor(() => {
      expect(screen.queryByText('Protected Content')).not.toBeInTheDocument()
    })
  })

  it('shows loading spinner while checking auth', () => {
    api.get.mockReturnValueOnce(new Promise(() => {}))
    renderWithAuth(['/'], 'some-token')

    expect(screen.getByRole('progressbar')).toBeInTheDocument()
  })

  it('renders children when user is authenticated', async () => {
    api.get.mockResolvedValueOnce({
      data: { id: '1', name: 'Test User', email: 'test@test.com', role: 'admin' },
    })

    renderWithAuth(['/'], 'valid-token')

    await waitFor(() => {
      expect(screen.getByText('Protected Content')).toBeInTheDocument()
    })
  })

  it('redirects to login when token validation fails', async () => {
    api.get.mockRejectedValueOnce(new Error('Unauthorized'))

    renderWithAuth(['/'], 'bad-token')

    await waitFor(() => {
      expect(screen.queryByText('Protected Content')).not.toBeInTheDocument()
    })
  })
})
