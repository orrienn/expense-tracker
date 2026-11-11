import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import './index.css'
import App from './App.jsx'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    {/* BrowserRouter provides routing to the whole app (URLs without #) */}
    <BrowserRouter>
      <App />
    </BrowserRouter>
  </StrictMode>,
)
