import { Link } from 'react-router-dom'

function NotFoundPage() {
  return (
    <main className="flex min-h-screen flex-col items-center justify-center gap-4 bg-slate-100 px-4">
      <h1 className="text-2xl font-semibold text-slate-800">404: nie ma takiej strony</h1>
      {/* Link zmienia adres bez przeładowania strony (w przeciwieństwie do <a href>) */}
      <Link to="/" className="text-blue-600 hover:underline">
        Wróć na stronę główną
      </Link>
    </main>
  )
}

export default NotFoundPage
