import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import api from '../services/api'

describe('api service', () => {
  const originalLocation = window.location

  beforeEach(() => {
    localStorage.clear()
  })

  afterEach(() => {
    Object.defineProperty(window, 'location', { value: originalLocation, writable: true })
  })

  it('creates axios instance with correct baseURL', () => {
    expect(api.defaults.baseURL).toBe('/api')
  })

  it('sets Content-Type header to application/json', () => {
    expect(api.defaults.headers['Content-Type']).toBe('application/json')
  })

  describe('request interceptor', () => {
    it('adds Authorization header when token exists', () => {
      localStorage.setItem('token', 'test-token-123')

      const config = { headers: {} }
      const interceptors = api.interceptors.request.handlers
      const handler = interceptors[interceptors.length - 1]
      const result = handler.fulfilled(config)

      expect(result.headers.Authorization).toBe('Bearer test-token-123')
    })

    it('does not add Authorization header when no token', () => {
      const config = { headers: {} }
      const interceptors = api.interceptors.request.handlers
      const handler = interceptors[interceptors.length - 1]
      const result = handler.fulfilled(config)

      expect(result.headers.Authorization).toBeUndefined()
    })
  })

  describe('response interceptor', () => {
    it('passes through successful responses', () => {
      const interceptors = api.interceptors.response.handlers
      const handler = interceptors[interceptors.length - 1]
      const response = { data: 'test' }
      const result = handler.fulfilled(response)

      expect(result).toBe(response)
    })

    it('clears token on 401 error', () => {
      localStorage.setItem('token', 'expired-token')

      const mockAssign = vi.fn()
      Object.defineProperty(window, 'location', {
        value: { get href() { return '' }, set href(v) { mockAssign(v) } },
        writable: true,
      })

      const interceptors = api.interceptors.response.handlers
      const handler = interceptors[interceptors.length - 1]
      const error = { response: { status: 401 } }

      handler.rejected(error).catch(() => {})

      expect(localStorage.getItem('token')).toBeNull()
    })

    it('rejects non-401 errors without clearing token', async () => {
      localStorage.setItem('token', 'some-token')

      const interceptors = api.interceptors.response.handlers
      const handler = interceptors[interceptors.length - 1]
      const error = { response: { status: 500 } }

      await expect(handler.rejected(error)).rejects.toEqual(error)
      expect(localStorage.getItem('token')).toBe('some-token')
    })
  })
})
