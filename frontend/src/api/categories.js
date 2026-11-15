import client from './client.js'

// Category endpoints. The token is attached by the client.js interceptor;
// the backend only ever returns/changes the logged-in user's categories.
// Category shape: { id, name }

// What the categories API's error statuses mean to the user (for getErrorMessage in errors.js)
export const CATEGORY_ERROR_MESSAGES = {
  400: 'Please check the name and try again.',
  409: 'A category with this name already exists.',
}

export async function getCategories() {
  const { data } = await client.get('/categories')
  return data
}

export async function createCategory(name) {
  const { data } = await client.post('/categories', { name })
  return data
}

export async function updateCategory(id, name) {
  const { data } = await client.put(`/categories/${id}`, { name })
  return data
}

// 204 No Content: nothing to return
export async function deleteCategory(id) {
  await client.delete(`/categories/${id}`)
}
