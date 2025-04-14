import React from 'react';
import styles from './AboutPage.module.css';
import NavBar from '../../components/NavBar/NavBar';
import AudioUploader from '../../components/AudioUploader';

const AboutPage = () => {
    return (
        <div className={`${styles.AboutPage} flex flex-col min-h-screen`}>
            <NavBar />
            <div className={styles.aboutPageHeroSection}>
                <div className="relative z-20">
                    <h1 className="text-5xl font-bold text-white mb-6">About Us</h1>
                    <p className="text-2xl text-white max-w-2xl mx-auto px-4">
                        "Where Music Meets the Universe – Get to Know AstroGlow's Creators."
                    </p>
                </div>
            </div>
            <div className="flex flex-col items-center gap-[30px] py-16 px-4 mt-48">
                <div className={`${styles.creatorContainer} hover:scale-102 transition-transform duration-400 mb-8`}>
                    <img
                        src="cg-pfp.jpg"
                        alt="Cg Fernandez"
                        className="w-1/2 h-[250px] object-cover rounded-xl shadow-lg"
                    />
                    <div className="flex-1 text-left">
                        <h1 className="text-3xl font-bold text-white mb-2">Cg Fernandez</h1>
                        <p className="text-xl text-gray-300">Mobile Frontend Development</p>
                    </div>
                </div>

                <div className={`${styles.creatorContainer} hover:scale-102 transition-transform duration-400 mb-8`}>
                    <img
                        src="john-pfp.png"
                        alt="John Gabriel Cañal"
                        className="w-1/2 h-[250px] object-cover rounded-xl shadow-lg"
                    />
                    <div className="flex-1 text-left">
                        <h1 className="text-3xl font-bold text-white mb-2">John Gabriel Cañal</h1>
                        <p className="text-xl text-gray-300">Web Frontend Development</p>
                    </div>
                </div>

                <div className={`${styles.creatorContainer} hover:scale-102 transition-transform duration-300 mb-8`}>
                    <img
                        src="allen-pfp.jpg"
                        alt="Allen"
                        className="w-1/2 h-[250px] object-cover rounded-xl shadow-lg"
                    />
                    <div className="flex-1 text-left">
                        <h1 className="text-3xl font-bold text-white mb-2">Allen Luis Mangoroban</h1>
                        <p className="text-xl text-gray-300">Backend Developer</p>
                    </div>
                </div>
            </div>
            <AudioUploader />
        </div>
    );
};

export default AboutPage;
