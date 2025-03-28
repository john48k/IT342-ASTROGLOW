import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import styles from "./LoginPage.module.css";

const LoginPage = () => {
  const [formData, setFormData] = useState({
    userEmail: "",
    userPassword: "",
  });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");

    try {
      // Create the login request payload
      const loginData = {
        userEmail: formData.userEmail,
        userPassword: formData.userPassword,
      };

      // Make API call to the login endpoint
      const response = await fetch("http://localhost:8080/api/user/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(loginData),
      });

      if (!response.ok) {
        if (response.status === 401) {
          throw new Error("Invalid email or password");
        }
        throw new Error("Failed to log in");
      }

      const userData = await response.json();
      console.log("Login successful:", userData);

      // Store user data in localStorage or state management
      localStorage.setItem("user", JSON.stringify(userData));

      // Redirect to home page
      navigate("/home");
    } catch (error) {
      console.error("Login error:", error);
      setError(error.message || "An error occurred during login");
    } finally {
      setLoading(false);
    }
  };

  const handleGoogleLogin = () => {
    window.location.href = "http://localhost:8080/oauth2/authorization/google";
  };

  const handleGithubLogin = () => {
    window.location.href = "http://localhost:8080/oauth2/authorization/github";
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

            {error && <div className={styles.errorMessage}>{error}</div>}

            <form onSubmit={handleSubmit}>
              <input
                type="email"
                name="userEmail"
                placeholder="EMAIL"
                value={formData.userEmail}
                onChange={handleChange}
                required
              />
              <input
                type="password"
                name="userPassword"
                placeholder="PASSWORD"
                value={formData.userPassword}
                onChange={handleChange}
                required
              />
              <button
                type="submit"
                className={styles.opacity}
                disabled={loading}
              >
                {loading ? "LOGGING IN..." : "SUBMIT"}
              </button>
            </form>

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
