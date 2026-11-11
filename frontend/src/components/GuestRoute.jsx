import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth.js'

// The opposite of ProtectedRoute: /login and /register are only for logged-out users.
// This is also what redirects right after a successful login/registration:
// back to the page the user originally wanted (state.from), or to /dashboard.
function GuestRoute() {
  const { isAuthenticated } = useAuth()
  const location = useLocation()

  if (isAuthenticated) {
    return <Navigate to={location.state?.from ?? '/dashboard'} replace />
  }
  return <Outlet />
}

export default GuestRoute
