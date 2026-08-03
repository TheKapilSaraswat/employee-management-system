import { useState } from 'react'
import { Box, Card, CardContent, TextField, Button, Typography, Tabs, Tab, Alert } from '@mui/material'
import { useAuth } from '../context/AuthContext'
import api from '../services/api'

export default function Login() {
  const { login } = useAuth()
  const [tab, setTab] = useState(0)
  const [form, setForm] = useState({ email: '', password: '', name: '' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      if (tab === 0) {
        const { data } = await api.post('/auth/login', { email: form.email, password: form.password })
        login(data.token, data)
      } else {
        const { data } = await api.post('/auth/register', { email: form.email, password: form.password, name: form.name })
        login(data.token, data)
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Something went wrong')
    } finally {
      setLoading(false)
    }
  }

  return (
    <Box display="flex" justifyContent="center" alignItems="center" minHeight="100vh" bgcolor="#f5f5f5">
      <Card sx={{ width: 420 }}>
        <CardContent>
          <Typography variant="h5" textAlign="center" gutterBottom>Employee Management System</Typography>
          <Tabs value={tab} onChange={(_, v) => { setTab(v); setError('') }} centered>
            <Tab label="Login" />
            <Tab label="Register" />
          </Tabs>
          {error && <Alert severity="error" sx={{ mt: 2 }}>{error}</Alert>}
          <Box component="form" onSubmit={handleSubmit} sx={{ mt: 2 }}>
            {tab === 1 && (
              <TextField fullWidth label="Name" margin="normal" value={form.name}
                onChange={(e) => setForm({ ...form, name: e.target.value })} required />
            )}
            <TextField fullWidth label="Email" type="email" margin="normal" value={form.email}
              onChange={(e) => setForm({ ...form, email: e.target.value })} required />
            <TextField fullWidth label="Password" type="password" margin="normal" value={form.password}
              onChange={(e) => setForm({ ...form, password: e.target.value })} required />
            <Button fullWidth variant="contained" type="submit" sx={{ mt: 2 }} disabled={loading}>
              {loading ? 'Please wait...' : tab === 0 ? 'Login' : 'Register'}
            </Button>
          </Box>
        </CardContent>
      </Card>
    </Box>
  )
}
