import styles from './FormField.module.css'

// Label + input + error message, shared by all forms.
// aria-invalid / aria-describedby let screen readers announce the error for the field
// (aria-invalid also switches the red border, see FormField.module.css).
// children (optional) are shown next to the input, e.g. an "Add" button.
// ...inputProps: any other <input> attributes (inputMode, max, placeholder...) are passed through.
function FormField({ id, label, type = 'text', value, onChange, error, autoComplete, children, ...inputProps }) {
  const errorId = `${id}-error`

  return (
    <div>
      <label htmlFor={id} className={styles.label}>
        {label}
      </label>
      <div className={styles.row}>
        <input
          {...inputProps}
          id={id}
          type={type}
          value={value}
          onChange={(event) => onChange(event.target.value)}
          autoComplete={autoComplete}
          aria-invalid={error ? 'true' : 'false'}
          aria-describedby={error ? errorId : undefined}
          className={styles.input}
        />
        {children}
      </div>
      {error && (
        <p id={errorId} className={styles.error}>
          {error}
        </p>
      )}
    </div>
  )
}

export default FormField
