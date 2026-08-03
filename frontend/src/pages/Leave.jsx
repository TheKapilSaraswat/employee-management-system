import { useState, useEffect } from 'react'
import { Box, Typography, Button, Dialog, DialogTitle, DialogContent, DialogActions, TextField, Select, MenuItem, InputLabel, FormControl, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, Grid, Card, CardContent, Alert, Snackbar } from '@mui/material'
import api from '../services/api'
import { useAuth } from '../context/AuthContext'

export default function Leave() {
  const { user } = useAuth()
  const [leaves, setLeaves] = useState([])
  const [employees, setEmployees] = useState([])
  const [balance, setBalance] = useState(null)
  const [open, setOpen] = useState(false)
  const [form, setForm] = useState({ employee_id: '', type: 'annual', start_date: '', end_date: '', reason: '' })
  const [snack, setSnack] = useState({ open: false, message: '', severity: 'success' })

  const fetchLeaves = async () => {
    try {
      const { data } = await api.get('/leaves')
      setLeaves(data)
    } catch (err) { console.error(err) }
  }

  const fetchEmployees = async () => {
    try {
      const { data } = await api.get('/employees?status=active&limit=100')
      setEmployees(data.employees || [])
    } catch (err) { console.error(err) }
  }

  useEffect(() => {
    fetchLeaves()
    fetchEmployees()
  }, [])

  const fetchBalance = async (empId) => {
    try {
      const { data } = await api.get(`/leaves/balance/${empId}`)
      setBalance(data)
    } catch (err) { console.error(err) }
  }

  const openApply = () => {
    setForm({ employee_id: '', type: 'annual', start_date: '', end_date: '', reason: '' })
    setBalance(null)
    setOpen(true)
  }

  const handleApply = async () => {
    try {
      await api.post('/leaves', form)
      setSnack({ open: true, message: 'Leave applied', severity: 'success' })
      setOpen(false)
      fetchLeaves()
    } catch (err) {
      setSnack({ open: true, message: err.response?.data?.message || 'Error', severity: 'error' })
    }
  }

  const handleAction = async (id, action) => {
    try {
      await api.put(`/leaves/${id}/${action}`)
      const label = action === 'approve' ? 'approved' : 'rejected'
      setSnack({ open: true, message: `Leave ${label}`, severity: 'success' })
      fetchLeaves()
    } catch (err) {
      setSnack({ open: true, message: err.response?.data?.message || 'Error', severity: 'error' })
    }
  }

  return (
    <Box>
      <Typography variant="h4" gutterBottom>Leave Management</Typography>

      {balance && (
        <Grid container spacing={2} sx={{ mb: 3 }}>
          {Object.entries(balance).map(([key, val]) => (
            <Grid size={4} key={key}>
              <Card>
                <CardContent>
                  <Typography variant="subtitle2" sx={{ textTransform: 'capitalize' }}>{key} Leave</Typography>
                  <Typography>Total: {val.total}</Typography>
                  <Typography>Used: {val.used}</Typography>
                  <Typography>Pending: {val.pending}</Typography>
                  <Typography fontWeight="bold">Remaining: {val.remaining}</Typography>
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>
      )}

      <Box sx={{ display: 'flex', gap: 2, mb: 2 }}>
        <Button variant="contained" onClick={openApply}>Apply Leave</Button>
      </Box>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Employee</TableCell>
              <TableCell>Type</TableCell>
              <TableCell>Start</TableCell>
              <TableCell>End</TableCell>
              <TableCell>Reason</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {leaves.length === 0 && (
              <TableRow><TableCell colSpan={7} align="center">No leave records</TableCell></TableRow>
            )}
            {leaves.map((l) => (
              <TableRow key={l.id}>
                <TableCell>{l.employee_name}</TableCell>
                <TableCell sx={{ textTransform: 'capitalize' }}>{l.type}</TableCell>
                <TableCell>{l.start_date}</TableCell>
                <TableCell>{l.end_date}</TableCell>
                <TableCell>{l.reason || '-'}</TableCell>
                <TableCell>
                  <Typography variant="body2" sx={{
                    color: l.status === 'approved' ? 'green' : l.status === 'rejected' ? 'red' : 'orange',
                    fontWeight: 'bold'
                  }}>
                    {l.status}
                  </Typography>
                </TableCell>
                <TableCell>
                  {user?.role === 'admin' && l.status === 'pending' && (
                    <Box>
                      <Button size="small" color="success" onClick={() => handleAction(l.id, 'approve')}>Approve</Button>
                      <Button size="small" color="error" onClick={() => handleAction(l.id, 'reject')}>Reject</Button>
                    </Box>
                  )}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      <Dialog open={open} onClose={() => setOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Apply Leave</DialogTitle>
        <DialogContent>
          <FormControl fullWidth margin="dense">
            <InputLabel>Employee</InputLabel>
            <Select value={form.employee_id} label="Employee" onChange={(e) => { setForm({ ...form, employee_id: e.target.value }); fetchBalance(e.target.value) }}>
              {employees.map((e) => <MenuItem key={e.id} value={e.id}>{e.first_name} {e.last_name}</MenuItem>)}
            </Select>
          </FormControl>
          <FormControl fullWidth margin="dense">
            <InputLabel>Leave Type</InputLabel>
            <Select value={form.type} label="Leave Type" onChange={(e) => setForm({ ...form, type: e.target.value })}>
              <MenuItem value="annual">Annual</MenuItem>
              <MenuItem value="sick">Sick</MenuItem>
              <MenuItem value="personal">Personal</MenuItem>
            </Select>
          </FormControl>
          <TextField fullWidth type="date" label="Start Date" margin="dense" value={form.start_date}
            onChange={(e) => setForm({ ...form, start_date: e.target.value })} InputLabelProps={{ shrink: true }} />
          <TextField fullWidth type="date" label="End Date" margin="dense" value={form.end_date}
            onChange={(e) => setForm({ ...form, end_date: e.target.value })} InputLabelProps={{ shrink: true }} />
          <TextField fullWidth label="Reason" margin="dense" value={form.reason} multiline rows={3}
            onChange={(e) => setForm({ ...form, reason: e.target.value })} />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={handleApply}>Apply</Button>
        </DialogActions>
      </Dialog>

      <Snackbar open={snack.open} autoHideDuration={4000} onClose={() => setSnack({ ...snack, open: false })}>
        <Alert severity={snack.severity}>{snack.message}</Alert>
      </Snackbar>
    </Box>
  )
}
