import { describe, it, expect } from 'vitest'
import { ROLES, LEAVE_STATUS, ATTENDANCE_STATUS } from '../utils/constants'

describe('constants', () => {
  describe('ROLES', () => {
    it('defines ADMIN role', () => {
      expect(ROLES.ADMIN).toBe('admin')
    })

    it('defines MANAGER role', () => {
      expect(ROLES.MANAGER).toBe('manager')
    })

    it('defines EMPLOYEE role', () => {
      expect(ROLES.EMPLOYEE).toBe('employee')
    })

    it('has exactly 3 roles', () => {
      expect(Object.keys(ROLES)).toHaveLength(3)
    })
  })

  describe('LEAVE_STATUS', () => {
    it('defines PENDING status', () => {
      expect(LEAVE_STATUS.PENDING).toBe('pending')
    })

    it('defines APPROVED status', () => {
      expect(LEAVE_STATUS.APPROVED).toBe('approved')
    })

    it('defines REJECTED status', () => {
      expect(LEAVE_STATUS.REJECTED).toBe('rejected')
    })

    it('has exactly 3 statuses', () => {
      expect(Object.keys(LEAVE_STATUS)).toHaveLength(3)
    })
  })

  describe('ATTENDANCE_STATUS', () => {
    it('defines PRESENT status', () => {
      expect(ATTENDANCE_STATUS.PRESENT).toBe('present')
    })

    it('defines ABSENT status', () => {
      expect(ATTENDANCE_STATUS.ABSENT).toBe('absent')
    })

    it('defines LATE status', () => {
      expect(ATTENDANCE_STATUS.LATE).toBe('late')
    })

    it('defines HALF_DAY status', () => {
      expect(ATTENDANCE_STATUS.HALF_DAY).toBe('half_day')
    })

    it('has exactly 4 statuses', () => {
      expect(Object.keys(ATTENDANCE_STATUS)).toHaveLength(4)
    })
  })
})
