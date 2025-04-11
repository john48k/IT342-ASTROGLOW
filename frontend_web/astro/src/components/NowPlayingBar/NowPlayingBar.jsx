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
  
  // Fetch music list and albums when a track is playing
  useEffect(() => {
    if (currentlyPlaying) {
      // Fetch music list
      const fetchMusicList = async () => {
        try {
          const response = await fetch('http://localhost:8080/api/music/getAllMusic');
          if (response.ok) {
            const data = await response.json();
            setMusicList(data);
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
  
  // Handle next song
  const handleNextSong = (e) => {
    e.preventDefault();
    
    debounce(() => {
      playNextSong(musicList, albums);
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
  
  // Handle previous song
  const handlePreviousSong = (e) => {
    e.preventDefault();
    
    debounce(() => {
      playPreviousSong(musicList, albums);
    });
  };
  
  // Handle play/pause with debounce
  const handlePlayPause = () => {
    debounce(() => {
      if (currentlyPlaying) {
        togglePlayPause(currentlyPlaying);
      }
    });
  };

  if (!currentlyPlaying || !currentTrackData) return null;

  const handleProgressBarClick = (e) => {
    if (audioElement) {
      // Calculate click position as percentage of total width
      const rect = e.currentTarget.getBoundingClientRect();
      const pos = (e.clientX - rect.left) / rect.width * 100;
      seekAudio(pos);
    }
  };

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
                  className={styles.playbackButton}
                  onClick={handlePlayPause}
                  title="Play/Pause"
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
                  onClick={handleProgressBarClick}
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
                className={styles.stopButton}
                onClick={() => debounce(stopPlayback)}
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