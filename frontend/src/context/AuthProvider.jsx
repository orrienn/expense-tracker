import { useCallback, useEffect, useMemo, useState } from 'react'
import * as authApi from '../api/auth.js'
import { setAuthToken, setUnauthorizedHandler } from '../api/client.js'
import { AuthContext } from './auth-context.js'

// Holds the logged-in state for the whole app.
// Token in memory only: refreshing the page logs the user out (see docs/PLAN.md).
export function AuthProvider({ children }) {
  const [token, setToken] = useState(null)
  const [user, setUser] = useState(null)

  const logout = useCallback(() => {
    setAuthToken(null)
    setToken(null)
    setUser(null)
  }, [])

  // When any API call gets a 401 (e.g. expired token), log out. ProtectedRoute then redirects to /login.
  useEffect(() => {
    setUnauthorizedHandler(logout)
    return () => setUnauthorizedHandler(null)
  }, [logout])

  const login = useCallback(async (email, password) => {
    const { accessToken } = await authApi.login(email, password)
    // Give the token to axios first, so the /users/me request below is authenticated
    setAuthToken(accessToken)
    try {
      const me = await authApi.getMe()
      setToken(accessToken)
      setUser(me)
    } catch (error) {
      setAuthToken(null)
      throw error
    }
  }, [])

  // Better UX: after creating the account, log in right away
  const register = useCallback(async (email, password) => {
    await authApi.register(email, password)
    await login(email, password)
  }, [login])

  // useMemo: components using the context re-render only when these values actually change
  const value = useMemo(
    () => ({ user, isAuthenticated: token !== null, login, register, logout }),
    [user, token, login, register, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
