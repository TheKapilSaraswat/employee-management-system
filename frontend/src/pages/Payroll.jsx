import { useState, useEffect } from 'react'
import { Box, Typography, Button, TextField, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, Grid, Card, CardContent, Alert, Snackbar } from '@mui/material'
import api from '../services/api'
import { useAuth } from '../context/AuthContext'

export default function Payroll() {
  const { user } = useAuth()
  const [records, setRecords] = useState([])
  const [summary, setSummary] = useState(null)
  const [month, setMonth] = useState(new Date().getMonth() + 1)
  const [year, setYear] = useState(new Date().getFullYear())
  const [snack, setSnack] = useState({ open: false, message: '', severity: 'success' })

  const fetchRecords = async () => {
    try {
      const { data } = await api.get('/payroll', { params: { month, year } })
      setRecords(data)
    } catch (err) { console.error(err) }
  }

  const fetchSummary = async () => {
    try {
      const { data } = await api.get('/payroll/summary')
      setSummary(data)
    } catch (err) { console.error(err) }
  }

  useEffect(() => {
    fetchRecords()
    fetchSummary()
  }, [month, year]) // eslint-disable-line react-hooks/exhaustive-deps

  const generatePayroll = async () => {
    try {
      await api.post('/payroll/generate', { month, year })
      setSnack({ open: true, message: 'Payroll generated', severity: 'success' })
      fetchRecords()
    } catch (err) {
      setSnack({ open: true, message: err.response?.data?.message || 'Error', severity: 'error' })
    }
  }

  const processPayment = async (id) => {
    try {
      await api.put(`/payroll/${id}/process`)
      setSnack({ open: true, message: 'Payment processed', severity: 'success' })
      fetchRecords()
      fetchSummary()
    } catch (err) {
      setSnack({ open: true, message: err.response?.data?.message || 'Error', severity: 'error' })
    }
  }

  return (
    <Box>
      <Typography variant="h4" gutterBottom>Payroll</Typography>

      {summary && (
        <Grid container spacing={2} sx={{ mb: 3 }}>
          <Grid size={{ xs: 6, md: 3 }}>
            <Card><CardContent>
              <Typography variant="body2" color="text.secondary">Total Paid</Typography>
              <Typography variant="h6">${(summary.totalPayroll || 0).toLocaleString()}</Typography>
            </CardContent></Card>
          </Grid>
          <Grid size={{ xs: 6, md: 3 }}>
            <Card><CardContent>
              <Typography variant="body2" color="text.secondary">Avg Salary</Typography>
              <Typography variant="h6">${(summary.averageSalary || 0).toLocaleString()}</Typography>
            </CardContent></Card>
          </Grid>
          <Grid size={{ xs: 6, md: 3 }}>
            <Card><CardContent>
              <Typography variant="body2" color="text.secondary">Paid</Typography>
              <Typography variant="h6">{summary.paidCount}</Typography>
            </CardContent></Card>
          </Grid>
          <Grid size={{ xs: 6, md: 3 }}>
            <Card><CardContent>
              <Typography variant="body2" color="text.secondary">Pending</Typography>
              <Typography variant="h6">{summary.pendingCount}</Typography>
            </CardContent></Card>
          </Grid>
        </Grid>
      )}

      <Box sx={{ display: 'flex', gap: 2, mb: 2, alignItems: 'center' }}>
        <TextField type="number" size="small" label="Month" value={month}
          onChange={(e) => setMonth(parseInt(e.target.value) || 1)} inputProps={{ min: 1, max: 12 }} sx={{ width: 100 }} />
        <TextField type="number" size="small" label="Year" value={year}
          onChange={(e) => setYear(parseInt(e.target.value) || new Date().getFullYear())} sx={{ width: 120 }} />
        {user?.role === 'admin' && (
          <Button variant="contained" onClick={generatePayroll}>Generate Payroll</Button>
        )}
      </Box>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Employee</TableCell>
              <TableCell>Code</TableCell>
              <TableCell>Department</TableCell>
              <TableCell align="right">Basic</TableCell>
              <TableCell align="right">Allowances</TableCell>
              <TableCell align="right">Deductions</TableCell>
              <TableCell align="right">Net Pay</TableCell>
              <TableCell>Status</TableCell>
              {user?.role === 'admin' && <TableCell>Action</TableCell>}
            </TableRow>
          </TableHead>
          <TableBody>
            {records.length === 0 && (
              <TableRow><TableCell colSpan={user?.role === 'admin' ? 9 : 8} align="center">No records</TableCell></TableRow>
            )}
            {records.map((r) => (
              <TableRow key={r.id}>
                <TableCell>{r.employee_name}</TableCell>
                <TableCell>{r.employee_code}</TableCell>
                <TableCell>{r.department_name || '-'}</TableCell>
                <TableCell align="right">${r.basic_pay?.toLocaleString()}</TableCell>
                <TableCell align="right">${r.allowances?.toLocaleString()}</TableCell>
                <TableCell align="right">${r.deductions?.toLocaleString()}</TableCell>
                <TableCell align="right"><strong>${r.net_pay?.toLocaleString()}</strong></TableCell>
                <TableCell>{r.status}</TableCell>
                {user?.role === 'admin' && (
                  <TableCell>
                    {r.status === 'pending' && (
                      <Button size="small" variant="outlined" onClick={() => processPayment(r.id)}>Process</Button>
                    )}
                    {r.status === 'paid' && <Typography variant="body2" color="text.secondary">{r.paid_at ? new Date(r.paid_at).toLocaleDateString() : 'Paid'}</Typography>}
                  </TableCell>
                )}
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
