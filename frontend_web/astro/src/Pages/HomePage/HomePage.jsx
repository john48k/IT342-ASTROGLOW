import React, { useState } from "react";
import { Link } from "react-router-dom";
import NavBar from "../../components/NavBar/NavBar";
import { useUser } from "../../context/UserContext";
import styles from "./HomePage.module.css";
import Modal from '../../components/Modal/Modal';

export const HomePage = () => {
  const { user } = useUser();
  const userName = user?.userName || "Guest";
  const [showUploadModal, setShowUploadModal] = useState(false);
  const [enteredPassword, setEnteredPassword] = useState('');
  const [selectedFile, setSelectedFile] = useState(null);
  const [uploadError, setUploadError] = useState('');
  const [isUploading, setIsUploading] = useState(false);
  const [isPasswordVerified, setIsPasswordVerified] = useState(false);
  const [passwordError, setPasswordError] = useState('');
  const [showPassword, setShowPassword] = useState(false);

  const handleUploadClick = () => {
    setShowUploadModal(true);
    setIsPasswordVerified(false);
    setEnteredPassword('');
    setSelectedFile(null);
    setUploadError('');
    setPasswordError('');
    setShowPassword(false);
  };

  const verifyPassword = async () => {
    if (!enteredPassword) {
      setPasswordError('Password cannot be empty');
      return;
    }
    
    try {
      const response = await fetch('http://localhost:8080/api/user/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          userEmail: user.userEmail,
          userPassword: enteredPassword
        }),
      });

      if (response.ok) {
        setIsPasswordVerified(true);
        setPasswordError('');
      } else {
        setPasswordError('Incorrect password. Please try again.');
      }
    } catch (error) {
      setPasswordError('Error verifying password. Please try again.');
    }
  };

  const handleFileChange = (e) => {
    const file = e.target.files[0];
    if (file && file.type === 'audio/mpeg') {
      setSelectedFile(file);
      setUploadError('');
    } else {
      setSelectedFile(null);
      setUploadError('Please select an MP3 file');
    }
  };

  const handleUpload = async () => {
    if (!selectedFile) {
      setUploadError('Please select an MP3 file');
      return;
    }

    setIsUploading(true);
    try {
      const formData = new FormData();
      formData.append('file', selectedFile);

      const response = await fetch('http://localhost:8080/api/music/upload', {
        method: 'POST',
        body: formData,
      });

      if (!response.ok) {
        throw new Error('Upload failed');
      }

      // Reset form and close modal
      setEnteredPassword('');
      setSelectedFile(null);
      setShowUploadModal(false);
      setUploadError('');
      setIsPasswordVerified(false);
    } catch (error) {
      setUploadError('Failed to upload file. Please try again.');
    } finally {
      setIsUploading(false);
    }
  };

  const handleCloseModal = () => {
    setShowUploadModal(false);
    setEnteredPassword('');
    setSelectedFile(null);
    setUploadError('');
    setPasswordError('');
    setIsPasswordVerified(false);
  };

  // Sample featured collections data
  const featuredCollections = [
    {
      title: "Sweater Weather",
      description: "The Neighborhood",
      image: "sweater-weather.png",
      track: 1,
    },
    {
      title: "Kanye West",
      description: "Kanye West",
      image: "homecoming.png",
      track: 1,
    },
    {
      title: "StarBoy",
      description: "The Weeknd",
      image: "starboy.png",
      track1: 1,
    },
  ];

  // Sample weekly discoveries data
  const weeklyDiscoveries = [
    "Stellar Voyage",
    "Cosmic Harmony",
    "Galactic Pulse",
    "Lunar Echoes",
    "Solar Flares",
    "Orbital Groove",
  ];

  return (
    <div className={styles.homePage}>
      {/* Animated stars background */}
      <div className={styles.starsBackground}></div>

      {/* Keep the existing navbar */}
      <NavBar />

      <div className={styles.container}>
        {/* Keep the existing sidebar */}
        <aside className={styles.sidebar}>
          <ul>
            <div className={styles.libraryHeader}>
              <img
                className={styles.libraryLogo}
                src="library-music.png"
                alt=""
              />
              <p>Your Library</p>
            </div>
            <Link to="/" className="">
              Your Home
            </Link>
            <br />
            <Link to="/" className="">
              Favorites
            </Link>
          </ul>
        </aside>

        {/* Main content area */}
        <main className={styles.mainContent}>
          {/* User greeting and upload button */}
          <div className={styles.headerSection}>
            <h1 className={styles.nameTitle}>Good Day, {userName}!</h1>
            <button className={styles.uploadBtn} onClick={handleUploadClick}>
              <img
                src="upload-arrow.png"
                alt="Upload"
                className={styles.uploadIcon}
              />
              upload
            </button>
          </div>

          {/* Hero Section */}
          <section className={styles.heroSection}>
            {/* Decorative elements */}
            <div className={styles.purpleGlow}></div>
            <div className={styles.pinkGlow}></div>

            <div className={styles.heroContent}>
              <h1 className={styles.heroTitle}>Your Universe of Sound</h1>
              <p className={styles.heroSubtitle}>
                Explore cosmic soundscapes and stellar rhythms that transcend
                ordinary listening experiences.
              </p>
              <button className={styles.ctaButton}>
                Download Mobile
                <span className={styles.arrowIcon}>→</span>
              </button>
            </div>

            {/* Floating music visualization */}
            <div className={styles.musicVisualization}>
              <div className={styles.playButton}>
                <span className={styles.playIcon}>▶</span>
              </div>

              {/* Audio visualization bars */}
              <div className={styles.equalizerContainer}>
                {[...Array(40)].map((_, i) => (
                  <div
                    key={i}
                    className={styles.equalizerBar}
                    style={{
                      height: `${Math.sin(i * 0.2) * 50 + 50}%`,
                      animationDelay: `${i * 0.05}s`,
                    }}
                  ></div>
                ))}
              </div>
            </div>
          </section>

          {/* Featured Music Section */}
          <section className={styles.featuredSection}>
            <h2 className={styles.sectionTitle}>Featured Songs</h2>

            {/* Featured playlists */}
            <div className={styles.featuredGrid}>
              {featuredCollections.map((playlist, index) => (
                <div key={index} className={styles.playlistCard}>
                  <div className={styles.playlistImageContainer}>
                    <img
                      src={playlist.image || "/placeholder.svg"}
                      alt={playlist.title}
                      className={styles.playlistImage}
                    />
                    <div className={styles.playlistOverlay}></div>
                    <button className={styles.playlistPlayButton}>▶</button>
                  </div>
                  <div className={styles.playlistInfo}>
                    <h3 className={styles.playlistTitle}>{playlist.title}</h3>
                    <p className={styles.playlistDescription}>
                      {playlist.description}
                    </p>
                    <p className={styles.playlistTracks}>{playlist.tracks}</p>
                  </div>
                </div>
              ))}
            </div>

            {/* Weekly Discoveries */}
            <h3 className={styles.subsectionTitle}>Weekly Discoveries</h3>

            <div className={styles.discoveriesGrid}>
              {weeklyDiscoveries.map((track, index) => (
                <div key={index} className={styles.trackCard}>
                  <div className={styles.trackImageContainer}>
                    <img
                      src="placeholder.jpg"
                      alt={track}
                      className={styles.trackImage}
                    />
                    <div className={styles.trackOverlay}></div>
                    <button className={styles.trackPlayButton}>▶</button>
                  </div>
                  <h3 className={styles.trackTitle}>{track}</h3>
                  <p className={styles.trackArtist}>AstroGlow Originals</p>
                </div>
              ))}
            </div>
          </section>
        </main>
      </div>

      {/* Now Playing Bar */}
      <div className={styles.nowPlayingBar}>
        <div className={styles.nowPlayingContent}>
          <div className={styles.nowPlayingInfo}>
            <img
              src="placeholder.jpg"
              alt="Now playing"
              className={styles.nowPlayingImage}
            />
            <div>
              <h4 className={styles.nowPlayingTitle}>Cosmic Voyage</h4>
              <p className={styles.nowPlayingArtist}>Astral Harmonies</p>
            </div>
          </div>

          <div className={styles.playbackControls}>
            <button className={styles.playbackButton}>▶</button>
            <div className={styles.progressBar}>
              <div className={styles.progressFill}></div>
            </div>
          </div>

          <div className={styles.playerActions}>
            <button className={styles.openPlayerButton}>Open Player</button>
          </div>
        </div>
      </div>

      <Modal
        isOpen={showUploadModal}
        onClose={handleCloseModal}
        onConfirm={isPasswordVerified ? handleUpload : verifyPassword}
        title={isPasswordVerified ? "Upload Music" : "Verify Password"}
        message={
          <div className={styles.uploadModalContent}>
            {!isPasswordVerified ? (
              <>
                <div className={styles.passwordInputContainer}>
                  <input
                    type={showPassword ? "text" : "password"}
                    placeholder="Enter your account password"
                    value={enteredPassword}
                    onChange={(e) => setEnteredPassword(e.target.value)}
                    className={styles.passwordInput}
                    onKeyDown={(e) => {
                      if (e.key === 'Enter') {
                        verifyPassword();
                      }
                    }}
                  />
                  <button
                    type="button"
                    className={styles.togglePasswordButton}
                    onClick={() => setShowPassword(!showPassword)}
                    aria-label={showPassword ? "Hide password" : "Show password"}
                  >
                    {showPassword ? (
                      <svg
                        xmlns="http://www.w3.org/2000/svg"
                        width="20"
                        height="20"
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="currentColor"
                        strokeWidth="2"
                        strokeLinecap="round"
                        strokeLinejoin="round"
                      >
                        <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
                        <line x1="1" y1="1" x2="23" y2="23"></line>
                      </svg>
                    ) : (
                      <svg
                        xmlns="http://www.w3.org/2000/svg"
                        width="20"
                        height="20"
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="currentColor"
                        strokeWidth="2"
                        strokeLinecap="round"
                        strokeLinejoin="round"
                      >
                        <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                        <circle cx="12" cy="12" r="3"></circle>
                      </svg>
                    )}
                  </button>
                </div>
                {passwordError && <p className={styles.errorMessage}>{passwordError}</p>}
              </>
            ) : (
              <>
                <p className={styles.verifiedMessage}>Password verified successfully!</p>
                <input
                  type="file"
                  accept=".mp3"
                  onChange={handleFileChange}
                  className={styles.fileInput}
                />
                {selectedFile && (
                  <p className={styles.selectedFile}>Selected: {selectedFile.name}</p>
                )}
                {uploadError && <p className={styles.errorMessage}>{uploadError}</p>}
                {isUploading && <p className={styles.uploadingMessage}>Uploading...</p>}
              </>
            )}
          </div>
        }
      />
    </div>
  );
};

export default HomePage;
