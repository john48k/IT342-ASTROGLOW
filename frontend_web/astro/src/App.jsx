// App.jsx
import React, { useEffect } from "react";
import { Route, Routes } from "react-router-dom";
import AboutPage from "./Pages/AboutPage/AboutPage";
import { HomePage } from "./Pages/HomePage/HomePage";
import LandingPage from "./Pages/LandingPage/LandingPage";
import LoginPage from "./Pages/LoginPage/LoginPage";
import SignUpPage from "./Pages/SignUpPage/SignUpPage";
import UserProfilePage from "./Pages/UserProfile/UserProfile";
import { FavoritesPage } from "./Pages/FavoritesPage/FavoritesPage";
import OAuth2Redirect from "./components/OAuth2Redirect";
import ProtectedRoute from "./components/ProtectedRoute";
import { UserProvider } from "./context/UserContext";
import { FavoritesProvider } from "./context/FavoritesContext";
import { AudioPlayerProvider } from "./context/AudioPlayerContext";
import NowPlayingBar from "./components/NowPlayingBar/NowPlayingBar";
import { db } from "./firebase"; // Import the Firebase instance

function App() {
  useEffect(() => {
    // Example of reading data from Cloud Firestore
    const fetchData = async () => {
      try {
        // collection is a method to get a CollectionReference instance
        const collectionRef = collection(db, "your-collection");
        const querySnapshot = await getDocs(collectionRef);
        querySnapshot.forEach((doc) => {
          console.log(`${doc.id} => ${doc.data()}`);
        });
      } catch (error) {
        console.error("Error fetching data: ", error);
      }
    };

    fetchData();
  }, []);

  return (
    <UserProvider>
      <FavoritesProvider>
        <AudioPlayerProvider>
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
          </Routes>
          <NowPlayingBar />
        </AudioPlayerProvider>
      </FavoritesProvider>
    </UserProvider>
  );
}

export default App;
