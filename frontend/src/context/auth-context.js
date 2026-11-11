import { createContext } from 'react'

// Kept in its own file: Vite's Fast Refresh only works when a .jsx file exports components only.
// The value is provided by AuthProvider and read with the useAuth() hook.
export const AuthContext = createContext(null)
