import React from 'react';
import './LandingPage.css';
import Navbar from '../../components/NavBar';

const LandingPage = () => {
    return (
        <div className="landing-page">
            <Navbar />
            <div className="landing-content">
                <h1>Welcome to the Landing Page</h1>
                <p>This is the landing page of our application.</p>
            </div>
        </div>
    );
};

export default LandingPage;