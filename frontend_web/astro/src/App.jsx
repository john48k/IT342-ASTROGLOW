// App.jsx
import React from "react";
import { Route, Routes } from "react-router-dom";
import AboutPage from "./Pages/AboutPage/AboutPage";
import { HomePage } from "./Pages/HomePage/HomePage";
import LandingPage from "./Pages/LandingPage/LandingPage";
import LoginPage from "./Pages/LoginPage/LoginPage";
import SignUpPage from "./Pages/SignUpPage/SignUpPage";
import UserProfilePage from "./Pages/UserProfile/UserProfile";
import { FavoritesPage } from "./Pages/FavoritesPage/FavoritesPage";
import SearchPage from "./Pages/SearchPage/SearchPage";
import OAuth2Redirect from "./components/OAuth2Redirect";
import ProtectedRoute from "./components/ProtectedRoute";
import { UserProvider } from "./context/UserContext";
import { FavoritesProvider } from "./context/FavoritesContext";
import { AudioPlayerProvider } from "./context/AudioPlayerContext";
import { PlaylistProvider } from "./context/PlaylistContext";
import NowPlayingBar from "./components/NowPlayingBar/NowPlayingBar";
import { useUser } from "./context/UserContext";

// Create a wrapper component to conditionally render NowPlayingBar
const AppContent = () => {
  const { isAuthenticated } = useUser();
  
  return (
    <FavoritesProvider>
      <PlaylistProvider>
        <Routes>
          <Route path="/" element={<LandingPage />} />
          <Route path="/about" element={<AboutPage />} />
          <Route path="/signup" element={<SignUpPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/oauth2/redirect" element={<OAuth2Redirect />} />
          
          {/* Protected Routes - Require Authentication */}
          <Route path="/home" element={
            <ProtectedRoute>
              <HomePage />
            </ProtectedRoute>
          } />
          <Route path="/profile" element={
            <ProtectedRoute>
              <UserProfilePage />
            </ProtectedRoute>
          } />
          <Route path="/favorites" element={
            <ProtectedRoute>
              <FavoritesPage />
            </ProtectedRoute>
          } />
          <Route path="/search" element={
            <ProtectedRoute>
              <SearchPage />
            </ProtectedRoute>
          } />
        </Routes>
        
        {/* Only render NowPlayingBar when user is authenticated */}
        {isAuthenticated && <NowPlayingBar />}
      </PlaylistProvider>
    </FavoritesProvider>
  );
};

function App() {
  return (
    <UserProvider>
      <AudioPlayerProvider>
        <AppContent />
      </AudioPlayerProvider>
    </UserProvider>
  );
}

export default App;
