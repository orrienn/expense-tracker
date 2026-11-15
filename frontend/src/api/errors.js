// Turns an axios error into a message for the user.
// messagesByStatus lets each form say what a given HTTP status means in its context, e.g. { 409: 'Email taken' }.
export function getErrorMessage(error, messagesByStatus = {}) {
  if (!error.response) {
    return "Can't reach the server. Check your connection and try again."
  }
  return messagesByStatus[error.response.status] ?? 'Something went wrong. Please try again.'
}

// Validation errors from the backend (400 ProblemDetail with an "errors" map, see GlobalExceptionHandler),
// e.g. { amount: 'must be greater than 0' }. Empty object for any other error.
export function getFieldErrors(error) {
  if (error.response?.status !== 400) return {}
  return error.response.data?.errors ?? {}
}
