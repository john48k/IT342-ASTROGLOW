import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import NavBar from "../../components/NavBar/NavBar";
import styles from "./HomePage.module.css";

export const HomePage = () => {
  const [userName, setUserName] = useState("Guest");
  const [userEmail, setUserEmail] = useState("");

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

  useEffect(() => {
    // Fetch user info from the backend
    fetch("http://localhost:8080/api/user/user-info")
      .then((response) => response.json())
      .then((data) => {
        if (data.name) {
          setUserName(data.name);
        }
        if (data.email) {
          setUserEmail(data.email);
        }
      })
      .catch((error) => console.error("Error fetching user info:", error));
  }, []);

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
            {userEmail && (
              <p className={styles.emailTitle}>Email: {userEmail}</p>
            )}
            <button className={styles.uploadBtn}>
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
    </div>
  );
};

export default HomePage;
