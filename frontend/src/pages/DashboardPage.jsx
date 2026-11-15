import { useAuth } from '../hooks/useAuth.js'
import styles from './DashboardPage.module.css'

// Placeholder: the real dashboard with charts comes in Stage 3.
// The top bar (navigation, log out) comes from AppLayout.
function DashboardPage() {
  const { user } = useAuth()

  return (
    <section>
      <h1 className={styles.title}>Dashboard</h1>
      <p className={styles.info}>
        Logged in as <span className={styles.email}>{user.email}</span>. Charts with your spending are coming soon.
      </p>
    </section>
  )
}

export default DashboardPage
