import React, { createContext, useContext, useEffect, useState } from "react";

const UserContext = createContext(null);

export const UserProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);

  // Check for existing authentication on initial load
  useEffect(() => {
    const checkAuth = async () => {
      setIsLoading(true);
      try {
        // First check localStorage
        const storedUser = localStorage.getItem("user");
        const storedToken = localStorage.getItem("token");

        // If found in localStorage, use it
        if (storedUser && storedToken) {
          const parsedUser = JSON.parse(storedUser);
          console.log("Found stored user in localStorage:", parsedUser.userName);
          
          // Set user data from localStorage
          setUser(parsedUser);
          setToken(storedToken);
          setIsAuthenticated(true);
        } else {
          // If not in localStorage, try sessionStorage as fallback
          const sessionUser = sessionStorage.getItem("user");
          const sessionToken = sessionStorage.getItem("token");
          
          if (sessionUser && sessionToken) {
            const parsedUser = JSON.parse(sessionUser);
            console.log("Found stored user in sessionStorage:", parsedUser.userName);
            
            // Copy from sessionStorage to localStorage for persistence
            localStorage.setItem("user", sessionUser);
            localStorage.setItem("token", sessionToken);
            
            // Set user data
            setUser(parsedUser);
            setToken(sessionToken);
            setIsAuthenticated(true);
          } else {
            console.log("No stored auth data found in localStorage or sessionStorage");
          }
        }
      } catch (error) {
        console.error("Error checking authentication:", error);
        // Clear potentially corrupted data
        localStorage.removeItem("user");
        localStorage.removeItem("token");
        sessionStorage.removeItem("user");
        sessionStorage.removeItem("token");
      } finally {
        setIsLoading(false);
      }
    };

    checkAuth();
  }, []);

  const login = (userData, authToken) => {
    console.log("Login attempt with:", { userData, authToken }); // Debug log

    // Validate the user data structure
    if (!userData || !authToken) {
      console.error("Login failed: Missing user data or token");
      return;
    }

    // Ensure userData has the required fields
    if (!userData.userEmail) {
      console.error("Login failed: Invalid user data structure");
      return;
    }

    try {
      // Store the data in localStorage for persistence across browser sessions
      localStorage.setItem("user", JSON.stringify(userData));
      localStorage.setItem("token", authToken);
      
      // Also try to store in sessionStorage as a backup (persists only for current tab)
      try {
        sessionStorage.setItem("user", JSON.stringify(userData));
        sessionStorage.setItem("token", authToken);
      } catch (e) {
        console.log("Could not store in sessionStorage:", e);
      }

      // Update state
      setUser(userData);
      setToken(authToken);
      setIsAuthenticated(true);

      console.log("Login successful:", { userData, authToken }); // Debug log
    } catch (error) {
      console.error("Error during login:", error);
      // Clean up if storage fails
      localStorage.removeItem("user");
      localStorage.removeItem("token");
    }
  };

  const logout = () => {
    setUser(null);
    setToken(null);
    setIsAuthenticated(false);
    localStorage.removeItem("user");
    localStorage.removeItem("token");
    sessionStorage.removeItem("user");
    sessionStorage.removeItem("token");
  };

  return (
    <UserContext.Provider
      value={{
        user,
        token,
        isAuthenticated,
        isLoading,
        login,
        logout,
      }}
    >
      {children}
    </UserContext.Provider>
  );
};

// Custom hook to use the user context
export const useUser = () => {
  const context = useContext(UserContext);
  if (context === undefined) {
    throw new Error("useUser must be used within a UserProvider");
  }
  return context;
};

export default UserContext;
