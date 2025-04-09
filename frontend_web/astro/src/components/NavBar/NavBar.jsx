import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useUser } from "../../context/UserContext";
// import './Navbar.css';
import logo from "../../assets/images/AstroGlow-logo.png";
import "../../components/NavBarStyle/NavBar.css";
import Modal from "../Modal/Modal";

const Navbar = () => {
  const { isAuthenticated, logout } = useUser();
  const navigate = useNavigate();
  const [showLogoutModal, setShowLogoutModal] = useState(false);

  const handleLogoutClick = () => {
    setShowLogoutModal(true);
  };

  const handleLogoutConfirm = () => {
    logout();
    setShowLogoutModal(false);
    navigate("/");
  };

  const handleLogoutCancel = () => {
    setShowLogoutModal(false);
  };

  return (
    <>
      <nav className="navbar">
        <div className="navbar-logo">
          <Link to="" style={{ display: "flex", alignItems: "center" }}>
            <img src={logo} alt="AstroGlow Logo" />
            <h1 className="navbar-title-text">AstroGlow</h1>
          </Link>
        </div>
        <div className="navbar-links">
          <Link to="/about" className="about-us">
            About Us
          </Link>
          <p className="navbar-separate">|</p>
          {isAuthenticated ? (
            <>
              <Link
                to="/profile"
                className="navbar-button navbar-profile-button"
              >
                Profile
              </Link>
              <button
                onClick={handleLogoutClick}
                className="navbar-button navbar-logout-button"
              >
                Logout
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="navbar-button navbar-login-button">
                Login
              </Link>
              <Link to="/signup" className="navbar-button navbar-signup-button">
                Sign Up
              </Link>
            </>
          )}
        </div>
      </nav>

      <Modal
        isOpen={showLogoutModal}
        onClose={handleLogoutCancel}
        onConfirm={handleLogoutConfirm}
        title="Confirm Logout"
        message="Are you sure you want to log out?"
      />
    </>
  );
};

export default Navbar;
