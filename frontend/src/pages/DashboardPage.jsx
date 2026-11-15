import Button from '../components/Button.jsx'
import { useAuth } from '../hooks/useAuth.js'
import styles from './DashboardPage.module.css'

// Placeholder: the real dashboard with charts comes in Stage 3
function DashboardPage() {
  const { user, logout } = useAuth()

  return (
    <main className={styles.page}>
      <div className={styles.card}>
        <h1 className={styles.title}>Dashboard</h1>
        <p className={styles.info}>
          Logged in as <span className={styles.email}>{user.email}</span>
        </p>
        <Button variant="secondary" onClick={logout} className={styles.logout}>
          Log out
        </Button>
      </div>
    </main>
  )
}

export default DashboardPage
