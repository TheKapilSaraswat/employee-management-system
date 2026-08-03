import { Outlet, NavLink, useNavigate } from 'react-router-dom'
import { AppBar, Toolbar, Typography, Drawer, List, ListItem, ListItemButton, ListItemText, Box, IconButton, Menu, MenuItem, Toolbar as Spacer } from '@mui/material'
import { useState } from 'react'
import { useAuth } from '../context/AuthContext'

const drawerWidth = 240
const navItems = [
  { label: 'Dashboard', path: '/' },
  { label: 'Employees', path: '/employees' },
  { label: 'Departments', path: '/departments' },
  { label: 'Attendance', path: '/attendance' },
  { label: 'Leaves', path: '/leave' },
  { label: 'Payroll', path: '/payroll' },
  { label: 'Reports', path: '/reports' },
  { label: 'Settings', path: '/settings' },
]

export default function Layout() {
  const { user, logout } = useAuth()
  const [anchorEl, setAnchorEl] = useState(null)
  const navigate = useNavigate()

  return (
    <Box sx={{ display: 'flex' }}>
      <AppBar position="fixed" sx={{ zIndex: (theme) => theme.zIndex.drawer + 1 }}>
        <Toolbar>
          <Typography variant="h6" noWrap sx={{ flexGrow: 1 }}>Employee Management System</Typography>
          <Typography variant="body2" sx={{ mr: 2 }}>{user?.name}</Typography>
          <IconButton color="inherit" onClick={(e) => setAnchorEl(e.currentTarget)}>
            <Typography variant="body2" sx={{ cursor: 'pointer' }}>▼</Typography>
          </IconButton>
          <Menu anchorEl={anchorEl} open={Boolean(anchorEl)} onClose={() => setAnchorEl(null)}>
            <MenuItem onClick={() => { setAnchorEl(null); navigate('/settings') }}>Settings</MenuItem>
            <MenuItem onClick={() => { setAnchorEl(null); logout() }}>Logout</MenuItem>
          </Menu>
        </Toolbar>
      </AppBar>
      <Drawer variant="permanent" sx={{ width: drawerWidth, flexShrink: 0, '& .MuiDrawer-paper': { width: drawerWidth, boxSizing: 'border-box' } }}>
        <Spacer />
        <List>
          {navItems.map((item) => (
            <ListItem key={item.path} disablePadding>
              <ListItemButton
                component={NavLink}
                to={item.path}
                sx={{ '&.active': { bgcolor: 'action.selected', fontWeight: 'bold' } }}
                end={item.path === '/'}
              >
                <ListItemText primary={item.label} />
              </ListItemButton>
            </ListItem>
          ))}
        </List>
      </Drawer>
      <Box component="main" sx={{ flexGrow: 1, p: 3, minHeight: '100vh' }}>
        <Spacer />
        <Outlet />
      </Box>
    </Box>
  )
}
