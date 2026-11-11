import { useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { getErrorMessage } from '../api/errors.js'
import FormField from '../components/FormField.jsx'
import { useAuth } from '../hooks/useAuth.js'
import { hasErrors, PASSWORD_MIN_LENGTH, validateEmail, validateNewPassword } from '../utils/validation.js'

function RegisterPage() {
  const { register } = useAuth()
  const location = useLocation()

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [errors, setErrors] = useState({})
  const [serverError, setServerError] = useState(null)
  const [submitting, setSubmitting] = useState(false)

  // Updates a field and clears its error, so the message disappears as soon as the user starts fixing it
  function updateField(setter, field) {
    return (value) => {
      setter(value)
      setErrors((prev) => ({ ...prev, [field]: null }))
    }
  }

  async function handleSubmit(event) {
    event.preventDefault()

    const newErrors = {
      email: validateEmail(email),
      password: validateNewPassword(password),
      confirmPassword: confirmPassword === password ? null : 'Passwords do not match.',
    }
    setErrors(newErrors)
    if (hasErrors(newErrors)) return

    setServerError(null)
    setSubmitting(true)
    try {
      // Creates the account and logs in; GuestRoute then redirects to the dashboard
      await register(email.trim(), password)
    } catch (error) {
      setServerError(
        getErrorMessage(error, {
          409: 'An account with this email already exists.',
          400: 'Please check the form and try again.',
        }),
      )
      setSubmitting(false)
    }
  }

  return (
    <main className="flex min-h-screen items-center justify-center bg-slate-100 px-4">
      <div className="w-full max-w-sm rounded-xl bg-white p-8 shadow-md">
        <h1 className="text-2xl font-bold text-slate-800">Create an account</h1>
        <p className="mt-1 text-sm text-slate-500">Start tracking your expenses.</p>

        {serverError && (
          <p role="alert" className="mt-4 rounded-md bg-red-50 px-3 py-2 text-sm text-red-700">
            {serverError}
          </p>
        )}

        <form onSubmit={handleSubmit} noValidate className="mt-6 space-y-4">
          <FormField
            id="email"
            label="Email"
            type="email"
            autoComplete="email"
            value={email}
            onChange={updateField(setEmail, 'email')}
            error={errors.email}
          />
          <FormField
            id="password"
            label={`Password (min. ${PASSWORD_MIN_LENGTH} characters)`}
            type="password"
            autoComplete="new-password"
            value={password}
            onChange={updateField(setPassword, 'password')}
            error={errors.password}
          />
          <FormField
            id="confirmPassword"
            label="Confirm password"
            type="password"
            autoComplete="new-password"
            value={confirmPassword}
            onChange={updateField(setConfirmPassword, 'confirmPassword')}
            error={errors.confirmPassword}
          />
          <button
            type="submit"
            disabled={submitting}
            className="w-full rounded-md bg-blue-600 px-4 py-2 font-medium text-white hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-60"
          >
            {submitting ? 'Creating account…' : 'Sign up'}
          </button>
        </form>

        <p className="mt-6 text-center text-sm text-slate-600">
          Already have an account?{' '}
          <Link to="/login" state={location.state} className="font-medium text-blue-600 hover:underline">
            Log in
          </Link>
        </p>
      </div>
    </main>
  )
}

export default RegisterPage
