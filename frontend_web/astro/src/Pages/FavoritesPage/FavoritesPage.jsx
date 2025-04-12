import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import NavBar from "../../components/NavBar/NavBar";
import Sidebar from "../../components/Sidebar/Sidebar";
import { useUser } from "../../context/UserContext";
import { useFavorites } from "../../context/FavoritesContext";
import { useAudioPlayer } from "../../context/AudioPlayerContext";
import styles from "./FavoritesPage.module.css";

export const FavoritesPage = () => {
  const { user } = useUser();
  const { toggleFavorite } = useFavorites();
  const {
    currentlyPlaying,
    isPlaying,
    playMusic,
    togglePlayPause,
    getImageUrl
  } = useAudioPlayer();
  const [favoriteMusic, setFavoriteMusic] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  // Helper function to check if a string is a data URI
  const isDataUri = (str) => {
    if (!str) return false;
    return str.startsWith('data:');
  };

  // Helper function to safely get image URL
  const getSafeImageUrl = (imageUrl) => {
    if (!imageUrl) return null;
    if (isDataUri(imageUrl)) return imageUrl;
    return getImageUrl(imageUrl);
  };

  useEffect(() => {
    const fetchFavorites = async () => {
      if (!user?.userId) {
        setIsLoading(false);
        return;
      }

      try {
        setIsLoading(true);
        setError(null);

        // Get all music first
        const musicResponse = await fetch('http://localhost:8080/api/music/getAllMusic');
        if (!musicResponse.ok) {
          throw new Error('Failed to fetch music');
        }
        const allMusic = await musicResponse.json();
        console.log('All music:', allMusic);
        console.log('Sample music item:', allMusic[0]);

        // Get user's favorites
        const favoritesResponse = await fetch(`http://localhost:8080/api/favorites/user/${user.userId}`);
        if (!favoritesResponse.ok) {
          throw new Error('Failed to fetch favorites');
        }
        const userFavorites = await favoritesResponse.json();
        console.log('User favorites:', userFavorites);

        // Get the favoriteIds from the user's favorites
        const userFavoriteIds = userFavorites.map(fav => fav.favoriteId);
        console.log('User favorite IDs:', userFavoriteIds);

        // Filter music to only show ones that have matching favorites
        const favoritedMusic = allMusic.filter(music => {
          console.log('Checking music:', music.title);
          // Check if any of this music's favorites match the user's favorites
          const isFavorited = music.favorites.some(favorite => 
            userFavoriteIds.includes(favorite.favoriteId)
          );
          
          if (isFavorited) {
            console.log('Found favorited music:', music.title);
          }
          return isFavorited;
        });

        console.log('Filtered favorite music:', favoritedMusic);
        setFavoriteMusic(favoritedMusic);
        setIsLoading(false);
      } catch (error) {
        console.error('Error:', error);
        setError('Failed to load favorites. Please try again later.');
        setIsLoading(false);
      }
    };

    fetchFavorites();
  }, [user?.userId]);

  const handlePlayClick = (e, musicId) => {
    e.stopPropagation();
    if (currentlyPlaying === musicId) {
      togglePlayPause();
    } else {
      playMusic(musicId);
    }
  };

  const handleFavoriteClick = async (e, musicId) => {
    e.stopPropagation();
    try {
      await toggleFavorite(musicId);
      
      // Refresh favorites
      const musicResponse = await fetch('http://localhost:8080/api/music/getAllMusic');
      const favoritesResponse = await fetch(`http://localhost:8080/api/favorites/user/${user.userId}`);

      if (musicResponse.ok && favoritesResponse.ok) {
        const allMusic = await musicResponse.json();
        const userFavorites = await favoritesResponse.json();
        const userFavoriteIds = userFavorites.map(fav => fav.favoriteId);

        // Filter music to only show favorited ones
        const favoritedMusic = allMusic.filter(music => 
          music.favorites.some(favorite => userFavoriteIds.includes(favorite.favoriteId))
        );

        setFavoriteMusic(favoritedMusic);
      }
    } catch (error) {
      console.error('Error updating favorites:', error);
    }
  };

  return (
    <div className={styles.favoritesPage}>
      <NavBar />
      <div className={styles.pageContent}>
        <Sidebar />
        <main className={styles.mainContent}>
          <h1 className={styles.pageTitle}>Your Favorites</h1>

          {isLoading ? (
            <div className={styles.loading}>Loading your favorites...</div>
          ) : error ? (
            <div className={styles.error}>{error}</div>
          ) : favoriteMusic.length === 0 ? (
            <div className={styles.noFavorites}>
              <h2>No favorites yet</h2>
              <p>Start adding some music to your favorites!</p>
              <Link to="/home" className={styles.browseLink}>
                Browse Music
              </Link>
            </div>
          ) : (
            <div className={styles.musicGrid}>
              {favoriteMusic.map((music) => {
                if (!music || !music.musicId) {
                  console.log('Skipping invalid music item:', music);
                  return null;
                }
                const imageUrl = getSafeImageUrl(music.imageUrl);
                return (
                  <div 
                    key={`favorite-${music.musicId}`} 
                    className={`${styles.musicCard} ${
                      currentlyPlaying === music.musicId ? styles.currentlyPlayingCard : ''
                    }`}
                  >
                    <div className={styles.musicImageContainer}>
                      {imageUrl ? (
                        <img 
                          src={imageUrl} 
                          alt={music.title} 
                          className={styles.musicImage}
                        />
                      ) : (
                        <div className={styles.musicPlaceholder}>
                          <span>{music?.title?.charAt(0).toUpperCase() || '?'}</span>
                        </div>
                      )}
                      <div className={styles.musicOverlay}>
                        <button
                          className={styles.playButton}
                          onClick={(e) => handlePlayClick(e, music.musicId)}
                        >
                          {currentlyPlaying === music.musicId && isPlaying ? "❚❚" : "▶"}
                        </button>
                        <button 
                          className={`${styles.favoriteButton} ${styles.favorited}`}
                          onClick={(e) => handleFavoriteClick(e, music.musicId)}
                        >
                          ★
                        </button>
                      </div>
                    </div>
                    <div className={styles.musicInfo}>
                      <h3 className={styles.musicTitle}>{music.title || 'Untitled'}</h3>
                      <p className={styles.musicArtist}>{music.artist || 'Unknown Artist'}</p>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </main>
      </div>
    </div>
  );
};

export default FavoritesPage; 