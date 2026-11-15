import { useState } from 'react'
import { Link } from 'react-router-dom'
import { getErrorMessage, getFieldErrors } from '../api/errors.js'
import { todayISO } from '../utils/dates.js'
import {
  hasErrors,
  normalizeAmount,
  validateAmount,
  validateDescription,
  validateExpenseDate,
} from '../utils/validation.js'
import Button from './Button.jsx'
import ErrorAlert from './ErrorAlert.jsx'
import styles from './ExpenseForm.module.css'
import FormField from './FormField.jsx'
import SelectField from './SelectField.jsx'

// Backend field names -> our form field names (only categoryId differs)
const SERVER_FIELD_NAMES = { amount: 'amount', date: 'date', description: 'description', categoryId: 'category' }

// Add/edit form for one expense. expense = null means "add".
// onSubmit(payload) comes from the page and throws on failure; on success the page closes the modal.
function ExpenseForm({ expense, categories, categoriesError, onSubmit, onCancel }) {
  const today = todayISO()

  // Form fields are always strings (that's what inputs work with); converted when submitting
  const [amount, setAmount] = useState(expense ? expense.amount.toFixed(2) : '')
  const [date, setDate] = useState(expense ? expense.date : today)
  const [category, setCategory] = useState(expense?.category ? String(expense.category.id) : '')
  const [description, setDescription] = useState(expense?.description ?? '')
  const [errors, setErrors] = useState({})
  const [serverError, setServerError] = useState(null)
  const [submitting, setSubmitting] = useState(false)

  function updateField(setter, field) {
    return (value) => {
      setter(value)
      setErrors((prev) => ({ ...prev, [field]: null }))
    }
  }

  async function handleSubmit(event) {
    event.preventDefault()

    const newErrors = {
      amount: validateAmount(amount),
      date: validateExpenseDate(date, today),
      description: validateDescription(description),
    }
    setErrors(newErrors)
    if (hasErrors(newErrors)) return

    setServerError(null)
    setSubmitting(true)
    try {
      await onSubmit({
        // Number is exact enough here: at most 12 significant digits (10 + 2), doubles keep ~15
        amount: Number(normalizeAmount(amount)),
        date,
        categoryId: category ? Number(category) : null,
        description: description.trim() || null,
      })
    } catch (err) {
      // 400 from the backend: show its messages under the matching fields
      const fieldErrors = getFieldErrors(err)
      const mapped = Object.fromEntries(
        Object.entries(fieldErrors).map(([field, message]) => [SERVER_FIELD_NAMES[field] ?? field, message]),
      )
      if (Object.keys(mapped).length > 0) {
        setErrors(mapped)
      } else {
        setServerError(getErrorMessage(err))
      }
      setSubmitting(false)
    }
  }

  let categoryHint = null
  if (categoriesError) {
    categoryHint = "Couldn't load categories. You can still save the expense as Uncategorized."
  } else if (categories.length === 0) {
    categoryHint = (
      <>
        No categories yet. <Link to="/categories">Create one</Link>
      </>
    )
  }

  return (
    <form onSubmit={handleSubmit} noValidate className={styles.form}>
      {serverError && <ErrorAlert>{serverError}</ErrorAlert>}

      <div className={styles.pair}>
        <FormField
          id="expense-amount"
          label="Amount"
          value={amount}
          onChange={updateField(setAmount, 'amount')}
          error={errors.amount}
          autoComplete="off"
          // Text instead of type="number": accepts "12,50", and the browser doesn't change the value on scroll.
          // inputMode still shows a numeric keyboard on phones.
          inputMode="decimal"
          placeholder="0.00"
          autoFocus
        />
        <FormField
          id="expense-date"
          label="Date"
          type="date"
          value={date}
          onChange={updateField(setDate, 'date')}
          error={errors.date}
          max={today}
        />
      </div>

      <SelectField
        id="expense-category"
        label="Category"
        value={category}
        onChange={updateField(setCategory, 'category')}
        error={errors.category}
        hint={categoryHint}
      >
        <option value="">Uncategorized</option>
        {categories.map((item) => (
          <option key={item.id} value={item.id}>
            {item.name}
          </option>
        ))}
      </SelectField>

      <FormField
        id="expense-description"
        label="Description (optional)"
        value={description}
        onChange={updateField(setDescription, 'description')}
        error={errors.description}
        autoComplete="off"
        placeholder="e.g. Lunch with friends"
      />

      <div className={styles.actions}>
        <Button variant="secondary" onClick={onCancel} disabled={submitting}>
          Cancel
        </Button>
        <Button type="submit" disabled={submitting}>
          {submitting ? 'Saving…' : expense ? 'Save changes' : 'Add expense'}
        </Button>
      </div>
    </form>
  )
}

export default ExpenseForm
