import { Routes, Route } from 'react-router-dom';
import { HomePage } from './Pages/HomePage/HomePage';
import LandingPage from './Pages/LandingPage/LandingPage';
import AboutPage from './Pages/AboutPage/AboutPage';

function App() {
  return (
    <>
      <Routes>
        <Route path="/" element={<LandingPage />} />
        <Route path="/home" element={<HomePage />} />
        <Route path="/about" element={<AboutPage />} />
      </Routes>
    </>
  );
}

export default App;