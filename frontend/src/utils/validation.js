// Client-side validation: gives instant feedback, but the backend still validates everything.
// Each function returns an error message, or null when the value is valid.

// Simple check: something@something.something, no spaces
const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

// Same limits as RegisterRequest on the backend
export const PASSWORD_MIN_LENGTH = 8
export const PASSWORD_MAX_LENGTH = 72
const PASSWORD_MAX_BYTES = 72

export function validateEmail(email) {
  const trimmed = email.trim()
  if (!trimmed) return 'Email is required.'
  if (!EMAIL_PATTERN.test(trimmed)) return 'Enter a valid email address.'
  return null
}

export function validateNewPassword(password) {
  if (!password) return 'Password is required.'
  if (password.length < PASSWORD_MIN_LENGTH) return `Password must be at least ${PASSWORD_MIN_LENGTH} characters.`
  if (password.length > PASSWORD_MAX_LENGTH) return `Password must be at most ${PASSWORD_MAX_LENGTH} characters.`
  // BCrypt's limit is 72 bytes, not characters: letters like "ą" take 2 bytes in UTF-8
  if (new TextEncoder().encode(password).length > PASSWORD_MAX_BYTES) return 'Password is too long.'
  return null
}

export function hasErrors(errors) {
  return Object.values(errors).some(Boolean)
}
