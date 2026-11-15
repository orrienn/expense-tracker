import { Link } from 'react-router-dom'
import styles from './NotFoundPage.module.css'

function NotFoundPage() {
  return (
    <main className={styles.page}>
      <h1 className={styles.title}>404: page not found</h1>
      {/* Link changes the URL without reloading the page (unlike <a href>); styled by the global "a" rule */}
      <Link to="/">Back to the home page</Link>
    </main>
  )
}

export default NotFoundPage
