import { useCallback, useEffect, useState } from 'react'
import * as expensesApi from '../api/expenses.js'

// Same order as the backend: newest date first; for the same date the later-added (higher id) first.
// ISO dates ("2026-11-15") compare correctly as plain strings.
function sortNewestFirst(expenses) {
  return [...expenses].sort((a, b) => b.date.localeCompare(a.date) || b.id - a.id)
}

// Same pattern as useCategories (see the comments there).
// In Stage 3 the list moves to server-side filtering and paging, so this hook will change.
export function useExpenses() {
  const [expenses, setExpenses] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [reloadKey, setReloadKey] = useState(0)

  useEffect(() => {
    let ignore = false

    expensesApi
      .getExpenses()
      .then((data) => {
        if (!ignore) setExpenses(sortNewestFirst(data))
      })
      .catch((err) => {
        if (!ignore) setError(err)
      })
      .finally(() => {
        if (!ignore) setLoading(false)
      })

    return () => {
      ignore = true
    }
  }, [reloadKey])

  const reload = useCallback(() => {
    setLoading(true)
    setError(null)
    setReloadKey((key) => key + 1)
  }, [])

  const add = useCallback(async (expense) => {
    const created = await expensesApi.createExpense(expense)
    setExpenses((prev) => sortNewestFirst([...prev, created]))
  }, [])

  const update = useCallback(async (id, expense) => {
    const updated = await expensesApi.updateExpense(id, expense)
    setExpenses((prev) => sortNewestFirst(prev.map((item) => (item.id === id ? updated : item))))
  }, [])

  const remove = useCallback(async (id) => {
    await expensesApi.deleteExpense(id)
    setExpenses((prev) => prev.filter((item) => item.id !== id))
  }, [])

  return { expenses, loading, error, reload, add, update, remove }
}
