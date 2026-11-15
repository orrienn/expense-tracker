import styles from './FormField.module.css'

// Same look as FormField (label, field, error), but with a <select>. Options are passed as children.
function SelectField({ id, label, value, onChange, error, hint, children }) {
  const errorId = `${id}-error`

  return (
    <div>
      <label htmlFor={id} className={styles.label}>
        {label}
      </label>
      <div className={styles.row}>
        <select
          id={id}
          value={value}
          onChange={(event) => onChange(event.target.value)}
          aria-invalid={error ? 'true' : 'false'}
          aria-describedby={error ? errorId : undefined}
          className={styles.input}
        >
          {children}
        </select>
      </div>
      {/* hint: extra info under the field, e.g. a link to create categories */}
      {hint && <div className={styles.hint}>{hint}</div>}
      {error && (
        <p id={errorId} className={styles.error}>
          {error}
        </p>
      )}
    </div>
  )
}

export default SelectField
