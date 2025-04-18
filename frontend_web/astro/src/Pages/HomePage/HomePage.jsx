import React, { useState, useEffect, useRef } from "react";
import { Link } from "react-router-dom";
import NavBar from "../../components/NavBar/NavBar";
import Sidebar from "../../components/Sidebar/Sidebar";
import { useUser } from "../../context/UserContext";
import { useFavorites } from "../../context/FavoritesContext";
import { useAudioPlayer } from "../../context/AudioPlayerContext";
import styles from "./HomePage.module.css";
import Modal from '../../components/Modal/Modal';
import AudioUploader from "../../components/AudioUploader";
import UploadModal from '../../components/UploadModal';

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

// Add this function after imports and before the component definition
const resizeImage = (file, maxWidth = 800, maxHeight = 600, quality = 0.7) => {
  return new Promise((resolve) => {
    const reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onload = (event) => {
      const img = new Image();
      img.src = event.target.result;

      img.onload = () => {
        // Calculate new dimensions while maintaining aspect ratio
        let width = img.width;
        let height = img.height;

        if (width > maxWidth) {
          height = Math.round(height * (maxWidth / width));
          width = maxWidth;
        }

        if (height > maxHeight) {
          width = Math.round(width * (maxHeight / height));
          height = maxHeight;
        }

        // Create canvas and resize
        const canvas = document.createElement('canvas');
        canvas.width = width;
        canvas.height = height;

        // Draw resized image
        const ctx = canvas.getContext('2d');
        ctx.drawImage(img, 0, 0, width, height);

        // Convert to base64 with reduced quality
        const resizedBase64 = canvas.toDataURL(file.type, quality);

        // Log size reduction
        console.log(`Original size: ~${Math.round(event.target.result.length / 1024)}KB, Resized: ~${Math.round(resizedBase64.length / 1024)}KB`);

        resolve(resizedBase64);
      };
    };
  });
};

export const HomePage = () => {
  const { user } = useUser();
  const { favorites, toggleFavorite, isFavorite, refreshFavorites } = useFavorites();
  const {
    currentlyPlaying,
    isPlaying,
    playMusic,
    togglePlayPause,
    playAlbumTrack,
    playNextSong,
    playPreviousSong,
    handleDoubleClick,
    stopPlayback,
    getImageUrl
  } = useAudioPlayer();

  const userName = user?.userName || "Guest";
  const [showUploadModal, setShowUploadModal] = useState(false);
  const [enteredPassword, setEnteredPassword] = useState('');
  const [selectedFile, setSelectedFile] = useState(null);
  const [uploadError, setUploadError] = useState('');
  const [isUploading, setIsUploading] = useState(false);
  const [isPasswordVerified, setIsPasswordVerified] = useState(true);
  const [passwordError, setPasswordError] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [musicTitle, setMusicTitle] = useState('');
  const [musicArtist, setMusicArtist] = useState('');
  const [musicGenre, setMusicGenre] = useState('');
  const [musicUrl, setMusicUrl] = useState('');
  const [useExternalUrl, setUseExternalUrl] = useState(false);
  const [musicImageUrl, setMusicImageUrl] = useState('');
  const [selectedImageFile, setSelectedImageFile] = useState(null);
  const [useImageUrl, setUseImageUrl] = useState(true);
  const [musicList, setMusicList] = useState([]);
  const [selectedFileInfo, setSelectedFileInfo] = useState(null);
  const [isImageLoading, setIsImageLoading] = useState(true);
  const [showEditModal, setShowEditModal] = useState(false);
  const [editingMusic, setEditingMusic] = useState(null);
  const [isEditing, setIsEditing] = useState(false);
  const [firebaseMusicList, setFirebaseMusicList] = useState([]);
  const [isUploadModalOpen, setIsUploadModalOpen] = useState(false);

  // Add refs for tracking double clicks and click prevention
  const lastClickTimeRef = useRef({});
  const doubleClickThreshold = 300; // milliseconds
  const isProcessingClickRef = useRef(false); // Track if we're currently processing a click
  const lockoutTimerRef = useRef(null); // For click lockout

  // Strict function to prevent any music playback during lockout period
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

  // Fetch music list when component mounts
  useEffect(() => {
    fetchMusicList();
    fetchFirebaseMusic();
  }, []);

  // Update the useEffect that fetches favorites
  useEffect(() => {
    if (user && user.userId) {
      refreshFavorites();
    }
  }, [user, refreshFavorites]);

  useEffect(() => {
    if (editingMusic) {
      setMusicTitle(editingMusic.title);
      setMusicArtist(editingMusic.artist);
      setMusicGenre(editingMusic.genre || '');
      setMusicImageUrl(editingMusic.imageUrl || '');
    }
  }, [editingMusic]);

  // Add useEffect for periodic refresh of Firebase music
  useEffect(() => {
    // Try to load Firebase music from localStorage first
    try {
      const savedFirebaseMusic = localStorage.getItem('firebase-music-list');
      if (savedFirebaseMusic) {
        const parsedList = JSON.parse(savedFirebaseMusic);
        console.log(`Loaded ${parsedList.length} Firebase music items from localStorage`);
        setFirebaseMusicList(parsedList);
        
        // Make Firebase music list globally available for debugging
        window.firebaseMusicList = parsedList;
      }
    } catch (error) {
      console.error('Error loading Firebase music from localStorage:', error);
    }
    
    // Initial fetch from Firebase storage
    fetchFirebaseMusic();

    // Set up a periodic refresh of Firebase music every 30 seconds
    const refreshInterval = setInterval(() => {
      fetchFirebaseMusic();
    }, 30000); // 30 seconds

    // Clean up the interval when component unmounts
    return () => clearInterval(refreshInterval);
  }, []); // Empty dependency array means this runs once on mount

  // Combine Firebase and regular music items when either list changes
  useEffect(() => {
    // Make Firebase music list globally available for debugging
    // This is useful for the next/previous buttons in the audio player
    window.firebaseMusicList = firebaseMusicList;

    // Create a combined list for display, sorting, etc. (optional)
    // const allMusic = [...musicList, ...firebaseMusicList];
    // console.log(`Combined music list has ${allMusic.length} items`);
  }, [firebaseMusicList, musicList]);

  const fetchMusicList = async () => {
    try {
      const response = await fetch('http://localhost:8080/api/music/getAllMusic');
      if (response.ok) {
        const data = await response.json();
        console.log('Got music data from server:', data);

        // Process the data to ensure image URLs are properly preserved
        const processedData = data.map(music => {
          // Check for stored image in localStorage
          const storedImage = localStorage.getItem(`music-image-${music.musicId}`);

          // If the music doesn't have an imageUrl but we have a stored one, use it
          if (!music.imageUrl && storedImage) {
            console.log(`Restoring image for music ${music.musicId} from localStorage`);
            music.imageUrl = storedImage;
          }
          // If we have an image URL from the server, store it for future sessions
          else if (music.imageUrl && !isDataUri(music.imageUrl)) {
            console.log(`Storing image URL for music ${music.musicId} in localStorage`);
            localStorage.setItem(`music-image-${music.musicId}`, music.imageUrl);
          }

          return music;
        });

        setMusicList(processedData);
      }
    } catch (error) {
      console.error('Error fetching music list:', error);
    }
  };

  // Function to fetch music from Firebase storage
  const fetchFirebaseMusic = async () => {
    try {
      console.log('Attempting to fetch Firebase music files...');

      // Use the imported Firebase storage directly
      const { storage } = await import('../../firebase');
      const { ref, listAll, getDownloadURL } = await import('firebase/storage');

      // Create reference to 'audios' folder in Firebase Storage
      const listRef = ref(storage, 'audios');

      console.log('Listing files in Firebase storage...');
      const listResult = await listAll(listRef);
      console.log(`Found ${listResult.items.length} files in Firebase storage`);

      if (listResult.items.length === 0) {
        console.log('No files found in Firebase storage');
        // Don't clear the list if empty, may have edited items in localStorage
        return [];
      }

      const musicFiles = await Promise.all(
        listResult.items.map(async (itemRef) => {
          try {
            // Get the download URL for each file
            console.log(`Getting download URL for ${itemRef.name}...`);
            const url = await getDownloadURL(itemRef);

            // Get file name
            const name = itemRef.name;
            console.log(`Processing file: ${name}`);

            // Parse metadata from filename
            let artist = "Unknown Artist";
            let title = name.replace(".mp3", "");
            let genre = "Music";

            // Parse artist and title from the filename
            const parts = name.split(' - ');
            if (parts.length >= 2) {
              artist = parts[0];
              title = parts[1].replace('.mp3', '');

              // Extract genre if present in brackets
              const genreMatch = title.match(/\[(.*?)\]/);
              if (genreMatch && genreMatch[1]) {
                genre = genreMatch[1];
                title = title.replace(/\[.*?\]/, '').trim();
              }
            }

            return {
              id: `firebase-${name}`,
              title: title,
              artist: artist,
              genre: genre,
              audioUrl: url
            };
          } catch (itemError) {
            console.error(`Error processing file ${itemRef.name}:`, itemError);
            return null;
          }
        })
      );

      // Filter out any null entries (from errors)
      const validMusicFiles = musicFiles.filter(item => item !== null);
      console.log(`Successfully processed ${validMusicFiles.length} music files`);

      // Get existing edited music from localStorage
      let existingEditedMusic = [];
      try {
        const savedMusicData = localStorage.getItem('firebase-music-list');
        if (savedMusicData) {
          existingEditedMusic = JSON.parse(savedMusicData);
        }
      } catch (error) {
        console.error('Error reading edited music from localStorage:', error);
      }

      // Merge fresh Firebase data with localStorage edits
      const mergedMusicList = validMusicFiles.map(newItem => {
        // Find if this item has been edited in localStorage
        const editedItem = existingEditedMusic.find(item => item.id === newItem.id);
        if (editedItem) {
          // Keep the audioUrl from the new Firebase data but use edited metadata
          return {
            ...editedItem,
            audioUrl: newItem.audioUrl // Always keep the latest URL
          };
        }
        return newItem;
      });

      // Set the merged list in state
      setFirebaseMusicList(mergedMusicList);
      
      // Also update localStorage with the merged list
      localStorage.setItem('firebase-music-list', JSON.stringify(mergedMusicList));
      
      return mergedMusicList;
    } catch (error) {
      console.error('Error fetching Firebase music:', error);

      // If fetching fails, try to get data from localStorage
      try {
        const savedMusicData = localStorage.getItem('firebase-music-list');
        if (savedMusicData) {
          const parsedData = JSON.parse(savedMusicData);
          console.warn('Using Firebase music data from localStorage as fallback.', parsedData.length, 'items found');
          setFirebaseMusicList(parsedData);
          return parsedData;
        }
      } catch (localStorageError) {
        console.error('Error reading from localStorage:', localStorageError);
      }

      // Otherwise use hardcoded fallback
      console.warn('Using fallback Firebase audio files.');
      const fallbackMusic = [
        {
          id: 'firebase-aaron-smith',
          title: 'Dancin (KRONO Remix)',
          artist: 'Aaron Smith',
          genre: 'Remix',
          audioUrl: 'https://firebasestorage.googleapis.com/v0/b/astroglowfirebase-d2411.firebasestorage.app/o/audios%2FAaron%20Smith%20-%20Dancin%20(KRONO%20Remix)%20-%20Lyrics.mp3?alt=media&token=c4035a45-81ad-4989-8a2b-6ce47a418d4b'
        },
        {
          id: 'firebase-smile-dk',
          title: 'Butterfly (Lyrics)',
          artist: 'Smile.Dk',
          genre: 'KPOP',
          audioUrl: 'https://firebasestorage.googleapis.com/v0/b/astroglowfirebase-d2411.firebasestorage.app/o/audios%2FSmile.Dk%20-%20Butterfly%20(Lyrics)%20Ay%20ay%20ayi%27m%20your%20little%20butterfly%20%5BTiktok%20song%5D.mp3?alt=media&token=c4035a45-81ad-4989-8a2b-6ce47a418d4b'
        }
      ];
      setFirebaseMusicList(fallbackMusic);
      return fallbackMusic;
    }
  };

  const handleUploadClick = () => {
    setShowUploadModal(true);
    setIsPasswordVerified(true);
    setSelectedFile(null);
    setSelectedImageFile(null);
    setUploadError('');
    setPasswordError('');
    setShowPassword(false);
    setMusicTitle('');
    setMusicArtist('');
    setMusicGenre('');
    setMusicUrl('');
    setMusicImageUrl('');
    setUseExternalUrl(false);
    setUseImageUrl(true);
  };

  const handleFileChange = (e) => {
    const file = e.target.files[0];
    if (file && file.type === 'audio/mpeg') {
      setSelectedFile(file);
      setUploadError('');

      // Try to extract title and artist from filename
      const filename = file.name.replace('.mp3', '');
      const parts = filename.split(' - ');
      if (parts.length >= 2) {
        setMusicArtist(parts[0]);
        setMusicTitle(parts[1]);
      } else {
        setMusicTitle(filename);
      }

      // Format file size for display
      let fileSize;
      if (file.size < 1024 * 1024) {
        fileSize = (file.size / 1024).toFixed(2) + ' KB';
      } else {
        fileSize = (file.size / (1024 * 1024)).toFixed(2) + ' MB';
      }

      setSelectedFileInfo({
        name: file.name,
        size: fileSize
      });
    } else {
      setSelectedFile(null);
      setUploadError('Please select an MP3 file');
      setSelectedFileInfo(null);
    }
  };

  const handleImageFileChange = async (e) => {
    const file = e.target.files[0];
    if (file && (file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/gif')) {
      setSelectedImageFile(file);

      try {
        // Check file size - if over 1MB, resize the image
        if (file.size > 1024 * 1024) {
          console.log(`Image is large (${(file.size / 1024 / 1024).toFixed(2)}MB), resizing...`);
          const resizedImage = await resizeImage(file);
          setMusicImageUrl(resizedImage);
        } else {
          // For smaller images, just convert to base64 without resizing
          const reader = new FileReader();
          reader.onload = (event) => {
            setMusicImageUrl(event.target.result);
          };
          reader.readAsDataURL(file);
        }

        // Set useImageUrl to true since we're using a data URI
        setUseImageUrl(true);
        setUploadError('');
      } catch (error) {
        console.error('Error processing image:', error);
        setUploadError('Error processing image. Please try another image.');
        setSelectedImageFile(null);
      }
    } else {
      setSelectedImageFile(null);
      setUploadError('Please select a valid image file (JPEG, PNG, or GIF)');
    }
  };

  const handleUpload = async () => {
    if (!useExternalUrl && !selectedFile) {
      setUploadError('Please select an MP3 file');
      return;
    }

    if (useExternalUrl && !musicUrl) {
      setUploadError('Please provide a music URL');
      return;
    }

    if (!musicTitle || !musicArtist) {
      setUploadError('Please provide a title and artist for the music');
      return;
    }

    // Check for duplicate music (same title and artist)
    const isDuplicate = musicList.some(music =>
      music.title.toLowerCase() === musicTitle.toLowerCase() &&
      music.artist.toLowerCase() === musicArtist.toLowerCase()
    );

    if (isDuplicate) {
      setUploadError('This song already exists in the library. Please upload a different song.');
      return;
    }

    setIsUploading(true);
    try {
      let response;

      if (useExternalUrl) {
        // Create URL-encoded form data for URL-based music
        const formData = new URLSearchParams();
        formData.append('title', musicTitle);
        formData.append('artist', musicArtist);
        formData.append('genre', musicGenre || 'Unknown');
        formData.append('audioUrl', musicUrl);

        // We're always using the imageUrl input field now
        if (musicImageUrl) {
          formData.append('imageUrl', musicImageUrl);
        }

        response = await fetch('http://localhost:8080/api/music/addMusicWithUrl', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
          },
          body: formData
        });
      } else {
        // Use FormData to send the file and metadata
        const formData = new FormData();
        formData.append('file', selectedFile);
        formData.append('title', musicTitle);
        formData.append('artist', musicArtist);
        formData.append('genre', musicGenre || 'Unknown');

        // We now always use imageUrl field to handle both URLs and base64 data
        if (musicImageUrl) {
          formData.append('imageUrl', musicImageUrl);
        }

        // Log the data being sent
        console.log('Uploading file:', selectedFile.name, 'Size:', selectedFile.size);
        console.log('Image URL provided:', musicImageUrl ? 'Yes (length: ' + musicImageUrl.substring(0, 20) + '...)' : 'No');

        response = await fetch('http://localhost:8080/api/music/upload', {
          method: 'POST',
          body: formData,
        });
      }

      if (!response.ok) {
        const errorData = await response.text();
        throw new Error(`Upload failed: ${errorData}`);
      }

      // Get the response data
      const result = await response.json();
      console.log('Upload successful:', result);

      // Store the image URL in localStorage for persistence
      if (result.musicId && musicImageUrl) {
        localStorage.setItem(`music-image-${result.musicId}`, musicImageUrl);
        console.log(`Saved image URL for music ${result.musicId} to localStorage`);
      }

      // Show success message
      alert('Music uploaded successfully!');

      // Refresh music list
      await fetchMusicList();

      // Reset form and close modal
      setEnteredPassword('');
      setSelectedFile(null);
      setSelectedImageFile(null);
      setSelectedFileInfo(null);
      setShowUploadModal(false);
      setUploadError('');
      setIsPasswordVerified(true);
      setMusicTitle('');
      setMusicArtist('');
      setMusicGenre('');
      setMusicUrl('');
      setMusicImageUrl('');
      setUseExternalUrl(false);
      setUseImageUrl(true);
    } catch (error) {
      console.error('Failed to upload file:', error);
      setUploadError(`Failed to upload: ${error.message}`);
    } finally {
      setIsUploading(false);
    }
  };

  const handleCloseModal = () => {
    setShowUploadModal(false);
    setEnteredPassword('');
    setSelectedFile(null);
    setSelectedImageFile(null);
    setUploadError('');
    setPasswordError('');
    setIsPasswordVerified(true);
    setMusicTitle('');
    setMusicArtist('');
    setMusicGenre('');
    setMusicUrl('');
    setMusicImageUrl('');
    setUseExternalUrl(false);
    setUseImageUrl(true);
  };

  // Add this function after the existing handleSaveEdit function
  const getFeaturedMusic = () => {
    // If there are no music tracks, return an empty array
    if (!musicList || musicList.length === 0) {
      return [];
    }

    // Use a deterministic selection method based on musicId to avoid shuffling on each render
    // Sort first by musicId to ensure stable order
    const sortedMusic = [...musicList].sort((a, b) => a.musicId - b.musicId);

    // Take the first 4 tracks (or fewer if there aren't enough)
    // These will be stable across renders
    return sortedMusic.slice(0, Math.min(4, sortedMusic.length));
  };

  // Add this function after getFeaturedMusic
  const getDiscoveries = () => {
    // If there are no music tracks, return an empty array
    if (!musicList || musicList.length === 0) {
      return [];
    }

    // Sort by newest tracks (assuming higher musicId means newer)
    // This ensures a stable sorting that won't change on re-renders
    const sorted = [...musicList].sort((a, b) => b.musicId - a.musicId);

    // Return up to 6 tracks for weekly discoveries, but handle if we have fewer
    return sorted.slice(0, Math.min(6, sorted.length));
  };

  // Handle click on music card - play music or pause if already playing
  const handleMusicCardClick = (e, musicId) => {
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
          // Find the Firebase item if it exists
          const firebaseItem = firebaseMusicList.find(item => {
            // Direct ID match
            if (item.id === musicId) return true;
            // ID match without .mp3 extension
            if (item.id.replace('.mp3', '') === musicId) return true;
            // ID match with .mp3 extension added
            if (item.id === musicId + '.mp3') return true;
            return false;
          });
          
          if (firebaseItem) {
            console.log('Playing Firebase audio file:', firebaseItem.title);
            playMusic(musicId, firebaseItem.audioUrl);
          } else {
            console.log('Playing regular music file, ID:', musicId);
            playMusic(musicId);
          }
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

  // Add function to delete music
  const handleDeleteMusic = async (musicId, event) => {
    event.stopPropagation(); // Prevent triggering card click

    if (window.confirm('Are you sure you want to delete this song? This action cannot be undone.')) {
      try {
        const response = await fetch(`http://localhost:8080/api/music/deleteMusic/${musicId}`, {
          method: 'DELETE',
          headers: {
            'Content-Type': 'application/json',
          },
        });

        if (response.ok) {
          // Remove the deleted music from the state
          setMusicList(prevList => prevList.filter(music => music.musicId !== musicId));
          // If the deleted music was playing, stop playback
          if (currentlyPlaying === musicId) {
            stopPlayback();
          }
          // Remove the stored image URL from localStorage
          localStorage.removeItem(`music-image-${musicId}`);
          console.log(`Removed image URL for deleted music ${musicId} from localStorage`);
        } else {
          console.error('Failed to delete music:', await response.text());
        }
      } catch (error) {
        console.error('Error deleting music:', error);
      }
    }
  };

  const handleEditClick = (music, event) => {
    event.stopPropagation(); // Prevent triggering card click

    // Store the ID type to distinguish between Firebase and database items
    const isFirebaseItem = music.id && !music.musicId;
    
    setEditingMusic({
      ...music,
      // Store both ID types for proper handling
      musicId: music.musicId || null,
      id: music.id || null,
      isFirebaseItem: isFirebaseItem
    });
    
    setUseImageUrl(true);
    setShowEditModal(true);
  };

  const handleCloseEditModal = () => {
    setShowEditModal(false);
    setEditingMusic(null);
    setMusicTitle('');
    setMusicArtist('');
    setMusicGenre('');
    setMusicImageUrl('');
    setSelectedImageFile(null);
    setUseImageUrl(true);
    setUploadError('');
    setIsEditing(false);
  };

  const handleSaveEdit = async () => {
    if (!musicTitle || !musicArtist) {
      setUploadError('Please provide a title and artist for the music');
      return;
    }

    setIsEditing(true);
    try {
      // Check if we're editing a Firebase item
      if (editingMusic.isFirebaseItem || (editingMusic.id && editingMusic.id.startsWith('firebase-'))) {
        // For Firebase items, we just update the local state
        console.log("Updating Firebase music item:", editingMusic.id);
        
        // Create updated Firebase item
        const updatedItem = {
          id: editingMusic.id,
          title: musicTitle,
          artist: musicArtist,
          genre: musicGenre || 'Unknown',
          audioUrl: editingMusic.audioUrl // Preserve the original audio URL
        };
        
        // Add image URL if available
        if (musicImageUrl) {
          updatedItem.imageUrl = musicImageUrl;
        } else if (editingMusic.imageUrl) {
          updatedItem.imageUrl = editingMusic.imageUrl;
        }
        
        // Update the Firebase music list in state
        setFirebaseMusicList(prevList => {
          const newList = prevList.map(item => {
            if (item.id === editingMusic.id) {
              return updatedItem;
            }
            return item;
          });
          
          // Store the updated list in localStorage for persistence across refreshes
          try {
            localStorage.setItem('firebase-music-list', JSON.stringify(newList));
            console.log('Firebase music list saved to localStorage');
          } catch (err) {
            console.error('Failed to save Firebase music list to localStorage:', err);
          }
          
          return newList;
        });
        
        // Show notification message in console
        console.log('Firebase music updated successfully!');
        
        // Close modal
        handleCloseEditModal();
        return;
      }
      
      // For database items, proceed with API call
      // Create a music object with the updated information
      const musicUpdate = {
        musicId: editingMusic.musicId,
        title: musicTitle,
        artist: musicArtist,
        genre: musicGenre || 'Unknown',
      };

      // Add image URL if available
      if (musicImageUrl) {
        musicUpdate.imageUrl = musicImageUrl;
      } else if (editingMusic.imageUrl) {
        // Keep existing image if no new one is provided
        musicUpdate.imageUrl = editingMusic.imageUrl;
      }

      // Ensure the musicId is valid before making the API call
      if (!editingMusic.musicId || isNaN(editingMusic.musicId)) {
        throw new Error('Invalid music ID for database update');
      }

      const response = await fetch(`http://localhost:8080/api/music/putMusic/${editingMusic.musicId}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(musicUpdate),
      });

      if (!response.ok) {
        const errorText = await response.text();
        console.error('Server response:', errorText);
        throw new Error(`Update failed: ${response.status} ${response.statusText}`);
      }

      // Get the response data
      const result = await response.json();

      // Log success message instead of alert
      console.log('Music updated successfully!');

      // Update music list with edited data
      setMusicList(prevList => prevList.map(item => {
        if (item.musicId === editingMusic.musicId) {
          // Create an updated item
          const updatedItem = {
            ...item,
            title: musicTitle,
            artist: musicArtist,
            genre: musicGenre || item.genre,
          };

          // Update the imageUrl if changed
          if (musicImageUrl) {
            updatedItem.imageUrl = musicImageUrl;
          }

          return updatedItem;
        }
        return item;
      }));

      // Close modal
      handleCloseEditModal();

    } catch (error) {
      console.error('Failed to update music:', error);
      setUploadError(`Failed to update: ${error.message}`);
    } finally {
      setIsEditing(false);
    }
  };

  // Function to handle opening the upload modal
  const openUploadModal = () => {
    setIsUploadModalOpen(true);
  };

  // Function to handle closing the upload modal
  const closeUploadModal = () => {
    setIsUploadModalOpen(false);
  };

  // Function to handle the final upload data from the modal
  const handleUploadComplete = (uploadData) => {
    console.log('HomePage: Upload Complete Data:', uploadData);

    // Create a properly formatted Firebase item from the uploaded data
    const newFirebaseMusic = {
      id: `firebase-${uploadData.audioFileName || Date.now()}`,
      title: uploadData.title,
      artist: uploadData.artist,
      genre: uploadData.genre || 'Music',
      audioUrl: uploadData.audioUrl,
      imageUrl: uploadData.imageUrl // Use the image from the modal
    };
    
    console.log('Adding new Firebase music item to list:', newFirebaseMusic);

    // Update the Firebase music list with the new item
    setFirebaseMusicList(prev => {
      // Check if this item already exists (in case of duplicate filename)
      const exists = prev.some(item => item.id === newFirebaseMusic.id);
      
      let updatedList;
      if (exists) {
        // Replace the existing item
        updatedList = prev.map(item => 
          item.id === newFirebaseMusic.id ? newFirebaseMusic : item
        );
      } else {
        // Add as a new item
        updatedList = [...prev, newFirebaseMusic];
      }
      
      // Save the updated list to localStorage for persistence
      try {
        localStorage.setItem('firebase-music-list', JSON.stringify(updatedList));
        console.log('Updated Firebase music list saved to localStorage');
      } catch (err) {
        console.error('Failed to save Firebase music list to localStorage:', err);
      }
      
      return updatedList;
    });

    // Optionally refresh the music list from DB if the modal might have saved there too
    fetchMusicList();
    
    // Also fetch from Firebase storage after a delay to sync with remote storage
    setTimeout(() => fetchFirebaseMusic(), 1500);
  };

  // Handle play button click for featured section
  const handleFeaturedPlayClick = (e, musicId) => {
    // Use the same handler as music cards for consistency
    handleMusicCardClick(e, musicId);
  };

  // Handle play button click for discoveries section 
  const handleDiscoveryPlayClick = (e, musicId) => {
    // Use the same handler as music cards for consistency
    handleMusicCardClick(e, musicId);
  };

  return (
    <div className={styles.homePage}>
      <NavBar />
      <div className={styles.pageContent}>
        <Sidebar />
        <main className={styles.mainContent}>
          <div className={styles.headerSection}>
            <h1 className={styles.pageTitle}>Welcome, {userName}!</h1>
            <button onClick={openUploadModal} className={styles.uploadModalButton}>
              Upload New Music
            </button>
          </div>

          {/* Uploaded Music Section */}
          {(musicList.length > 0 || firebaseMusicList.length > 0) && (
            <section className={styles.uploadedMusicSection}>
              <h2 className={styles.sectionTitle}>Your Uploaded Music</h2>
              <div className={styles.musicGrid}>
                {/* Dynamic Firebase Music Cards */}
                {firebaseMusicList.map((music) => {
                  const isCurrentlyPlaying = currentlyPlaying === music.id;
                  const isFavorited = isFavorite(music.id);
                  // Use getSafeImageUrl for Firebase images too
                  const imageUrl = getSafeImageUrl(music.imageUrl, getImageUrl);

                  // Format title and artist for display
                  const displayTitle = music.title || 'Unknown Title';
                  const displayArtist = music.artist || 'Unknown Artist';
                  const displayGenre = music.genre || 'Music';

                  return (
                    <div key={music.id}
                      className={`${styles.musicCard} ${isCurrentlyPlaying ?
                        (!isPlaying ? styles.pausedCard : styles.currentlyPlayingCard) : ''}`}
                      onClick={(e) => handleMusicCardClick(e, music.id)}
                    >
                      <div className={styles.musicImageContainer}>
                        {/* Conditionally render image or placeholder */}
                        {imageUrl ? (
                          <img
                            src={imageUrl}
                            alt={displayTitle}
                            className={styles.musicImage} // Use the same class as DB images
                            onError={(e) => {
                              e.target.onerror = null;
                              e.target.style.display = 'none';
                              const placeholderElement = e.target.parentNode.querySelector(`.${styles.musicPlaceholder}`);
                              if (placeholderElement) {
                                placeholderElement.style.display = 'flex';
                              }
                            }}
                          />
                        ) : (
                          <div className={styles.musicPlaceholder}>
                            <span>{displayArtist ? displayArtist.charAt(0).toUpperCase() : '♪'}</span>
                          </div>
                        )}
                        <div className={styles.musicOverlay}></div>
                        <button
                          className={styles.musicPlayButton}
                          onClick={(e) => handleMusicCardClick(e, music.id)}
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
                        <div className={styles.musicCardControls}>
                          <button
                            className={styles.musicCardControlButton}
                            onClick={(e) => handleEditClick(music, e)}
                            title="Edit song"
                          >
                            ✎
                          </button>
                          <button
                            className={styles.musicCardControlButton}
                            onClick={(e) => handleDeleteMusic(music.id, e)}
                            title="Delete song"
                          >
                            🗑
                          </button>
                        </div>
                      </div>
                      <div className={styles.musicInfo}>
                        <h3 className={styles.musicTitle} title={displayTitle}>{displayTitle}</h3>
                        <p className={styles.musicArtist} title={displayArtist}>{displayArtist}</p>
                        <p className={styles.musicGenre}>{displayGenre}</p>
                      </div>
                    </div>
                  );
                })}

                {/* Database Music Cards */}
                {musicList.map((music) => {
                  // Process the image URL with improved handler
                  const imageUrl = getSafeImageUrl(music.imageUrl, getImageUrl);
                  const isFavorited = isFavorite(music.musicId);
                  const isCurrentlyPlaying = currentlyPlaying === music.musicId;

                  return (
                    <div key={music.musicId}
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
                          onClick={(e) => {
                            handleMusicCardClick(e, music.musicId);
                          }}
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
                        <div className={styles.musicCardControls}>
                          <button
                            className={styles.musicCardControlButton}
                            onClick={(e) => handleEditClick(music, e)}
                            title="Edit song"
                          >
                            ✎
                          </button>
                          <button
                            className={styles.musicCardControlButton}
                            onClick={(e) => handleDeleteMusic(music.musicId, e)}
                            title="Delete song"
                          >
                            🗑
                          </button>
                        </div>
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
          )}

          {/* Hero Section */}
          {/* Your universe of sound section */}
          {/* <section className={styles.heroSection}>
            <div className={styles.purpleGlow}></div>
            <div className={styles.pinkGlow}></div>

            <div className={styles.heroContent}>
              <h1 className={styles.heroTitle}>Your Universe of Sound</h1>
              <p className={styles.heroSubtitle}>
                Explore cosmic soundscapes and stellar rhythms that transcend
                ordinary listening experiences.
              </p>
              <button className={styles.uploadButton} onClick={handleUploadClick}>
                Upload Your Music
                <span className={styles.arrowIcon}>→</span>
              </button>
            </div>
          </section> */}

          {/* Featured Music Section */}
          <section className={styles.featuredSection}>
            <h2 className={styles.sectionTitle}>Featured Songs</h2>

            {/* Featured songs from uploaded music */}
            <div className={styles.featuredGrid}>
              {getFeaturedMusic().map((music) => {
                const imageUrl = getSafeImageUrl(music.imageUrl, getImageUrl);
                const isCurrentlyPlaying = currentlyPlaying === music.musicId;

                return (
                  <div
                    key={music.musicId}
                    className={styles.playlistCard}
                    onClick={() => handleFeaturedPlayClick(null, music.musicId)}
                  >
                    <div className={styles.playlistImageContainer}>
                      {imageUrl ? (
                        <img
                          src={imageUrl}
                          alt={music.title}
                          className={styles.playlistImage}
                        />
                      ) : (
                        <div className={styles.musicPlaceholder}>
                          <span>{music.title ? music.title.charAt(0).toUpperCase() : '♪'}</span>
                        </div>
                      )}
                      <div className={styles.playlistOverlay}></div>
                      <button
                        className={`${styles.playlistPlayButton} ${isCurrentlyPlaying && isPlaying ? styles.playing : ''}`}
                        onClick={(e) => handleFeaturedPlayClick(e, music.musicId)}
                      >
                        {isCurrentlyPlaying && isPlaying ? '❚❚' : '▶'}
                      </button>
                    </div>
                    <div className={styles.playlistInfo}>
                      <h3 className={styles.playlistTitle}>{music.title}</h3>
                      <p className={styles.playlistDescription}>
                        {music.artist}
                      </p>
                      <p className={styles.playlistTracks}>{music.genre || 'Music'}</p>
                    </div>
                  </div>
                );
              })}
            </div>

            {/* Weekly discovories section*/}
            {/* <h3 className={styles.subsectionTitle}>Weekly Discoveries</h3>

            <div className={styles.discoveriesGrid}>
              {getDiscoveries().map((music) => {
                const imageUrl = getImageUrlWithFallback(music);
                const isCurrentlyPlaying = currentlyPlaying === music.musicId;

                return (
                  <div
                    key={music.musicId}
                    className={styles.trackCard}
                    onClick={() => playMusic(music.musicId)}
                  >
                    <div className={styles.trackImageContainer}>
                      {imageUrl ? (
                        <img
                          src={imageUrl}
                          alt={music.title}
                          className={styles.trackImage}
                        />
                      ) : (
                        <div className={styles.musicPlaceholder}>
                          <span>{music.title ? music.title.charAt(0).toUpperCase() : '♪'}</span>
                        </div>
                      )}
                      <div className={styles.trackOverlay}></div>
                      <button
                        className={styles.trackPlayButton}
                        onClick={(e) => handleDiscoveryPlayClick(e, music.musicId)}
                      >
                        {isCurrentlyPlaying && isPlaying ? '❚❚' : '▶'}
                      </button>
                    </div>
                    <h3 className={styles.trackTitle}>{music.title}</h3>
                    <p className={styles.trackArtist}>{music.artist}</p>
                  </div>
                );
              })}
            </div> */}
          </section>
        </main>
      </div>

      {/* Add the UploadModal component */}
      <UploadModal
        isOpen={isUploadModalOpen}
        onClose={closeUploadModal}
        onUploadComplete={handleUploadComplete}
      />

      {/* Edit Music Modal */}
      <Modal
        isOpen={showEditModal}
        onClose={handleCloseEditModal}
        title="Edit Music"
        message={
          <div className={styles.uploadModalContent}>
            <div className={styles.formField}>
              <label>Music Title</label>
              <input
                type="text"
                value={musicTitle}
                onChange={(e) => setMusicTitle(e.target.value)}
                placeholder="Enter music title"
                className={styles.textInput}
              />
            </div>
            <div className={styles.formField}>
              <label>Artist</label>
              <input
                type="text"
                value={musicArtist}
                onChange={(e) => setMusicArtist(e.target.value)}
                placeholder="Enter artist name"
                className={styles.textInput}
              />
            </div>
            <div className={styles.formField}>
              <label>Genre</label>
              <select
                value={musicGenre}
                onChange={(e) => setMusicGenre(e.target.value)}
                className={styles.textInput}
                required
              >
                <option value="">Select a genre</option>
                <option value="Rap">Rap</option>
                <option value="Pop">Pop</option>
                <option value="K-pop">K-pop</option>
                <option value="Hip Hop">Hip Hop</option>
                <option value="Rock">Rock</option>
                <option value="Indie">Indie</option>
                <option value="EDM">EDM</option>
              </select>
            </div>

            <div className={styles.formField}>
              <label>Cover Image</label>

              <div className="flex gap-5 mb-4 p-2.5 bg-black/20 rounded-lg" style={{ background: 'rgba(0, 0, 0, 0.2)' }}>
                <label
                  className="flex items-center gap-2 cursor-pointer p-2 rounded-md transition-colors text-white"
                  style={{ background: 'linear-gradient(160deg, #000000 0%, #653895 100%)' }}
                >
                  <input
                    type="radio"
                    name="imageUploadType"
                    checked={useImageUrl}
                    onChange={() => setUseImageUrl(true)}
                    className="text-purple-600"
                  />
                  Use Image URL
                </label>
                <label
                  className="flex items-center gap-2 cursor-pointer p-2 rounded-md transition-colors text-white"
                  style={{ background: 'linear-gradient(160deg, #000000 0%, #653895 100%)' }}
                  onMouseOver={(e) => e.currentTarget.style.background = 'linear-gradient(160deg, #000000 0%, #7a45b0 100%)'}
                  onMouseOut={(e) => e.currentTarget.style.background = 'linear-gradient(160deg, #000000 0%, #653895 100%)'}
                >
                  <input
                    type="radio"
                    name="imageUploadType"
                    checked={!useImageUrl}
                    onChange={() => setUseImageUrl(false)}
                    className="text-purple-600"
                  />
                  Upload Image File
                </label>
              </div>

              {useImageUrl ? (
                <div>
                  <input
                    type="text"
                    value={musicImageUrl}
                    onChange={(e) => setMusicImageUrl(e.target.value)}
                    placeholder="Enter URL to cover image"
                    className={styles.textInput}
                  />
                  <p className={styles.inputHelp}>Enter a direct URL to an image (JPG, PNG, etc.)</p>
                  {musicImageUrl && (
                    <div className={styles.imagePreview}>
                      <img
                        src={musicImageUrl}
                        alt="Cover preview"
                        onError={(e) => {
                          e.target.onerror = null;
                          e.target.src = "/placeholder.jpg";
                          e.target.style.opacity = 0.5;
                        }}
                      />
                    </div>
                  )}
                </div>
              ) : (
                <>
                  <div className={styles.fileInputWrapper}>
                    <label htmlFor="editCoverImageFile" className={styles.fileInputLabel}>
                      Choose Image File
                      <input
                        id="editCoverImageFile"
                        type="file"
                        accept="image/jpeg,image/png,image/gif"
                        onChange={handleImageFileChange}
                        className={styles.fileInput}
                      />
                    </label>
                  </div>
                  {selectedImageFile && (
                    <div className={styles.imagePreview}>
                      <img
                        src={URL.createObjectURL(selectedImageFile)}
                        alt="Cover preview"
                      />
                      <p>{selectedImageFile.name}</p>
                    </div>
                  )}
                </>
              )}
            </div>

            {uploadError && <p className={styles.errorMessage}>{uploadError}</p>}
            {isEditing && <p className={styles.uploadingMessage}>Updating...</p>}

            <button
              className={styles.uploadButton}
              onClick={handleSaveEdit}
              disabled={isEditing}
            >
              {isEditing ? 'Updating...' : 'Save Changes'}
            </button>
          </div>
        }
        showConfirmButton={false}
      />
    </div>
  );
};

export default HomePage;
