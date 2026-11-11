import { useAuth } from '../hooks/useAuth.js'

// Placeholder: the real dashboard with charts comes in Stage 3
function DashboardPage() {
  const { user, logout } = useAuth()

  return (
    <main className="flex min-h-screen items-center justify-center bg-slate-100 px-4">
      <div className="w-full max-w-sm rounded-xl bg-white p-8 text-center shadow-md">
        <h1 className="text-2xl font-bold text-slate-800">Dashboard</h1>
        <p className="mt-2 text-slate-600">
          Logged in as <span className="font-medium text-slate-800">{user.email}</span>
        </p>
        <button
          type="button"
          onClick={logout}
          className="mt-6 rounded-md border border-slate-300 px-4 py-2 font-medium text-slate-700 hover:bg-slate-50"
        >
          Log out
        </button>
      </div>
    </main>
  )
}

export default DashboardPage
