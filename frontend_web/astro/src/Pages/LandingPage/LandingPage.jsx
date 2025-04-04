import React from 'react';
import './LandingPage.css';
import '../../components/NavBarStyle/NavBar.css';
import Navbar from '../../components/./NavBar/NavBar';

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

