import axios from 'axios'

// One shared axios instance for the whole app. "/api" is forwarded to the backend by the Vite proxy.
const client = axios.create({ baseURL: '/api' })

// The token lives only in memory (never localStorage), so a script injected into the page can't read it from storage.
// AuthProvider keeps it in sync via setAuthToken.
let authToken = null
let unauthorizedHandler = null

export function setAuthToken(token) {
  authToken = token
}

export function setUnauthorizedHandler(handler) {
  unauthorizedHandler = handler
}

// Request interceptor: runs before every request and attaches the token
client.interceptors.request.use((config) => {
  if (authToken) {
    config.headers.Authorization = `Bearer ${authToken}`
  }
  return config
})

// Response interceptor: a 401 on a request that carried a token means the token expired or is invalid,
// so we log out. (A 401 from the login form itself has no token and is handled by the form.)
client.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 && authToken && unauthorizedHandler) {
      unauthorizedHandler()
    }
    return Promise.reject(error)
  },
)

export default client
