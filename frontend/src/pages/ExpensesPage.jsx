import { useState } from 'react'
import { getErrorMessage } from '../api/errors.js'
import Button from '../components/Button.jsx'
import ErrorAlert from '../components/ErrorAlert.jsx'
import ExpenseForm from '../components/ExpenseForm.jsx'
import ExpenseTable from '../components/ExpenseTable.jsx'
import Modal from '../components/Modal.jsx'
import { useCategories } from '../hooks/useCategories.js'
import { useExpenses } from '../hooks/useExpenses.js'
import { formatAmount, formatDate } from '../utils/format.js'
import styles from './ExpensesPage.module.css'

const MISSING_MESSAGE = 'This expense or its category no longer exists. The list has been refreshed.'

function ExpensesPage() {
  const { expenses, loading, error, reload, add, update, remove } = useExpenses()
  // Categories for the <select> in the form
  const categoriesState = useCategories()

  // null = modal closed; { expense: null } = adding; { expense } = editing that expense
  const [modal, setModal] = useState(null)
  const [deletingId, setDeletingId] = useState(null)
  // Page-level message, e.g. when an expense was already deleted in another tab
  const [notice, setNotice] = useState(null)

  // 404: the expense or the chosen category was deleted elsewhere -> refresh both lists
  function handleMissing() {
    setNotice(MISSING_MESSAGE)
    reload()
    categoriesState.reload()
  }

  async function handleSubmit(payload) {
    try {
      if (modal.expense) {
        await update(modal.expense.id, payload)
      } else {
        await add(payload)
      }
      setModal(null)
    } catch (err) {
      if (err.response?.status === 404) {
        setModal(null)
        handleMissing()
        return
      }
      throw err // other errors are shown inside the form
    }
  }

  async function handleDelete(expense) {
    const what = expense.description ? ` "${expense.description}"` : ''
    const confirmed = window.confirm(
      `Delete ${formatAmount(expense.amount)}${what} from ${formatDate(expense.date)}?`,
    )
    if (!confirmed) return

    setNotice(null)
    setDeletingId(expense.id)
    try {
      await remove(expense.id)
    } catch (err) {
      if (err.response?.status === 404) {
        handleMissing()
      } else {
        setNotice(`Couldn't delete the expense. ${getErrorMessage(err)}`)
      }
    } finally {
      setDeletingId(null)
    }
  }

  function renderContent() {
    if (loading) {
      return <p className={styles.status}>Loading expenses…</p>
    }

    if (error) {
      return (
        <div className={styles.loadError}>
          <ErrorAlert>Couldn&apos;t load expenses. {getErrorMessage(error)}</ErrorAlert>
          <Button variant="secondary" onClick={reload}>
            Try again
          </Button>
        </div>
      )
    }

    return (
      <div className={styles.card}>
        {expenses.length === 0 ? (
          <p className={styles.empty}>No expenses yet. Click &quot;Add expense&quot; to record your first one.</p>
        ) : (
          <ExpenseTable
            expenses={expenses}
            onEdit={(expense) => {
              setNotice(null)
              setModal({ expense })
            }}
            onDelete={handleDelete}
            deletingId={deletingId}
          />
        )}
      </div>
    )
  }

  return (
    <section>
      <div className={styles.header}>
        <div>
          <h1 className={styles.title}>Expenses</h1>
          <p className={styles.subtitle}>Everything you&apos;ve spent, newest first.</p>
        </div>
        <Button
          onClick={() => {
            setNotice(null)
            setModal({ expense: null })
          }}
        >
          Add expense
        </Button>
      </div>

      {notice && <ErrorAlert className={styles.alert}>{notice}</ErrorAlert>}
      {renderContent()}

      <Modal open={modal !== null} title={modal?.expense ? 'Edit expense' : 'Add expense'} onClose={() => setModal(null)}>
        <ExpenseForm
          expense={modal?.expense ?? null}
          categories={categoriesState.categories}
          categoriesError={categoriesState.error}
          onSubmit={handleSubmit}
          onCancel={() => setModal(null)}
        />
      </Modal>
    </section>
  )
}

export default ExpensesPage
