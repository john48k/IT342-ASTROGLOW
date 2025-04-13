import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import NavBar from "../../components/NavBar/NavBar";
import Sidebar from "../../components/Sidebar/Sidebar";
import { useUser } from "../../context/UserContext";
import { useAudioPlayer } from "../../context/AudioPlayerContext";
import { useFavorites } from "../../context/FavoritesContext";
import styles from "./FavoritesPage.module.css";

// Helper function to check if a string is a data URI
const isDataUri = (str) => {
  if (!str) return false;
  return str.startsWith('data:');
};

// Helper function to safely get image URL
const getSafeImageUrl = (imageUrl, getImageUrl) => {
  if (!imageUrl) return null;
  if (isDataUri(imageUrl)) return imageUrl;
  return getImageUrl(imageUrl);
};

export const FavoritesPage = () => {
  const { user } = useUser();
  const { 
    currentlyPlaying, 
    isPlaying, 
    playMusic, 
    togglePlayPause,
    getImageUrl
  } = useAudioPlayer();
  
  const { isFavorite, toggleFavorite } = useFavorites();
  
  const [favoriteMusic, setFavoriteMusic] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [imageUrls, setImageUrls] = useState({});

  // Function to fetch image URL for a music item
  const fetchImageUrl = async (musicId) => {
    try {
      const response = await fetch(
        `http://localhost:8080/api/music/getMusic/${musicId}?includeAudioData=false`,
        {
          method: 'GET',
          headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            'Cache-Control': 'no-cache, no-store, must-revalidate',
            'Pragma': 'no-cache'
          },
          credentials: 'include'
        }
      );

      if (!response.ok) {
        console.error(`HTTP error! status: ${response.status}`);
        return null;
      }

      const data = await response.json();
      return data.imageUrl;
    } catch (error) {
      console.error('Error fetching image URL:', error);
      return null;
    }
  };

  // Function to fetch all image URLs for the favorite music
  const fetchAllImageUrls = async (musicList) => {
    const newImageUrls = { ...imageUrls };
    
    for (const music of musicList) {
      if (!newImageUrls[music.musicId]) {
        const imageUrl = await fetchImageUrl(music.musicId);
        if (imageUrl) {
          newImageUrls[music.musicId] = imageUrl;
        }
      }
    }
    
    setImageUrls(newImageUrls);
  };

  const fetchFavoriteMusic = async (retryCount = 0) => {
    if (!user?.userId) {
      console.log('No user ID available');
      setIsLoading(false);
      return;
    }

    try {
      setIsLoading(true);
      setError(null);
      console.log(`Fetching favorite music for user ID: ${user.userId} (attempt ${retryCount + 1})`);

      // Create an AbortController to handle timeouts
      const controller = new AbortController();
      const timeoutId = setTimeout(() => controller.abort(), 10000); // 10 second timeout

      const response = await fetch(
        `http://localhost:8080/api/favorites/user/${user.userId}/music-details`,
        {
          method: 'GET',
          headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            'Cache-Control': 'no-cache, no-store, must-revalidate',
            'Pragma': 'no-cache',
            'Connection': 'close'
          },
          credentials: 'include',
          signal: controller.signal
        }
      );

      clearTimeout(timeoutId);

      if (!response.ok) {
        console.error(`HTTP error! status: ${response.status}, statusText: ${response.statusText}`);
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      // Get the response as text first to debug any JSON parsing issues
      const responseText = await response.text();
      console.log('Response text:', responseText.substring(0, 100) + '...'); // Log first 100 chars for debugging
      
      // Try to parse the JSON
      let musicData;
      try {
        musicData = JSON.parse(responseText);
      } catch (parseError) {
        console.error('JSON parse error:', parseError);
        console.error('Response text that failed to parse:', responseText);
        throw new Error(`Failed to parse JSON: ${parseError.message}`);
      }
      
      console.log(`Fetched ${musicData.length} favorite music items for user ID: ${user.userId}`);
      setFavoriteMusic(Array.isArray(musicData) ? musicData : []);
      
      // Fetch image URLs separately
      if (Array.isArray(musicData) && musicData.length > 0) {
        fetchAllImageUrls(musicData);
      }
      
      setIsLoading(false);
    } catch (error) {
      console.error('Error fetching favorite music:', error);
      
      if (error.name === 'AbortError') {
        setError('Request timed out. Please try again.');
      } else {
        setError(`Failed to load favorites: ${error.message}`);
      }
      
      setIsLoading(false);
      
      // Retry logic
      if (retryCount < 3) {
        console.log(`Retrying fetch (attempt ${retryCount + 1} of 3)...`);
        const delay = Math.pow(2, retryCount) * 1000;
        await new Promise(resolve => setTimeout(resolve, delay));
        return fetchFavoriteMusic(retryCount + 1);
      }
    }
  };

  useEffect(() => {
    fetchFavoriteMusic();
  }, [user?.userId]);

  const handleRetry = () => {
    fetchFavoriteMusic();
  };

  // Handle play button click
  const handlePlayClick = (e, musicId) => {
    e.stopPropagation();
    
    if (currentlyPlaying === musicId) {
      // If already playing this track, toggle play/pause
      togglePlayPause(musicId);
    } else {
      // If not playing this track, start playing it
      playMusic(musicId);
    }
  };

  // Handle click on music card
  const handleMusicCardClick = (e, musicId) => {
    e.stopPropagation();
    
    if (currentlyPlaying === musicId) {
      // If this is the current track, toggle play/pause
      togglePlayPause(musicId);
    } else {
      // If not the current track, start playing it
      playMusic(musicId);
    }
  };

  // Handle favorite button click
  const handleFavoriteClick = (e, musicId) => {
    e.stopPropagation();
    toggleFavorite(musicId);
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
            <div className={styles.error}>
              {error}
              <button 
                onClick={handleRetry}
                className={styles.retryButton}
              >
                Retry
              </button>
            </div>
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
                const imageUrl = getSafeImageUrl(imageUrls[music.musicId], getImageUrl);
                const isCurrentlyPlaying = currentlyPlaying === music.musicId;
                const isFavorited = isFavorite(music.musicId);
                
                return (
                  <div 
                    key={`favorite-${music.musicId}`} 
                    className={`${styles.musicCard} ${isCurrentlyPlaying ? 
                      (!isPlaying ? styles.pausedCard : styles.currentlyPlayingCard) : ''}`}
                    onClick={(e) => handleMusicCardClick(e, music.musicId)}
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
                      <div className={styles.musicOverlay}></div>
                      <button 
                        className={styles.musicPlayButton}
                        onClick={(e) => handlePlayClick(e, music.musicId)}
                      >
                        {isCurrentlyPlaying && isPlaying ? '❚❚' : '▶'}
                      </button>
                      <button 
                        className={`${styles.favoriteButton} ${isFavorited ? styles.favorited : ''}`}
                        onClick={(e) => handleFavoriteClick(e, music.musicId)}
                        title={isFavorited ? "Remove from favorites" : "Add to favorites"}
                      >
                        {isFavorited ? '★' : '☆'}
                      </button>
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