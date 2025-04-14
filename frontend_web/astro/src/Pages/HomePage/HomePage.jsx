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

  // Add refs for tracking double clicks
  const lastClickTimeRef = useRef({});
  const doubleClickThreshold = 300; // milliseconds

  // Fetch music list when component mounts
  useEffect(() => {
    fetchMusicList();
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

  // Handle play button click for featured section
  const handleFeaturedPlayClick = (e, musicId) => {
    e.stopPropagation();
    playMusic(musicId);
  };

  // Handle play button click for discoveries section
  const handleDiscoveryPlayClick = (e, musicId) => {
    e.stopPropagation();
    playMusic(musicId);
  };

  // Handle click on music card play button - properly toggle play/pause
  const handleMusicPlayClick = (e, musicId) => {
    e.stopPropagation();

    // Prevent the card click handler from also firing
    e.preventDefault();

    if (currentlyPlaying === musicId) {
      // If already playing this track, toggle play/pause
      togglePlayPause(musicId);
    } else {
      // If not playing this track, start playing it
      playMusic(musicId);
    }
  };

  // Handle click on music card - play music or pause if already playing
  const handleMusicCardClick = (e, musicId) => {
    e.stopPropagation();

    if (currentlyPlaying === musicId) {
      // If this is the current track, toggle play/pause
      togglePlayPause(musicId);
    } else {
      // If not the current track, start playing it
      playMusic(musicId);
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

    setEditingMusic(music);
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
      };

      // Add image URL if available
      if (musicImageUrl) {
        musicUpdate.imageUrl = musicImageUrl;
      } else if (editingMusic.imageUrl) {
        // Keep existing image if no new one is provided
        musicUpdate.imageUrl = editingMusic.imageUrl;
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

      // Store the updated image URL in localStorage for persistence
      if (musicImageUrl) {
        localStorage.setItem(`music-image-${editingMusic.musicId}`, musicImageUrl);
      }

      // Show success message
      alert('Music updated successfully!');

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

  return (
    <div className={styles.homePage}>
      <NavBar />
      <div className={styles.pageContent}>
        <Sidebar />
        <main className={styles.mainContent}>
          <div className={styles.headerSection}>
            <h1 className={styles.pageTitle}>Welcome, {userName}!</h1>
            <AudioUploader />
            <button onClick={handleUploadClick} className={styles.uploadButton}>
              Upload Music
            </button>
          </div>

          {/* Uploaded Music Section */}
          {musicList.length > 0 && (
            <section className={styles.uploadedMusicSection}>
              <h2 className={styles.sectionTitle}>Your Uploaded Music</h2>
              <div className={styles.musicGrid}>
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
                            handleMusicPlayClick(e, music.musicId);
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
                    onClick={() => playMusic(music.musicId)}
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

      {showUploadModal && (
        <Modal
          isOpen={true}
          onClose={handleCloseModal}
          title="Upload Music"
        >
          <AudioUploader
            onClose={handleCloseModal}
            onUploadSuccess={handleUpload}
          />
        </Modal>
      )}

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
              <label>Genre (optional)</label>
              <input
                type="text"
                value={musicGenre}
                onChange={(e) => setMusicGenre(e.target.value)}
                placeholder="Enter genre"
                className={styles.textInput}
              />
            </div>

            <div className={styles.formField}>
              <label>Cover Image</label>

              <div className={styles.uploadOptions}>
                <label className={styles.optionLabel}>
                  <input
                    type="radio"
                    name="imageUploadType"
                    checked={useImageUrl}
                    onChange={() => setUseImageUrl(true)}
                  />
                  Use Image URL
                </label>
                <label className={styles.optionLabel}>
                  <input
                    type="radio"
                    name="imageUploadType"
                    checked={!useImageUrl}
                    onChange={() => setUseImageUrl(false)}
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
