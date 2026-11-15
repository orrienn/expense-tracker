import { formatAmount, formatDate } from '../utils/format.js'
import Button from './Button.jsx'
import styles from './ExpenseTable.module.css'

// The list of expenses. Editing and deleting are handled by the page (onEdit / onDelete).
// deletingId: the expense being deleted right now, its buttons are disabled.
function ExpenseTable({ expenses, onEdit, onDelete, deletingId }) {
  return (
    <div className={styles.scroll}>
      <table className={styles.table}>
        <thead>
          <tr>
            <th scope="col">Date</th>
            <th scope="col">Description</th>
            <th scope="col">Category</th>
            <th scope="col" className={styles.amount}>
              Amount
            </th>
            {/* Visually empty header, but screen readers still get a column name */}
            <th scope="col">
              <span className="visually-hidden">Actions</span>
            </th>
          </tr>
        </thead>
        <tbody>
          {expenses.map((expense) => {
            const busy = expense.id === deletingId
            return (
              <tr key={expense.id}>
                <td className={styles.date}>{formatDate(expense.date)}</td>
                <td className={styles.description}>
                  {expense.description ?? <span className={styles.muted}>No description</span>}
                </td>
                <td>{expense.category?.name ?? <span className={styles.muted}>Uncategorized</span>}</td>
                <td className={styles.amount}>{formatAmount(expense.amount)}</td>
                <td>
                  <div className={styles.actions}>
                    <Button variant="secondary" onClick={() => onEdit(expense)} disabled={busy}>
                      Edit
                    </Button>
                    <Button variant="danger" onClick={() => onDelete(expense)} disabled={busy}>
                      {busy ? 'Deleting…' : 'Delete'}
                    </Button>
                  </div>
                </td>
              </tr>
            )
          })}
        </tbody>
      </table>
    </div>
  )
}

export default ExpenseTable
