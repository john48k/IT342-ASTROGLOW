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
import { registerHomePageNavHandlers, unregisterHomePageNavHandlers } from "../../components/NowPlayingBar/NowPlayingBar";

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
  return new Promise((resolve, reject) => {
    // Create a FileReader to read the file
    const reader = new FileReader();

    // Set up the FileReader onload event
    reader.onload = (readerEvent) => {
      // Create an image object
      const img = new Image();

      // Set up the image onload event
      img.onload = () => {
        // Check if resizing is needed
        let width = img.width;
        let height = img.height;

        // Calculate new dimensions while maintaining aspect ratio
        if (width > maxWidth) {
          height = (height * maxWidth) / width;
          width = maxWidth;
        }

        if (height > maxHeight) {
          width = (width * maxHeight) / height;
          height = maxHeight;
        }

        // Create a canvas element
        const canvas = document.createElement('canvas');
        canvas.width = width;
        canvas.height = height;

        // Draw the image on the canvas
        const ctx = canvas.getContext('2d');
        ctx.drawImage(img, 0, 0, width, height);

        // Get the resized image as a data URL
        const dataUrl = canvas.toDataURL(file.type, quality);

        // Resolve the promise with the resized image data URL
        resolve(dataUrl);
      };

      // Handle image load error
      img.onerror = (error) => {
        reject(new Error('Error loading image.'));
      };

      // Set the image source to the file data
      img.src = readerEvent.target.result;
    };

    // Handle FileReader error
    reader.onerror = (error) => {
      reject(new Error('Error reading file.'));
    };

    // Read the file as a data URL
    reader.readAsDataURL(file);
  });
};

export const HomePage = () => {
  const { user, isAuthenticated } = useUser();
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
    getImageUrl,
    musicCategory,
    setMusicCategory
  } = useAudioPlayer();

  const userName = user?.userName || "Guest";
  const [musicList, setMusicList] = useState([]);
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
  const [selectedFileInfo, setSelectedFileInfo] = useState(null);
  const [isImageLoading, setIsImageLoading] = useState(true);
  const [showEditModal, setShowEditModal] = useState(false);
  const [editingMusic, setEditingMusic] = useState(null);
  const [isEditing, setIsEditing] = useState(false);
  const [isUploadModalOpen, setIsUploadModalOpen] = useState(false);
  const [availableMusicList, setAvailableMusicList] = useState([]);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [musicToDelete, setMusicToDelete] = useState(null);

  // Add refs for tracking double clicks and click prevention
  const lastClickTimeRef = useRef({});
  const doubleClickThreshold = 300; // milliseconds
  const isProcessingClickRef = useRef(false); // Track if we're currently processing a click
  const lockoutTimerRef = useRef(null); // For click lockout

  // Albums state for playback functionality
  const [albums, setAlbums] = useState([]);

  // Register next/previous handlers and add key listeners
  useEffect(() => {
    // Register our handlers with the NowPlayingBar
    registerHomePageNavHandlers(handleNextSong, handlePreviousSong);

    // Add keyboard navigation
    const handleKeyDown = (e) => {
      // Handle media key or keyboard shortcuts for next/previous
      if (e.key === 'MediaTrackNext' || (e.ctrlKey && e.key === 'ArrowRight')) {
        handleNextSong();
      } else if (e.key === 'MediaTrackPrevious' || (e.ctrlKey && e.key === 'ArrowLeft')) {
        handlePreviousSong();
      }
    };

    window.addEventListener('keydown', handleKeyDown);

    // Clean up on unmount
    return () => {
      unregisterHomePageNavHandlers();
      window.removeEventListener('keydown', handleKeyDown);
    };
  }, [musicList, availableMusicList, currentlyPlaying, musicCategory]);

  // Fetch music lists when component mounts
  useEffect(() => {
    fetchUserMusic();
    fetchAvailableMusic();
  }, [user]);

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

  // Function to fetch user's music from database
  const fetchUserMusic = async () => {
    try {
      if (!user || !user.userId) {
        console.log('No user logged in');
        return;
      }

      const response = await fetch(`https://astroglowfirebase-d2411.uc.r.appspot.com/api/music/user/${user.userId}`, {
        method: 'GET',
        credentials: 'include'
      });

      if (!response.ok) {
        throw new Error('Failed to fetch user music');
      }

      const data = await response.json();
      setMusicList(data);
    } catch (error) {
      console.error('Error fetching user music:', error);
    }
  };

  // Function to fetch available music from MySQL
  const fetchAvailableMusic = async () => {
    try {
      console.log('Fetching music from MySQL database...');

      // Fetch music from MySQL database
      const dbResponse = await fetch('https://astroglowfirebase-d2411.uc.r.appspot.com/api/music/getAllMusic');
      if (!dbResponse.ok) {
        throw new Error('Failed to fetch music from database');
      }
      const dbMusic = await dbResponse.json();
      console.log(`Found ${dbMusic.length} music entries in database`);

      // Transform database music to match our format
      const transformedDbMusic = dbMusic.map(music => ({
        id: music.musicId,
        title: music.title,
        artist: music.artist,
        genre: music.genre,
        audioUrl: music.audioUrl,
        imageUrl: music.imageUrl,
        userName: music.owner?.userName || 'Unknown User'
      }));

      // Filter out the user's own music
      const otherUsersMusic = transformedDbMusic.filter(music =>
        !music.userName.toLowerCase().includes(user?.userName?.toLowerCase() || '')
      );

      console.log(`Total available music: ${otherUsersMusic.length} from database`);

      // Set the available music list in state
      setAvailableMusicList(otherUsersMusic);

      // Save to localStorage with display index to preserve UI order
      const musicFilesWithIndex = otherUsersMusic.map((item, index) => ({
        ...item,
        displayIndex: index,
        category: 'available'
      }));

      console.log('Saving music list to localStorage with display indices',
        `(${musicFilesWithIndex.length} items)`);

      // Log the first few items to verify correct order
      if (musicFilesWithIndex.length > 0) {
        console.log('First few items in order:');
        musicFilesWithIndex.slice(0, 3).forEach((item, idx) => {
          console.log(`${idx}: ${item.title} by ${item.artist} (ID: ${item.id})`);
        });
      }

      // Save to localStorage for the NowPlayingBar to use
      localStorage.setItem('firebase-music-list', JSON.stringify(musicFilesWithIndex));

      return otherUsersMusic;
    } catch (error) {
      console.error('Error fetching music:', error);
      return [];
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
      // Create form data for the upload
      const formData = new FormData();
      formData.append('title', musicTitle);
      formData.append('artist', musicArtist);
      formData.append('genre', musicGenre || 'Unknown');
      if (selectedFile) {
        formData.append('audioFile', selectedFile);
      }
      if (selectedImageFile) {
        formData.append('imageFile', selectedImageFile);
      }
      if (musicUrl) {
        formData.append('audioUrl', musicUrl);
      }
      if (musicImageUrl) {
        formData.append('imageUrl', musicImageUrl);
      }
      formData.append('userId', user.userId); // Add user ID to the upload

      // Upload to database
      const response = await fetch('https://astroglowfirebase-d2411.uc.r.appspot.com/api/music/upload', {
        method: 'POST',
        body: formData,
        credentials: 'include'
      });

      if (!response.ok) {
        throw new Error('Failed to upload music to database');
      }

      // Success log
      console.log('Music uploaded successfully!');

      // Refresh the user's music list
      fetchUserMusic();

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

  // Handle music card click
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
          // Determine if this is in uploaded music or available music list
          const uploadedItem = musicList.find(item => {
            const itemId = String(item.musicId || item.id);
            const searchId = String(musicId);
            return itemId === searchId;
          });

          const availableItem = availableMusicList.find(item => {
            const itemId = String(item.musicId || item.id);
            const searchId = String(musicId);
            return itemId === searchId;
          });

          // Set the music category based on which list contains the item
          if (uploadedItem) {
            console.log('Playing from uploaded music:', uploadedItem.title);
            setMusicCategory('uploaded');
            playMusic(String(musicId), uploadedItem.audioUrl, 'uploaded');
          } else if (availableItem) {
            console.log('Playing from available music:', availableItem.title);
            setMusicCategory('available');
            playMusic(String(musicId), availableItem.audioUrl, 'available');
          } else {
            console.log('Music item not found, ID:', musicId);
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

  // Handle delete music click
  const handleDeleteClick = (musicId, event) => {
    event.stopPropagation(); // Prevent triggering card click

    // Find the music item to display in the confirmation modal
    const musicItem = musicList.find(music => music.musicId === musicId);
    if (musicItem) {
      setMusicToDelete({
        id: musicId,
        title: musicItem.title,
        artist: musicItem.artist
      });
      setShowDeleteModal(true);
    }
  };

  // Handle confirming deletion in the modal
  const confirmDelete = async () => {
    if (!musicToDelete) return;

    const musicId = musicToDelete.id;

    try {
      // First, get the music details to check if it's stored in Firebase
      const musicItem = musicList.find(music => music.musicId === musicId);

      if (musicItem && musicItem.audioUrl && musicItem.audioUrl.includes('firebasestorage.googleapis.com')) {
        // Extract the filename from the Firebase URL
        const urlParts = musicItem.audioUrl.split('/');
        const fileName = decodeURIComponent(urlParts[urlParts.length - 1].split('?')[0]);

        // Delete from Firebase storage
        const { storage } = await import('../../firebase');
        const { ref, deleteObject } = await import('firebase/storage');

        // Create a reference to the file in Firebase storage
        const storageRef = ref(storage, fileName);

        try {
          await deleteObject(storageRef);
          console.log('File deleted from Firebase storage');

          // Immediately update the available music list
          setAvailableMusicList(prevList => {
            const newList = prevList.filter(music => {
              // Check both the ID and the audio URL to ensure we remove the correct item
              return music.id !== musicId &&
                (!music.audioUrl || !music.audioUrl.includes(fileName));
            });
            console.log('Updated available music list:', newList);
            return newList;
          });
        } catch (firebaseError) {
          console.error('Error deleting from Firebase:', firebaseError);
          // Continue with database deletion even if Firebase deletion fails
        }
      }

      // Delete from database
      const response = await fetch(`https://astroglowfirebase-d2411.uc.r.appspot.com/api/music/deleteMusic/${musicId}`, {
        method: 'DELETE',
        credentials: 'include'
      });

      if (!response.ok) {
        throw new Error('Failed to delete music from database');
      }

      // Immediately update both lists
      setMusicList(prevList => {
        const newList = prevList.filter(music => music.musicId !== musicId);
        console.log('Updated music list:', newList);
        return newList;
      });

      setAvailableMusicList(prevList => {
        const newList = prevList.filter(music => music.id !== musicId);
        console.log('Updated available music list:', newList);
        return newList;
      });

      // If the deleted music was playing, stop playback
      if (currentlyPlaying === musicId) {
        stopPlayback();
      }

      // Success log
      console.log('Music deleted successfully!');
    } catch (error) {
      console.error('Error deleting music:', error);
    } finally {
      // Close delete modal
      setShowDeleteModal(false);
      setMusicToDelete(null);
    }
  };

  // Handle cancelling deletion
  const cancelDelete = () => {
    setShowDeleteModal(false);
    setMusicToDelete(null);
  };

  const handleEditClick = (music, event) => {
    event.stopPropagation(); // Prevent triggering card click

    // Store the ID type to distinguish between Firebase and database items
    const isFirebaseItem = music.musicId && !music.musicId;

    setEditingMusic({
      ...music,
      // Store both ID types for proper handling
      musicId: music.musicId || null,
      id: music.musicId || null,
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
      // Create a music object with the updated information
      const musicUpdate = {
        musicId: editingMusic.musicId,
        title: musicTitle,
        artist: musicArtist,
        genre: musicGenre || 'Unknown',
        userId: user.userId // Add user ID to the update
      };

      // Add image URL if available
      if (musicImageUrl) {
        musicUpdate.imageUrl = musicImageUrl;
      } else if (editingMusic.imageUrl) {
        // Keep existing image if no new one is provided
        musicUpdate.imageUrl = editingMusic.imageUrl;
      }

      const response = await fetch(`https://astroglowfirebase-d2411.uc.r.appspot.com/api/music/putMusic/${editingMusic.musicId}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(musicUpdate),
        credentials: 'include'
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

  // Handle upload complete
  const handleUploadComplete = async (uploadData) => {
    console.log('HomePage: Upload Complete Data:', uploadData);

    try {
      // Create form data for the upload
      const formData = new FormData();
      formData.append('title', uploadData.title);
      formData.append('artist', uploadData.artist);
      formData.append('genre', uploadData.genre || 'Unknown');
      formData.append('audioUrl', uploadData.audioUrl);
      if (uploadData.imageUrl) {
        formData.append('imageUrl', uploadData.imageUrl);
      }
      formData.append('userId', user.userId); // Add user ID to the upload
      formData.append('firebaseFileName', uploadData.audioFileName); // Add Firebase filename

      // Upload to database
      const response = await fetch('https://astroglowfirebase-d2411.uc.r.appspot.com/api/music/upload', {
        method: 'POST',
        body: formData,
        credentials: 'include'
      });

      if (!response.ok) {
        throw new Error('Failed to upload music to database');
      }

      // Get the response data which should include the new music ID
      const newMusicData = await response.json();
      console.log('New music data:', newMusicData);

      // Create a new music item for the available music list
      const newMusicItem = {
        id: newMusicData.musicId,
        title: uploadData.title,
        artist: uploadData.artist,
        genre: uploadData.genre || 'Unknown',
        audioUrl: uploadData.audioUrl,
        imageUrl: uploadData.imageUrl,
        userName: user.userName || 'Unknown User'
      };

      // Update the available music list immediately
      setAvailableMusicList(prevList => [...prevList, newMusicItem]);

      // Show success message
      console.log('Music uploaded successfully!');

      // Refresh the user's music list
      fetchUserMusic();

    } catch (error) {
      console.error('Failed to upload music:', error);
    }
  };

  // Handle play button click for featured section
  const handleFeaturedPlayClick = (e, musicId) => {
    e.stopPropagation();
    handleMusicCardClick(e, musicId);
  };

  // Handle play button click for discoveries section 
  const handleDiscoveryPlayClick = (e, musicId) => {
    e.stopPropagation();
    handleMusicCardClick(e, musicId);
  };

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

  // Handler for next song button - ensures it stays within the current section
  const handleNextSong = () => {
    console.log("Next song requested in HomePage");
    console.log("Current music category:", musicCategory);

    // Set processing flag to prevent rapid clicks
    if (isProcessingClickRef.current) {
      console.log("Still processing previous action, ignoring request");
      return;
    }

    // Lock playback to prevent multiple simultaneous actions
    lockPlayback(1000);

    // First stop the current playback to prevent multiple tracks playing
    stopPlayback().then(() => {
      // Determine which list to use based on the current category
      if (musicCategory === 'uploaded') {
        // Use uploaded music list for navigation
        console.log("Using uploaded music list for next song");

        if (musicList.length > 0) {
          // Find current track index
          const currentIndex = musicList.findIndex(item => {
            const itemId = String(item.musicId || item.id || '');
            return itemId === String(currentlyPlaying);
          });

          console.log(`Current index in uploaded list: ${currentIndex} of ${musicList.length}`);

          // If found in list and not the last track
          if (currentIndex !== -1 && currentIndex < musicList.length - 1) {
            const nextTrack = musicList[currentIndex + 1];
            console.log(`Playing next track: ${nextTrack.title || 'Unknown'}`);

            // Play the track directly to avoid playNextSong complexity
            playMusic(String(nextTrack.musicId), nextTrack.audioUrl, 'uploaded');
          }
          // If last track, loop to first
          else if (currentIndex === musicList.length - 1) {
            const firstTrack = musicList[0];
            console.log(`Looping to first track: ${firstTrack.title || 'Unknown'}`);

            playMusic(String(firstTrack.musicId), firstTrack.audioUrl, 'uploaded');
          }
          // If not found, start with the first track
          else {
            const firstTrack = musicList[0];
            console.log(`Track not found in list, playing first: ${firstTrack.title || 'Unknown'}`);

            playMusic(String(firstTrack.musicId), firstTrack.audioUrl, 'uploaded');
          }
        } else {
          console.log("Uploaded music list is empty");
          playNextSong(musicList, albums);
        }
      } else if (musicCategory === 'available') {
        // Use available music list for navigation
        console.log("Using available music list for next song");

        if (availableMusicList.length > 0) {
          // Find current track index
          const currentIndex = availableMusicList.findIndex(item => {
            const itemId = String(item.musicId || item.id || '');
            return itemId === String(currentlyPlaying);
          });

          console.log(`Current index in available list: ${currentIndex} of ${availableMusicList.length}`);

          // If found in list and not the last track
          if (currentIndex !== -1 && currentIndex < availableMusicList.length - 1) {
            const nextTrack = availableMusicList[currentIndex + 1];
            console.log(`Playing next track: ${nextTrack.title || 'Unknown'}`);

            // Play the track directly to avoid playNextSong complexity
            playMusic(String(nextTrack.id), nextTrack.audioUrl, 'available');
          }
          // If last track, loop to first
          else if (currentIndex === availableMusicList.length - 1) {
            const firstTrack = availableMusicList[0];
            console.log(`Looping to first track: ${firstTrack.title || 'Unknown'}`);

            playMusic(String(firstTrack.id), firstTrack.audioUrl, 'available');
          }
          // If not found, start with the first track
          else {
            const firstTrack = availableMusicList[0];
            console.log(`Track not found in list, playing first: ${firstTrack.title || 'Unknown'}`);

            playMusic(String(firstTrack.id), firstTrack.audioUrl, 'available');
          }
        } else {
          console.log("Available music list is empty");
          playNextSong(availableMusicList, albums);
        }
      } else {
        // If category is not set, try to determine it from the current track
        const isInUploadedList = musicList.some(item => {
          const itemId = String(item.musicId || item.id || '');
          return itemId === String(currentlyPlaying);
        });

        const isInAvailableList = availableMusicList.some(item => {
          const itemId = String(item.musicId || item.id || '');
          return itemId === String(currentlyPlaying);
        });

        if (isInUploadedList) {
          console.log("Current track found in uploaded list, using uploaded for next");
          setMusicCategory('uploaded');
          playNextSong(musicList, albums);
        } else if (isInAvailableList) {
          console.log("Current track found in available list, using available for next");
          setMusicCategory('available');
          playNextSong(availableMusicList, albums);
        } else {
          // Fallback to combined list if we can't determine where the current track is
          console.log("Using combined list for next (fallback)");
          const combinedList = [...musicList, ...availableMusicList];
          playNextSong(combinedList, albums);
        }
      }
    }).catch(err => {
      console.error("Error stopping playback before next song:", err);
      // Reset processing flag in case of error
      isProcessingClickRef.current = false;
    });
  };

  // Handler for previous song button - ensures it stays within the current section
  const handlePreviousSong = () => {
    console.log("Previous song requested in HomePage");
    console.log("Current music category:", musicCategory);

    // Set processing flag to prevent rapid clicks
    if (isProcessingClickRef.current) {
      console.log("Still processing previous action, ignoring request");
      return;
    }

    // Lock playback to prevent multiple simultaneous actions
    lockPlayback(1000);

    // First stop the current playback to prevent multiple tracks playing
    stopPlayback().then(() => {
      // Determine which list to use based on the current category
      if (musicCategory === 'uploaded') {
        // Use uploaded music list for navigation
        console.log("Using uploaded music list for previous song");

        if (musicList.length > 0) {
          // Find current track index
          const currentIndex = musicList.findIndex(item => {
            const itemId = String(item.musicId || item.id || '');
            return itemId === String(currentlyPlaying);
          });

          console.log(`Current index in uploaded list: ${currentIndex} of ${musicList.length}`);

          // If found in list and not the first track
          if (currentIndex > 0) {
            const prevTrack = musicList[currentIndex - 1];
            console.log(`Playing previous track: ${prevTrack.title || 'Unknown'}`);

            // Play the track directly to avoid playPreviousSong complexity
            playMusic(String(prevTrack.musicId), prevTrack.audioUrl, 'uploaded');
          }
          // If first track, loop to last
          else if (currentIndex === 0) {
            const lastTrack = musicList[musicList.length - 1];
            console.log(`Looping to last track: ${lastTrack.title || 'Unknown'}`);

            playMusic(String(lastTrack.musicId), lastTrack.audioUrl, 'uploaded');
          }
          // If not found, start with the last track
          else {
            const lastTrack = musicList[musicList.length - 1];
            console.log(`Track not found in list, playing last: ${lastTrack.title || 'Unknown'}`);

            playMusic(String(lastTrack.musicId), lastTrack.audioUrl, 'uploaded');
          }
        } else {
          console.log("Uploaded music list is empty");
          playPreviousSong(musicList, albums);
        }
      } else if (musicCategory === 'available') {
        // Use available music list for navigation
        console.log("Using available music list for previous song");

        if (availableMusicList.length > 0) {
          // Find current track index
          const currentIndex = availableMusicList.findIndex(item => {
            const itemId = String(item.musicId || item.id || '');
            return itemId === String(currentlyPlaying);
          });

          console.log(`Current index in available list: ${currentIndex} of ${availableMusicList.length}`);

          // If found in list and not the first track
          if (currentIndex > 0) {
            const prevTrack = availableMusicList[currentIndex - 1];
            console.log(`Playing previous track: ${prevTrack.title || 'Unknown'}`);

            // Play the track directly to avoid playPreviousSong complexity
            playMusic(String(prevTrack.id), prevTrack.audioUrl, 'available');
          }
          // If first track, loop to last
          else if (currentIndex === 0) {
            const lastTrack = availableMusicList[availableMusicList.length - 1];
            console.log(`Looping to last track: ${lastTrack.title || 'Unknown'}`);

            playMusic(String(lastTrack.id), lastTrack.audioUrl, 'available');
          }
          // If not found, start with the last track
          else {
            const lastTrack = availableMusicList[availableMusicList.length - 1];
            console.log(`Track not found in list, playing last: ${lastTrack.title || 'Unknown'}`);

            playMusic(String(lastTrack.id), lastTrack.audioUrl, 'available');
          }
        } else {
          console.log("Available music list is empty");
          playPreviousSong(availableMusicList, albums);
        }
      } else {
        // If category is not set, try to determine it from the current track
        const isInUploadedList = musicList.some(item => {
          const itemId = String(item.musicId || item.id || '');
          return itemId === String(currentlyPlaying);
        });

        const isInAvailableList = availableMusicList.some(item => {
          const itemId = String(item.musicId || item.id || '');
          return itemId === String(currentlyPlaying);
        });

        if (isInUploadedList) {
          console.log("Current track found in uploaded list, using uploaded for previous");
          setMusicCategory('uploaded');
          playPreviousSong(musicList, albums);
        } else if (isInAvailableList) {
          console.log("Current track found in available list, using available for previous");
          setMusicCategory('available');
          playPreviousSong(availableMusicList, albums);
        } else {
          // Fallback to combined list if we can't determine where the current track is
          console.log("Using combined list for previous (fallback)");
          const combinedList = [...musicList, ...availableMusicList];
          playPreviousSong(combinedList, albums);
        }
      }
    }).catch(err => {
      console.error("Error stopping playback before previous song:", err);
      // Reset processing flag in case of error
      isProcessingClickRef.current = false;
    });
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
          {musicList.length > 0 && (
            <section className={styles.uploadedMusicSection}>
              <h2 className={styles.sectionTitle}>Your Uploaded Music</h2>
              <div className={styles.musicGrid}>
                {musicList.map((music) => {
                  const isCurrentlyPlaying = currentlyPlaying === music.musicId;
                  const imageUrl = getSafeImageUrl(music.imageUrl, getImageUrl);

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
                          onClick={(e) => handleMusicCardClick(e, music.musicId)}
                        >
                          {isCurrentlyPlaying && isPlaying ? '❚❚' : '▶'}
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
                            onClick={(e) => handleDeleteClick(music.musicId, e)}
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

          {/* Available Music Section */}
          <section className={styles.availableMusicSection}>
            <h2 className={styles.sectionTitle}>Available Music</h2>
            <div className={styles.musicGrid}>
              {availableMusicList.map((music) => {
                const isCurrentlyPlaying = currentlyPlaying === music.id;
                const isFavorited = isFavorite(music.id);
                const imageUrl = getSafeImageUrl(music.imageUrl, getImageUrl);

                return (
                  <div key={music.id}
                    className={`${styles.musicCard} ${isCurrentlyPlaying ?
                      (!isPlaying ? styles.pausedCard : styles.currentlyPlayingCard) : ''}`}
                    onClick={(e) => handleMusicCardClick(e, music.id)}
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
                    </div>
                    <div className={styles.musicInfo}>
                      <h3 className={styles.musicTitle}>{music.title}</h3>
                      <p className={styles.musicArtist}>{music.artist}</p>
                      {music.genre && <p className={styles.musicGenre}>{music.genre}</p>}
                      {/* <p className={styles.uploadedBy}>Uploaded by: {music.userName || 'Unknown User'}</p> */}
                    </div>
                  </div>
                );
              })}
            </div>
          </section>

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

      {/* Delete Confirmation Modal */}
      <Modal
        isOpen={showDeleteModal}
        onClose={cancelDelete}
        title="Confirm Delete"
        message={
          musicToDelete ?
            <div>
              <p>Are you sure you want to delete this song?</p>
              <p className={styles.songDeleteInfo}>
                <strong>{musicToDelete.title}</strong> by {musicToDelete.artist}
              </p>
              <p>This action cannot be undone.</p>
            </div> :
            <p>Are you sure you want to delete this song?</p>
        }
        confirmText="Delete"
        onConfirm={confirmDelete}
      />
    </div>
  );
};

export default HomePage;
