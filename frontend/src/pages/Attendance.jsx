import { useState, useEffect } from 'react'
import { Box, Typography, TextField, Button, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, FormControl, InputLabel, Select, MenuItem, Alert, Snackbar, Tabs, Tab } from '@mui/material'
import api from '../services/api'

const today = () => new Date().toISOString().split('T')[0]

export default function Attendance() {
  const [tab, setTab] = useState(0)
  const [date, setDate] = useState(today())
  const [month, setMonth] = useState(today().slice(0, 7))
  const [employeeId, setEmployeeId] = useState('')
  const [employees, setEmployees] = useState([])
  const [records, setRecords] = useState([])
  const [snack, setSnack] = useState({ open: false, message: '', severity: 'success' })

  useEffect(() => {
    api.get('/employees?status=active&limit=100')
      .then(({ data }) => setEmployees(data.employees || []))
      .catch(() => {})
  }, [])

  const fetchRecords = async () => {
    try {
      const params = {}
      if (tab === 0) {
        params.date = date
      } else {
        params.start_date = month + '-01'
        const lastDay = new Date(parseInt(month.split('-')[0]), parseInt(month.split('-')[1]), 0).getDate()
        params.end_date = month + '-' + String(lastDay).padStart(2, '0')
      }
      if (employeeId) params.employee_id = employeeId
      const { data } = await api.get('/attendance', { params })
      setRecords(data)
    } catch (err) {
      console.error(err)
    }
  }

  useEffect(() => { fetchRecords() }, [tab, date, month, employeeId]) // eslint-disable-line react-hooks/exhaustive-deps

  const markAttendance = async (empId, action) => {
    try {
      await api.post('/attendance', { employee_id: empId, action })
      setSnack({ open: true, message: `Check-${action} recorded`, severity: 'success' })
      fetchRecords()
    } catch (err) {
      setSnack({ open: true, message: err.response?.data?.message || 'Error', severity: 'error' })
    }
  }

  return (
    <Box>
      <Typography variant="h4" gutterBottom>Attendance</Typography>
      <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ mb: 2 }}>
        <Tab label="Daily View" />
        <Tab label="Monthly View" />
      </Tabs>
      <Box sx={{ display: 'flex', gap: 2, mb: 2, alignItems: 'center' }}>
        {tab === 0 ? (
          <TextField type="date" size="small" value={date} onChange={(e) => setDate(e.target.value)} />
        ) : (
          <TextField type="month" size="small" value={month} onChange={(e) => setMonth(e.target.value)} />
        )}
        <FormControl size="small" sx={{ minWidth: 200 }}>
          <InputLabel>Employee</InputLabel>
          <Select value={employeeId} label="Employee" onChange={(e) => setEmployeeId(e.target.value)}>
            <MenuItem value="">All Employees</MenuItem>
            {employees.map((e) => (
              <MenuItem key={e.id} value={e.id}>{e.first_name} {e.last_name} ({e.employee_code})</MenuItem>
            ))}
          </Select>
        </FormControl>
        <Button variant="outlined" onClick={fetchRecords}>Refresh</Button>
      </Box>
      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Employee</TableCell>
              <TableCell>Date</TableCell>
              <TableCell>Check In</TableCell>
              <TableCell>Check Out</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {records.length === 0 && (
              <TableRow><TableCell colSpan={6} align="center">No records found</TableCell></TableRow>
            )}
            {records.map((r) => (
              <TableRow key={r.id || r.employee_id}>
                <TableCell>{r.employee_name || `${r.first_name} ${r.last_name}`}</TableCell>
                <TableCell>{r.date || tab === 0 ? date : ''}</TableCell>
                <TableCell>{r.check_in || '-'}</TableCell>
                <TableCell>{r.check_out || '-'}</TableCell>
                <TableCell>{r.status || (r.check_in ? 'present' : '-')}</TableCell>
                <TableCell>
                  {!r.check_in && (
                    <Button size="small" variant="outlined" onClick={() => markAttendance(r.employee_id, 'in')}>Check In</Button>
                  )}
                  {r.check_in && !r.check_out && (
                    <Button size="small" variant="outlined" color="secondary" onClick={() => markAttendance(r.employee_id, 'out')} sx={{ ml: 1 }}>Check Out</Button>
                  )}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
      <Snackbar open={snack.open} autoHideDuration={4000} onClose={() => setSnack({ ...snack, open: false })}>
        <Alert severity={snack.severity}>{snack.message}</Alert>
      </Snackbar>
    </Box>
  )
}
