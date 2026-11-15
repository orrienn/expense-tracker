import styles from './ErrorAlert.module.css'

// Error message box, e.g. for an error returned by the server.
// role="alert" makes screen readers read it out as soon as it appears.
function ErrorAlert({ className, children }) {
  return (
    <p role="alert" className={className ? `${styles.alert} ${className}` : styles.alert}>
      {children}
    </p>
  )
}

export default ErrorAlert
