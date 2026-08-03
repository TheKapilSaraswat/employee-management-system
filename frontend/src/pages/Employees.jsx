import { useState, useEffect, useCallback } from 'react'
import { Box, Button, Dialog, DialogTitle, DialogContent, DialogActions, TextField, Select, MenuItem, InputLabel, FormControl, Typography, Alert, Snackbar } from '@mui/material'
import { DataGrid, GridToolbar } from '@mui/x-data-grid'
import api from '../services/api'

export default function Employees() {
  const [employees, setEmployees] = useState([])
  const [departments, setDepartments] = useState([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(false)
  const [page, setPage] = useState(0)
  const [pageSize, setPageSize] = useState(20)
  const [search, setSearch] = useState('')
  const [deptFilter, setDeptFilter] = useState('')
  const [statusFilter, setStatusFilter] = useState('')
  const [open, setOpen] = useState(false)
  const [editId, setEditId] = useState(null)
  const [form, setForm] = useState({ first_name: '', last_name: '', email: '', phone: '', position: '', department_id: '', salary: '', hire_date: '' })
  const [snack, setSnack] = useState({ open: false, message: '', severity: 'success' })

  const fetchEmployees = useCallback(async () => {
    setLoading(true)
    try {
      const params = { page, size: pageSize }
      if (search) params.search = search
      if (deptFilter) params.department_id = deptFilter
      if (statusFilter) params.status = statusFilter
      const { data } = await api.get('/employees', { params })
      setEmployees(data.employees.map((e) => ({ ...e, id: e.id })))
      setTotal(data.total)
    } catch (err) {
      console.error(err)
    } finally {
      setLoading(false)
    }
  }, [page, pageSize, search, deptFilter, statusFilter])

  useEffect(() => { fetchEmployees() }, [fetchEmployees])

  useEffect(() => {
    api.get('/departments').then(({ data }) => setDepartments(data)).catch(() => {})
  }, [])

  const openAdd = () => {
    setEditId(null)
    setForm({ first_name: '', last_name: '', email: '', phone: '', position: '', department_id: '', salary: '', hire_date: '' })
    setOpen(true)
  }

  const openEdit = (emp) => {
    setEditId(emp.id)
    setForm({
      first_name: emp.first_name, last_name: emp.last_name, email: emp.email || '', phone: emp.phone || '',
      position: emp.position || '', department_id: emp.department_id || '', salary: emp.salary || '', hire_date: emp.hire_date || '',
    })
    setOpen(true)
  }

  const handleSave = async () => {
    try {
      if (editId) {
        await api.put(`/employees/${editId}`, form)
        setSnack({ open: true, message: 'Employee updated', severity: 'success' })
      } else {
        await api.post('/employees', form)
        setSnack({ open: true, message: 'Employee created', severity: 'success' })
      }
      setOpen(false)
      fetchEmployees()
    } catch (err) {
      setSnack({ open: true, message: err.response?.data?.message || 'Error', severity: 'error' })
    }
  }

  const handleDelete = async (id) => {
    if (!window.confirm('Deactivate this employee?')) return
    try {
      await api.delete(`/employees/${id}`)
      setSnack({ open: true, message: 'Employee deactivated', severity: 'success' })
      fetchEmployees()
    } catch {
      setSnack({ open: true, message: 'Error deactivating', severity: 'error' })
    }
  }

  const columns = [
    { field: 'employee_code', headerName: 'Code', width: 110 },
    { field: 'first_name', headerName: 'First Name', width: 130 },
    { field: 'last_name', headerName: 'Last Name', width: 130 },
    { field: 'email', headerName: 'Email', width: 200 },
    { field: 'department_name', headerName: 'Department', width: 150 },
    { field: 'position', headerName: 'Position', width: 150 },
    { field: 'status', headerName: 'Status', width: 100 },
    {
      field: 'actions', headerName: 'Actions', width: 150, sortable: false,
      renderCell: (params) => (
        <Box>
          <Button size="small" onClick={() => openEdit(params.row)}>Edit</Button>
          <Button size="small" color="error" onClick={() => handleDelete(params.row.id)}>Deactivate</Button>
        </Box>
      ),
    },
  ]

  return (
    <Box>
      <Typography variant="h4" gutterBottom>Employees</Typography>
      <Box sx={{ display: 'flex', gap: 2, mb: 2, alignItems: 'center' }}>
        <TextField size="small" label="Search" value={search} onChange={(e) => setSearch(e.target.value)} />
        <FormControl size="small" sx={{ minWidth: 150 }}>
          <InputLabel>Department</InputLabel>
          <Select value={deptFilter} label="Department" onChange={(e) => setDeptFilter(e.target.value)}>
            <MenuItem value="">All</MenuItem>
            {departments.map((d) => <MenuItem key={d.id} value={d.id}>{d.name}</MenuItem>)}
          </Select>
        </FormControl>
        <FormControl size="small" sx={{ minWidth: 120 }}>
          <InputLabel>Status</InputLabel>
          <Select value={statusFilter} label="Status" onChange={(e) => setStatusFilter(e.target.value)}>
            <MenuItem value="">All</MenuItem>
            <MenuItem value="active">Active</MenuItem>
            <MenuItem value="inactive">Inactive</MenuItem>
          </Select>
        </FormControl>
        <Button variant="contained" onClick={openAdd}>Add Employee</Button>
      </Box>
      <DataGrid
        rows={employees}
        columns={columns}
        rowCount={total}
        loading={loading}
        pageSizeOptions={[10, 20, 50]}
        paginationModel={{ page, pageSize }}
        onPaginationModelChange={(m) => { setPage(m.page); setPageSize(m.pageSize) }}
        paginationMode="server"
        slots={{ toolbar: GridToolbar }}
        slotProps={{ toolbar: { showQuickFilter: false } }}
        autoHeight
        disableRowSelectionOnClick
      />
      <Dialog open={open} onClose={() => setOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{editId ? 'Edit Employee' : 'Add Employee'}</DialogTitle>
        <DialogContent>
          <TextField fullWidth label="First Name" margin="dense" value={form.first_name}
            onChange={(e) => setForm({ ...form, first_name: e.target.value })} required />
          <TextField fullWidth label="Last Name" margin="dense" value={form.last_name}
            onChange={(e) => setForm({ ...form, last_name: e.target.value })} required />
          <TextField fullWidth label="Email" margin="dense" value={form.email}
            onChange={(e) => setForm({ ...form, email: e.target.value })} />
          <TextField fullWidth label="Phone" margin="dense" value={form.phone}
            onChange={(e) => setForm({ ...form, phone: e.target.value })} />
          <TextField fullWidth label="Position" margin="dense" value={form.position}
            onChange={(e) => setForm({ ...form, position: e.target.value })} />
          <FormControl fullWidth margin="dense">
            <InputLabel>Department</InputLabel>
            <Select value={form.department_id} label="Department" onChange={(e) => setForm({ ...form, department_id: e.target.value })}>
              <MenuItem value="">None</MenuItem>
              {departments.map((d) => <MenuItem key={d.id} value={d.id}>{d.name}</MenuItem>)}
            </Select>
          </FormControl>
          <TextField fullWidth label="Salary" type="number" margin="dense" value={form.salary}
            onChange={(e) => setForm({ ...form, salary: e.target.value })} />
          <TextField fullWidth label="Hire Date" type="date" margin="dense" value={form.hire_date}
            onChange={(e) => setForm({ ...form, hire_date: e.target.value })} InputLabelProps={{ shrink: true }} />
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
