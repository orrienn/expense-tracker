import { useState } from 'react'
import { CATEGORY_ERROR_MESSAGES } from '../api/categories.js'
import { getErrorMessage } from '../api/errors.js'
import { validateCategoryName } from '../utils/validation.js'
import Button from './Button.jsx'
import styles from './CategoryRow.module.css'
// Reuse the input/error look of FormField, so all inputs in the app look the same
import fieldStyles from './FormField.module.css'

// One category on the list. "Edit" turns the row into an inline form (Enter saves, Escape cancels).
// onRename(id, name) and onDelete(id) come from the page and throw on failure.
function CategoryRow({ category, onRename, onDelete }) {
  const [editing, setEditing] = useState(false)
  const [name, setName] = useState(category.name)
  const [error, setError] = useState(null)
  const [busy, setBusy] = useState(false)

  const inputId = `category-${category.id}-name`
  const errorId = `category-${category.id}-error`

  function startEditing() {
    setName(category.name)
    setError(null)
    setEditing(true)
  }

  function cancelEditing() {
    setEditing(false)
    setError(null)
  }

  async function handleSave(event) {
    event.preventDefault()

    const validationError = validateCategoryName(name)
    if (validationError) {
      setError(validationError)
      return
    }
    // Nothing changed: no need to call the server
    if (name.trim() === category.name) {
      setEditing(false)
      return
    }

    setBusy(true)
    try {
      await onRename(category.id, name.trim())
      setEditing(false)
    } catch (err) {
      setError(getErrorMessage(err, CATEGORY_ERROR_MESSAGES))
    } finally {
      setBusy(false)
    }
  }

  async function handleDelete() {
    // Built-in browser dialog; returns false when the user clicks "Cancel"
    const confirmed = window.confirm(`Delete "${category.name}"? Its expenses will become Uncategorized.`)
    if (!confirmed) return

    setError(null)
    setBusy(true)
    try {
      await onDelete(category.id)
      // Success: the row disappears from the list, so there's no state left to update
    } catch (err) {
      setError(getErrorMessage(err))
      setBusy(false)
    }
  }

  return (
    <li className={styles.row}>
      {editing ? (
        <form onSubmit={handleSave} noValidate className={styles.editForm}>
          <input
            id={inputId}
            aria-label="Category name"
            value={name}
            onChange={(event) => {
              setName(event.target.value)
              setError(null)
            }}
            onKeyDown={(event) => {
              if (event.key === 'Escape') cancelEditing()
            }}
            aria-invalid={error ? 'true' : 'false'}
            aria-describedby={error ? errorId : undefined}
            autoComplete="off"
            autoFocus
            className={`${fieldStyles.input} ${styles.input}`}
          />
          <Button type="submit" disabled={busy}>
            {busy ? 'Saving…' : 'Save'}
          </Button>
          <Button variant="secondary" onClick={cancelEditing} disabled={busy}>
            Cancel
          </Button>
        </form>
      ) : (
        <>
          <span className={styles.name}>{category.name}</span>
          <div className={styles.actions}>
            <Button variant="secondary" onClick={startEditing} disabled={busy}>
              Edit
            </Button>
            <Button variant="danger" onClick={handleDelete} disabled={busy}>
              {busy ? 'Deleting…' : 'Delete'}
            </Button>
          </div>
        </>
      )}

      {error && (
        <p id={errorId} className={`${fieldStyles.error} ${styles.error}`}>
          {error}
        </p>
      )}
    </li>
  )
}

export default CategoryRow
