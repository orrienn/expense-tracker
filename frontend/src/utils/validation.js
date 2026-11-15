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

// Same limit as Category.NAME_MAX_LENGTH on the backend
export const CATEGORY_NAME_MAX_LENGTH = 50

export function validateCategoryName(name) {
  const trimmed = name.trim()
  if (!trimmed) return 'Name is required.'
  if (trimmed.length > CATEGORY_NAME_MAX_LENGTH) {
    return `Name must be at most ${CATEGORY_NAME_MAX_LENGTH} characters.`
  }
  return null
}

// Same limits as ExpenseRequest on the backend: @Positive, @Digits(integer = 10, fraction = 2)
const AMOUNT_MAX_INTEGER_DIGITS = 10
const AMOUNT_MAX_FRACTION_DIGITS = 2
export const EXPENSE_DESCRIPTION_MAX_LENGTH = 255

// "12,50" and "12.50" both mean the same (Polish keyboards use a comma)
export function normalizeAmount(text) {
  return text.trim().replace(',', '.')
}

export function validateAmount(text) {
  const amount = normalizeAmount(text)
  if (!amount) return 'Amount is required.'
  if (!/^\d+(\.\d+)?$/.test(amount)) return 'Enter a valid amount, e.g. 12.50.'

  const [integerPart, fractionPart = ''] = amount.split('.')
  if (fractionPart.length > AMOUNT_MAX_FRACTION_DIGITS) return 'Use at most 2 decimal places.'
  // Leading zeros don't count as digits ("007" is 7)
  if (integerPart.replace(/^0+/, '').length > AMOUNT_MAX_INTEGER_DIGITS) return 'Amount is too large.'
  if (Number(amount) <= 0) return 'Amount must be greater than 0.'
  return null
}

// date: "YYYY-MM-DD" from <input type="date"> ("" when empty or incomplete)
export function validateExpenseDate(date, today) {
  if (!date) return 'Date is required.'
  // ISO dates compare correctly as strings
  if (date > today) return 'Date cannot be in the future.'
  return null
}

export function validateDescription(description) {
  if (description.trim().length > EXPENSE_DESCRIPTION_MAX_LENGTH) {
    return `Description must be at most ${EXPENSE_DESCRIPTION_MAX_LENGTH} characters.`
  }
  return null
}

export function hasErrors(errors) {
  return Object.values(errors).some(Boolean)
}
