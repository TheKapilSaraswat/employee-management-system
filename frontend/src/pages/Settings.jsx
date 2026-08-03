import { useState } from 'react'
import { Box, Typography, TextField, Button, Card, CardContent, Alert, Snackbar } from '@mui/material'
import api from '../services/api'
import { useAuth } from '../context/AuthContext'

export default function Settings() {
  const { user } = useAuth()
  const [profile, setProfile] = useState({ name: user?.name || '', email: user?.email || '' })
  const [password, setPassword] = useState({ current: '', newPass: '', confirm: '' })
  const [snack, setSnack] = useState({ open: false, message: '', severity: 'success' })

  const updateProfile = async () => {
    try {
      await api.put('/auth/profile', profile)
      setSnack({ open: true, message: 'Profile updated', severity: 'success' })
    } catch (err) {
      setSnack({ open: true, message: err.response?.data?.message || 'Error', severity: 'error' })
    }
  }

  const changePassword = async () => {
    if (password.newPass !== password.confirm) {
      setSnack({ open: true, message: 'Passwords do not match', severity: 'error' })
      return
    }
    try {
      await api.put('/auth/password', { currentPassword: password.current, newPassword: password.newPass })
      setSnack({ open: true, message: 'Password changed', severity: 'success' })
      setPassword({ current: '', newPass: '', confirm: '' })
    } catch (err) {
      setSnack({ open: true, message: err.response?.data?.message || 'Error', severity: 'error' })
    }
  }

  return (
    <Box sx={{ maxWidth: 600 }}>
      <Typography variant="h4" gutterBottom>Settings</Typography>

      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Typography variant="h6" gutterBottom>Profile</Typography>
          <TextField fullWidth label="Name" margin="dense" value={profile.name}
            onChange={(e) => setProfile({ ...profile, name: e.target.value })} />
          <TextField fullWidth label="Email" margin="dense" value={profile.email} disabled />
          <TextField fullWidth label="Role" margin="dense" value={user?.role || ''} disabled />
          <Button variant="contained" sx={{ mt: 2 }} onClick={updateProfile}>Update Profile</Button>
        </CardContent>
      </Card>

      <Card>
        <CardContent>
          <Typography variant="h6" gutterBottom>Change Password</Typography>
          <TextField fullWidth type="password" label="Current Password" margin="dense" value={password.current}
            onChange={(e) => setPassword({ ...password, current: e.target.value })} />
          <TextField fullWidth type="password" label="New Password" margin="dense" value={password.newPass}
            onChange={(e) => setPassword({ ...password, newPass: e.target.value })} />
          <TextField fullWidth type="password" label="Confirm New Password" margin="dense" value={password.confirm}
            onChange={(e) => setPassword({ ...password, confirm: e.target.value })} />
          <Button variant="contained" sx={{ mt: 2 }} onClick={changePassword}>Change Password</Button>
        </CardContent>
      </Card>

      <Snackbar open={snack.open} autoHideDuration={4000} onClose={() => setSnack({ ...snack, open: false })}>
        <Alert severity={snack.severity}>{snack.message}</Alert>
      </Snackbar>
    </Box>
  )
}
