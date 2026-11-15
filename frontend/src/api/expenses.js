import client from './client.js'

// Expense endpoints; like categories, the backend only returns/changes the logged-in user's expenses.
// Expense shape: { id, amount, description, date: "2026-11-15", category: { id, name } | null, createdAt }
// Request body: { amount, description, date, categoryId }

export async function getExpenses() {
  const { data } = await client.get('/expenses')
  return data
}

export async function createExpense(expense) {
  const { data } = await client.post('/expenses', expense)
  return data
}

export async function updateExpense(id, expense) {
  const { data } = await client.put(`/expenses/${id}`, expense)
  return data
}

// 204 No Content: nothing to return
export async function deleteExpense(id) {
  await client.delete(`/expenses/${id}`)
}
