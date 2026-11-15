import styles from './Button.module.css'

// Shared button. variant: 'primary' | 'secondary' | 'danger'.
// type defaults to "button": a plain <button> inside a <form> is "submit" and would submit it by accident.
// className lets the parent add spacing (margins belong to the layout, not to the button itself).
// ...props passes everything else (onClick, disabled, type="submit"...) to the real <button>.
function Button({ variant = 'primary', fullWidth = false, className, type = 'button', ...props }) {
  const classes = [styles.button, styles[variant], fullWidth && styles.fullWidth, className]
    .filter(Boolean)
    .join(' ')

  return <button type={type} className={classes} {...props} />
}

export default Button
