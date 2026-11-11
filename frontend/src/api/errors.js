// Turns an axios error into a message for the user.
// messagesByStatus lets each form say what a given HTTP status means in its context, e.g. { 409: 'Email taken' }.
export function getErrorMessage(error, messagesByStatus = {}) {
  if (!error.response) {
    return "Can't reach the server. Check your connection and try again."
  }
  return messagesByStatus[error.response.status] ?? 'Something went wrong. Please try again.'
}
