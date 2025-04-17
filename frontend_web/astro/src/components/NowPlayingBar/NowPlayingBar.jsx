import React, { useRef, useState, useEffect } from 'react';
import { useAudioPlayer } from '../../context/AudioPlayerContext';
import styles from './NowPlayingBar.module.css';

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
    stopPlayback
  } = useAudioPlayer();
  
  // Add state for music list and albums
  const [musicList, setMusicList] = useState([]);
  const [albums, setAlbums] = useState([]);
  
  // Add a hover effect to show potential seek position
  const [hoverPosition, setHoverPosition] = useState(null);
  
  // Debouncing mechanism to prevent rapid successive clicks
  const isActionAllowedRef = useRef(true);
  const debounceTime = 300; // milliseconds
  
  const debounce = (callback) => {
    if (!isActionAllowedRef.current) return;
    
    isActionAllowedRef.current = false;
    callback();
    
    setTimeout(() => {
      isActionAllowedRef.current = true;
    }, debounceTime);
  };
  
  // Fetch music list and albums when a track is playing
  useEffect(() => {
    if (currentlyPlaying) {
      // Fetch music list
      const fetchMusicList = async () => {
        try {
          // Regular music from backend
          const response = await fetch('http://localhost:8080/api/music/getAllMusic');
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
  
  // Handle forward button click - simple version without double-click
  const handleForwardClick = (e) => {
    e.preventDefault();
    
    debounce(() => {
      if (audioElement) {
        // Forward 10 seconds
        const newTime = Math.min(audioElement.duration, audioElement.currentTime + 10);
        audioElement.currentTime = newTime;
        seekAudio(newTime / audioElement.duration * 100);
      }
    });
  };
  
  // Handle backward button click - simple version without double-click
  const handleBackwardClick = (e) => {
    e.preventDefault();
    
    debounce(() => {
      if (audioElement) {
        // Rewind 10 seconds
        const newTime = Math.max(0, audioElement.currentTime - 10);
        audioElement.currentTime = newTime;
        seekAudio(newTime / audioElement.duration * 100);
      }
    });
  };
  
  // Handle next song
  const handleNextSong = (e) => {
    e.preventDefault();
    
    console.log("🔵 Next button clicked!");
    console.log("🔵 Current playing:", currentlyPlaying);
    console.log("🔵 Current track data:", currentTrackData?.title, "by", currentTrackData?.artist);
    console.log("🔵 Music list has", musicList?.length || 0, "items");
    
    // Print full details of first few tracks to help debugging
    if (musicList && musicList.length > 0) {
      console.log("🔵 First track in list:", {
        id: musicList[0].id || musicList[0].musicId,
        title: musicList[0].title,
        artist: musicList[0].artist
      });
      
      if (musicList.length > 1) {
        console.log("🔵 Second track in list:", {
          id: musicList[1].id || musicList[1].musicId,
          title: musicList[1].title,
          artist: musicList[1].artist
        });
      }
    }
    
    // If musicList is empty or not properly loaded, fetch it immediately
    if (!musicList || musicList.length === 0) {
      console.log("🔵 Music list is empty, fetching it now");
      
      const fetchMusicList = async () => {
        try {
          // Regular music from backend
          const response = await fetch('http://localhost:8080/api/music/getAllMusic');
          if (response.ok) {
            const data = await response.json();
            
            // Get Firebase music items from localStorage
            let firebaseItems = [];
            try {
              const savedFirebaseMusic = localStorage.getItem('firebase-music-list');
              if (savedFirebaseMusic) {
                firebaseItems = JSON.parse(savedFirebaseMusic);
                console.log(`[NextButton] Loaded ${firebaseItems.length} Firebase items for playback`);
              }
            } catch (error) {
              console.error('[NextButton] Error loading Firebase music:', error);
            }
            
            // Create a combined list
            const combinedList = [...data, ...firebaseItems];
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
      console.log("🔵 Music list IDs:", musicList.map(item => {
        const id = item.id || item.musicId;
        return id ? id.toString().substring(0, 15) + "..." : "unknown";
      }).join(", "));
      
      debounce(() => {
        console.log("🔵 Calling playNextSong with musicList:", musicList?.length || 0, "items");
        playNextSong(musicList, albums);
      });
    }
  };
  
  // Handle previous song
  const handlePreviousSong = (e) => {
    e.preventDefault();
    
    console.log("🔵 Previous button clicked!");
    console.log("🔵 Current playing:", currentlyPlaying);
    console.log("🔵 Music list has", musicList?.length || 0, "items");
    
    // If musicList is empty or not properly loaded, fetch it immediately
    if (!musicList || musicList.length === 0) {
      console.log("🔵 Music list is empty, fetching it now");
      
      const fetchMusicList = async () => {
        try {
          // Regular music from backend
          const response = await fetch('http://localhost:8080/api/music/getAllMusic');
          if (response.ok) {
            const data = await response.json();
            
            // Get Firebase music items from localStorage
            let firebaseItems = [];
            try {
              const savedFirebaseMusic = localStorage.getItem('firebase-music-list');
              if (savedFirebaseMusic) {
                firebaseItems = JSON.parse(savedFirebaseMusic);
                console.log(`[PrevButton] Loaded ${firebaseItems.length} Firebase items for playback`);
              }
            } catch (error) {
              console.error('[PrevButton] Error loading Firebase music:', error);
            }
            
            // Create a combined list
            const combinedList = [...data, ...firebaseItems];
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
      debounce(() => {
        console.log("🔵 Calling playPreviousSong with musicList:", musicList?.length || 0, "items");
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