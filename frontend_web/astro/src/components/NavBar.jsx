import React from 'react';
import './Navbar.css';

const Navbar = () => {
    return (
        <nav className="navbar">
            <div className="navbar-logo">
                <h1>AstroGlow</h1>
            </div>
            <div className="navbar-links">
                <button className="navbar-button login-button">Login</button>
                <button className="navbar-button signup-button">Sign Up</button>
            </div>
        </nav>
    );
};

export default Navbar;