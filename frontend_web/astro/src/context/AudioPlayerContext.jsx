import React, { createContext, useContext, useRef, useState, useEffect } from 'react';
import { useUser } from './UserContext';

const AudioPlayerContext = createContext(null);

export const AudioPlayerProvider = ({ children }) => {
  const [currentlyPlaying, setCurrentlyPlaying] = useState(null);
  const [audioElement, setAudioElement] = useState(null);
  const [audioProgress, setAudioProgress] = useState(0);
  const [audioTime, setAudioTime] = useState({ elapsed: '0:00', total: '0:00' });
  const [isPlaying, setIsPlaying] = useState(false);
  const [currentTrackData, setCurrentTrackData] = useState(null);
  const progressIntervalRef = useRef(null);
  const { isAuthenticated } = useUser();
  
  // Add a state to track which category of music the user is browsing
  const [musicCategory, setMusicCategory] = useState('all'); // 'uploaded', 'available', or 'all'

  // Need to track the pending seek request
  const pendingSeekRef = useRef(null);
  
  // A function to apply any pending seek when the audio element becomes valid
  const applyPendingSeek = (newAudioElement) => {
    if (pendingSeekRef.current !== null && newAudioElement && newAudioElement.duration) {
      try {
        const seekPercentage = pendingSeekRef.current;
        console.log(`Applying pending seek to ${seekPercentage.toFixed(2)}%`);
        
        const newTime = (seekPercentage / 100) * newAudioElement.duration;
        newAudioElement.currentTime = newTime;
        
        // Update time display
        setAudioTime({
          elapsed: formatTime(newTime),
          total: formatTime(newAudioElement.duration)
        });
        
        // Clear the pending seek
        pendingSeekRef.current = null;
      } catch (error) {
        console.error("Error applying pending seek:", error);
      }
    }
  };
  
  // Apply pending seek when audio element changes
  useEffect(() => {
    if (audioElement) {
      // Wait for the audio to be ready with metadata
      const handleMetadata = () => {
        applyPendingSeek(audioElement);
      };
      
      audioElement.addEventListener('loadedmetadata', handleMetadata);
      
      // Try immediately in case metadata is already loaded
      if (audioElement.readyState >= 1) {
        applyPendingSeek(audioElement);
      }
      
      return () => {
        audioElement.removeEventListener('loadedmetadata', handleMetadata);
      };
    }
  }, [audioElement]);

  // Helper for determining audio type from URL
  const getAudioTypeFromUrl = (url) => {
    if (!url) return null;
    
    const lowerUrl = url.toLowerCase();
    if (lowerUrl.endsWith('.mp3')) {
      return 'audio/mpeg';
    } else if (lowerUrl.endsWith('.wav')) {
      return 'audio/wav';
    } else if (lowerUrl.endsWith('.ogg')) {
      return 'audio/ogg';
    } else if (lowerUrl.endsWith('.m4a')) {
      return 'audio/mp4';
    } else if (lowerUrl.includes('.mp3?')) {
      return 'audio/mpeg';
    } else if (lowerUrl.includes('.wav?')) {
      return 'audio/wav';
    } else if (lowerUrl.includes('.ogg?')) {
      return 'audio/ogg';
    } else if (lowerUrl.includes('.m4a?')) {
      return 'audio/mp4';
    }
    
    // Default to MP3 if we can't determine
    return 'audio/mpeg';
  };

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

  // Helper for image URLs
  const getImageUrl = (url) => {
    if (!url) return null;
    
    // If it's already a data URI, return it as is
    if (isDataUri(url)) return url;
    
    // If it's a relative URL, prepend the API base URL
    if (url.startsWith('/')) {
      return `http://localhost:8080${url}`;
    }
    
    // Otherwise, return the URL as is
    return url;
  };

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

  // Stop playback when user logs out
  useEffect(() => {
    if (!isAuthenticated && isPlaying) {
      if (audioElement) {
        try {
          if (audioElement.pause && typeof audioElement.pause === 'function') {
            audioElement.pause();
          }
          if (audioElement.src) {
            audioElement.src = '';
          }
        } catch (err) {
          console.error('Error stopping audio on logout:', err);
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
    }
  }, [isAuthenticated, isPlaying, audioElement]);

  const playMusic = async (musicId, directAudioUrl = null, category = null) => {
    try {
      console.log(`Attempting to play music with ID: ${musicId}`);
      
      // Ensure any current playback is completely stopped
      await stopPlayback();
      
      // Short delay to ensure clean audio context
      await new Promise(resolve => setTimeout(resolve, 50));
      
      // Create a completely new audio element to avoid any state issues
      const newAudioElement = new Audio();
      
      // Set current track info right away to update UI
      setCurrentlyPlaying(musicId);
      
      // Set or detect the music category
      if (category) {
        // Category is explicitly provided
        setMusicCategory(category);
      } else if (musicId) {
        // Try to determine the category from the ID
        const idString = String(musicId);
        if (idString.startsWith('firebase-')) {
          setMusicCategory('available');
        } else if (idString.match(/^\d+$/)) {
          // Numeric IDs are usually from uploaded music
          setMusicCategory('uploaded');
        }
        // Otherwise keep the current category
      }
      
      // Function to handle audio fetch failures
      const handleFetchError = (error) => {
        console.error('Error fetching audio:', error);
        stopPlayback(); // Clean up on failure
        // We could show a user-facing error here
      };
      
      // Ensure musicId is a string before checking if it starts with 'firebase-'
      const musicIdString = String(musicId);
      
      // Check if this is a Firebase music file
      if (musicIdString.startsWith('firebase-') || directAudioUrl) {
        try {
          // Skip setting audio URL if it's the page URL (which causes errors)
          if (directAudioUrl && 
              directAudioUrl !== window.location.href && 
              directAudioUrl !== 'http://localhost:5173/home') {
              
            console.log('Using direct audio URL:', directAudioUrl);
            
            // Set the source and type
            newAudioElement.src = directAudioUrl;
            newAudioElement.type = getAudioTypeFromUrl(directAudioUrl);
          } else if (directAudioUrl) {
            console.log('Invalid audio URL detected, skipping URL assignment');
            // If we're a Firebase track but have an invalid URL, return early
            // to avoid attempting to play without a valid source
            if (musicIdString.startsWith('firebase-')) {
              return;
            }
          }
          
          // Set up event handlers for the new audio element
          const setupAudioEvents = () => {
            newAudioElement.onended = () => {
              console.log('Audio playback ended');
              // Reset state when audio ends
              setIsPlaying(false);
              setCurrentlyPlaying(null);
              setAudioElement(null);
              setAudioProgress(0);
              clearInterval(progressIntervalRef.current);
              progressIntervalRef.current = null;
            };
            
            newAudioElement.onplay = () => {
              console.log('Audio started playing');
              setIsPlaying(true);
            };
            
            newAudioElement.onpause = () => {
              console.log('Audio paused');
              setIsPlaying(false);
            };
            
            newAudioElement.onerror = (e) => {
              // Minimal error handling without extra logging
              stopPlayback();
            };
            
            // Add error event listener for more detailed error information
            newAudioElement.addEventListener('error', (e) => {
              // Minimal error handling without extra logging
              stopPlayback();
            });
            
            // Set up the progress interval
            if (progressIntervalRef.current) {
              clearInterval(progressIntervalRef.current);
            }
            
            progressIntervalRef.current = setInterval(() => {
              if (newAudioElement && !newAudioElement.paused) {
                const progress = (newAudioElement.currentTime / newAudioElement.duration) * 100;
                setAudioProgress(progress);
                
                // Update the time display
                setAudioTime({
                  elapsed: formatTime(newAudioElement.currentTime),
                  total: formatTime(newAudioElement.duration)
                });
              }
            }, 1000);
          };
          
          // Start playing
          setupAudioEvents();
          
          // Wait for metadata before playing to avoid playback errors
          newAudioElement.onloadedmetadata = () => {
            try {
              console.log('Audio metadata loaded, starting playback');
              newAudioElement.play().catch(playError => {
                console.error('Error starting playback:', playError);
                console.error('Audio element state during play error:', {
                  error: newAudioElement.error,
                  networkState: newAudioElement.networkState,
                  readyState: newAudioElement.readyState,
                  src: newAudioElement.src
                });
              });
            } catch (playErr) {
              console.error('Error in play attempt:', playErr);
            }
          };
          
          // Set as the current audio element
          setAudioElement(newAudioElement);
          
          // Create a placeholder track data object
          const trackData = {
            title: musicIdString.includes('firebase-') ? 
              musicIdString.replace('firebase-', '').split('.')[0] : 
              `Track ${musicId}`,
            artist: 'Unknown',
            id: musicId
          };
          
          // Attempt to find more detailed track info from Firebase music list
          if (directAudioUrl && musicIdString.includes('firebase-')) {
            // Try to find Firebase item with a matching URL or ID
            const matchingItem = window.firebaseMusicList?.find(item => 
              item.id === musicId || 
              item.audioUrl === directAudioUrl
            );
            
            if (matchingItem) {
              trackData.title = matchingItem.title || trackData.title;
              trackData.artist = matchingItem.artist || trackData.artist;
              trackData.genre = matchingItem.genre;
              trackData.imageUrl = matchingItem.imageUrl;
            }
          }
          
          // Set current track data
          setCurrentTrackData(trackData);
          
        } catch (error) {
          handleFetchError(error);
        }
      } else {
        // Fetch music details first to get the audio URL
        try {
          const response = await fetch(`http://localhost:8080/api/music/getMusic/${musicId}?includeAudioData=true`);
          if (!response.ok) {
            throw new Error(`Failed to fetch music details: ${response.status}`);
          }
          
          const musicData = await response.json();
          console.log('Fetched music data:', musicData);
          
          // Check if we have an audio URL
          if (musicData.audioUrl) {
            console.log('Using audio URL from music data:', musicData.audioUrl);
            newAudioElement.src = musicData.audioUrl;
          } else if (musicData.audioData) {
            // If we have base64 audio data, create a data URL
            console.log('Using base64 audio data');
            newAudioElement.src = `data:audio/mpeg;base64,${musicData.audioData}`;
          } else {
            throw new Error('No audio data available for this track');
          }
          
          // Set up event handlers
          const setupAudioEvents = () => {
            newAudioElement.onended = () => {
              console.log('Audio playback ended');
              setIsPlaying(false);
              setCurrentlyPlaying(null);
              setAudioElement(null);
              setAudioProgress(0);
              clearInterval(progressIntervalRef.current);
              progressIntervalRef.current = null;
            };
            
            newAudioElement.onplay = () => {
              console.log('Audio started playing');
              setIsPlaying(true);
            };
            
            newAudioElement.onpause = () => {
              console.log('Audio paused');
              setIsPlaying(false);
            };
            
            newAudioElement.onerror = (e) => {
              // Minimal error handling without extra logging
              stopPlayback();
            };
            
            // Set up progress tracking
            if (progressIntervalRef.current) {
              clearInterval(progressIntervalRef.current);
            }
            
            progressIntervalRef.current = setInterval(() => {
              if (newAudioElement && !newAudioElement.paused) {
                const progress = (newAudioElement.currentTime / newAudioElement.duration) * 100;
                setAudioProgress(progress);
                setAudioTime({
                  elapsed: formatTime(newAudioElement.currentTime),
                  total: formatTime(newAudioElement.duration)
                });
              }
            }, 1000);
          };
          
          // Set up events
          setupAudioEvents();
          
          // Wait for metadata before playing
          newAudioElement.onloadedmetadata = () => {
            try {
              console.log('Audio metadata loaded, starting playback');
              newAudioElement.play().catch(playError => {
                console.error('Error starting playback:', playError);
              });
            } catch (playErr) {
              console.error('Error in play attempt:', playErr);
            }
          };
          
          // Set as current audio element
          setAudioElement(newAudioElement);
          
          // Set current track data
          setCurrentTrackData({
            title: musicData.title,
            artist: musicData.artist,
            genre: musicData.genre,
            imageUrl: musicData.imageUrl,
            id: musicId
          });
          
        } catch (error) {
          handleFetchError(error);
        }
      }
    } catch (error) {
      console.error('Error in playMusic:', error);
      stopPlayback(); // Ensure cleanup on any error
    }
  };

  const playAlbumTrack = (track) => {
    try {
      console.log("Playing album track:", track);
      
      // Stop current audio if playing
      if (audioElement) {
        try {
          // Properly clean up and release resources
          // Remove all event listeners first
          audioElement.onended = null;
          audioElement.onplay = null;
          audioElement.onpause = null;
          audioElement.onerror = null;
          audioElement.onloadedmetadata = null;
          
          // Remove all event listeners added via addEventListener
          const events = ['play', 'pause', 'error', 'loadedmetadata', 'ended'];
          events.forEach(event => {
            audioElement.removeEventListener(event, () => {});
          });
          
          // Then pause and clear source
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
        });
      });

      // Set state - use the track id for currentlyPlaying
      setAudioElement(audio);
      setCurrentlyPlaying(track.id);
      setIsPlaying(true);
    } catch (error) {
      console.error('Error playing album track:', error);
    }
  };

  const togglePlayPause = (musicId) => {
    if (currentlyPlaying === musicId && audioElement) {
      if (audioElement.paused) {
        console.log("Attempting to play paused audio");
        audioElement.play()
          .catch(err => {
            console.error('Error playing audio:', err);
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
      if (!currentlyPlaying) {
        console.log("No song currently playing");
        return false;
      }
      
      // Debug information
      console.log('⏭️ Play Next: Current track ID:', currentlyPlaying);
      console.log('⏭️ Music list contains', musicList?.length || 0, 'items');
      console.log('⏭️ Current music category:', musicCategory);
      
      // If musicList is empty, just restart the current song
      if (!musicList || !Array.isArray(musicList) || musicList.length === 0) {
        console.log("⏭️ No music list provided - restarting current song");
        if (audioElement) {
          audioElement.currentTime = 0;
          audioElement.play().catch(err => {
            console.error('Error restarting audio:', err);
          });
          return true;
        }
        return false;
      }

      // Create a normalized copy of the music list
      const normalizedList = [...(musicList || [])].map(item => {
        // Get the proper ID from the item (could be in different formats)
        const rawId = item.id || item.musicId;
        const rawIdStr = String(rawId || '');
        
        // For Firebase items, the ID starts with firebase-
        const isFirebaseItem = rawIdStr.includes('firebase-');
        
        // For uploaded items, typically numeric IDs or other non-Firebase IDs
        const isUploadedItem = !isFirebaseItem && rawIdStr !== '';
        
        // Determine the category based on ID format
        const category = isFirebaseItem ? 'available' : (isUploadedItem ? 'uploaded' : 'unknown');
        
        // Create a normalized item with consistent ID field for comparison
        return {
          ...item,
          // Keep the original ID for consistent comparison
          trackId: rawIdStr,
          // For determining if it's a Firebase item
          isFirebase: isFirebaseItem,
          // For determining if it's an uploaded item
          isUploaded: isUploadedItem,
          // For category filtering
          category: category,
          // Extract the title portion of the filename for Firebase items
          // This helps with partial matching
          titleFromId: isFirebaseItem ? extractTitleFromFirebaseId(rawIdStr) : ''
        };
      }).filter(item => item.trackId !== null);
      
      // Helper function to extract the title portion from a Firebase ID
      function extractTitleFromFirebaseId(id) {
        if (!id || !id.includes('firebase-')) return '';
        
        // Remove the firebase- prefix
        const filename = id.replace('firebase-', '');
        
        // Remove file extension
        const withoutExtension = filename.replace('.mp3', '');
        
        // If it contains artist - title format, extract the title
        if (withoutExtension.includes(' - ')) {
          const parts = withoutExtension.split(' - ');
          if (parts.length >= 2) return parts[1];
        }
        
        return withoutExtension;
      }
      
      // Filter by category if needed
      let filteredList = normalizedList;
      if (musicCategory === 'uploaded') {
        filteredList = normalizedList.filter(item => !item.isFirebase);
        console.log('⏭️ Filtered to uploaded music only:', filteredList.length, 'items');
      } else if (musicCategory === 'available') {
        filteredList = normalizedList.filter(item => item.isFirebase);
        console.log('⏭️ Filtered to available music only:', filteredList.length, 'items');
      }
      
      // If the filtered list is empty, use the full list
      if (filteredList.length === 0) {
        console.log('⏭️ Filtered list is empty, using full list');
        filteredList = normalizedList;
      }
      
      // Find the current track
      const currentTrackIndex = filteredList.findIndex(item => 
        String(item.trackId) === String(currentlyPlaying)
      );
      
      console.log(`⏭️ Current track ID: "${currentlyPlaying}"`);
      console.log(`⏭️ Filtered list track IDs:`, filteredList.map(item => item.trackId));
      console.log(`⏭️ Current track index: ${currentTrackIndex} of ${filteredList.length}`);
      
      // If the track wasn't found in the list (index = -1) but we have tracks available,
      // try flexible matching first, then fall back to the first track
      if (currentTrackIndex === -1 && filteredList.length > 0) {
        // Try to find by partial match - sometimes IDs can have prefixes or different formats
        const moreFlexibleMatch = filteredList.findIndex(item => {
          // Convert both to strings for comparison
          const itemId = String(item.trackId || '');
          const currentId = String(currentlyPlaying || '');
          
          // Direct ID comparison
          if (itemId === currentId) return true;
          
          // Check if one contains the other (both ways)
          if (itemId.includes(currentId) || currentId.includes(itemId)) return true;
          
          // If both are Firebase items, try matching by the title portion
          const currentIsFirebase = currentId.includes('firebase-');
          const itemIsFirebase = itemId.includes('firebase-');
          
          if (currentIsFirebase && itemIsFirebase) {
            // Extract title portions for comparison
            const currentTitle = extractTitleFromFirebaseId(currentId).toLowerCase();
            const itemTitle = extractTitleFromFirebaseId(itemId).toLowerCase();
            
            // Check for title match if we have both titles
            if (currentTitle && itemTitle) {
              // Direct title match
              if (currentTitle === itemTitle) return true;
              
              // Title contains check
              if (currentTitle.includes(itemTitle) || itemTitle.includes(currentTitle)) return true;
            }
          }
          
          return false;
        });
        
        if (moreFlexibleMatch !== -1) {
          console.log(`⏭️ Found track with flexible matching at index ${moreFlexibleMatch}`);
          
          // Get the next track (or loop back to first)
          const nextIndex = (moreFlexibleMatch + 1) % filteredList.length;
          const nextTrack = filteredList[nextIndex];
          
          // Make sure nextTrack exists before proceeding
          if (!nextTrack) {
            console.error("⏭️ Next track is undefined, cannot proceed");
            return false;
          }
          
          console.log(`⏭️ Playing next track using flexible matching: ${nextTrack.title || 'Unknown Title'}`);
          
          // Only use the URL if it's not the page URL
          if (nextTrack.isFirebase && nextTrack.audioUrl && 
              nextTrack.audioUrl !== window.location.href && 
              nextTrack.audioUrl !== 'http://localhost:5173/home') {
            playMusic(nextTrack.trackId, nextTrack.audioUrl, nextTrack.category);
          } else {
            playMusic(nextTrack.trackId, null, nextTrack.category);
          }
          return true;
        }
        
        // If flexible matching failed, use the first track as fallback
        const firstTrack = filteredList[0];
        
        // Make sure firstTrack exists before proceeding
        if (!firstTrack) {
          return false;
        }
        
        console.log(`⏭️ Current track not in list, playing first track: ${firstTrack.title || 'Unknown'}`);
        
        // Only use the URL if it's not the page URL
        if (firstTrack.isFirebase && firstTrack.audioUrl && 
            firstTrack.audioUrl !== window.location.href && 
            firstTrack.audioUrl !== 'http://localhost:5173/home') {
          playMusic(firstTrack.trackId, firstTrack.audioUrl, firstTrack.category);
        } else {
          playMusic(firstTrack.trackId, null, firstTrack.category);
        }
        return true;
      }
      
      // If we found the current track and it's not the last one
      if (currentTrackIndex !== -1 && currentTrackIndex < filteredList.length - 1) {
        // Get the next track
        const nextTrack = filteredList[currentTrackIndex + 1];
        
        // Make sure nextTrack exists and has necessary properties before proceeding
        if (!nextTrack) {
          console.error("⏭️ Next track is undefined, cannot proceed");
          return false;
        }
        
        console.log(`⏭️ Next track: ${nextTrack.title || 'Unknown Title'} by ${nextTrack.artist || 'Unknown'}`);
        
        // Only use the URL if it's not the page URL
        if (nextTrack.isFirebase && nextTrack.audioUrl && 
            nextTrack.audioUrl !== window.location.href && 
            nextTrack.audioUrl !== 'http://localhost:5173/home') {
          playMusic(nextTrack.trackId, nextTrack.audioUrl, nextTrack.category);
        } else {
          playMusic(nextTrack.trackId, null, nextTrack.category);
        }
        return true;
      }
      
      // If it was the last track, loop back to the first one
      if (currentTrackIndex === filteredList.length - 1 && filteredList.length > 0) {
        const firstTrack = filteredList[0];
        
        // Make sure firstTrack exists before proceeding
        if (!firstTrack) {
          console.error("⏭️ First track is undefined, cannot loop");
          return false;
        }
        
        console.log(`⏭️ Looping to first track: ${firstTrack.title || 'Unknown Title'}`);
        
        // Only use the URL if it's not the page URL
        if (firstTrack.isFirebase && firstTrack.audioUrl && 
            firstTrack.audioUrl !== window.location.href && 
            firstTrack.audioUrl !== 'http://localhost:5173/home') {
          playMusic(firstTrack.trackId, firstTrack.audioUrl, firstTrack.category);
        } else {
          playMusic(firstTrack.trackId, null, firstTrack.category);
        }
        return true;
      }
      
      // If we still haven't found a match, restart the current song
      console.log("⏭️ No next song found, restarting current");
      if (audioElement) {
        audioElement.currentTime = 0;
        audioElement.play().catch(err => {
          console.error('Error restarting audio:', err);
        });
        return true;
      }
      
      // If no audio element, but we have tracks, play the first one
      if (filteredList.length > 0) {
        const firstTrack = filteredList[0];
        console.log("⏭️ No audio element, playing first track");
        
        if (firstTrack.isFirebase && firstTrack.audioUrl && 
            firstTrack.audioUrl !== window.location.href && 
            firstTrack.audioUrl !== 'http://localhost:5173/home') {
          playMusic(firstTrack.trackId, firstTrack.audioUrl, firstTrack.category);
        } else {
          playMusic(firstTrack.trackId, null, firstTrack.category);
        }
        return true;
      }
      
      return false;
    } catch (error) {
      console.error("Error in playNextSong:", error);
      return false;
    }
  };

  // Function to play previous song in the list
  const playPreviousSong = (musicList, albums) => {
    try {
      if (!currentlyPlaying) {
        console.log("No song currently playing");
        return false;
      }
      
      // Debug information
      console.log('⏮️ Play Previous: Current track ID:', currentlyPlaying);
      console.log('⏮️ Music list contains', musicList?.length || 0, 'items');
      console.log('⏮️ Current music category:', musicCategory);
      
      // If musicList is empty, just restart the current song
      if (!musicList || !Array.isArray(musicList) || musicList.length === 0) {
        console.log("⏮️ No music list provided - restarting current song");
        if (audioElement) {
          audioElement.currentTime = 0;
          audioElement.play().catch(err => {
            console.error('Error restarting audio:', err);
          });
          return true;
        }
        return false;
      }

      // Create a normalized copy of the music list
      const normalizedList = [...(musicList || [])].map(item => {
        // Get the proper ID from the item (could be in different formats)
        const rawId = item.id || item.musicId;
        const rawIdStr = String(rawId || '');
        
        // For Firebase items, the ID starts with firebase-
        const isFirebaseItem = rawIdStr.includes('firebase-');
        
        // For uploaded items, typically numeric IDs or other non-Firebase IDs
        const isUploadedItem = !isFirebaseItem && rawIdStr !== '';
        
        // Determine the category based on ID format
        const category = isFirebaseItem ? 'available' : (isUploadedItem ? 'uploaded' : 'unknown');
        
        // Create a normalized item with consistent ID field for comparison
        return {
          ...item,
          // Keep the original ID for consistent comparison
          trackId: rawIdStr,
          // For determining if it's a Firebase item
          isFirebase: isFirebaseItem,
          // For determining if it's an uploaded item
          isUploaded: isUploadedItem,
          // For category filtering
          category: category,
          // Extract the title portion of the filename for Firebase items
          // This helps with partial matching
          titleFromId: isFirebaseItem ? extractTitleFromFirebaseId(rawIdStr) : ''
        };
      }).filter(item => item.trackId !== null);
      
      // Helper function to extract the title portion from a Firebase ID
      function extractTitleFromFirebaseId(id) {
        if (!id || !id.includes('firebase-')) return '';
        
        // Remove the firebase- prefix
        const filename = id.replace('firebase-', '');
        
        // Remove file extension
        const withoutExtension = filename.replace('.mp3', '');
        
        // If it contains artist - title format, extract the title
        if (withoutExtension.includes(' - ')) {
          const parts = withoutExtension.split(' - ');
          if (parts.length >= 2) return parts[1];
        }
        
        return withoutExtension;
      }
      
      // Filter by category if needed
      let filteredList = normalizedList;
      if (musicCategory === 'uploaded') {
        filteredList = normalizedList.filter(item => !item.isFirebase);
        console.log('⏮️ Filtered to uploaded music only:', filteredList.length, 'items');
      } else if (musicCategory === 'available') {
        filteredList = normalizedList.filter(item => item.isFirebase);
        console.log('⏮️ Filtered to available music only:', filteredList.length, 'items');
      }
      
      // If the filtered list is empty, use the full list
      if (filteredList.length === 0) {
        console.log('⏮️ Filtered list is empty, using full list');
        filteredList = normalizedList;
      }
      
      // Find the current track
      const currentTrackIndex = filteredList.findIndex(item => 
        String(item.trackId) === String(currentlyPlaying)
      );
      
      console.log(`⏮️ Current track ID: "${currentlyPlaying}"`);
      console.log(`⏮️ Filtered list track IDs:`, filteredList.map(item => item.trackId));
      console.log(`⏮️ Current track index: ${currentTrackIndex} of ${filteredList.length}`);
      
      // If the track wasn't found in the list (index = -1) but we have tracks available,
      // try flexible matching first, then fall back to the last track
      if (currentTrackIndex === -1 && filteredList.length > 0) {
        // Try to find by partial match - sometimes IDs can have prefixes or different formats
        const moreFlexibleMatch = filteredList.findIndex(item => {
          // Convert both to strings for comparison
          const itemId = String(item.trackId || '');
          const currentId = String(currentlyPlaying || '');
          
          // Direct ID comparison
          if (itemId === currentId) return true;
          
          // Check if one contains the other (both ways)
          if (itemId.includes(currentId) || currentId.includes(itemId)) return true;
          
          // If both are Firebase items, try matching by the title portion
          const currentIsFirebase = currentId.includes('firebase-');
          const itemIsFirebase = itemId.includes('firebase-');
          
          if (currentIsFirebase && itemIsFirebase) {
            // Extract title portions for comparison
            const currentTitle = extractTitleFromFirebaseId(currentId).toLowerCase();
            const itemTitle = extractTitleFromFirebaseId(itemId).toLowerCase();
            
            // Check for title match if we have both titles
            if (currentTitle && itemTitle) {
              // Direct title match
              if (currentTitle === itemTitle) return true;
              
              // Title contains check
              if (currentTitle.includes(itemTitle) || itemTitle.includes(currentTitle)) return true;
            }
          }
          
          return false;
        });
        
        if (moreFlexibleMatch !== -1) {
          console.log(`⏮️ Found track with flexible matching at index ${moreFlexibleMatch}`);
          
          // Get the previous track (or loop back to last)
          const prevIndex = (moreFlexibleMatch - 1 + filteredList.length) % filteredList.length;
          const prevTrack = filteredList[prevIndex];
          
          // Make sure prevTrack exists before proceeding
          if (!prevTrack) {
            console.error("⏮️ Previous track is undefined, cannot proceed");
            return false;
          }
          
          console.log(`⏮️ Playing previous track using flexible matching: ${prevTrack.title || 'Unknown Title'}`);
          
          // Only use the URL if it's not the page URL
          if (prevTrack.isFirebase && prevTrack.audioUrl && 
              prevTrack.audioUrl !== window.location.href && 
              prevTrack.audioUrl !== 'http://localhost:5173/home') {
            playMusic(prevTrack.trackId, prevTrack.audioUrl, prevTrack.category);
          } else {
            playMusic(prevTrack.trackId, null, prevTrack.category);
          }
          return true;
        }
        
        // If flexible matching failed, use the last track as fallback
        const lastTrack = filteredList[filteredList.length - 1];
        
        // Make sure lastTrack exists before proceeding
        if (!lastTrack) {
          return false;
        }
        
        console.log(`⏮️ Current track not in list, playing last track: ${lastTrack.title || 'Unknown'}`);
        
        // Only use the URL if it's not the page URL
        if (lastTrack.isFirebase && lastTrack.audioUrl && 
            lastTrack.audioUrl !== window.location.href && 
            lastTrack.audioUrl !== 'http://localhost:5173/home') {
          playMusic(lastTrack.trackId, lastTrack.audioUrl, lastTrack.category);
        } else {
          playMusic(lastTrack.trackId, null, lastTrack.category);
        }
        return true;
      }
      
      // If we found the current track and it's not the first one
      if (currentTrackIndex > 0) {
        // Get the previous track
        const prevTrack = filteredList[currentTrackIndex - 1];
        
        // Make sure prevTrack exists and has necessary properties before proceeding
        if (!prevTrack) {
          console.error("⏮️ Previous track is undefined, cannot proceed");
          return false;
        }
        
        console.log(`⏮️ Previous track: ${prevTrack.title || 'Unknown Title'} by ${prevTrack.artist || 'Unknown'}`);
        
        // Only use the URL if it's not the page URL
        if (prevTrack.isFirebase && prevTrack.audioUrl && 
            prevTrack.audioUrl !== window.location.href && 
            prevTrack.audioUrl !== 'http://localhost:5173/home') {
          playMusic(prevTrack.trackId, prevTrack.audioUrl, prevTrack.category);
        } else {
          playMusic(prevTrack.trackId, null, prevTrack.category);
        }
        return true;
      }
      
      // If it was the first track, loop back to the last one
      if (currentTrackIndex === 0 && filteredList.length > 0) {
        const lastTrack = filteredList[filteredList.length - 1];
        
        // Make sure lastTrack exists before proceeding
        if (!lastTrack) {
          console.error("⏮️ Last track is undefined, cannot loop");
          return false;
        }
        
        console.log(`⏮️ Looping to last track: ${lastTrack.title || 'Unknown Title'}`);
        
        // Only use the URL if it's not the page URL
        if (lastTrack.isFirebase && lastTrack.audioUrl && 
            lastTrack.audioUrl !== window.location.href && 
            lastTrack.audioUrl !== 'http://localhost:5173/home') {
          playMusic(lastTrack.trackId, lastTrack.audioUrl, lastTrack.category);
        } else {
          playMusic(lastTrack.trackId, null, lastTrack.category);
        }
        return true;
      }
      
      // If we still haven't found a match, restart the current song
      console.log("⏮️ No previous song found, restarting current");
      if (audioElement) {
        audioElement.currentTime = 0;
        audioElement.play().catch(err => {
          console.error('Error restarting audio:', err);
        });
        return true;
      }
      
      // If no audio element, but we have tracks, play the last one
      if (filteredList.length > 0) {
        const lastTrack = filteredList[filteredList.length - 1];
        console.log("⏮️ No audio element, playing last track");
        
        if (lastTrack.isFirebase && lastTrack.audioUrl && 
            lastTrack.audioUrl !== window.location.href && 
            lastTrack.audioUrl !== 'http://localhost:5173/home') {
          playMusic(lastTrack.trackId, lastTrack.audioUrl, lastTrack.category);
        } else {
          playMusic(lastTrack.trackId, null, lastTrack.category);
        }
        return true;
      }
      
      return false;
    } catch (error) {
      console.error("Error in playPreviousSong:", error);
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
    try {
      // Store the seek request in case we need to apply it later
      pendingSeekRef.current = Math.max(0, Math.min(100, positionPercent));
      
      // Log what we're trying to do
      console.log(`Request to seek to ${pendingSeekRef.current.toFixed(2)}%`);
      
      if (!audioElement) {
        console.log("Audio element not available - stored seek request for later");
        return;
      }
      
      if (!audioElement.duration || isNaN(audioElement.duration) || audioElement.duration === Infinity) {
        console.log("Invalid duration - stored seek request for later");
        return;
      }
      
      // Make sure position is valid
      const position = pendingSeekRef.current;
      const newTime = (position / 100) * audioElement.duration;
      
      console.log(`Setting audio time to ${newTime.toFixed(2)}s / ${audioElement.duration.toFixed(2)}s`);
      
      // Set the current time
      audioElement.currentTime = newTime;
      
      // Update the time display
      setAudioTime({
        elapsed: formatTime(newTime),
        total: formatTime(audioElement.duration)
      });
      
      // Clear the pending seek since we applied it
      pendingSeekRef.current = null;
    } catch (error) {
      console.error("Error in seekAudio:", error);
    }
  };

  const stopPlayback = () => {
    return new Promise(resolve => {
      try {
        if (audioElement) {
          // Create a local reference to the audio element
          const audio = audioElement;
          
          // Remove all event listeners to prevent errors
          audio.onended = null;
          audio.onplay = null;
          audio.onpause = null;
          audio.onerror = null;
          audio.onloadedmetadata = null;
          
          // Remove all event listeners added via addEventListener
          const events = ['play', 'pause', 'error', 'loadedmetadata', 'ended'];
          events.forEach(event => {
            audio.removeEventListener(event, () => {});
          });
          
          // Pause and clear the audio source
          try {
            if (audio.pause && typeof audio.pause === 'function') {
              audio.pause();
            }
          } catch (err) {
            console.error('Error pausing audio:', err);
          }
          
          // Clear the source and load to release resources
          try {
            if (audio.src) {
              audio.src = '';
              if (audio.load && typeof audio.load === 'function') {
                audio.load();
              }
            }
          } catch (err) {
            console.error('Error clearing audio source:', err);
          }
          
          // Reset state
          setCurrentlyPlaying(null);
          setAudioProgress(0);
          setAudioElement(null);
          setIsPlaying(false);
          setCurrentTrackData(null);
          
          // Clear the progress interval
          if (progressIntervalRef.current) {
            clearInterval(progressIntervalRef.current);
            progressIntervalRef.current = null;
          }
        }
        
        // Give the browser a moment to release audio resources
        setTimeout(resolve, 10);
      } catch (err) {
        console.error('Error in stopPlayback:', err);
        resolve(); // Resolve anyway to allow execution to continue
      }
    });
  };

  return (
    <AudioPlayerContext.Provider
      value={{
        currentlyPlaying,
        isPlaying,
        audioProgress,
        audioTime,
        playMusic,
        togglePlayPause,
        seekAudio,
        playAlbumTrack,
        playNextSong,
        playPreviousSong,
        handleDoubleClick,
        stopPlayback,
        currentTrackData,
        getImageUrl,
        musicCategory,
        setMusicCategory
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