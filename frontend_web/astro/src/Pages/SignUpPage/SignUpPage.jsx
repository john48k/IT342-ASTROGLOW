import React, { useState } from "react";
import "./SignUpPage.css";
import NavBar from "../../components/NavBar/NavBar";
import { Link, useNavigate } from "react-router-dom"; // Added useNavigate for redirection

const SignUpPage = () => {
  const [formData, setFormData] = useState({
    userName: "",
    userPassword: "",
    userEmail: "",
  });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);
  const navigate = useNavigate();

  // Handle input changes
  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value,
    });
  };

  // Handle form submission
  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");

    try {
      // Map the form fields to the expected backend structure
      const userData = {
        userName: formData.userName,
        userPassword: formData.userPassword,
        userEmail: formData.userEmail,
        authentication: null,
        playlists: null,
        offlineLibraries: null,
        favorites: null
      };

      // Make API call to the signup endpoint
      const response = await fetch("http://localhost:8080/api/user/postUser", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(userData),
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || "Failed to sign up");
      }

      const data = await response.json();
      console.log("Signup successful:", data);
      
      // Show success message and redirect after a delay
      setSuccess(true);
      setTimeout(() => {
        navigate("/login");
      }, 2000);
    } catch (error) {
      console.error("Signup error:", error);
      setError(error.message || "An error occurred during sign up");
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
            
            {error && <div className="error-message">{error}</div>}
            {success && <div className="success-message">Account created successfully! Redirecting to login...</div>}
            
            <div className="form-group">
              <label htmlFor="userName">Username</label>
              <input 
                type="text" 
                id="userName" 
                name="userName" 
                value={formData.userName}
                onChange={handleChange}
                required 
              />
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
              />
            </div>
            
            <div className="form-group">
              <label htmlFor="userPassword">Password</label>
              <input 
                type="password" 
                id="userPassword" 
                name="userPassword" 
                value={formData.userPassword}
                onChange={handleChange}
                required 
              />
            </div>
            
            <button 
              type="submit" 
              className="signup-button"
              disabled={loading}
            >
              {loading ? "Signing up..." : "Sign up"}
            </button>
            
            <div className="signup-google-login">
              <a href="http://google.com/">Login with Google</a>
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