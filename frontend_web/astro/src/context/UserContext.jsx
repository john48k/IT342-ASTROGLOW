import React, { createContext, useContext, useEffect, useState } from "react";

const UserContext = createContext(null);

export const UserProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  useEffect(() => {
    // Check localStorage for existing user and token on app load
    const storedUser = localStorage.getItem("user");
    const storedToken = localStorage.getItem("token");

    // Only parse and set if the values exist
    if (storedUser && storedToken) {
      try {
        const parsedUser = JSON.parse(storedUser);
        setUser(parsedUser);
        setToken(storedToken);
        setIsAuthenticated(true);
      } catch (error) {
        // If there's an error parsing the stored user data, clear it
        console.error("Error parsing stored user data:", error);
        localStorage.removeItem("user");
        localStorage.removeItem("token");
      }
    }
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
      // Store the data
      localStorage.setItem("user", JSON.stringify(userData));
      localStorage.setItem("token", authToken);

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
  };

  return (
    <UserContext.Provider
      value={{
        user,
        token,
        isAuthenticated,
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
