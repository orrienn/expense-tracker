import { useEffect, useId, useRef } from 'react'
import styles from './Modal.module.css'

// Modal window built on the native HTML <dialog> element (no library needed).
// showModal() gives us for free: a dimmed backdrop, closing with Escape,
// and keeping keyboard focus inside the dialog (Tab doesn't jump to the page behind it).
function Modal({ open, title, onClose, children }) {
  const dialogRef = useRef(null)
  const titleId = useId() // unique id, so aria-labelledby works even with several modals

  // React only describes the "open" state; opening/closing a <dialog> needs a method call (DOM API)
  useEffect(() => {
    const dialog = dialogRef.current
    if (open && !dialog.open) dialog.showModal()
    if (!open && dialog.open) dialog.close()
  }, [open])

  // Escape fires "cancel" before the browser closes the dialog. We stop the browser from closing it
  // and ask the parent instead, so React state stays the single source of truth: the effect above
  // then closes it. (Letting the browser close it first left a short moment where the dialog was
  // already closed but the state still said "open", and a quick click on "Add" was lost.)
  function handleCancel(event) {
    event.preventDefault()
    onClose()
  }

  // Safety net: if the dialog closes any other way, the parent's state follows.
  // The "close" event also arrives a moment *after* dialog.close(); if the modal was reopened
  // in the meantime, that late event must not close it again, hence the check.
  function handleClose() {
    if (!dialogRef.current.open) onClose()
  }

  return (
    <dialog
      ref={dialogRef}
      className={styles.dialog}
      onCancel={handleCancel}
      onClose={handleClose}
      aria-labelledby={titleId}
    >
      <h2 id={titleId} className={styles.title}>
        {title}
      </h2>
      {/* Content is rendered only while open, so a form starts fresh every time the modal opens */}
      {open && <div className={styles.body}>{children}</div>}
    </dialog>
  )
}

export default Modal
