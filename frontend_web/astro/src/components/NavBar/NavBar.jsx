import React, { useState } from "react";
import { Link, useNavigate, useLocation } from "react-router-dom";
import { useUser } from "../../context/UserContext";
// import './Navbar.css';
import logo from "../../assets/images/AstroGlow-logo.png";
import "../../components/NavBarStyle/NavBar.css";
import Modal from "../Modal/Modal";
import { FaSearch } from 'react-icons/fa';

const Navbar = () => {
  const { isAuthenticated, logout } = useUser();
  const navigate = useNavigate();
  const location = useLocation();
  // Explicitly initialize modal to closed
  const [showLogoutModal, setShowLogoutModal] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");

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

  const handleSearchChange = (e) => {
    setSearchQuery(e.target.value);
  };

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      // Navigate to search results page with the query as a parameter
      navigate(`/search?q=${encodeURIComponent(searchQuery.trim())}`);
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter') {
      handleSearchSubmit(e);
    }
  };

  return (
    <>
      <nav className="navbar">
        <div className="navbar-logo">
          <Link
            to={isAuthenticated ? "/home" : "/"}
            style={{ display: "flex", alignItems: "center" }}
          >
            <img src={logo} alt="AstroGlow Logo" />
            <h1 className="navbar-title-text">AstroGlow</h1>
          </Link>
        </div>

        {isAuthenticated && (location.pathname === "/home" || location.pathname === "/favorites") && (
          <div className="navbar-search">
            <form onSubmit={handleSearchSubmit} className="search-form">
              <input
                type="text"
                placeholder="Search for music..."
                value={searchQuery}
                onChange={handleSearchChange}
                onKeyDown={handleKeyDown}
                className="search-input"
                aria-label="Search for music"
              />
              <button type="submit" className="search-button" aria-label="Search">
                <FaSearch />
              </button>
            </form>
          </div>
        )}

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

      {/* Only render the Modal when showLogoutModal is true */}
      {showLogoutModal && (
        <Modal
          isOpen={true}
          onClose={handleLogoutCancel}
          onConfirm={handleLogoutConfirm}
          title="Confirm Logout"
          message="Are you sure you want to log out of your AstroGlow account?"
          confirmText="Logout"
          showConfirmButton={true}
        />
      )}
    </>
  );
};

export default Navbar;