import { Routes, Route } from 'react-router-dom'
import { HomePage } from './Pages/HomePage/HomePage'



import './App.css'

function App() {

  return (
    <>
      <Routes>
        <Route index element={<HomePage />} />
        {/* <Route path="about" element={<About />} /> */}

        {/* <Route element={<AuthLayout />}>
          <Route path="login" element={<Login />} />
          <Route path="register" element={<Register />} />
        </Route>

        <Route path="concerts">
          <Route index element={<ConcertsHome />} />
          <Route path=":city" element={<City />} />
          <Route path="trending" element={<Trending />} />
        </Route> */}
      </Routes>
    </>
  )
}

export default App
