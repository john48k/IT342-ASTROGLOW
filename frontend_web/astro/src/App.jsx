import { Routes, Route } from 'react-router-dom';
import { HomePage } from './Pages/HomePage/HomePage';
import LandingPage from './Pages/LandingPage/LandingPage';
import AboutPage from './Pages/AboutPage/AboutPage';
import SignUpPage from './Pages/SignUpPage/SignUpPage';
import LoginPage from './Pages/LoginPage/LoginPage'
import UserProfilePage from './Pages/UserProfile/UserProfile';

function App() {
  return (
    <>
      <Routes>
        <Route path="/" element={<LandingPage />} />
        <Route path="/home" element={<HomePage />} />
        <Route path="/about" element={<AboutPage />} />
        <Route path='/signup' element={<SignUpPage />} />
        <Route path='/login' element={<LoginPage />} />
        <Route path='/profile' element={<UserProfilePage />} />
      </Routes>
    </>
  );
}

export default App;