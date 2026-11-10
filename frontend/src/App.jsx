import { Route, Routes } from 'react-router-dom'
import HomePage from './pages/HomePage.jsx'
import NotFoundPage from './pages/NotFoundPage.jsx'

// Tu trzymamy listę tras. Kolejne strony (/login, /expenses...) dojdą w następnych etapach.
function App() {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      {/* "*" łapie każdy adres, który nie pasuje do tras powyżej */}
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  )
}

export default App
