import { useState, useEffect } from 'react'
import { Grid, Card, CardContent, Typography, CircularProgress, Alert } from '@mui/material'
import api from '../services/api'

export default function Dashboard() {
  const [stats, setStats] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const [empRes, deptRes, leaveRes, attRes, payRes] = await Promise.all([
          api.get('/employees?limit=1'),
          api.get('/departments'),
          api.get('/leaves?status=pending'),
          api.get('/attendance/today'),
          api.get('/payroll/summary'),
        ])
        setStats({
          totalEmployees: empRes.data.total,
          departments: deptRes.data.length,
          pendingLeaves: leaveRes.data.length,
          todayAttendance: attRes.data.filter((a) => a.check_in).length,
          paidCount: payRes.data.paidCount,
          pendingPayroll: payRes.data.pendingCount,
        })
      } catch (err) {
        console.error(err)
      } finally {
        setLoading(false)
      }
    }
    fetchStats()
  }, [])

  if (loading) return <CircularProgress />

  if (!stats) {
    return (
      <>
        <Typography variant="h4" gutterBottom>Dashboard</Typography>
        <Alert severity="error">Failed to load dashboard data. Please try again later.</Alert>
      </>
    )
  }

  const cards = [
    { title: 'Total Employees', value: stats.totalEmployees, color: '#1976d2' },
    { title: 'Departments', value: stats.departments, color: '#388e3c' },
    { title: 'Pending Leaves', value: stats.pendingLeaves, color: '#f57c00' },
    { title: "Today's Check-ins", value: stats.todayAttendance, color: '#7b1fa2' },
    { title: 'Paid Payrolls', value: stats.paidCount, color: '#00796b' },
    { title: 'Pending Payrolls', value: stats.pendingPayroll, color: '#c62828' },
  ]

  return (
    <>
      <Typography variant="h4" gutterBottom>Dashboard</Typography>
      <Grid container spacing={3}>
        {cards.map((card) => (
          <Grid size={{ xs: 12, sm: 6, md: 4 }} key={card.title}>
            <Card sx={{ borderLeft: 4, borderColor: card.color }}>
              <CardContent>
                <Typography variant="body2" color="text.secondary">{card.title}</Typography>
                <Typography variant="h3">{card.value}</Typography>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
    </>
  )
}
