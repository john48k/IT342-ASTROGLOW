// App.jsx
import React, { useEffect } from "react";
import { Route, Routes } from "react-router-dom";
import AboutPage from "./Pages/AboutPage/AboutPage";
import { HomePage } from "./Pages/HomePage/HomePage";
import LandingPage from "./Pages/LandingPage/LandingPage";
import LoginPage from "./Pages/LoginPage/LoginPage";
import SignUpPage from "./Pages/SignUpPage/SignUpPage";
import UserProfilePage from "./Pages/UserProfile/UserProfile";
import OAuth2Redirect from "./components/OAuth2Redirect";
import { UserProvider } from "./context/UserContext";
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
      <Routes>
        <Route path="/" element={<LandingPage />} />
        <Route path="/home" element={<HomePage />} />
        <Route path="/about" element={<AboutPage />} />
        <Route path="/signup" element={<SignUpPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/profile" element={<UserProfilePage />} />
        <Route path="/oauth2/redirect" element={<OAuth2Redirect />} />
      </Routes>
    </UserProvider>
  );
}

export default App;
