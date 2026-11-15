import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth.js'
import Button from './Button.jsx'
import styles from './AppLayout.module.css'

const NAV_ITEMS = [
  { to: '/dashboard', label: 'Dashboard' },
  { to: '/expenses', label: 'Expenses' },
  { to: '/categories', label: 'Categories' },
]

// Shared frame for every page of a logged-in user: top bar with navigation + the page below.
// <Outlet /> renders the matched child route (DashboardPage, CategoriesPage...).
function AppLayout() {
  const { user, logout } = useAuth()

  return (
    <>
      <header className={styles.header}>
        <div className={styles.headerInner}>
          <span className={styles.brand}>Expense Tracker</span>

          <nav className={styles.nav}>
            {NAV_ITEMS.map(({ to, label }) => (
              // NavLink knows whether its route is the current one and passes isActive
              <NavLink
                key={to}
                to={to}
                className={({ isActive }) => (isActive ? `${styles.link} ${styles.active}` : styles.link)}
              >
                {label}
              </NavLink>
            ))}
          </nav>

          <div className={styles.user}>
            <span className={styles.email} title={user.email}>
              {user.email}
            </span>
            <Button variant="secondary" onClick={logout}>
              Log out
            </Button>
          </div>
        </div>
      </header>

      <main className={styles.main}>
        <Outlet />
      </main>
    </>
  )
}

export default AppLayout
