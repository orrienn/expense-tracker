import client from './client.js'

// Each function returns the response body (axios puts it in "data")

export async function register(email, password) {
  const { data } = await client.post('/auth/register', { email, password })
  return data // { id, email, createdAt }
}

export async function login(email, password) {
  const { data } = await client.post('/auth/login', { email, password })
  return data // { accessToken, tokenType, expiresAt }
}

export async function getMe() {
  const { data } = await client.get('/users/me')
  return data // { id, email, createdAt }
}
