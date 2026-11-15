import { useState } from 'react'
import { CATEGORY_ERROR_MESSAGES } from '../api/categories.js'
import { getErrorMessage } from '../api/errors.js'
import { validateCategoryName } from '../utils/validation.js'
import Button from './Button.jsx'
import FormField from './FormField.jsx'

// "New category" input with an Add button. onAdd(name) comes from useCategories and throws on failure.
function AddCategoryForm({ onAdd }) {
  const [name, setName] = useState('')
  const [error, setError] = useState(null)
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(event) {
    event.preventDefault()

    const validationError = validateCategoryName(name)
    if (validationError) {
      setError(validationError)
      return
    }

    setSubmitting(true)
    try {
      await onAdd(name.trim())
      setName('') // ready for the next one
    } catch (err) {
      setError(getErrorMessage(err, CATEGORY_ERROR_MESSAGES))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <form onSubmit={handleSubmit} noValidate>
      <FormField
        id="new-category"
        label="New category"
        value={name}
        onChange={(value) => {
          setName(value)
          setError(null)
        }}
        error={error}
        autoComplete="off"
      >
        <Button type="submit" disabled={submitting}>
          {submitting ? 'Adding…' : 'Add'}
        </Button>
      </FormField>
    </form>
  )
}

export default AddCategoryForm
