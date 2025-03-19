import React from 'react';
import './LandingPage.css';
import '../../components/Navbar.css'
import Navbar from '../../components/NavBar';

const LandingPage = () => {
    return (
        <div className="landing-page">
            <Navbar />
            <div className="landing-content">
                <h1 className='astro-title-text'>"AstroGlow – Music That Lights Up Your Universe."</h1>
            </div>
        </div>
    );
};

export default LandingPage;