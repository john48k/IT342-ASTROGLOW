import React from 'react';
import styles from './UserProfile.module.css';
import NavBar from '../../components/NavBar/NavBar';
import { Link } from 'react-router-dom';

export const UserProfile = () => {
    // Sample user data - in a real app, this would come from your authentication system
    const user = {
        email: "CgFernandez.@gmail.com"
    };

    // Extract name from email (everything before @)
    const userName = user.email.split('@')[0].replace(/\./g, ' ');
    // Capitalize each word in the name
    const formattedName = userName.split(' ')
        .map(word => word.charAt(0).toUpperCase() + word.slice(1))
        .join(' ');

    // Handle logout function
    const handleLogout = () => {
        // In a real app, this would handle the logout process
        console.log("User logged out");
        // Redirect to login page or home page
    };

    return (
        <div className={styles.userInfoPage}>
            {/* Keep the existing navbar */}
            <NavBar />

            <div className={styles.container}>
                {/* Sidebar */}
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

                {/* Main content area */}
                <main className={styles.mainContent}>
                    <div className={styles.profileCard}>
                        <div className={styles.profileAvatar}>
                            {formattedName.charAt(0)}
                        </div>

                        <h1 className={styles.welcomeTitle}>Welcome, {formattedName}</h1>
                        <p className={styles.emailText}>{user.email}</p>

                        <button className={styles.logoutButton} onClick={handleLogout}>
                            Logout
                        </button>
                    </div>
                </main>
            </div>
        </div>
    );
};

export default UserProfile;
