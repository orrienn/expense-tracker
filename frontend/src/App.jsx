import { Route, Routes } from 'react-router-dom'
import HomePage from './pages/HomePage.jsx'
import NotFoundPage from './pages/NotFoundPage.jsx'

// The list of routes. More pages (/login, /expenses...) will be added in later stages.
function App() {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      {/* "*" matches any path that doesn't match the routes above */}
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  )
}

export default App
