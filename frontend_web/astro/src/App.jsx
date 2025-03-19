import { Routes, Route } from 'react-router-dom'
import { HomePage } from './Pages/HomePage/HomePage'
import LandingPage from './Pages/LandingPage/LandingPage'




function App() {

  return (
    <>
      <Routes>
        <Route path="/" element={<LandingPage />} />
        <Route path="/home" element={<HomePage />} />

      </Routes>
    </>
  )
}

export default App
