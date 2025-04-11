import React, { useState, useEffect, useRef } from "react";
import { Link } from "react-router-dom";
import NavBar from "../../components/NavBar/NavBar";
import { useUser } from "../../context/UserContext";
import { useFavorites } from "../../context/FavoritesContext";
import styles from "./FavoritesPage.module.css";

export const FavoritesPage = () => {
  const { user } = useUser();
  const { favorites, toggleFavorite } = useFavorites();
  const userName = user?.userName || "Guest";
  const [musicList, setMusicList] = useState([]);
  const [currentlyPlaying, setCurrentlyPlaying] = useState(null);
  const [audioElement, setAudioElement] = useState(null);
  const [audioProgress, setAudioProgress] = useState(0);
  const progressIntervalRef = useRef(null);
  const [audioTime, setAudioTime] = useState({ elapsed: '0:00', total: '0:00' });
  const [isImageLoading, setIsImageLoading] = useState(true);

  // Fetch music list when component mounts
  useEffect(() => {
    fetchMusicList();
  }, []);

  const fetchMusicList = async () => {
    try {
      const response = await fetch('http://localhost:8080/api/music/getAllMusic');
      if (response.ok) {
        const data = await response.json();
        setMusicList(data);
      }
    } catch (error) {
      console.error('Error fetching music list:', error);
    }
  };

  const playMusic = async (musicId) => {
    try {
      // If clicking on currently playing track, just toggle play/pause and return
      if (currentlyPlaying === musicId && audioElement) {
        if (audioElement.paused) {
          audioElement.play()
            .catch(err => {
              console.error('Error playing audio:', err);
              alert('Error playing audio: ' + err.message);
            });
        } else {
          audioElement.pause();
        }
        // Force a re-render to update UI state
        setAudioElement({ ...audioElement });
        return;
      }

      // Stop current audio if playing
      if (audioElement && audioElement.pause && typeof audioElement.pause === 'function') {
        audioElement.pause();
        audioElement.src = '';
        if (progressIntervalRef.current) {
          clearInterval(progressIntervalRef.current);
          progressIntervalRef.current = null;
        }
      }

      // First check if the music has a direct URL
      const musicResponse = await fetch(`http://localhost:8080/api/music/getMusic/${musicId}?includeAudioData=false`);
      if (!musicResponse.ok) {
        throw new Error(`Music with ID ${musicId} not found`);
      }

      const musicData = await musicResponse.json();
      
      // Check if audioUrl exists and use it directly
      if (musicData.audioUrl) {
        console.log("Using external URL for playback:", musicData.audioUrl);
        const audio = new Audio(musicData.audioUrl);
        
        // Set up audio events (same as before)
        const setupAudioEvents = () => {
          audio.onended = () => {
            setCurrentlyPlaying(null);
            setAudioProgress(0);
            if (progressIntervalRef.current) {
              clearInterval(progressIntervalRef.current);
              progressIntervalRef.current = null;
            }
          };

          // Set up progress tracking
          if (progressIntervalRef.current) {
            clearInterval(progressIntervalRef.current);
          }

          progressIntervalRef.current = setInterval(() => {
            if (audio.duration) {
              const progress = (audio.currentTime / audio.duration) * 100;
              setAudioProgress(progress);
              setAudioTime({
                elapsed: formatTime(audio.currentTime),
                total: formatTime(audio.duration)
              });
            }
          }, 500);

          // Force state update on play/pause to update UI
          audio.addEventListener('play', () => {
            setAudioElement(prevAudio => {
              if (prevAudio === audio) return { ...audio };
              return audio;
            });
          });

          audio.addEventListener('pause', () => {
            setAudioElement(prevAudio => {
              if (prevAudio === audio) return { ...audio };
              return audio;
            });
          });
        };

        // Set up events once audio is loaded
        audio.addEventListener('loadeddata', setupAudioEvents);

        // Play the audio
        audio.play().catch(err => {
          console.error('Error playing audio from URL:', err);
          alert('Error playing audio: ' + err.message);
        });

        // Set state
        setAudioElement(audio);
        setCurrentlyPlaying(musicId);
        return;
      }

      // If no audioUrl, proceed with the base64 data fetching
      const response = await fetch(`http://localhost:8080/api/music/audio/${musicId}`);
      if (!response.ok) {
        if (!musicData.audioData) {
          throw new Error(`No audio data available for this music`);
        }
        throw new Error('Failed to fetch audio data');
      }

      const base64Audio = await response.text();
      if (!base64Audio || base64Audio === 'null' || base64Audio.trim() === '') {
        throw new Error('No audio data available for this music');
      }

      // Convert base64 to Blob
      const byteCharacters = atob(base64Audio);
      const byteArrays = [];

      for (let i = 0; i < byteCharacters.length; i += 512) {
        const slice = byteCharacters.slice(i, i + 512);
        const byteNumbers = new Array(slice.length);

        for (let j = 0; j < slice.length; j++) {
          byteNumbers[j] = slice.charCodeAt(j);
        }

        const byteArray = new Uint8Array(byteNumbers);
        byteArrays.push(byteArray);
      }

      const blob = new Blob(byteArrays, { type: 'audio/mpeg' });
      const audioUrl = URL.createObjectURL(blob);

      // Create a new audio element with the blob URL
      const audio = new Audio(audioUrl);

      // Set up event listeners
      const setupAudioEvents = () => {
        // Add event listener to clean up object URL when audio is done
        audio.onended = () => {
          URL.revokeObjectURL(audioUrl);
          setCurrentlyPlaying(null);
          setAudioProgress(0);
          if (progressIntervalRef.current) {
            clearInterval(progressIntervalRef.current);
            progressIntervalRef.current = null;
          }
        };

        // Set up progress tracking
        if (progressIntervalRef.current) {
          clearInterval(progressIntervalRef.current);
        }

        progressIntervalRef.current = setInterval(() => {
          if (audio.duration) {
            const progress = (audio.currentTime / audio.duration) * 100;
            setAudioProgress(progress);
            setAudioTime({
              elapsed: formatTime(audio.currentTime),
              total: formatTime(audio.duration)
            });
          }
        }, 500);

        // Force state update on play/pause to update UI
        audio.addEventListener('play', () => {
          setAudioElement(prevAudio => {
            if (prevAudio === audio) return { ...audio };
            return audio;
          });
        });

        audio.addEventListener('pause', () => {
          setAudioElement(prevAudio => {
            if (prevAudio === audio) return { ...audio };
            return audio;
          });
        });
      };

      // Set up events once audio is loaded
      audio.addEventListener('loadeddata', setupAudioEvents);

      // Play the audio
      audio.play().catch(err => {
        console.error('Error playing audio:', err);
        alert('Error playing audio: ' + err.message);
      });

      // Set state
      setAudioElement(audio);
      setCurrentlyPlaying(musicId);
    } catch (error) {
      console.error('Error playing music:', error);
      alert('Error playing music: ' + error.message);
    }
  };

  const togglePlayPause = (musicId) => {
    if (currentlyPlaying === musicId && audioElement) {
      if (audioElement.paused) {
        audioElement.play()
          .catch(err => {
            console.error('Error playing audio:', err);
            alert('Error playing audio: ' + err.message);
          });
      } else if (audioElement.pause && typeof audioElement.pause === 'function') {
        audioElement.pause();
      }
      // Force a re-render to update UI state
      setAudioElement({ ...audioElement });
    } else {
      // If not the current track, start playing the new track
      playMusic(musicId);
    }
  };

  // Clean up interval on component unmount
  useEffect(() => {
    return () => {
      if (progressIntervalRef.current) {
        clearInterval(progressIntervalRef.current);
      }
      if (audioElement) {
        try {
          if (audioElement.pause && typeof audioElement.pause === 'function') {
            audioElement.pause();
          }
          if (audioElement.src) {
            audioElement.src = '';
          }
        } catch (err) {
          console.error('Error cleaning up audio element:', err);
        }
      }
    };
  }, []);

  // Format time function
  const formatTime = (seconds) => {
    if (isNaN(seconds) || seconds === Infinity) return '0:00';
    const mins = Math.floor(seconds / 60);
    const secs = Math.floor(seconds % 60);

    // Only show minutes if we have them or if it's exactly 0 minutes
    if (mins > 0) {
      return `${mins}:${secs < 10 ? '0' + secs : secs}`;
    } else {
      // Just show seconds for short durations
      return `${secs}s`;
    }
  };

  // Improved data URI check function with more robust detection
  const isDataUri = (str) => {
    if (!str) return false;
    if (typeof str !== 'string') return false;
    
    // Check for common data URI patterns
    if (str.startsWith('data:image/')) return true;
    if (str.startsWith('data:') && str.includes(';base64,')) return true;
    
    return false;
  };

  // Improved function to handle image URLs and data URIs
  const getImageUrl = (url) => {
    if (!url) return null;
    
    // If it's already a data URI, return it as is
    if (isDataUri(url)) {
      console.log("Using data URI for image");
      return url;
    }
    
    // Handle potential malformed URLs
    try {
      // Basic validation for http/https URLs
      if (url.startsWith('http://') || url.startsWith('https://')) {
        return url;
      }
      
      // If URL doesn't have protocol, try to add https://
      if (!url.includes('://') && !url.startsWith('data:')) {
        console.log("Adding https:// to URL:", url);
        return 'https://' + url;
      }
      
      console.warn("Invalid image URL format:", url);
      return null;
    } catch (error) {
      console.error("Error parsing image URL:", error);
      return null;
    }
  };

  // Get only favorite songs
  const favoriteSongs = musicList.filter(music => favorites.includes(music.musicId));

  return (
    <div className={styles.favoritesPage}>
      {/* Animated stars background */}
      <div className={styles.starsBackground}></div>

      {/* Keep the existing navbar */}
      <NavBar />

      <div className={styles.container}>
        {/* Sidebar */}
        <aside className={styles.sidebar}>
          <ul>
            <li>
              <Link to="/home" className={styles.sidebarLink}>
                Your Home
              </Link>
            </li>
            <li>
              <Link to="/favorites" className={styles.sidebarLink}>
                Favorites
              </Link>
            </li>
          </ul>
        </aside>

        {/* Main content area */}
        <main className={styles.mainContent}>
          {/* User greeting */}
          <div className={styles.headerSection}>
            <h1 className={styles.nameTitle}>Your Favorites, {userName}!</h1>
          </div>

          {/* Favorites Section */}
          {favoriteSongs.length > 0 ? (
            <section className={styles.favoritesSection}>
              <h2 className={styles.sectionTitle}>Your Favorite Music</h2>
              <div className={styles.musicGrid}>
                {favoriteSongs.map((music) => {
                  // Process the image URL with improved handler
                  const imageUrl = getImageUrl(music.imageUrl);
                  const isFavorite = favorites.includes(music.musicId);
                  
                  return (
                    <div key={music.musicId}
                      className={`${styles.musicCard} ${currentlyPlaying === music.musicId ?
                        (audioElement && audioElement.paused ? styles.pausedCard : styles.currentlyPlayingCard) : ''}`}
                    >
                      <div className={styles.musicImageContainer}>
                        {imageUrl ? (
                          <img 
                            src={imageUrl} 
                            alt={music.title} 
                            className={styles.musicImage}
                            onLoad={() => {
                              console.log("Image loaded successfully:", imageUrl.substring(0, 50) + '...');
                              setIsImageLoading(false);
                            }}
                            onError={(e) => {
                              console.error("Failed to load image:", imageUrl?.substring(0, 50) + '...');
                              e.target.onerror = null; // Prevent infinite loop
                              e.target.style.display = 'none';
                              // Show the placeholder
                              const placeholderElement = e.target.parentNode.querySelector(`.${styles.musicPlaceholder}`);
                              if (placeholderElement) {
                                placeholderElement.style.display = 'flex';
                              }
                              setIsImageLoading(false);
                            }}
                          />
                        ) : null}
                        <div 
                          className={styles.musicPlaceholder}
                          style={{ display: imageUrl && isImageLoading ? 'none' : 'flex' }}
                        >
                          <span>{music.title ? music.title.charAt(0).toUpperCase() : '♪'}</span>
                        </div>
                        <div className={styles.musicOverlay}></div>
                        <button
                          className={styles.musicPlayButton}
                          onClick={() => togglePlayPause(music.musicId)}
                        >
                          {currentlyPlaying === music.musicId && audioElement && !audioElement.paused ? '❚❚' : '▶'}
                        </button>
                        <button 
                          className={`${styles.favoriteButton} ${isFavorite ? styles.favorited : ''}`}
                          onClick={(e) => {
                            e.stopPropagation();
                            toggleFavorite(music.musicId);
                          }}
                          title={isFavorite ? "Remove from favorites" : "Add to favorites"}
                        >
                          {isFavorite ? '★' : '☆'}
                        </button>
                      </div>
                      <div className={styles.musicInfo}>
                        <h3 className={styles.musicTitle}>{music.title}</h3>
                        <p className={styles.musicArtist}>{music.artist}</p>
                        {music.genre && <p className={styles.musicGenre}>{music.genre}</p>}
                      </div>
                    </div>
                  );
                })}
              </div>
            </section>
          ) : (
            <div className={styles.emptyState}>
              <h2>You don't have any favorites yet</h2>
              <p>Go to the home page and mark some songs as favorites!</p>
              <Link to="/home" className={styles.homeButton}>
                Go to Home
              </Link>
            </div>
          )}
        </main>
      </div>

      {/* Now Playing Bar */}
      {currentlyPlaying && (
        <div className={styles.nowPlayingBar}>
          <div className={styles.nowPlayingContent}>
            <div className={styles.nowPlayingInfo}>
              <div className={styles.nowPlayingImage}>
                {(() => {
                  const currentMusic = musicList.find(m => m.musicId === currentlyPlaying);
                  const imageUrl = currentMusic?.imageUrl ? getImageUrl(currentMusic.imageUrl) : null;
                  
                  if (imageUrl) {
                    return (
                      <img 
                        src={imageUrl} 
                        alt={currentMusic.title} 
                        className={styles.nowPlayingCover}
                        onError={(e) => {
                          e.target.onerror = null;
                          e.target.style.display = 'none';
                          e.target.nextElementSibling.style.display = 'block';
                        }} 
                      />
                    );
                  }
                  return <span className={styles.nowPlayingFallback}>{currentMusic?.title?.charAt(0).toUpperCase() || '♪'}</span>;
                })()}
              </div>
              <div>
                <h4 className={styles.nowPlayingTitle}>
                  {musicList.find(m => m.musicId === currentlyPlaying)?.title}
                </h4>
                <p className={styles.nowPlayingArtist}>
                  {musicList.find(m => m.musicId === currentlyPlaying)?.artist}
                </p>
              </div>
            </div>

            <div className={styles.playbackControls}>
              <button
                className={styles.playbackButton}
                onClick={() => {
                  if (audioElement) {
                    togglePlayPause(currentlyPlaying);
                  }
                }}
              >
                {audioElement && !audioElement.paused ? '❚❚' : '▶'}
              </button>
              <div className={styles.progressContainer}>
                <span className={styles.timeDisplay}>{audioTime.elapsed}</span>
                <div
                  className={styles.progressBar}
                  onClick={(e) => {
                    if (audioElement) {
                      // Calculate click position as percentage of total width
                      const rect = e.currentTarget.getBoundingClientRect();
                      const pos = (e.clientX - rect.left) / rect.width;
                      // Set audio position
                      audioElement.currentTime = pos * audioElement.duration;
                      // Update progress bar
                      setAudioProgress(pos * 100);
                      // Update elapsed time
                      setAudioTime({
                        ...audioTime,
                        elapsed: formatTime(audioElement.currentTime)
                      });
                    }
                  }}
                >
                  <div
                    className={styles.progressFill}
                    style={{ width: `${audioProgress}%` }}
                  ></div>
                </div>
                <span className={styles.timeDisplay}>{audioTime.total}</span>
              </div>
            </div>

            <div className={styles.playerActions}>
              <button
                className={styles.openPlayerButton}
                onClick={() => {
                  if (audioElement) {
                    try {
                      if (audioElement.pause && typeof audioElement.pause === 'function') {
                        audioElement.pause();
                      }
                    } catch (err) {
                      console.error('Error stopping audio:', err);
                    }
                    // Reset state regardless of whether pause succeeded
                    setCurrentlyPlaying(null);
                    setAudioProgress(0);
                    setAudioElement(null);
                    if (progressIntervalRef.current) {
                      clearInterval(progressIntervalRef.current);
                      progressIntervalRef.current = null;
                    }
                  }
                }}
              >
                Stop
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default FavoritesPage; 