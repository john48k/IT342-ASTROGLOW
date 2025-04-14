import React, { useState } from "react";
import { Link, useNavigate, useLocation } from "react-router-dom";
import { useUser } from "../../context/UserContext";
import styles from "./LoginPage.module.css";

const LoginPage = () => {
  const [formData, setFormData] = useState({
    userEmail: "",
    userPassword: "",
  });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useUser();

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value,
    });
  };

  const togglePasswordVisibility = () => {
    setShowPassword(!showPassword);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");

    try {
      const loginData = {
        userEmail: formData.userEmail,
        userPassword: formData.userPassword,
        rememberMe: true // Added to request persistent cookie
      };

      console.log("Attempting to connect to backend at http://localhost:8080/api/user/login");

      const response = await fetch("http://localhost:8080/api/user/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(loginData),
        credentials: 'include', // Include cookies in the request
        // Add timeout to prevent long hangs
        signal: AbortSignal.timeout(10000), // 10 second timeout
      });

      if (!response.ok) {
        if (response.status === 401) {
          throw new Error("Invalid email or password");
        }
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || "Failed to log in");
      }

      const userData = await response.json();
      console.log("Login response:", userData);

      if (!userData || !userData.userEmail) {
        throw new Error("Invalid credentials");
      }

      // Generate a session ID with timestamp to ensure uniqueness
      const sessionId = btoa(userData.userEmail + ":" + new Date().getTime());

      // Store a session cookie on the client side as well
      document.cookie = `auth_session=${sessionId}; path=/; max-age=2592000`; // 30 days

      // Call login with the user data and session ID
      login(userData, sessionId);

      // Check if we have a redirect location from the protected route
      const from = location.state?.from || "/home";
      navigate(from);
    } catch (error) {
      console.error("Login error:", error);

      // Provide more specific error messages based on the error type
      if (error.name === 'AbortError') {
        setError("Request timed out. Please check if the server is running.");
      } else if (error.name === 'TypeError' && error.message.includes('fetch')) {
        setError("Cannot connect to the server. Please ensure the backend is running at http://localhost:8080");
      } else {
        setError(error.message || "An error occurred during login");
      }

      setLoading(false);
    }
  };

  const handleGoogleLogin = () => {
    window.location.href = "http://localhost:8080/oauth2/authorization/google";
  };

  const handleGithubLogin = () => {
    window.location.href = "http://localhost:8080/oauth2/authorization/github";
  };

  // Function to check backend health
  const checkBackendStatus = async () => {
    try {
      setError("Checking server status...");
      const response = await fetch("http://localhost:8080/api/user/api-test", {
        method: "GET",
        signal: AbortSignal.timeout(5000) // 5 second timeout
      });

      if (response.ok) {
        setError("Server is running but login failed. Check your credentials.");
      } else {
        setError("Server responded with an error: " + response.status);
      }
    } catch (err) {
      setError("Cannot connect to server. Please ensure backend is running.");
    }
  };

  return (
    <div className={styles.LoginPage}>
      <section className={styles.container}>
        <div className={styles["login-container"]}>
          <div className={`${styles.circle} ${styles["circle-one"]}`}></div>
          <div className={styles["form-container"]}>
            <img
              src="login-character.png"
              alt="login-character"
              className={styles.illustration}
            />
            <h1 className={styles.opacity}>LOGIN</h1>

            <form onSubmit={handleSubmit}>
              <input
                type="email"
                name="userEmail"
                placeholder="EMAIL"
                value={formData.userEmail}
                onChange={handleChange}
                required
              />
              <div className={styles.passwordContainer}>
                <input
                  type={showPassword ? "text" : "password"}
                  name="userPassword"
                  placeholder="PASSWORD"
                  value={formData.userPassword}
                  onChange={handleChange}
                  required
                />
                <button
                  type="button"
                  className={styles.passwordToggle}
                  onClick={togglePasswordVisibility}
                  aria-label={showPassword ? "Hide password" : "Show password"}
                >
                  {showPassword ? (
                    <svg
                      xmlns="http://www.w3.org/2000/svg"
                      width="20"
                      height="20"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      strokeWidth="2"
                      strokeLinecap="round"
                      strokeLinejoin="round"
                    >
                      <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
                      <line x1="1" y1="1" x2="23" y2="23"></line>
                    </svg>
                  ) : (
                    <svg
                      xmlns="http://www.w3.org/2000/svg"
                      width="20"
                      height="20"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      strokeWidth="2"
                      strokeLinecap="round"
                      strokeLinejoin="round"
                    >
                      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                      <circle cx="12" cy="12" r="3"></circle>
                    </svg>
                  )}
                </button>
              </div>
              <button
                type="submit"
                className={styles.opacity}
                disabled={loading}
              >
                {loading ? "LOGGING IN..." : "SUBMIT"}
              </button>
            </form>


            {error && (
              <div className={styles.errorContainer}>
                <div className={styles.errorMessage}>{error}</div>
                {error.includes("Cannot connect") && (
                  <button
                    className={styles.checkServerButton}
                    onClick={checkBackendStatus}
                  >
                    Check Server Status
                  </button>
                )}
              </div>
            )}


            <div className={`${styles["social-login"]} ${styles.opacity}`}>
              <p>Or login with</p>
              <div className={styles["social-buttons"]}>
                <button
                  onClick={handleGoogleLogin}
                  className={`${styles["social-button"]} ${styles["google-button"]}`}
                >
                  <img
                    src="/google-icon.png"
                    alt="Google"
                    className={styles["social-icon"]}
                  />
                  Google
                </button>
                <button
                  onClick={handleGithubLogin}
                  className={`${styles["social-button"]} ${styles["github-button"]}`}
                >
                  <img
                    src="/github-icon.png"
                    alt="GitHub"
                    className={`${styles["social-icon"]} ${styles["github-icon"]}`}
                  />
                  GitHub
                </button>
              </div>
            </div>

            <div className={`${styles["register-forget"]} ${styles.opacity}`}>
              <Link className={styles["register-hover"]} to="/signup">
                REGISTER
              </Link>
            </div>
          </div>
          <div className={`${styles.circle} ${styles["circle-two"]}`}></div>
        </div>
        <div className={styles["theme-btn-container"]}></div>
      </section>
    </div>
  );
};

export default LoginPage;
