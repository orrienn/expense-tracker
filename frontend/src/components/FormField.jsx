import styles from './FormField.module.css'

// Label + input + error message, shared by all forms.
// aria-invalid / aria-describedby let screen readers announce the error for the field
// (aria-invalid also switches the red border, see FormField.module.css).
function FormField({ id, label, type = 'text', value, onChange, error, autoComplete }) {
  const errorId = `${id}-error`

  return (
    <div>
      <label htmlFor={id} className={styles.label}>
        {label}
      </label>
      <input
        id={id}
        type={type}
        value={value}
        onChange={(event) => onChange(event.target.value)}
        autoComplete={autoComplete}
        aria-invalid={error ? 'true' : 'false'}
        aria-describedby={error ? errorId : undefined}
        className={styles.input}
      />
      {error && (
        <p id={errorId} className={styles.error}>
          {error}
        </p>
      )}
    </div>
  )
}

export default FormField
