import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import NavBar from "../../components/NavBar/NavBar";
import { useUser } from "../../context/UserContext";
import "./SignUpPage.css";

const SignUpPage = () => {
  const [formData, setFormData] = useState({
    userName: "",
    userPassword: "",
    confirmPassword: "",
    userEmail: "",
  });
  const [errors, setErrors] = useState({
    userName: "",
    userPassword: "",
    confirmPassword: "",
    userEmail: "",
  });
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const navigate = useNavigate();
  const { login } = useUser();

  // Validation functions
  const validateUsername = (username) => {
    if (!username) return "Username is required";
    if (username.length < 3 || username.length > 30)
      return "Username must be between 3 and 30 characters";
    if (!/^[a-zA-Z0-9_]+$/.test(username))
      return "Username can only contain letters, numbers, and underscores";
    if (username.includes("admin") || username.includes("moderator"))
      return "Username cannot contain prohibited terms";
    return "";
  };

  const validateEmail = (email) => {
    if (!email) return "Email is required";
    if (email.length > 255) return "Email is too long";
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email))
      return "Invalid email format";
    return "";
  };

  const validatePassword = (password) => {
    if (!password) return "Password is required";
    if (password.length < 8)
      return "Password must be at least 8 characters long";
    if (!/[A-Z]/.test(password))
      return "Password must contain at least one uppercase letter";
    if (!/[a-z]/.test(password))
      return "Password must contain at least one lowercase letter";
    if (!/[0-9]/.test(password))
      return "Password must contain at least one number";
    if (!/[!@#$%^&*(),.?":{}|<>]/.test(password))
      return "Password must contain at least one special character";
    if (password.length > 128) return "Password is too long";
    return "";
  };

  const validateConfirmPassword = (password, confirmPassword) => {
    if (!confirmPassword) return "Please confirm your password";
    if (password !== confirmPassword) return "Passwords do not match";
    return "";
  };

  // Handle input changes with validation
  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value,
    });

    // Validate on change
    let error = "";
    switch (name) {
      case "userName":
        error = validateUsername(value);
        break;
      case "userEmail":
        error = validateEmail(value);
        break;
      case "userPassword":
        error = validatePassword(value);
        break;
      case "confirmPassword":
        error = validateConfirmPassword(formData.userPassword, value);
        break;
    }

    setErrors((prev) => ({
      ...prev,
      [name]: error,
    }));
  };

  // Handle form submission
  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    // Reset errors
    setErrors({
      userName: "",
      userPassword: "",
      confirmPassword: "",
      userEmail: "",
    });

    // Validate all fields
    const usernameError = validateUsername(formData.userName);
    const emailError = validateEmail(formData.userEmail);
    const passwordError = validatePassword(formData.userPassword);
    const confirmPasswordError = validateConfirmPassword(
      formData.userPassword,
      formData.confirmPassword
    );

    if (usernameError || emailError || passwordError || confirmPasswordError) {
      setErrors({
        userName: usernameError,
        userEmail: emailError,
        userPassword: passwordError,
        confirmPassword: confirmPasswordError,
      });
      setLoading(false);
      return;
    }

    try {
      const response = await fetch("http://localhost:8080/api/user/signup", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          userName: formData.userName,
          userEmail: formData.userEmail,
          userPassword: formData.userPassword,
        }),
      });

      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.message || "Failed to sign up");
      }

      console.log("Signup successful:", data);

      // Show success message
      setSuccess(true);

      // Automatically log in the user
      const sessionId = btoa(data.userEmail + ":" + new Date().getTime());
      login(data, sessionId);

      // Navigate to home page
      navigate("/home");
    } catch (error) {
      console.error("Signup error:", error);
      setErrors((prev) => ({
        ...prev,
        submit: error.message || "An error occurred during sign up",
      }));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="SignUpPage">
      <div className="split-screen">
        <div className="left-side">
          <div className="left-side-text">
            <h1 className="left-side-header">Create your free account</h1>
            <p className="left-side-sub-title">
              All Your Sounds, All in One Place.
            </p>
            <div className="astro-pic">
              <img src="/astro-pic.png" alt="Astro character" />
            </div>
          </div>
        </div>
        <div className="right-side">
          <form className="signup-form" onSubmit={handleSubmit}>
            <h2>
              Sign up to <span className="astro-glow-font">AstroGlow</span>
            </h2>

            <div className="form-group">
              <label htmlFor="userName">Username</label>
              <input
                type="text"
                id="userName"
                name="userName"
                value={formData.userName}
                onChange={handleChange}
                required
                className={errors.userName ? "error" : ""}
              />
              {errors.userName && (
                <span className="error-text">{errors.userName}</span>
              )}
            </div>

            <div className="form-group">
              <label htmlFor="userEmail">Email</label>
              <input
                type="email"
                id="userEmail"
                name="userEmail"
                value={formData.userEmail}
                onChange={handleChange}
                required
                className={errors.userEmail ? "error" : ""}
              />
              {errors.userEmail && (
                <span className="error-text">{errors.userEmail}</span>
              )}
            </div>

            <div className="form-group">
              <label htmlFor="userPassword">Password</label>
              <div className="password-input-container">
                <input
                  type={showPassword ? "text" : "password"}
                  id="userPassword"
                  name="userPassword"
                  value={formData.userPassword}
                  onChange={handleChange}
                  required
                  className={errors.userPassword ? "error" : ""}
                />
                <button
                  type="button"
                  className="toggle-password"
                  onClick={() => setShowPassword(!showPassword)}
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
              {errors.userPassword && (
                <span className="error-text">{errors.userPassword}</span>
              )}
            </div>

            <div className="form-group">
              <label htmlFor="confirmPassword">Confirm Password</label>
              <div className="password-input-container">
                <input
                  type={showConfirmPassword ? "text" : "password"}
                  id="confirmPassword"
                  name="confirmPassword"
                  value={formData.confirmPassword}
                  onChange={handleChange}
                  required
                  className={errors.confirmPassword ? "error" : ""}
                />
                <button
                  type="button"
                  className="toggle-password"
                  onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                  aria-label={
                    showConfirmPassword ? "Hide password" : "Show password"
                  }
                >
                  {showConfirmPassword ? (
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
              {errors.confirmPassword && (
                <span className="error-text">{errors.confirmPassword}</span>
              )}
            </div>

            {errors.submit && (
              <div className="error-message">{errors.submit}</div>
            )}
            {success && (
              <div className="success-message">
                Account created successfully! Redirecting to home...
              </div>
            )}

            <button
              type="submit"
              className="signup-button"
              disabled={
                loading || Object.values(errors).some((error) => error !== "")
              }
            >
              {loading ? "Signing up..." : "Sign up"}
            </button>

            <div className="signup-google-login">
              <a href="http://localhost:8080/oauth2/authorization/google">
                Login with Google
              </a>
            </div>

            <div className="signup-login-link">
              <span className="already">Already have an account? </span>
              <Link to="/login">Login</Link>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default SignUpPage;
