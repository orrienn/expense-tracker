import { useCallback, useEffect, useState } from 'react'
import * as categoriesApi from '../api/categories.js'

// Same order as the backend (by name), also after adding/renaming locally
function sortByName(categories) {
  return [...categories].sort((a, b) => a.name.localeCompare(b.name))
}

// Loads the logged-in user's categories and offers operations that keep the list in sync.
// add/rename/remove throw on failure (e.g. 409 duplicate), so the calling form can show the error.
export function useCategories() {
  const [categories, setCategories] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  // Changing this number re-runs the effect below, i.e. loads the list again
  const [reloadKey, setReloadKey] = useState(0)

  useEffect(() => {
    // If the component disappears (or the effect re-runs) before the response arrives,
    // "ignore" makes us drop that stale response instead of setting state with it.
    let ignore = false

    categoriesApi
      .getCategories()
      .then((data) => {
        if (!ignore) setCategories(sortByName(data))
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

  // Called from event handlers (e.g. "Try again"), so setting state here is fine
  const reload = useCallback(() => {
    setLoading(true)
    setError(null)
    setReloadKey((key) => key + 1)
  }, [])

  // The list is updated from the server's response, without fetching everything again.
  // State is never mutated: we always build a new array (that's how React notices the change).
  const add = useCallback(async (name) => {
    const created = await categoriesApi.createCategory(name)
    setCategories((prev) => sortByName([...prev, created]))
  }, [])

  const rename = useCallback(async (id, name) => {
    const updated = await categoriesApi.updateCategory(id, name)
    setCategories((prev) => sortByName(prev.map((category) => (category.id === id ? updated : category))))
  }, [])

  const remove = useCallback(async (id) => {
    await categoriesApi.deleteCategory(id)
    setCategories((prev) => prev.filter((category) => category.id !== id))
  }, [])

  return { categories, loading, error, reload, add, rename, remove }
}
