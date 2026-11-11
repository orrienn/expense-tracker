import { Link } from 'react-router-dom'

function NotFoundPage() {
  return (
    <main className="flex min-h-screen flex-col items-center justify-center gap-4 bg-slate-100 px-4">
      <h1 className="text-2xl font-semibold text-slate-800">404: page not found</h1>
      {/* Link changes the URL without reloading the page (unlike <a href>) */}
      <Link to="/" className="text-blue-600 hover:underline">
        Back to the home page
      </Link>
    </main>
  )
}

export default NotFoundPage
