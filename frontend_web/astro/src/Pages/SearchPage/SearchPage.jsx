import React, { useState, useEffect, useRef } from 'react';
import { useLocation } from 'react-router-dom';
import NavBar from '../../components/NavBar/NavBar';
import Sidebar from '../../components/Sidebar/Sidebar';
import { useAudioPlayer } from '../../context/AudioPlayerContext';
import { useFavorites } from '../../context/FavoritesContext';
import styles from './SearchPage.module.css';

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

const SearchPage = () => {
  const location = useLocation();
  const searchParams = new URLSearchParams(location.search);
  const query = searchParams.get('q') || '';

  const [isLoading, setIsLoading] = useState(true);
  const [databaseResults, setDatabaseResults] = useState([]);
  const [firebaseResults, setFirebaseResults] = useState([]);
  const [error, setError] = useState(null);
  const [isImageLoading, setIsImageLoading] = useState(true);

  const {
    currentlyPlaying,
    isPlaying,
    playMusic,
    togglePlayPause,
    getImageUrl,
    setMusicCategory,
    stopPlayback
  } = useAudioPlayer();

  const { isFavorite, toggleFavorite } = useFavorites();

  // Add refs for tracking double clicks and click prevention similar to HomePage
  const lastClickTimeRef = useRef({});
  const doubleClickThreshold = 300; // milliseconds
  const isProcessingClickRef = useRef(false); // Track if we're currently processing a click
  const lockoutTimerRef = useRef(null); // For click lockout

  useEffect(() => {
    if (!query) {
      setIsLoading(false);
      return;
    }

    const fetchSearchResults = async () => {
      setIsLoading(true);
      setError(null);

      try {
        // Instead of using a general search endpoint, we'll search by title, artist, and genre separately
        // and combine the results
        const results = new Set(); // Use a Set to avoid duplicates

        // Search by title
        const titleResponse = await fetch(`https://astroglowfirebase-d2411.uc.r.appspot.com/api/music/search/title?title=${encodeURIComponent(query)}`, {
          credentials: 'include'
        });

        if (titleResponse.ok) {
          const titleData = await titleResponse.json();
          titleData.forEach(item => results.add(JSON.stringify(item)));
        }

        // Search by artist
        const artistResponse = await fetch(`https://astroglowfirebase-d2411.uc.r.appspot.com/api/music/search/artist?artist=${encodeURIComponent(query)}`, {
          credentials: 'include'
        });

        if (artistResponse.ok) {
          const artistData = await artistResponse.json();
          artistData.forEach(item => results.add(JSON.stringify(item)));
        }

        // Search by genre
        const genreResponse = await fetch(`https://astroglowfirebase-d2411.uc.r.appspot.com/api/music/search/genre?genre=${encodeURIComponent(query)}`, {
          credentials: 'include'
        });

        if (genreResponse.ok) {
          const genreData = await genreResponse.json();
          genreData.forEach(item => results.add(JSON.stringify(item)));
        }

        // Convert back from Set of strings to array of objects
        const combinedResults = Array.from(results).map(item => JSON.parse(item));
        setDatabaseResults(combinedResults);

        // Fetch Firebase results from localStorage
        const firebaseMusicList = localStorage.getItem('firebase-music-list');
        if (firebaseMusicList) {
          const parsedFirebaseMusic = JSON.parse(firebaseMusicList);

          // Filter Firebase music based on search query
          const lowercaseQuery = query.toLowerCase();
          const matchingFirebaseMusic = parsedFirebaseMusic.filter(music => {
            return (
              (music.title && music.title.toLowerCase().includes(lowercaseQuery)) ||
              (music.artist && music.artist.toLowerCase().includes(lowercaseQuery)) ||
              (music.genre && music.genre.toLowerCase().includes(lowercaseQuery))
            );
          });

          setFirebaseResults(matchingFirebaseMusic);
        }

        setIsLoading(false);
      } catch (error) {
        console.error('Error searching music:', error);
        setError('Failed to search music. Please try again.');
        setIsLoading(false);
      }
    };

    fetchSearchResults();
  }, [query]);

  // Strict function to prevent any music playback during lockout period (from HomePage)
  const lockPlayback = (duration = 500) => {
    // Set processing flag to prevent any clicks
    isProcessingClickRef.current = true;

    // Clear any existing timer
    if (lockoutTimerRef.current) {
      clearTimeout(lockoutTimerRef.current);
    }

    // Set a new timer
    lockoutTimerRef.current = setTimeout(() => {
      isProcessingClickRef.current = false;
      lockoutTimerRef.current = null;
    }, duration);
  };

  // Updated to match HomePage's implementation for consistent behavior
  const handleMusicCardClick = (e, musicId, audioUrl, category) => {
    // If event exists, stop propagation
    if (e) e.stopPropagation();

    // Hard lockout - prevent any clicks during processing
    if (isProcessingClickRef.current) {
      console.log('Ignoring click during lockout period');
      return;
    }

    // Immediately lock to prevent multiple rapid clicks
    lockPlayback(500);

    // Check for double-clicks
    const now = Date.now();
    const lastClickTime = lastClickTimeRef.current[musicId] || 0;
    lastClickTimeRef.current[musicId] = now;

    // If this is a double-click, ignore the second click
    if (now - lastClickTime < doubleClickThreshold) {
      console.log('Double-click detected, ignoring second click');
      return;
    }

    // If the clicked card is already playing, toggle play/pause
    if (currentlyPlaying === musicId) {
      togglePlayPause(musicId);
      return;
    }

    try {
      // If any other song is playing, completely stop it first
      if (currentlyPlaying) {
        console.log('Stopping current playback before playing new song');
        stopPlayback();
      }

      // Ensure we've completely stopped before playing new song
      setTimeout(() => {
        try {
          console.log(`Playing music ID: ${musicId}, Category: ${category}`);
          setMusicCategory(category);
          playMusic(musicId, audioUrl, category);
        } catch (err) {
          console.error('Error playing music:', err);
          // Reset lock if there was an error
          isProcessingClickRef.current = false;
        }
      }, 100);
    } catch (err) {
      console.error('Error in music card click handler:', err);
      // Reset lock if there was an error
      isProcessingClickRef.current = false;
    }
  };

  // Handle play button click
  const handlePlayClick = (e, musicId, audioUrl = null, category) => {
    e.stopPropagation();
    handleMusicCardClick(e, musicId, audioUrl, category);
  };

  // Calculate total number of results
  const totalResults = databaseResults.length + firebaseResults.length;

  return (
    <div className={styles.searchPage}>
      <NavBar />
      <div className={styles.pageContent}>
        <Sidebar />
        <main className={styles.mainContent}>
          <h1 className={styles.pageTitle}>Search Results for "{query}"</h1>

          {isLoading ? (
            <div className={styles.loading}>Searching...</div>
          ) : error ? (
            <div className={styles.error}>{error}</div>
          ) : totalResults === 0 ? (
            <div className={styles.noResults}>
              <h2>No results found</h2>
              <p>Try different keywords or check your spelling.</p>
            </div>
          ) : (
            <div className={styles.resultsContainer}>
              <div className={styles.resultsSummary}>
                Found {totalResults} results
              </div>

              {/* Database Results */}
              {databaseResults.length > 0 && (
                <section className={styles.resultsSection}>
                  <h2 className={styles.sectionTitle}>From Your Library</h2>
                  <div className={styles.musicGrid}>
                    {databaseResults.map((music) => {
                      const imageUrl = getSafeImageUrl(music.imageUrl, getImageUrl);
                      const isCurrentlyPlaying = currentlyPlaying === music.musicId;
                      const isFavorited = isFavorite(music.musicId);

                      return (
                        <div
                          key={music.musicId}
                          className={`${styles.musicCard} ${isCurrentlyPlaying ?
                            (!isPlaying ? styles.pausedCard : styles.currentlyPlayingCard) : ''}`}
                          onClick={(e) => handleMusicCardClick(e, music.musicId, music.audioUrl, 'uploaded')}
                        >
                          <div className={styles.musicImageContainer}>
                            {imageUrl ? (
                              <img
                                src={imageUrl}
                                alt={music.title}
                                className={styles.musicImage}
                                onLoad={() => setIsImageLoading(false)}
                                onError={(e) => {
                                  e.target.onerror = null;
                                  e.target.style.display = 'none';
                                  const placeholderElement = e.target.parentNode.querySelector(`.${styles.musicPlaceholder}`);
                                  if (placeholderElement) {
                                    placeholderElement.style.display = 'flex';
                                  }
                                  setIsImageLoading(false);
                                }}
                              />
                            ) : (
                              <div className={styles.musicPlaceholder}>
                                <span>{music.title ? music.title.charAt(0).toUpperCase() : '♪'}</span>
                              </div>
                            )}
                            <div className={styles.musicOverlay}></div>
                            <button
                              className={styles.musicPlayButton}
                              onClick={(e) => handlePlayClick(e, music.musicId, music.audioUrl, 'uploaded')}
                            >
                              {isCurrentlyPlaying && isPlaying ? '❚❚' : '▶'}
                            </button>
                            <button
                              className={`${styles.favoriteButton} ${isFavorited ? styles.favorited : ''}`}
                              onClick={(e) => {
                                e.stopPropagation();
                                toggleFavorite(music.musicId);
                              }}
                              title={isFavorited ? "Remove from favorites" : "Add to favorites"}
                            >
                              {isFavorited ? '★' : '☆'}
                            </button>
                          </div>
                          <div className={styles.musicInfo}>
                            <h3 className={styles.musicTitle}>{music.title || 'Untitled'}</h3>
                            <p className={styles.musicArtist}>{music.artist || 'Unknown Artist'}</p>
                            {music.genre && <p className={styles.musicGenre}>{music.genre}</p>}
                          </div>
                        </div>
                      );
                    })}
                  </div>
                </section>
              )}

              {/* Firebase Results */}
              {firebaseResults.length > 0 && (
                <section className={styles.resultsSection}>
                  <h2 className={styles.sectionTitle}>Available Music</h2>
                  <div className={styles.musicGrid}>
                    {firebaseResults.map((music) => {
                      const imageUrl = getSafeImageUrl(music.imageUrl, getImageUrl);
                      const isCurrentlyPlaying = currentlyPlaying === music.id;
                      const isFavorited = isFavorite(music.id);

                      return (
                        <div
                          key={music.id}
                          className={`${styles.musicCard} ${isCurrentlyPlaying ?
                            (!isPlaying ? styles.pausedCard : styles.currentlyPlayingCard) : ''}`}
                          onClick={(e) => handleMusicCardClick(e, music.id, music.audioUrl, 'available')}
                        >
                          <div className={styles.musicImageContainer}>
                            {imageUrl ? (
                              <img
                                src={imageUrl}
                                alt={music.title}
                                className={styles.musicImage}
                                onLoad={() => setIsImageLoading(false)}
                                onError={(e) => {
                                  e.target.onerror = null;
                                  e.target.style.display = 'none';
                                  const placeholderElement = e.target.parentNode.querySelector(`.${styles.musicPlaceholder}`);
                                  if (placeholderElement) {
                                    placeholderElement.style.display = 'flex';
                                  }
                                  setIsImageLoading(false);
                                }}
                              />
                            ) : (
                              <div className={styles.musicPlaceholder}>
                                <span>{music.title ? music.title.charAt(0).toUpperCase() : '♪'}</span>
                              </div>
                            )}
                            <div className={styles.musicOverlay}></div>
                            <button
                              className={styles.musicPlayButton}
                              onClick={(e) => handlePlayClick(e, music.id, music.audioUrl, 'available')}
                            >
                              {isCurrentlyPlaying && isPlaying ? '❚❚' : '▶'}
                            </button>
                            <button
                              className={`${styles.favoriteButton} ${isFavorited ? styles.favorited : ''}`}
                              onClick={(e) => {
                                e.stopPropagation();
                                toggleFavorite(music.id);
                              }}
                              title={isFavorited ? "Remove from favorites" : "Add to favorites"}
                            >
                              {isFavorited ? '★' : '☆'}
                            </button>
                          </div>
                          <div className={styles.musicInfo}>
                            <h3 className={styles.musicTitle}>{music.title || 'Untitled'}</h3>
                            <p className={styles.musicArtist}>{music.artist || 'Unknown Artist'}</p>
                            {music.genre && <p className={styles.musicGenre}>{music.genre}</p>}
                            <p className={styles.uploadedBy}>Uploaded by: {music.userName || 'Unknown User'}</p>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                </section>
              )}
            </div>
          )}
        </main>
      </div>
    </div>
  );
};

export default SearchPage; 