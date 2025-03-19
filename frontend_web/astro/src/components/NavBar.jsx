import React from 'react';
import './Navbar.css';
import logo from '../assets/images/AstroGlow-logo.png';

const Navbar = () => {
    return (
        <nav className="navbar">
            <div className="navbar-logo">
                <img src={logo} alt="AstroGlow Logo" />
                <h1 className='navbar-title-text'>AstroGlow</h1>
            </div>
            <div className="navbar-links">
                <button className="navbar-button navbar-login-button">Login</button>
                <button className="navbar-button navbar-signup-button">Sign Up</button>
            </div>
        </nav>
    );
};

export default Navbar;