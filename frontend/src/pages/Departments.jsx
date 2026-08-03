import { useState, useEffect } from 'react'
import { Box, Button, Dialog, DialogTitle, DialogContent, DialogActions, TextField, Typography, Card, CardContent, CardActions, Grid, Alert, Snackbar } from '@mui/material'
import api from '../services/api'
import { useAuth } from '../context/AuthContext'

export default function Departments() {
  const { user } = useAuth()
  const [departments, setDepartments] = useState([])
  const [open, setOpen] = useState(false)
  const [editId, setEditId] = useState(null)
  const [form, setForm] = useState({ name: '', description: '' })
  const [snack, setSnack] = useState({ open: false, message: '', severity: 'success' })

  const fetchDepts = async () => {
    try {
      const { data } = await api.get('/departments')
      setDepartments(data)
    } catch (err) {
      console.error(err)
    }
  }

  useEffect(() => { fetchDepts() }, [])

  const openAdd = () => {
    setEditId(null)
    setForm({ name: '', description: '' })
    setOpen(true)
  }

  const openEdit = (dept) => {
    setEditId(dept.id)
    setForm({ name: dept.name, description: dept.description || '' })
    setOpen(true)
  }

  const handleSave = async () => {
    try {
      if (editId) {
        await api.put(`/departments/${editId}`, form)
        setSnack({ open: true, message: 'Department updated', severity: 'success' })
      } else {
        await api.post('/departments', form)
        setSnack({ open: true, message: 'Department created', severity: 'success' })
      }
      setOpen(false)
      fetchDepts()
    } catch (err) {
      setSnack({ open: true, message: err.response?.data?.message || 'Error', severity: 'error' })
    }
  }

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this department?')) return
    try {
      await api.delete(`/departments/${id}`)
      setSnack({ open: true, message: 'Department deleted', severity: 'success' })
      fetchDepts()
    } catch (err) {
      setSnack({ open: true, message: err.response?.data?.message || 'Error', severity: 'error' })
    }
  }

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
        <Typography variant="h4">Departments</Typography>
        {user?.role === 'admin' && <Button variant="contained" onClick={openAdd}>Add Department</Button>}
      </Box>
      <Grid container spacing={3}>
        {departments.map((dept) => (
          <Grid size={{ xs: 12, sm: 6, md: 4 }} key={dept.id}>
            <Card>
              <CardContent>
                <Typography variant="h6">{dept.name}</Typography>
                <Typography variant="body2" color="text.secondary">{dept.description || 'No description'}</Typography>
                <Typography variant="body2" sx={{ mt: 1 }}>Employees: {dept.employee_count || 0}</Typography>
              </CardContent>
              {user?.role === 'admin' && (
                <CardActions>
                  <Button size="small" onClick={() => openEdit(dept)}>Edit</Button>
                  <Button size="small" color="error" onClick={() => handleDelete(dept.id)}>Delete</Button>
                </CardActions>
              )}
            </Card>
          </Grid>
        ))}
      </Grid>

      <Dialog open={open} onClose={() => setOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{editId ? 'Edit Department' : 'Add Department'}</DialogTitle>
        <DialogContent>
          <TextField fullWidth label="Name" margin="dense" value={form.name}
            onChange={(e) => setForm({ ...form, name: e.target.value })} required />
          <TextField fullWidth label="Description" margin="dense" value={form.description} multiline rows={3}
            onChange={(e) => setForm({ ...form, description: e.target.value })} />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={handleSave}>Save</Button>
        </DialogActions>
      </Dialog>

      <Snackbar open={snack.open} autoHideDuration={4000} onClose={() => setSnack({ ...snack, open: false })}>
        <Alert severity={snack.severity}>{snack.message}</Alert>
      </Snackbar>
    </Box>
  )
}
