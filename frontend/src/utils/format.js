import { parseISODate } from './dates.js'

// The backend stores plain numbers without a currency; for now the whole app uses one currency
export const CURRENCY = 'PLN'

// Intl is built into the browser. "undefined" as the locale = the browser's language,
// so a Polish browser shows "12,50 zł" and an English one "PLN 12.50".
const amountFormat = new Intl.NumberFormat(undefined, { style: 'currency', currency: CURRENCY })
const dateFormat = new Intl.DateTimeFormat(undefined, { dateStyle: 'medium' })

export function formatAmount(amount) {
  return amountFormat.format(amount)
}

export function formatDate(iso) {
  return dateFormat.format(parseISODate(iso))
}
