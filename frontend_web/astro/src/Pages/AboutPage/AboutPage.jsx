
import React from 'react';
import styles from './AboutPage.module.css';
import NavBar from '../../components/NavBar/NavBar';
import AudioUploader from '../../components/AudioUploader';


const AboutPage = () => {
    return (
        <div className={styles.AboutPage}>
            <NavBar />
            <div className={styles.aboutPageHeroSection}>
                <h1 className={styles.aboutPageHeroTitle}>About Us</h1>
                <p className={styles.aboutPageHeroSubTitle}>
                    "Where Music Meets the Universe – Get to Know AstroGlow's Creators."
                </p>
            </div>
            <div className={styles.aboutSection}>

                <div className={styles.creatorContainer}>
                    <img src="cg-pfp.jpg" alt="Cg Fernandez" className={styles.creatorImage} />
                    <div className={styles.creatorInfo}>
                        <h1 className={styles.name}>Cg Fernandez</h1>
                        {/* <p className={styles.course}>Mobile Frontend Development</p> */}
                        <p className="text-[22px]">Mobile Frontend Development</p>


                    </div>
                </div>
                <div className={styles.creatorContainer}>
                    <img src="john-pfp.png" alt="John Gabriel Cañal" className={styles.creatorImage} />
                    <div className={styles.creatorInfo}>
                        <h1 className={styles.name}>John Gabriel Cañal</h1>
                        {/* <p className={styles.course}>Web Frontend Development</p> */}
                        <p className="text-[22px]">Web Frontend Development</p>

                    </div>
                </div>
                <div className={styles.creatorContainer}>
                    <img src="allen-pfp.jpg" alt="Allen" className={styles.allencreatorImage} />
                    <div className={styles.creatorInfo}>
                        <h1 className={styles.name}>Allen Luis Mangoroban</h1>
                        {/* <p className={styles.course}>Backend Developer</p> */}
                        <p className="text-[22px]">Backend Developer</p>
                    </div>
                </div>
            </div>
            <AudioUploader />

        </div>
    );
};

export default AboutPage;
