import { useState, useEffect } from 'react'
import { Box, Typography, Tabs, Tab, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, TextField, CircularProgress } from '@mui/material'
import api from '../services/api'

const today = () => new Date().toISOString().split('T')[0]
const monthStart = () => {
  const d = new Date()
  return new Date(d.getFullYear(), d.getMonth(), 1).toISOString().split('T')[0]
}

export default function Reports() {
  const [tab, setTab] = useState(0)
  const [data, setData] = useState([])
  const [loading, setLoading] = useState(false)
  const [dateRange, setDateRange] = useState({ startDate: monthStart(), endDate: today() })

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true)
      try {
        let res
        switch (tab) {
          case 0:
            res = await api.get('/reports/employees-by-department')
            break
          case 1:
            res = await api.get('/reports/attendance-summary', { params: dateRange })
            break
          case 2:
            res = await api.get('/reports/leave-summary')
            break
          case 3:
            res = await api.get('/reports/payroll-summary')
            break
        }
        setData(res.data || [])
      } catch (err) {
        console.error(err)
        setData([])
      } finally {
        setLoading(false)
      }
    }
    fetchData()
  }, [tab, dateRange])

  const renderTable = (columns, rows) => (
    <TableContainer component={Paper}>
      <Table>
        <TableHead>
          <TableRow>
            {columns.map((col) => <TableCell key={col.key}>{col.label}</TableCell>)}
          </TableRow>
        </TableHead>
        <TableBody>
          {rows.length === 0 && (
            <TableRow><TableCell colSpan={columns.length} align="center">No data</TableCell></TableRow>
          )}
          {rows.map((row, i) => (
            <TableRow key={i}>
              {columns.map((col) => (
                <TableCell key={col.key} align={col.align || 'left'}>{row[col.key] ?? '-'}</TableCell>
              ))}
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  )

  const tabs = ['Employees by Dept', 'Attendance Summary', 'Leave Summary', 'Payroll Summary']

  return (
    <Box>
      <Typography variant="h4" gutterBottom>Reports</Typography>
      <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ mb: 2 }}>
        {tabs.map((t, i) => <Tab key={i} label={t} />)}
      </Tabs>

      {tab === 1 && (
        <Box sx={{ display: 'flex', gap: 2, mb: 2 }}>
          <TextField type="date" size="small" label="Start" value={dateRange.startDate}
            onChange={(e) => setDateRange({ ...dateRange, startDate: e.target.value })} InputLabelProps={{ shrink: true }} />
          <TextField type="date" size="small" label="End" value={dateRange.endDate}
            onChange={(e) => setDateRange({ ...dateRange, endDate: e.target.value })} InputLabelProps={{ shrink: true }} />
        </Box>
      )}

      {loading ? <CircularProgress /> : (
        <>
          {tab === 0 && renderTable(
            [{ key: 'department', label: 'Department' }, { key: 'employee_count', label: 'Total' }, { key: 'active_count', label: 'Active' }, { key: 'inactive_count', label: 'Inactive' }],
            data
          )}
          {tab === 1 && renderTable(
            [{ key: 'department', label: 'Department' }, { key: 'total_attendance', label: 'Total Records' }, { key: 'present', label: 'Present' }, { key: 'absent', label: 'Absent' }, { key: 'late', label: 'Late' }, { key: 'attendance_percentage', label: 'Attendance %' }],
            data
          )}
          {tab === 2 && renderTable(
            [{ key: 'type', label: 'Type' }, { key: 'total_applied', label: 'Applied' }, { key: 'approved', label: 'Approved' }, { key: 'rejected', label: 'Rejected' }, { key: 'pending', label: 'Pending' }, { key: 'total_days', label: 'Total Days' }],
            data
          )}
          {tab === 3 && renderTable(
            [{ key: 'month', label: 'Month' }, { key: 'year', label: 'Year' }, { key: 'employee_count', label: 'Employees' }, { key: 'total_basic', label: 'Total Basic', align: 'right' }, { key: 'total_allowances', label: 'Allowances', align: 'right' }, { key: 'total_deductions', label: 'Deductions', align: 'right' }, { key: 'total_net_pay', label: 'Net Pay', align: 'right' }, { key: 'average_net_pay', label: 'Avg Net Pay', align: 'right' }],
            data
          )}
        </>
      )}
    </Box>
  )
}
