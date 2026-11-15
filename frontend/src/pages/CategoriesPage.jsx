import { useState } from 'react'
import { getErrorMessage } from '../api/errors.js'
import AddCategoryForm from '../components/AddCategoryForm.jsx'
import Button from '../components/Button.jsx'
import CategoryRow from '../components/CategoryRow.jsx'
import ErrorAlert from '../components/ErrorAlert.jsx'
import { useCategories } from '../hooks/useCategories.js'
import styles from './CategoriesPage.module.css'

function CategoriesPage() {
  const { categories, loading, error, reload, add, rename, remove } = useCategories()
  // Page-level message, e.g. when a category was already deleted in another tab
  const [notice, setNotice] = useState(null)

  // 404 on rename/delete: the category no longer exists (deleted elsewhere).
  // Instead of an error in a row that's out of date, refresh the whole list.
  async function runRowAction(action) {
    setNotice(null)
    try {
      await action()
    } catch (err) {
      if (err.response?.status === 404) {
        setNotice('This category no longer exists. The list has been refreshed.')
        reload()
        return
      }
      throw err // other errors are shown by the row itself
    }
  }

  function renderContent() {
    if (loading) {
      return <p className={styles.status}>Loading categories…</p>
    }

    if (error) {
      return (
        <div className={styles.loadError}>
          <ErrorAlert>Couldn&apos;t load categories. {getErrorMessage(error)}</ErrorAlert>
          <Button variant="secondary" onClick={reload}>
            Try again
          </Button>
        </div>
      )
    }

    return (
      <div className={styles.card}>
        <AddCategoryForm
          onAdd={(name) => {
            setNotice(null)
            return add(name)
          }}
        />

        {categories.length === 0 ? (
          <p className={styles.empty}>No categories yet. Add your first one above.</p>
        ) : (
          <ul className={styles.list}>
            {categories.map((category) => (
              // key: React's way to tell list items apart between renders (the id never changes)
              <CategoryRow
                key={category.id}
                category={category}
                onRename={(id, name) => runRowAction(() => rename(id, name))}
                onDelete={(id) => runRowAction(() => remove(id))}
              />
            ))}
          </ul>
        )}
      </div>
    )
  }

  return (
    <section>
      <h1 className={styles.title}>Categories</h1>
      <p className={styles.subtitle}>Group your expenses, e.g. Food, Transport, Bills.</p>
      {notice && <ErrorAlert className={styles.alert}>{notice}</ErrorAlert>}
      {renderContent()}
    </section>
  )
}

export default CategoriesPage
