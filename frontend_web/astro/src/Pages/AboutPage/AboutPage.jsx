import React from 'react';
import './AboutPage.css';
import NavBar from '../../components/NavBar/NavBar';

const AboutPage = () => {
    return (
        <div className="AboutPage">
            <NavBar />
            <div className="hero-section">
                <h1 className='hero-title'>About Us</h1>
                <p className='hero-sub-title'>"Where Music Meets the Universe – Get to Know AstroGlow's Creators."</p>
            </div>
            <div className="cards-section">
                <div className="card">
                    <img src="john-pfp.png" alt="Profile 1" className="card-image" />
                    <div className="card-content">
                        <h2 className="card-name">John Gabriel Cañal</h2>
                        <p className="card-course">BSIT-3</p>
                    </div>
                </div>
                <div className="card">
                    <img src="allen-pfp.jpg" alt="Profile 2" className="card-image" />
                    <div className="card-content">
                        <h2 className="card-name">Allen Luis Mangoroban</h2>
                        <p className="card-course">BSIT-3</p>
                    </div>
                </div>
                <div className="card">
                    <img src="cg-pfp.jpg" alt="Profile 3" className="card-image" />
                    <div className="card-content">
                        <h2 className="card-name">Cg Fernandez</h2>
                        <p className="card-course">BSIT-3</p>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default AboutPage;