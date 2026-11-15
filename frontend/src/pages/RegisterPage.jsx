import { useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { getErrorMessage } from '../api/errors.js'
import Button from '../components/Button.jsx'
import ErrorAlert from '../components/ErrorAlert.jsx'
import FormField from '../components/FormField.jsx'
import { useAuth } from '../hooks/useAuth.js'
import { hasErrors, PASSWORD_MIN_LENGTH, validateEmail, validateNewPassword } from '../utils/validation.js'
import styles from './AuthPage.module.css'

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
    <main className={styles.page}>
      <div className={styles.card}>
        <h1 className={styles.title}>Create an account</h1>
        <p className={styles.subtitle}>Start tracking your expenses.</p>

        {serverError && (
          <ErrorAlert className={styles.alert}>{serverError}</ErrorAlert>
        )}

        <form onSubmit={handleSubmit} noValidate className={styles.form}>
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
          <Button type="submit" disabled={submitting} fullWidth>
            {submitting ? 'Creating account…' : 'Sign up'}
          </Button>
        </form>

        <p className={styles.footer}>
          Already have an account?{' '}
          <Link to="/login" state={location.state} className={styles.footerLink}>
            Log in
          </Link>
        </p>
      </div>
    </main>
  )
}

export default RegisterPage
