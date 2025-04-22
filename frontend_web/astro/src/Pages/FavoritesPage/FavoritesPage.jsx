import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import NavBar from "../../components/NavBar/NavBar";
import Sidebar from "../../components/Sidebar/Sidebar";
import { useUser } from "../../context/UserContext";
import { useAudioPlayer } from "../../context/AudioPlayerContext";
import { useFavorites } from "../../context/FavoritesContext";
import { usePlaylist } from "../../context/PlaylistContext";
import PlaylistModal from "../../components/PlaylistModal/PlaylistModal";
import { registerHomePageNavHandlers, unregisterHomePageNavHandlers } from "../../components/NowPlayingBar/NowPlayingBar";
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
    getImageUrl,
    setMusicCategory,
    playNextSong,
    playPreviousSong,
    stopPlayback
  } = useAudioPlayer();
  
  const { isFavorite, toggleFavorite, favorites } = useFavorites();
  const { openPlaylistModal } = usePlaylist();
  
  const [favoriteMusic, setFavoriteMusic] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [imageUrls, setImageUrls] = useState({});
  // Add state to store Firebase music cache
  const [firebaseMusicCache, setFirebaseMusicCache] = useState({});
  // Add albums state for playback functionality
  const [albums, setAlbums] = useState([]);

  // Register handlers for next/previous navigation
  useEffect(() => {
    // Register our handlers with the NowPlayingBar
    registerHomePageNavHandlers(handleNextSong, handlePreviousSong);
    
    // Clean up on unmount
    return () => {
      unregisterHomePageNavHandlers();
    };
  }, [favoriteMusic, currentlyPlaying]);

  // Load Firebase music cache from localStorage
  useEffect(() => {
    try {
      const savedFirebaseMusic = localStorage.getItem('firebase-music-list');
      if (savedFirebaseMusic) {
        const parsedItems = JSON.parse(savedFirebaseMusic);
        console.log(`[FavoritesPage] Loaded ${parsedItems.length} Firebase items from cache`);
        
        // Create a map for faster lookups
        const cacheMap = {};
        parsedItems.forEach(item => {
          if (item.id && item.audioUrl) {
            cacheMap[item.id] = item;
          }
        });
        
        setFirebaseMusicCache(cacheMap);
      }
    } catch (error) {
      console.error('[FavoritesPage] Error loading Firebase music cache:', error);
    }
  }, []);

  // Handler for next song button - navigates only within favorites
  const handleNextSong = () => {
    console.log("Next song requested in FavoritesPage");
    
    if (favoriteMusic && favoriteMusic.length > 0) {
      // Make sure we're in favorites mode
      setMusicCategory('favorites');
      
      // Add displayIndex to ensure proper order based on visual presentation
      const favoritesWithIndex = favoriteMusic.map((item, index) => ({
        ...item,
        displayIndex: index,
        originalIndex: index, // Add this for consistent sorting
        category: 'favorites'
      }));
      
      console.log(`Playing next song from favorites list with ${favoritesWithIndex.length} items`);
      
      // Find current playing index
      const currentIndex = favoritesWithIndex.findIndex(item => {
        const itemId = String(item.musicId || '');
        return itemId === String(currentlyPlaying);
      });
      
      console.log(`Current playing index: ${currentIndex}`);
      
      // Manual calculation of next track index
      if (currentIndex !== -1 && currentIndex < favoritesWithIndex.length - 1) {
        // There is a next track
        const nextTrack = favoritesWithIndex[currentIndex + 1];
        console.log(`Will play next track: ${nextTrack.title || 'Unknown'}`);
        
        // Store the ordered favorites for next/previous navigation
        localStorage.setItem('favorites-music-list', JSON.stringify(favoritesWithIndex));
        
        // Stop current playback first to avoid overlapping audio
        stopPlayback().then(() => {
          // Play the next track directly
          if (nextTrack.musicId.startsWith('firebase-') && nextTrack.audioUrl) {
            playMusic(nextTrack.musicId, nextTrack.audioUrl, 'favorites');
          } else {
            playMusic(nextTrack.musicId, null, 'favorites');
          }
        });
      } else if (currentIndex === favoritesWithIndex.length - 1) {
        // Loop back to the first track
        const firstTrack = favoritesWithIndex[0];
        console.log(`Will loop back to first track: ${firstTrack.title || 'Unknown'}`);
        
        // Store the ordered favorites
        localStorage.setItem('favorites-music-list', JSON.stringify(favoritesWithIndex));
        
        // Stop current playback first
        stopPlayback().then(() => {
          // Play the first track
          if (firstTrack.musicId.startsWith('firebase-') && firstTrack.audioUrl) {
            playMusic(firstTrack.musicId, firstTrack.audioUrl, 'favorites');
          } else {
            playMusic(firstTrack.musicId, null, 'favorites');
          }
        });
      } else {
        // Fall back to using playNextSong function
        localStorage.setItem('favorites-music-list', JSON.stringify(favoritesWithIndex));
        playNextSong(favoritesWithIndex, []);
      }
    } else {
      console.log("No favorites available for next song");
    }
  };
  
  // Handler for previous song button - navigates only within favorites
  const handlePreviousSong = () => {
    console.log("Previous song requested in FavoritesPage");
    
    if (favoriteMusic && favoriteMusic.length > 0) {
      // Make sure we're in favorites mode
      setMusicCategory('favorites');
      
      // Add displayIndex to ensure proper order based on visual presentation
      const favoritesWithIndex = favoriteMusic.map((item, index) => ({
        ...item,
        displayIndex: index,
        originalIndex: index, // Add this for consistent sorting
        category: 'favorites'
      }));
      
      console.log(`Playing previous song from favorites list with ${favoritesWithIndex.length} items`);
      
      // Find current playing index
      const currentIndex = favoritesWithIndex.findIndex(item => {
        const itemId = String(item.musicId || '');
        return itemId === String(currentlyPlaying);
      });
      
      console.log(`Current playing index: ${currentIndex}`);
      
      // Manual calculation of previous track index
      if (currentIndex > 0) {
        // There is a previous track
        const prevTrack = favoritesWithIndex[currentIndex - 1];
        console.log(`Will play previous track: ${prevTrack.title || 'Unknown'}`);
        
        // Store the ordered favorites for next/previous navigation
        localStorage.setItem('favorites-music-list', JSON.stringify(favoritesWithIndex));
        
        // Stop current playback first to avoid overlapping audio
        stopPlayback().then(() => {
          // Play the previous track directly
          if (prevTrack.musicId.startsWith('firebase-') && prevTrack.audioUrl) {
            playMusic(prevTrack.musicId, prevTrack.audioUrl, 'favorites');
          } else {
            playMusic(prevTrack.musicId, null, 'favorites');
          }
        });
      } else if (currentIndex === 0) {
        // Loop back to the last track
        const lastTrack = favoritesWithIndex[favoritesWithIndex.length - 1];
        console.log(`Will loop back to last track: ${lastTrack.title || 'Unknown'}`);
        
        // Store the ordered favorites
        localStorage.setItem('favorites-music-list', JSON.stringify(favoritesWithIndex));
        
        // Stop current playback first
        stopPlayback().then(() => {
          // Play the last track
          if (lastTrack.musicId.startsWith('firebase-') && lastTrack.audioUrl) {
            playMusic(lastTrack.musicId, lastTrack.audioUrl, 'favorites');
          } else {
            playMusic(lastTrack.musicId, null, 'favorites');
          }
        });
      } else {
        // Fall back to using playPreviousSong function
        localStorage.setItem('favorites-music-list', JSON.stringify(favoritesWithIndex));
        playPreviousSong(favoritesWithIndex, []);
      }
    } else {
      console.log("No favorites available for previous song");
    }
  };

  // Function to fetch image URL for a music item
  const fetchImageUrl = async (musicId) => {
    try {
      // Skip image fetch for Firebase music
      if (typeof musicId === 'string' && musicId.startsWith('firebase-')) {
        return null;
      }

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
      
      // Combine database favorites with Firebase favorites
      const databaseFavorites = Array.isArray(musicData) ? musicData : [];
      
      // Only get Firebase favorites for the current user
      const firebaseFavorites = favorites.filter(fav => 
        typeof fav.music?.filename === 'string' && 
        fav.music.filename.startsWith('firebase-') &&
        fav.userId === user.userId // Only include favorites for this user
      ).map(fav => {
        // Use cached Firebase music if available
        const musicId = fav.music.filename;
        const cachedItem = firebaseMusicCache[musicId];
        
        if (cachedItem && cachedItem.audioUrl) {
          console.log(`[FavoritesPage] Using cached Firebase URL for ${musicId}`);
          return {
            musicId: musicId,
            title: cachedItem.title || musicId.replace('firebase-', '').replace('.mp3', ''),
            artist: cachedItem.artist || 'Unknown Artist',
            genre: cachedItem.genre || 'Firebase Music',
            audioUrl: cachedItem.audioUrl,
            userId: user.userId // Tag with user ID to ensure it's user-specific
          };
        }
        
        // Fallback to constructed URL if not in cache
        return {
          musicId: musicId,
          title: musicId.replace('firebase-', '').replace('.mp3', ''),
          artist: 'Unknown Artist',
          genre: 'Firebase Music',
          audioUrl: `https://firebasestorage.googleapis.com/v0/b/astroglowfirebase-d2411.appspot.com/o/audios%2F${encodeURIComponent(musicId.replace('firebase-', ''))}?alt=media`,
          userId: user.userId // Tag with user ID
        };
      });

      // Tag database favorites with user ID
      const taggedDatabaseFavorites = databaseFavorites.map(item => ({
        ...item,
        userId: user.userId
      }));

      const allFavorites = [...taggedDatabaseFavorites, ...firebaseFavorites];
      console.log(`Fetched ${allFavorites.length} favorite music items (${databaseFavorites.length} database, ${firebaseFavorites.length} Firebase) for user ${user.userId}`);
      
      // Save to localStorage with proper display indices and user ID
      localStorage.setItem('favorites-music-list', JSON.stringify(
        allFavorites.map((item, index) => ({
          ...item,
          displayIndex: index,
          originalIndex: index,
          category: 'favorites',
          userId: user.userId // Ensure user ID is included
        }))
      ));
      
      setFavoriteMusic(allFavorites);
      
      // Fetch image URLs separately for database music only
      if (databaseFavorites.length > 0) {
        fetchAllImageUrls(databaseFavorites);
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
    
    // Get albums from localStorage
    try {
      const savedAlbums = localStorage.getItem('albums');
      if (savedAlbums) {
        setAlbums(JSON.parse(savedAlbums));
      }
    } catch (error) {
      console.error('Error fetching albums from localStorage:', error);
    }
  }, [user?.userId, firebaseMusicCache]);

  const handleRetry = () => {
    fetchFavoriteMusic();
  };

  // Handle play button click
  const handlePlayClick = (e, musicId) => {
    e.stopPropagation();
    
    // Find the music item to get its URL if it's Firebase music
    const musicItem = favoriteMusic.find(item => item.musicId === musicId);
    
    // Set category to favorites for proper navigation
    setMusicCategory('favorites');
    
    // Save the ordered favorites list to localStorage with proper indices
    const orderedFavorites = favoriteMusic.map((item, index) => ({
      ...item,
      displayIndex: index,
      originalIndex: index,
      category: 'favorites'
    }));
    localStorage.setItem('favorites-music-list', JSON.stringify(orderedFavorites));
    console.log(`Stored ${orderedFavorites.length} favorites in order before playing`);
    
    if (currentlyPlaying === musicId) {
      // If already playing this track, toggle play/pause
      togglePlayPause(musicId);
    } else {
      // If not playing this track, start playing it
      if (musicItem && typeof musicId === 'string' && musicId.startsWith('firebase-') && musicItem.audioUrl) {
        // For Firebase music, pass the direct URL and set category to 'favorites'
        console.log(`Playing Firebase music with ID: ${musicId} and URL: ${musicItem.audioUrl}`);
        playMusic(musicId, musicItem.audioUrl, 'favorites');
      } else {
        // For regular music, just pass the ID
        console.log(`Playing regular music with ID: ${musicId}`);
        playMusic(musicId, null, 'favorites');
      }
    }
  };

  // Handle click on music card
  const handleMusicCardClick = (e, musicId) => {
    e.stopPropagation();
    
    // Find the music item to get its URL if it's Firebase music
    const musicItem = favoriteMusic.find(item => item.musicId === musicId);
    
    // Set category to favorites for proper navigation
    setMusicCategory('favorites');
    
    // Save the ordered favorites list to localStorage with proper indices
    const orderedFavorites = favoriteMusic.map((item, index) => ({
      ...item,
      displayIndex: index,
      originalIndex: index,
      category: 'favorites'
    }));
    localStorage.setItem('favorites-music-list', JSON.stringify(orderedFavorites));
    console.log(`Stored ${orderedFavorites.length} favorites in order before playing`);
    
    if (currentlyPlaying === musicId) {
      // If this is the current track, toggle play/pause
      togglePlayPause(musicId);
    } else {
      // If not the current track, start playing it
      if (musicItem && typeof musicId === 'string' && musicId.startsWith('firebase-') && musicItem.audioUrl) {
        // For Firebase music, pass the direct URL and set category to 'favorites'
        console.log(`Playing Firebase music with ID: ${musicId} and URL: ${musicItem.audioUrl}`);
        playMusic(musicId, musicItem.audioUrl, 'favorites');
      } else {
        // For regular music, just pass the ID
        console.log(`Playing regular music with ID: ${musicId}`);
        playMusic(musicId, null, 'favorites');
      }
    }
  };

  // Handle favorite button click
  const handleFavoriteClick = (e, musicId) => {
    e.stopPropagation();
    console.log(`Toggling favorite for music ID: ${musicId} and user ID: ${user?.userId}`);
    toggleFavorite(musicId);
    
    // Immediately update the favoriteMusic state
    // Only remove this specific music for the current user
    setFavoriteMusic(prevList => 
      prevList.filter(music => !(music.musicId === musicId && music.userId === user?.userId))
    );
  };

  // Handle add to playlist button click
  const handleAddToPlaylistClick = (e, musicId) => {
    e.stopPropagation();
    openPlaylistModal(musicId);
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
              {favoriteMusic.map((music, index) => {
                const imageUrl = getSafeImageUrl(imageUrls[music.musicId], getImageUrl);
                const isCurrentlyPlaying = currentlyPlaying === music.musicId;
                const isFavorited = isFavorite(music.musicId);
                
                // Generate a unique key by combining the music ID and index
                const uniqueKey = `favorite-${music.musicId}-${index}`;
                
                return (
                  <div 
                    key={uniqueKey}
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
                      <button 
                        className={styles.addToPlaylistButton}
                        onClick={(e) => handleAddToPlaylistClick(e, music.musicId)}
                        title="Add to playlist"
                      >
                        +
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
      <PlaylistModal />
    </div>
  );
};

export default FavoritesPage; 