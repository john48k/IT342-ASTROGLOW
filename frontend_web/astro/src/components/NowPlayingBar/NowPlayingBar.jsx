import React, { useRef, useState, useEffect } from 'react';
import { useAudioPlayer } from '../../context/AudioPlayerContext';
import { useLocation } from 'react-router-dom';
import styles from './NowPlayingBar.module.css';

// Add global event listeners for HomePage navigation
let homePageNavHandlers = {
  next: null,
  previous: null
};

// Register HomePage navigation handlers globally
export const registerHomePageNavHandlers = (nextHandler, prevHandler) => {
  homePageNavHandlers.next = nextHandler;
  homePageNavHandlers.previous = prevHandler;
  console.log('HomePage navigation handlers registered');
};

// Unregister handlers when HomePage unmounts
export const unregisterHomePageNavHandlers = () => {
  homePageNavHandlers.next = null;
  homePageNavHandlers.previous = null;
  console.log('HomePage navigation handlers unregistered');
};

const NowPlayingBar = () => {
  const {
    currentlyPlaying,
    audioElement,
    audioProgress,
    audioTime,
    isPlaying,
    currentTrackData,
    formatTime,
    getImageUrl,
    togglePlayPause,
    playNextSong,
    playPreviousSong,
    seekAudio,
    skipForward,
    skipBackward,
    stopPlayback,
    musicCategory,
    setMusicCategory
  } = useAudioPlayer();

  // Get current route to determine if we're on the HomePage
  const location = useLocation();
  const isHomePage = location.pathname === '/home' || location.pathname === '/';

  // Add state for music list and albums
  const [musicList, setMusicList] = useState([]);
  const [albums, setAlbums] = useState([]);

  // Add a hover effect to show potential seek position
  const [hoverPosition, setHoverPosition] = useState(null);

  // Debouncing mechanism to prevent rapid successive clicks
  const isActionAllowedRef = useRef(true);
  const debounceTime = 500; // increased from 300 to 500 milliseconds

  const debounce = (callback) => {
    if (!isActionAllowedRef.current) {
      console.log("Action blocked by debounce");
      return;
    }

    isActionAllowedRef.current = false;
    console.log("Action allowed, debouncing for", debounceTime, "ms");
    callback();

    setTimeout(() => {
      isActionAllowedRef.current = true;
      console.log("Debounce period ended, new actions allowed");
    }, debounceTime);
  };

  // Fetch music list and albums when a track is playing
  useEffect(() => {
    if (currentlyPlaying) {
      // Fetch music list
      const fetchMusicList = async () => {
        try {
          // Regular music from backend
          const response = await fetch('https://astroglowfirebase-d2411.uc.r.appspot.com/api/music/getAllMusic');
          if (response.ok) {
            const data = await response.json();

            // Get Firebase music items from localStorage
            let firebaseItems = [];
            try {
              const savedFirebaseMusic = localStorage.getItem('firebase-music-list');
              if (savedFirebaseMusic) {
                firebaseItems = JSON.parse(savedFirebaseMusic);
                console.log(`[NowPlayingBar] Loaded ${firebaseItems.length} Firebase items for playback sequence`);
              }
            } catch (error) {
              console.error('[NowPlayingBar] Error loading Firebase music:', error);
            }

            // Create a combined list - IMPORTANT for next/previous functionality
            const combinedList = [...data, ...firebaseItems];
            console.log(`[NowPlayingBar] Combined music list: ${combinedList.length} items`,
              `(${data.length} regular, ${firebaseItems.length} Firebase)`);

            // Expose the Firebase music list to the window for debugging
            if (!window.firebaseMusicList) {
              window.firebaseMusicList = firebaseItems;
            }

            setMusicList(combinedList);
          } else {
            console.error('Failed to fetch music list');
          }
        } catch (error) {
          console.error('Error fetching music list:', error);
        }
      };

      // Get albums from localStorage
      const getAlbums = () => {
        try {
          const savedAlbums = localStorage.getItem('albums');
          if (savedAlbums) {
            setAlbums(JSON.parse(savedAlbums));
          }
        } catch (error) {
          console.error('Error fetching albums from localStorage:', error);
        }
      };

      fetchMusicList();
      getAlbums();
    }
  }, [currentlyPlaying]);

  // Handle forward button click - uses skipForward from context
  const handleForwardClick = (e) => {
    e.preventDefault();
    e.stopPropagation();

    console.log("⏩ Forward 10 seconds button clicked");

    debounce(() => {
      // Use the skipForward function directly from context
      skipForward(10);
    });
  };

  // Handle backward button click - uses skipBackward from context
  const handleBackwardClick = (e) => {
    e.preventDefault();
    e.stopPropagation();

    console.log("⏪ Rewind 10 seconds button clicked");

    debounce(() => {
      // Use the skipBackward function directly from context
      skipBackward(10);
    });
  };

  // Handle next song with strong debounce
  const handleNextSong = (e) => {
    if (e) {
      e.preventDefault();
      e.stopPropagation();
    }

    console.log("🔵 Next button clicked!");

    // Check if we're already in the process of changing tracks
    if (!isActionAllowedRef.current) {
      console.log("🔵 Next action blocked by debounce - still processing previous action");
      return;
    }

    // If we're on the HomePage and have a registered next handler, use it
    if (isHomePage && homePageNavHandlers.next) {
      console.log("🔵 Using HomePage's next song handler");
      debounce(() => {
        homePageNavHandlers.next();
      });
      return;
    }

    // If we're on the favorites page, load the favorites list directly
    const isFavoritesPage = location.pathname.includes('/favorites');
    if (isFavoritesPage) {
      console.log("🔵 On favorites page, using favorites list directly");
      try {
        // Load the favorites list from localStorage
        const savedFavorites = localStorage.getItem('favorites-music-list');
        if (savedFavorites) {
          const favoritesList = JSON.parse(savedFavorites);
          console.log(`🔵 Loaded ${favoritesList.length} favorites from localStorage`);

          // Make sure we're in favorites mode
          setMusicCategory('favorites');

          debounce(() => {
            // First stop any current playback to ensure clean state
            stopPlayback().then(() => {
              playNextSong(favoritesList, []);
            });
          });
          return;
        }
      } catch (error) {
        console.error("Error loading favorites list:", error);
      }
    }

    console.log("🔵 Current playing:", currentlyPlaying);
    console.log("🔵 Current track data:", currentTrackData?.title, "by", currentTrackData?.artist);
    console.log("🔵 Current music category:", musicCategory || "not set");
    console.log("🔵 Music list has", musicList?.length || 0, "items");

    // If musicList is empty or not properly loaded, fetch it immediately
    if (!musicList || musicList.length === 0) {
      console.log("🔵 Music list is empty, fetching it now");

      const fetchMusicList = async () => {
        try {
          // Regular music from backend
          const response = await fetch('https://astroglowfirebase-d2411.uc.r.appspot.com/api/music/getAllMusic');
          if (response.ok) {
            const data = await response.json();

            // Add displayIndex to uploaded items
            const uploadedItems = data.map((item, index) => ({
              ...item,
              displayIndex: index,
              category: 'uploaded'
            }));

            // Get Firebase music items from localStorage
            let firebaseItems = [];
            try {
              const savedFirebaseMusic = localStorage.getItem('firebase-music-list');
              if (savedFirebaseMusic) {
                const parsedItems = JSON.parse(savedFirebaseMusic);
                console.log(`[NextButton] Loaded ${parsedItems.length} Firebase items`);

                // Ensure each item has the correct properties
                firebaseItems = parsedItems.map((item, index) => ({
                  ...item,
                  // If displayIndex doesn't exist, use the array index
                  displayIndex: item.displayIndex !== undefined ? item.displayIndex : index,
                  category: 'available'
                }));

                // Log the Firebase items to verify correct order
                console.log("[NextButton] Firebase items with display indices:");
                firebaseItems.slice(0, 3).forEach((item, idx) => {
                  console.log(`${idx}: ${item.title} by ${item.artist} (Index: ${item.displayIndex})`);
                });
              }
            } catch (error) {
              console.error('[NextButton] Error loading Firebase music:', error);
            }

            // Create a combined list
            const combinedList = [...uploadedItems, ...firebaseItems];
            console.log(`[NextButton] Combined music list: ${combinedList.length} items`);

            setMusicList(combinedList);

            // Now play the next song with the fresh list
            playNextSong(combinedList, albums);
          } else {
            console.error('Failed to fetch music list for Next button');
            // Even if fetch fails, try with whatever might be in the existing list
            playNextSong(musicList, albums);
          }
        } catch (error) {
          console.error('Error fetching music list for Next button:', error);
          // Even if fetch fails, try with whatever might be in the existing list
          playNextSong(musicList, albums);
        }
      };

      fetchMusicList();
    } else {
      // If music list is already loaded, just play next song
      if (typeof musicCategory !== 'undefined') {
        console.log("🔵 Using music category:", musicCategory, "for next song navigation");

        // Filter music list by category first
        let filteredList = musicList;
        if (musicCategory === 'available') {
          filteredList = musicList.filter(item => {
            const id = String(item.id || item.musicId || '');
            return id.includes('firebase-') || item.category === 'available';
          });
          console.log(`🔵 Filtered to ${filteredList.length} available music items`);
        } else if (musicCategory === 'uploaded') {
          filteredList = musicList.filter(item => {
            const id = String(item.id || item.musicId || '');
            return !id.includes('firebase-') || item.category === 'uploaded';
          });
          console.log(`🔵 Filtered to ${filteredList.length} uploaded music items`);
        }

        // Sort by displayIndex to ensure correct order
        filteredList.sort((a, b) => {
          const indexA = a.displayIndex !== undefined ? a.displayIndex : 0;
          const indexB = b.displayIndex !== undefined ? b.displayIndex : 0;
          return indexA - indexB;
        });

        // Log current track index and what should be next
        const currentIndex = filteredList.findIndex(item => {
          const itemId = String(item.id || item.musicId || '');
          return itemId === String(currentlyPlaying);
        });

        console.log(`🔵 Current track index in filtered list: ${currentIndex} of ${filteredList.length}`);

        if (currentIndex !== -1 && currentIndex < filteredList.length - 1) {
          const nextItem = filteredList[currentIndex + 1];
          console.log("🔵 Next track should be:",
            nextItem?.title || 'Unknown', "by", nextItem?.artist || 'Unknown Artist',
            `(ID: ${nextItem?.id || nextItem?.musicId || 'undefined'})`);
        } else if (currentIndex === filteredList.length - 1) {
          const firstItem = filteredList[0];
          console.log("🔵 Looping back to first track:",
            firstItem?.title || 'Unknown', "by", firstItem?.artist || 'Unknown Artist');
        }

        // If we have a properly filtered list, use it directly
        if (filteredList.length > 0) {
          console.log("🔵 Calling playNextSong with filtered list:", filteredList.length, "items");
          debounce(() => {
            playNextSong(filteredList, albums);
          });
          return;
        }
      }

      // Fallback to using the full list
      console.log("🔵 Using full music list of", musicList.length, "items");
      debounce(() => {
        playNextSong(musicList, albums);
      });
    }
  };

  // Handle previous song with strong debounce
  const handlePreviousSong = (e) => {
    if (e) {
      e.preventDefault();
      e.stopPropagation();
    }

    console.log("🔵 Previous button clicked!");

    // Check if we're already in the process of changing tracks
    if (!isActionAllowedRef.current) {
      console.log("🔵 Previous action blocked by debounce - still processing previous action");
      return;
    }

    // If we're on the HomePage and have a registered previous handler, use it
    if (isHomePage && homePageNavHandlers.previous) {
      console.log("🔵 Using HomePage's previous song handler");
      debounce(() => {
        homePageNavHandlers.previous();
      });
      return;
    }

    // If we're on the favorites page, load the favorites list directly
    const isFavoritesPage = location.pathname.includes('/favorites');
    if (isFavoritesPage) {
      console.log("🔵 On favorites page, using favorites list directly");
      try {
        // Load the favorites list from localStorage
        const savedFavorites = localStorage.getItem('favorites-music-list');
        if (savedFavorites) {
          const favoritesList = JSON.parse(savedFavorites);
          console.log(`🔵 Loaded ${favoritesList.length} favorites from localStorage`);

          // Make sure we're in favorites mode
          setMusicCategory('favorites');

          debounce(() => {
            // First stop any current playback to ensure clean state
            stopPlayback().then(() => {
              playPreviousSong(favoritesList, []);
            });
          });
          return;
        }
      } catch (error) {
        console.error("Error loading favorites list:", error);
      }
    }

    console.log("🔵 Current playing:", currentlyPlaying);
    console.log("🔵 Current track data:", currentTrackData?.title, "by", currentTrackData?.artist);
    console.log("🔵 Current music category:", musicCategory || "not set");
    console.log("🔵 Music list has", musicList?.length || 0, "items");

    // If musicList is empty or not properly loaded, fetch it immediately
    if (!musicList || musicList.length === 0) {
      console.log("🔵 Music list is empty, fetching it now");

      const fetchMusicList = async () => {
        try {
          // Regular music from backend
          const response = await fetch('https://astroglowfirebase-d2411.uc.r.appspot.com/api/music/getAllMusic');
          if (response.ok) {
            const data = await response.json();

            // Add displayIndex to uploaded items
            const uploadedItems = data.map((item, index) => ({
              ...item,
              displayIndex: index,
              category: 'uploaded'
            }));

            // Get Firebase music items from localStorage
            let firebaseItems = [];
            try {
              const savedFirebaseMusic = localStorage.getItem('firebase-music-list');
              if (savedFirebaseMusic) {
                const parsedItems = JSON.parse(savedFirebaseMusic);
                console.log(`[PrevButton] Loaded ${parsedItems.length} Firebase items`);

                // Ensure each item has the correct properties
                firebaseItems = parsedItems.map((item, index) => ({
                  ...item,
                  // If displayIndex doesn't exist, use the array index
                  displayIndex: item.displayIndex !== undefined ? item.displayIndex : index,
                  category: 'available'
                }));

                // Log the Firebase items to verify correct order
                console.log("[PrevButton] Firebase items with display indices:");
                firebaseItems.slice(0, 3).forEach((item, idx) => {
                  console.log(`${idx}: ${item.title} by ${item.artist} (Index: ${item.displayIndex})`);
                });
              }
            } catch (error) {
              console.error('[PrevButton] Error loading Firebase music:', error);
            }

            // Create a combined list
            const combinedList = [...uploadedItems, ...firebaseItems];
            console.log(`[PrevButton] Combined music list: ${combinedList.length} items`);

            setMusicList(combinedList);

            // Now play the previous song with the fresh list
            playPreviousSong(combinedList, albums);
          } else {
            console.error('Failed to fetch music list for Previous button');
            // Even if fetch fails, try with whatever might be in the existing list
            playPreviousSong(musicList, albums);
          }
        } catch (error) {
          console.error('Error fetching music list for Previous button:', error);
          // Even if fetch fails, try with whatever might be in the existing list
          playPreviousSong(musicList, albums);
        }
      };

      fetchMusicList();
    } else {
      // If music list is already loaded, just play previous song
      if (typeof musicCategory !== 'undefined') {
        console.log("🔵 Using music category:", musicCategory, "for previous song navigation");

        // Filter music list by category first
        let filteredList = musicList;
        if (musicCategory === 'available') {
          filteredList = musicList.filter(item => {
            const id = String(item.id || item.musicId || '');
            return id.includes('firebase-') || item.category === 'available';
          });
          console.log(`🔵 Filtered to ${filteredList.length} available music items`);
        } else if (musicCategory === 'uploaded') {
          filteredList = musicList.filter(item => {
            const id = String(item.id || item.musicId || '');
            return !id.includes('firebase-') || item.category === 'uploaded';
          });
          console.log(`🔵 Filtered to ${filteredList.length} uploaded music items`);
        }

        // Sort by displayIndex to ensure correct order
        filteredList.sort((a, b) => {
          const indexA = a.displayIndex !== undefined ? a.displayIndex : 0;
          const indexB = b.displayIndex !== undefined ? b.displayIndex : 0;
          return indexA - indexB;
        });

        // Log current track index and what should be previous
        const currentIndex = filteredList.findIndex(item => {
          const itemId = String(item.id || item.musicId || '');
          return itemId === String(currentlyPlaying);
        });

        console.log(`🔵 Current track index in filtered list: ${currentIndex} of ${filteredList.length}`);

        if (currentIndex > 0) {
          const prevItem = filteredList[currentIndex - 1];
          console.log("🔵 Previous track should be:",
            prevItem?.title || 'Unknown', "by", prevItem?.artist || 'Unknown Artist',
            `(ID: ${prevItem?.id || prevItem?.musicId || 'undefined'})`);
        } else if (currentIndex === 0 && filteredList.length > 0) {
          const lastItem = filteredList[filteredList.length - 1];
          console.log("🔵 Looping back to last track:",
            lastItem?.title || 'Unknown', "by", lastItem?.artist || 'Unknown Artist');
        }

        // If we have a properly filtered list, use it directly
        if (filteredList.length > 0) {
          console.log("🔵 Calling playPreviousSong with filtered list:", filteredList.length, "items");
          debounce(() => {
            playPreviousSong(filteredList, albums);
          });
          return;
        }
      }

      // Fallback to using the full list
      console.log("🔵 Using full music list of", musicList.length, "items");
      debounce(() => {
        playPreviousSong(musicList, albums);
      });
    }
  };

  // Handle play/pause with debounce
  const handlePlayPause = () => {
    debounce(() => {
      if (currentlyPlaying) {
        togglePlayPause(currentlyPlaying);
      }
    });
  };

  // Simple function to handle progress bar clicks
  const handleProgressClick = (e) => {
    e.stopPropagation();

    // Don't proceed if we're not playing anything
    if (!currentlyPlaying || !currentTrackData) {
      console.log("Nothing playing, can't seek");
      return;
    }

    // Get progress bar dimensions directly from the event target
    const progressBar = e.currentTarget;
    if (!progressBar) {
      console.error("Could not find progress bar element from event");
      return;
    }

    // Get the bounding rectangle
    const rect = progressBar.getBoundingClientRect();
    const offsetX = e.clientX - rect.left;
    const width = rect.width;

    // Ensure we have valid dimensions
    if (width <= 0) {
      console.error("Invalid progress bar width:", width);
      return;
    }

    // Calculate percentage (clamped between 0-100)
    const percentage = Math.max(0, Math.min(100, (offsetX / width) * 100));

    console.log(`Clicking progress bar at ${percentage.toFixed(2)}%`);

    // Use seekAudio directly from the context
    // This will handle all the audio element checks
    seekAudio(percentage);
  };

  // Hover handler for progress bar
  const handleProgressHover = (e) => {
    // Don't proceed if we're not playing anything
    if (!currentlyPlaying || !currentTrackData) {
      return;
    }

    // Get progress bar dimensions directly from the event target
    const progressBar = e.currentTarget;
    if (!progressBar) return;

    const rect = progressBar.getBoundingClientRect();
    const offsetX = e.clientX - rect.left;
    const width = rect.width;

    // Ensure we have valid dimensions
    if (width <= 0) return;

    // Calculate percentage (clamped between 0-100)
    const percentage = Math.max(0, Math.min(100, (offsetX / width) * 100));

    // Calculate the time if audio element is available
    let timeLabel = '0:00';
    if (audioElement && audioElement.duration) {
      const time = (percentage / 100) * audioElement.duration;
      timeLabel = formatTime(time);
    } else {
      // Fallback to display percentage
      timeLabel = `${Math.floor(percentage)}%`;
    }

    setHoverPosition({
      percent: percentage,
      time: timeLabel
    });
  };

  const handleProgressLeave = () => {
    setHoverPosition(null);
  };

  if (!currentlyPlaying || !currentTrackData) return null;

  // Get image URL from track data
  const imageUrl = currentTrackData.imageUrl ? getImageUrl(currentTrackData.imageUrl) : null;

  return (
    <div className={styles.nowPlayingContainer}>
      {currentTrackData && (
        <div className={styles.nowPlayingBar}>
          <div className={styles.nowPlayingContent}>
            <div className={styles.nowPlayingInfo}>
              <div
                className={`${styles.nowPlayingImage} ${isPlaying ? styles.isPlaying : ''}`}
                title={`${currentTrackData.title} by ${currentTrackData.artist}`}
              >
                {currentTrackData.imageUrl ? (
                  <img
                    src={currentTrackData.imageUrl}
                    alt={`${currentTrackData.title} cover`}
                    className={styles.nowPlayingCover}
                  />
                ) : (
                  <div className={styles.nowPlayingFallback}>
                    {currentTrackData.title.charAt(0)}
                  </div>
                )}
              </div>

              {/* Expanded Album View */}
              <div className={styles.expandedAlbumView}>
                {currentTrackData.imageUrl ? (
                  <img
                    src={currentTrackData.imageUrl}
                    alt={`${currentTrackData.title} cover`}
                    className={styles.expandedCover}
                  />
                ) : (
                  <div className={styles.nowPlayingFallback}>
                    {currentTrackData.title.charAt(0)}
                  </div>
                )}
                <div className={styles.expandedInfo}>
                  <p className={styles.expandedTitle}>{currentTrackData.title}</p>
                  <p className={styles.expandedArtist}>{currentTrackData.artist}</p>
                </div>
              </div>

              <div className={styles.trackInfo}>
                <h3 className={styles.nowPlayingTitle}>{currentTrackData.title}</h3>
                {currentTrackData.artist && (
                  <p className={styles.nowPlayingArtist}>{currentTrackData.artist}</p>
                )}
              </div>
            </div>

            <div className={styles.playbackSection}>
              <div className={styles.playbackControls}>
                <button
                  className={`${styles.playbackButton} ${styles.previousButton}`}
                  onClick={handlePreviousSong}
                  title="Previous song"
                >
                  ⏮
                </button>
                <button
                  className={`${styles.playbackButton} ${styles.backwardButton}`}
                  onClick={handleBackwardClick}
                  title="Rewind 10 seconds"
                >
                  ⏪
                </button>
                <button
                  className={`${styles.playbackButton} ${styles.playButton}`}
                  onClick={handlePlayPause}
                  title={isPlaying ? 'Pause' : 'Play'}
                >
                  {isPlaying ? '❚❚' : '▶'}
                </button>
                <button
                  className={`${styles.playbackButton} ${styles.forwardButton}`}
                  onClick={handleForwardClick}
                  title="Forward 10 seconds"
                >
                  ⏩
                </button>
                <button
                  className={`${styles.playbackButton} ${styles.nextButton}`}
                  onClick={handleNextSong}
                  title="Next song"
                >
                  ⏭
                </button>
              </div>

              <div className={styles.progressContainer}>
                <span className={styles.timeDisplay}>{audioTime.elapsed}</span>
                <div
                  className={styles.progressBar}
                  onClick={handleProgressClick}
                  onMouseMove={handleProgressHover}
                  onMouseLeave={handleProgressLeave}
                >
                  <div className={styles.progressTrack}></div>
                  <div
                    className={styles.progressFill}
                    style={{ width: `${audioProgress}%` }}
                  ></div>
                  {hoverPosition && (
                    <div
                      className={styles.hoverIndicator}
                      style={{ left: `${hoverPosition.percent}%` }}
                    >
                      <span className={styles.hoverTime}>{hoverPosition.time}</span>
                    </div>
                  )}
                </div>
                <span className={styles.timeDisplay}>{audioTime.total}</span>
              </div>
            </div>

            <div className={styles.playerActions}>
              <button
                className={styles.stopButton}
                onClick={() => {
                  // Immediately stop playback without debouncing to ensure proper cleanup
                  stopPlayback();
                }}
                title="Stop playback and close player"
              >
                ⏹
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default NowPlayingBar; 