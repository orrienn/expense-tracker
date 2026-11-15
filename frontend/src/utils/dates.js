// Dates as "YYYY-MM-DD" strings: the format of <input type="date"> and of the API (LocalDate on the backend).

// Today in the user's *local* time zone. Not new Date().toISOString(): that uses UTC,
// so e.g. at 00:30 in Poland it would still say yesterday.
export function todayISO() {
  const now = new Date()
  const month = String(now.getMonth() + 1).padStart(2, '0') // getMonth() is 0-based
  const day = String(now.getDate()).padStart(2, '0')
  return `${now.getFullYear()}-${month}-${day}`
}

// "2026-11-15" -> Date at local midnight. Not new Date("2026-11-15"): that means midnight UTC,
// which west of Greenwich is still the previous day.
export function parseISODate(iso) {
  const [year, month, day] = iso.split('-').map(Number)
  return new Date(year, month - 1, day)
}
