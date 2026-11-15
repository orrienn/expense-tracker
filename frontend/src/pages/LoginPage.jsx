import { useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { getErrorMessage } from '../api/errors.js'
import Button from '../components/Button.jsx'
import ErrorAlert from '../components/ErrorAlert.jsx'
import FormField from '../components/FormField.jsx'
import { useAuth } from '../hooks/useAuth.js'
import { hasErrors, validateEmail } from '../utils/validation.js'
import styles from './AuthPage.module.css'

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
    <main className={styles.page}>
      <div className={styles.card}>
        <h1 className={styles.title}>Log in</h1>
        <p className={styles.subtitle}>Welcome back to Expense Tracker.</p>

        {serverError && (
          <ErrorAlert className={styles.alert}>{serverError}</ErrorAlert>
        )}

        {/* noValidate: use our own validation messages instead of the browser's built-in ones */}
        <form onSubmit={handleSubmit} noValidate className={styles.form}>
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
          <Button type="submit" disabled={submitting} fullWidth>
            {submitting ? 'Logging in…' : 'Log in'}
          </Button>
        </form>

        <p className={styles.footer}>
          Don&apos;t have an account?{' '}
          {/* Pass state on, so after registering the user still lands on the page they wanted */}
          <Link to="/register" state={location.state} className={styles.footerLink}>
            Sign up
          </Link>
        </p>
      </div>
    </main>
  )
}

export default LoginPage
