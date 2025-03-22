import React from 'react';
import styles from './HomePage.module.css';
import NavBar from '../../components/NavBar/NavBar';
import { Link } from 'react-router-dom';

export const HomePage = () => {
    return (
        <div className={styles.homePage}>
            <NavBar />
            <div className={styles.container}>
                <aside className={styles.sidebar}>
                    <ul>
                        <div className={styles.libraryHeader}>
                            <img className={styles.libraryLogo} src="library-music.png" alt="" />
                            <p>Your Library</p>
                        </div>
                        <Link to="/" className="">Your Home</Link>
                        <br />
                        <Link to="/" className="">Favorites</Link>

                    </ul>
                </aside>
                <main className={styles.mainContent}>
                    <h1 className={styles.nameTitle}>Good Day "USER NAME"</h1>
                    <button className={styles.uploadBtn}>
                        <img src="upload-arrow.png" alt="Upload" className={styles.uploadIcon} />
                        upload
                    </button>
                </main>
            </div>
        </div>
    );
};

export default HomePage;