import React, { createContext, useContext, useRef, useState, useEffect } from 'react';

const AudioPlayerContext = createContext(null);

export const AudioPlayerProvider = ({ children }) => {
  const [currentlyPlaying, setCurrentlyPlaying] = useState(null);
  const [audioElement, setAudioElement] = useState(null);
  const [audioProgress, setAudioProgress] = useState(0);
  const [audioTime, setAudioTime] = useState({ elapsed: '0:00', total: '0:00' });
  const [isPlaying, setIsPlaying] = useState(false);
  const [currentTrackData, setCurrentTrackData] = useState(null);
  const progressIntervalRef = useRef(null);

  // Clean up on unmount
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
    
    // Always format as MM:SS
    return `${mins}:${secs < 10 ? '0' + secs : secs}`;
  };

  // Helper for image URLs
  const isDataUri = (str) => {
    if (!str) return false;
    if (typeof str !== 'string') return false;
    
    // Check for common data URI patterns
    if (str.startsWith('data:image/')) return true;
    if (str.startsWith('data:') && str.includes(';base64,')) return true;
    
    return false;
  };

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
      
      // For file paths that may have been saved
      if (url.startsWith('/')) {
        console.log("Using relative path for image:", url);
        return url;
      }
      
      console.warn("Invalid image URL format:", url);
      return '/placeholder.jpg'; // Return default placeholder instead of null
    } catch (error) {
      console.error("Error parsing image URL:", error);
      return '/placeholder.jpg';
    }
  };

  const playMusic = async (musicId) => {
    try {
      console.log(`Attempting to play music with ID: ${musicId}`);
      
      // Stop current audio if playing
      if (audioElement) {
        try {
          // Properly pause and release resources
          if (audioElement.pause && typeof audioElement.pause === 'function') {
            audioElement.pause();
          }
          if (audioElement.src) {
            audioElement.src = '';
          }
          // Release any media resources
          if (audioElement.load && typeof audioElement.load === 'function') {
            audioElement.load();
          }
        } catch (err) {
          console.error('Error stopping previous audio:', err);
        }
        
        if (progressIntervalRef.current) {
          clearInterval(progressIntervalRef.current);
          progressIntervalRef.current = null;
        }
        setIsPlaying(false);
      }

      // First check if the music has a direct URL
      const musicResponse = await fetch(`http://localhost:8080/api/music/getMusic/${musicId}?includeAudioData=false`);
      if (!musicResponse.ok) {
        throw new Error(`Music with ID ${musicId} not found`);
      }

      const musicData = await musicResponse.json();
      setCurrentTrackData(musicData);
      
      // Check if audioUrl exists and use it directly
      if (musicData.audioUrl) {
        console.log("Using external URL for playback:", musicData.audioUrl);
        
        // Create a new audio element to prevent conflicts with previous instances
        const audio = new Audio();
        audio.preload = 'auto';
        
        // Set up audio events before setting the source to avoid race conditions
        const setupAudioEvents = () => {
          audio.onended = () => {
            setCurrentlyPlaying(null);
            setAudioProgress(0);
            setIsPlaying(false);
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
            setIsPlaying(true);
          });

          audio.addEventListener('pause', () => {
            setIsPlaying(false);
          });
        };

        // Set up events
        setupAudioEvents();
        
        // Now set the source and play
        audio.src = musicData.audioUrl;
        
        // Wait for metadata to load before playing
        audio.addEventListener('loadedmetadata', () => {
          // Play the audio
          audio.play().catch(err => {
            console.error('Error playing audio from URL:', err);
            alert('Error playing audio: ' + err.message);
          });
        });

        // Set state
        setAudioElement(audio);
        setCurrentlyPlaying(musicId);
        setIsPlaying(true);
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
      const audio = new Audio();
      audio.preload = 'auto';
      
      // Set up event listeners before setting source
      const setupAudioEvents = () => {
        // Add event listener to clean up object URL when audio is done
        audio.onended = () => {
          URL.revokeObjectURL(audioUrl);
          setCurrentlyPlaying(null);
          setAudioProgress(0);
          setIsPlaying(false);
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
          setIsPlaying(true);
        });

        audio.addEventListener('pause', () => {
          setIsPlaying(false);
        });
      };

      // Set up events
      setupAudioEvents();
      
      // Now set the source
      audio.src = audioUrl;
      
      // Wait for metadata to load before playing
      audio.addEventListener('loadedmetadata', () => {
        // Play the audio
        audio.play().catch(err => {
          console.error('Error playing audio:', err);
          alert('Error playing audio: ' + err.message);
        });
      });

      // Set state
      setAudioElement(audio);
      setCurrentlyPlaying(musicId);
      setIsPlaying(true);
    } catch (error) {
      console.error('Error playing music:', error);
      alert('Error playing music: ' + error.message);
    }
  };

  const playAlbumTrack = (track) => {
    try {
      console.log("Playing album track:", track);
      
      // Stop current audio if playing
      if (audioElement) {
        try {
          // Properly pause and release resources
          if (audioElement.pause && typeof audioElement.pause === 'function') {
            audioElement.pause();
          }
          if (audioElement.src) {
            audioElement.src = '';
          }
          // Release any media resources
          if (audioElement.load && typeof audioElement.load === 'function') {
            audioElement.load();
          }
        } catch (err) {
          console.error('Error stopping previous audio:', err);
        }
        
        if (progressIntervalRef.current) {
          clearInterval(progressIntervalRef.current);
          progressIntervalRef.current = null;
        }
        setIsPlaying(false);
      }
      
      // Check if the track has an audioUrl
      if (!track.audioUrl) {
        throw new Error("This track doesn't have an audio URL");
      }
      
      // Set current track data
      setCurrentTrackData({
        title: track.title,
        artist: track.artist,
        genre: track.genre,
        imageUrl: track.imageUrl
      });
      
      console.log("Using album track URL for playback:", track.audioUrl);
      
      // Create a new audio element to prevent conflicts
      const audio = new Audio();
      audio.preload = 'auto';
      
      // Set up audio events before setting source
      const setupAudioEvents = () => {
        audio.onended = () => {
          setCurrentlyPlaying(null);
          setAudioProgress(0);
          setIsPlaying(false);
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
          setIsPlaying(true);
        });

        audio.addEventListener('pause', () => {
          setIsPlaying(false);
        });
      };

      // Set up events
      setupAudioEvents();
      
      // Now set the source
      audio.src = track.audioUrl;
      
      // Wait for metadata to load before playing
      audio.addEventListener('loadedmetadata', () => {
        // Play the audio
        audio.play().catch(err => {
          console.error('Error playing audio from URL:', err);
          alert('Error playing audio: ' + err.message);
        });
      });

      // Set state - use the track id for currentlyPlaying
      setAudioElement(audio);
      setCurrentlyPlaying(track.id);
      setIsPlaying(true);
    } catch (error) {
      console.error('Error playing album track:', error);
      alert('Error playing album track: ' + error.message);
    }
  };

  const togglePlayPause = (musicId) => {
    if (currentlyPlaying === musicId && audioElement) {
      if (audioElement.paused) {
        console.log("Attempting to play paused audio");
        audioElement.play()
          .catch(err => {
            console.error('Error playing audio:', err);
            alert('Error playing audio: ' + err.message);
          });
        setIsPlaying(true);
      } else if (audioElement.pause && typeof audioElement.pause === 'function') {
        console.log("Pausing currently playing audio");
        audioElement.pause();
        setIsPlaying(false);
      }
      // Don't force a re-render by modifying the audioElement state
      // This was causing the audio to not play after pause
    } else {
      // If not the current track, start playing the new track
      playMusic(musicId);
    }
  };

  // Function to play next song in the list
  const playNextSong = (musicList, albums) => {
    try {
      if (!currentlyPlaying) return false;
      
      // If no lists provided, try to use the currentTrackData to play next
      if ((!musicList || musicList.length === 0) && (!albums || albums.length === 0)) {
        console.log("No music list or albums provided - trying to restart current song");
        if (audioElement) {
          // If we have an audio element but no music list, restart the current song
          audioElement.currentTime = 0;
          audioElement.play().catch(err => {
            console.error('Error restarting audio:', err);
          });
          return true;
        }
        return false;
      }

      // Find the index of the current song
      let currentIndex = -1;
      
      // First check in the regular music list
      currentIndex = musicList?.findIndex(music => music.musicId === currentlyPlaying);
      
      // If found in the regular music list
      if (currentIndex !== -1 && currentIndex < musicList.length - 1) {
        // Play the next song in the list
        const nextSong = musicList[currentIndex + 1];
        playMusic(nextSong.musicId);
        return true;
      }
      
      // If not found or was the last song, check if we're playing from an album
      const playingAlbum = albums?.find(album => 
        album.songs && album.songs.some(song => song.id === currentlyPlaying)
      );
      
      if (playingAlbum) {
        const albumSongs = playingAlbum.songs;
        const songIndex = albumSongs.findIndex(song => song.id === currentlyPlaying);
        
        if (songIndex !== -1 && songIndex < albumSongs.length - 1) {
          // Play the next song in the album
          const nextSong = albumSongs[songIndex + 1];
          playAlbumTrack(nextSong);
          return true;
        }
      }
      
      console.log("No next song available, restarting current song");
      if (audioElement) {
        // If no next song is available, restart the current song
        audioElement.currentTime = 0;
        audioElement.play().catch(err => {
          console.error('Error restarting audio:', err);
        });
        return true;
      }
      
      return false;
    } catch (error) {
      console.error("Error playing next song:", error);
      return false;
    }
  };

  // Function to play previous song in the list
  const playPreviousSong = (musicList, albums) => {
    try {
      if (!currentlyPlaying) return false;
      
      // If no lists provided, try to restart the current song
      if ((!musicList || musicList.length === 0) && (!albums || albums.length === 0)) {
        console.log("No music list or albums provided - trying to restart current song");
        if (audioElement) {
          // If we have an audio element but no music list, restart the current song
          audioElement.currentTime = 0;
          audioElement.play().catch(err => {
            console.error('Error restarting audio:', err);
          });
          return true;
        }
        return false;
      }

      // Find the index of the current song
      let currentIndex = -1;
      
      // First check in the regular music list
      currentIndex = musicList?.findIndex(music => music.musicId === currentlyPlaying);
      
      // If found in the regular music list and not the first song
      if (currentIndex > 0) {
        // Play the previous song in the list
        const previousSong = musicList[currentIndex - 1];
        playMusic(previousSong.musicId);
        return true;
      }
      
      // If not found or was the first song, check if we're playing from an album
      const playingAlbum = albums?.find(album => 
        album.songs && album.songs.some(song => song.id === currentlyPlaying)
      );
      
      if (playingAlbum) {
        const albumSongs = playingAlbum.songs;
        const songIndex = albumSongs.findIndex(song => song.id === currentlyPlaying);
        
        if (songIndex > 0) {
          // Play the previous song in the album
          const previousSong = albumSongs[songIndex - 1];
          playAlbumTrack(previousSong);
          return true;
        }
      }
      
      console.log("No previous song available, restarting current track");
      // Track is the first one in the list/album - restart the current track
      if (audioElement) {
        audioElement.currentTime = 0;
        audioElement.play().catch(err => {
          console.error('Error restarting audio:', err);
        });
        return true;
      }
      
      return false;
    } catch (error) {
      console.error("Error playing previous song:", error);
      return false;
    }
  };

  // Handler for double-click actions on playback controls
  const handleDoubleClick = (direction, musicList, albums) => {
    if (direction === 'next') {
      return playNextSong(musicList, albums);
    } else if (direction === 'previous') {
      return playPreviousSong(musicList, albums);
    }
    return false;
  };

  const seekAudio = (positionPercent) => {
    if (audioElement && audioElement.duration) {
      const newTime = (positionPercent / 100) * audioElement.duration;
      audioElement.currentTime = newTime;
      setAudioTime({
        elapsed: formatTime(newTime),
        total: formatTime(audioElement.duration)
      });
    }
  };

  const stopPlayback = () => {
    if (audioElement) {
      try {
        if (audioElement.pause && typeof audioElement.pause === 'function') {
          audioElement.pause();
        }
        if (audioElement.src) {
          audioElement.src = '';
        }
      } catch (err) {
        console.error('Error stopping audio:', err);
      }
      // Reset state
      setCurrentlyPlaying(null);
      setAudioProgress(0);
      setAudioElement(null);
      setIsPlaying(false);
      setCurrentTrackData(null);
      if (progressIntervalRef.current) {
        clearInterval(progressIntervalRef.current);
        progressIntervalRef.current = null;
      }
    }
  };

  return (
    <AudioPlayerContext.Provider
      value={{
        currentlyPlaying,
        audioElement,
        audioProgress,
        audioTime,
        isPlaying,
        currentTrackData,
        formatTime,
        getImageUrl,
        playMusic,
        togglePlayPause,
        playAlbumTrack,
        playNextSong,
        playPreviousSong,
        handleDoubleClick,
        seekAudio,
        stopPlayback
      }}
    >
      {children}
    </AudioPlayerContext.Provider>
  );
};

export const useAudioPlayer = () => {
  const context = useContext(AudioPlayerContext);
  if (!context) {
    throw new Error('useAudioPlayer must be used within an AudioPlayerProvider');
  }
  return context;
};

export default AudioPlayerContext; 