import { useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { getErrorMessage } from '../api/errors.js'
import FormField from '../components/FormField.jsx'
import { useAuth } from '../hooks/useAuth.js'
import { hasErrors, validateEmail } from '../utils/validation.js'

function LoginPage() {
  const { login } = useAuth()
  const location = useLocation()

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [errors, setErrors] = useState({})
  const [serverError, setServerError] = useState(null)
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(event) {
    event.preventDefault() // stop the browser from reloading the page on submit

    const newErrors = {
      email: validateEmail(email),
      // No length rules on login: we only check whether the password matches
      password: password ? null : 'Password is required.',
    }
    setErrors(newErrors)
    if (hasErrors(newErrors)) return

    setServerError(null)
    setSubmitting(true)
    try {
      await login(email.trim(), password)
      // Success: no navigate() here - GuestRoute sees we're logged in and redirects
    } catch (error) {
      setServerError(getErrorMessage(error, { 401: 'Invalid email or password.' }))
      setSubmitting(false)
    }
  }

  return (
    <main className="flex min-h-screen items-center justify-center bg-slate-100 px-4">
      <div className="w-full max-w-sm rounded-xl bg-white p-8 shadow-md">
        <h1 className="text-2xl font-bold text-slate-800">Log in</h1>
        <p className="mt-1 text-sm text-slate-500">Welcome back to Expense Tracker.</p>

        {serverError && (
          <p role="alert" className="mt-4 rounded-md bg-red-50 px-3 py-2 text-sm text-red-700">
            {serverError}
          </p>
        )}

        {/* noValidate: use our own validation messages instead of the browser's built-in ones */}
        <form onSubmit={handleSubmit} noValidate className="mt-6 space-y-4">
          <FormField
            id="email"
            label="Email"
            type="email"
            autoComplete="email"
            value={email}
            onChange={(value) => {
              setEmail(value)
              setErrors((prev) => ({ ...prev, email: null }))
            }}
            error={errors.email}
          />
          <FormField
            id="password"
            label="Password"
            type="password"
            autoComplete="current-password"
            value={password}
            onChange={(value) => {
              setPassword(value)
              setErrors((prev) => ({ ...prev, password: null }))
            }}
            error={errors.password}
          />
          <button
            type="submit"
            disabled={submitting}
            className="w-full rounded-md bg-blue-600 px-4 py-2 font-medium text-white hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-60"
          >
            {submitting ? 'Logging in…' : 'Log in'}
          </button>
        </form>

        <p className="mt-6 text-center text-sm text-slate-600">
          Don&apos;t have an account?{' '}
          {/* Pass state on, so after registering the user still lands on the page they wanted */}
          <Link to="/register" state={location.state} className="font-medium text-blue-600 hover:underline">
            Sign up
          </Link>
        </p>
      </div>
    </main>
  )
}

export default LoginPage
