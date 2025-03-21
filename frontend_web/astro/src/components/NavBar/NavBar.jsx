import React from 'react';
import { Link } from 'react-router-dom';
import './Navbar.css';
import logo from '../../assets/images/AstroGlow-logo.png';

const Navbar = () => {
    return (
        <nav className="navbar">
            <div className="navbar-logo">
                <Link to="/home" style={{ display: 'flex', alignItems: 'center' }}>
                    <img src={logo} alt="AstroGlow Logo" />
                    <h1 className='navbar-title-text'>AstroGlow</h1>
                </Link>
            </div>
            <div className="navbar-links">
                <Link to="/about" className='about-us'>About Us</Link>
                <p className="navbar-separate">|</p>
                {/* <button className="navbar-button navbar-login-button">Login</button> */}
                <Link to="/login" className="navbar-button navbar-login-button">Login</Link>
                <Link to="/signup" className="navbar-button navbar-signup-button">Sign Up</Link>
            </div>
        </nav>
    );
};

export default Navbar;