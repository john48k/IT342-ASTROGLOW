import React, { useState, useEffect, useRef } from "react";
import { Link } from "react-router-dom";
import NavBar from "../../components/NavBar/NavBar";
import { useUser } from "../../context/UserContext";
import styles from "./HomePage.module.css";
import Modal from '../../components/Modal/Modal';

export const HomePage = () => {
  const { user } = useUser();
  const userName = user?.userName || "Guest";
  const [showUploadModal, setShowUploadModal] = useState(false);
  const [enteredPassword, setEnteredPassword] = useState('');
  const [selectedFile, setSelectedFile] = useState(null);
  const [uploadError, setUploadError] = useState('');
  const [isUploading, setIsUploading] = useState(false);
  const [isPasswordVerified, setIsPasswordVerified] = useState(false);
  const [passwordError, setPasswordError] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [musicTitle, setMusicTitle] = useState('');
  const [musicArtist, setMusicArtist] = useState('');
  const [musicGenre, setMusicGenre] = useState('');
  const [musicList, setMusicList] = useState([]);
  const [currentlyPlaying, setCurrentlyPlaying] = useState(null);
  const [audioElement, setAudioElement] = useState(null);
  const [audioProgress, setAudioProgress] = useState(0);
  const progressIntervalRef = useRef(null);
  const [selectedFileInfo, setSelectedFileInfo] = useState(null);
  const [audioTime, setAudioTime] = useState({ elapsed: '0:00', total: '0:00' });

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

  const handleUploadClick = () => {
    setShowUploadModal(true);
    setIsPasswordVerified(false);
    setEnteredPassword('');
    setSelectedFile(null);
    setUploadError('');
    setPasswordError('');
    setShowPassword(false);
    setMusicTitle('');
    setMusicArtist('');
    setMusicGenre('');
  };

  const verifyPassword = async () => {
    if (!enteredPassword) {
      setPasswordError('Password cannot be empty');
      return;
    }
    
    try {
      const response = await fetch('http://localhost:8080/api/user/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          userEmail: user.userEmail,
          userPassword: enteredPassword
        }),
      });

      if (response.ok) {
        setIsPasswordVerified(true);
        setPasswordError('');
      } else {
        setPasswordError('Incorrect password. Please try again.');
      }
    } catch (error) {
      setPasswordError('Error verifying password. Please try again.');
    }
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

  const handleUpload = async () => {
    if (!selectedFile) {
      setUploadError('Please select an MP3 file');
      return;
    }
    
    if (!musicTitle || !musicArtist) {
      setUploadError('Please provide a title and artist for the music');
      return;
    }

    setIsUploading(true);
    try {
      // Use FormData to send the file and metadata
      const formData = new FormData();
      formData.append('file', selectedFile);
      formData.append('title', musicTitle);
      formData.append('artist', musicArtist);
      formData.append('genre', musicGenre || 'Unknown');

      // Log the data being sent
      console.log('Uploading file:', selectedFile.name, 'Size:', selectedFile.size);

      const response = await fetch('http://localhost:8080/api/music/upload', {
        method: 'POST',
        body: formData,
      });

      if (!response.ok) {
        const errorData = await response.text();
        throw new Error(`Upload failed: ${errorData}`);
      }
      
      // Get the response data
      const result = await response.json();
      console.log('Upload successful:', result);
      
      // Show success message
      alert('Music uploaded successfully!');
      
      // Refresh music list
      await fetchMusicList();

      // Reset form and close modal
      setEnteredPassword('');
      setSelectedFile(null);
      setSelectedFileInfo(null);
      setShowUploadModal(false);
      setUploadError('');
      setIsPasswordVerified(false);
      setMusicTitle('');
      setMusicArtist('');
      setMusicGenre('');
    } catch (error) {
      console.error('Failed to upload file:', error);
      setUploadError(`Failed to upload file: ${error.message}`);
    } finally {
      setIsUploading(false);
    }
  };

  const handleCloseModal = () => {
    setShowUploadModal(false);
    setEnteredPassword('');
    setSelectedFile(null);
    setUploadError('');
    setPasswordError('');
    setIsPasswordVerified(false);
    setMusicTitle('');
    setMusicArtist('');
    setMusicGenre('');
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
        setAudioElement({...audioElement});
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
      
      // Fetch the audio data
      const response = await fetch(`http://localhost:8080/api/music/audio/${musicId}`);
      if (!response.ok) {
        // Try to get the specific music entity to see if it exists
        const musicResponse = await fetch(`http://localhost:8080/api/music/getMusic/${musicId}`);
        if (!musicResponse.ok) {
          throw new Error(`Music with ID ${musicId} not found`);
        }
        
        const musicData = await musicResponse.json();
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
            if (prevAudio === audio) return {...audio};
            return audio;
          });
        });
        
        audio.addEventListener('pause', () => {
          setAudioElement(prevAudio => {
            if (prevAudio === audio) return {...audio};
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
      setAudioElement({...audioElement});
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

  // Sample featured collections data
  const featuredCollections = [
    {
      title: "Sweater Weather",
      description: "The Neighborhood",
      image: "sweater-weather.png",
      track: 1,
    },
    {
      title: "Kanye West",
      description: "Kanye West",
      image: "homecoming.png",
      track: 1,
    },
    {
      title: "StarBoy",
      description: "The Weeknd",
      image: "starboy.png",
      track1: 1,
    },
  ];

  // Sample weekly discoveries data
  const weeklyDiscoveries = [
    "Stellar Voyage",
    "Cosmic Harmony",
    "Galactic Pulse",
    "Lunar Echoes",
    "Solar Flares",
    "Orbital Groove",
  ];

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

  return (
    <div className={styles.homePage}>
      {/* Animated stars background */}
      <div className={styles.starsBackground}></div>

      {/* Keep the existing navbar */}
      <NavBar />

      <div className={styles.container}>
        {/* Keep the existing sidebar */}
        <aside className={styles.sidebar}>
          <ul>
            <div className={styles.libraryHeader}>
              <img
                className={styles.libraryLogo}
                src="library-music.png"
                alt=""
              />
              <p>Your Library</p>
            </div>
            <Link to="/" className="">
              Your Home
            </Link>
            <br />
            <Link to="/" className="">
              Favorites
            </Link>
          </ul>
        </aside>

        {/* Main content area */}
        <main className={styles.mainContent}>
          {/* User greeting and upload button */}
          <div className={styles.headerSection}>
            <h1 className={styles.nameTitle}>Good Day, {userName}!</h1>
            <button className={styles.uploadBtn} onClick={handleUploadClick}>
              <img
                src="upload-arrow.png"
                alt="Upload"
                className={styles.uploadIcon}
              />
              upload
            </button>
          </div>

          {/* Uploaded Music Section */}
          {musicList.length > 0 && (
            <section className={styles.uploadedMusicSection}>
              <h2 className={styles.sectionTitle}>Your Uploaded Music</h2>
              <div className={styles.musicGrid}>
                {musicList.map((music) => (
                  <div key={music.musicId} 
                    className={`${styles.musicCard} ${currentlyPlaying === music.musicId ? 
                      (audioElement && audioElement.paused ? styles.pausedCard : styles.currentlyPlayingCard) : ''}`}
                  >
                    <div className={styles.musicImageContainer}>
                      <div className={styles.musicPlaceholder}>
                        <span>{music.title.charAt(0)}</span>
                      </div>
                      <div className={styles.musicOverlay}></div>
                      <button 
                        className={styles.musicPlayButton}
                        onClick={() => togglePlayPause(music.musicId)}
                      >
                        {currentlyPlaying === music.musicId && audioElement && !audioElement.paused ? '❚❚' : '▶'}
                      </button>
                    </div>
                    <div className={styles.musicInfo}>
                      <h3 className={styles.musicTitle}>{music.title}</h3>
                      <p className={styles.musicArtist}>{music.artist}</p>
                      {music.genre && <p className={styles.musicGenre}>{music.genre}</p>}
                    </div>
                  </div>
                ))}
              </div>
            </section>
          )}

          {/* Hero Section */}
          <section className={styles.heroSection}>
            {/* Decorative elements */}
            <div className={styles.purpleGlow}></div>
            <div className={styles.pinkGlow}></div>

            <div className={styles.heroContent}>
              <h1 className={styles.heroTitle}>Your Universe of Sound</h1>
              <p className={styles.heroSubtitle}>
                Explore cosmic soundscapes and stellar rhythms that transcend
                ordinary listening experiences.
              </p>
              <button className={styles.ctaButton}>
                Download Mobile
                <span className={styles.arrowIcon}>→</span>
              </button>
            </div>
          </section>

          {/* Featured Music Section */}
          <section className={styles.featuredSection}>
            <h2 className={styles.sectionTitle}>Featured Songs</h2>

            {/* Featured playlists */}
            <div className={styles.featuredGrid}>
              {featuredCollections.map((playlist, index) => (
                <div key={index} className={styles.playlistCard}>
                  <div className={styles.playlistImageContainer}>
                    <img
                      src={playlist.image || "/placeholder.svg"}
                      alt={playlist.title}
                      className={styles.playlistImage}
                    />
                    <div className={styles.playlistOverlay}></div>
                    <button className={styles.playlistPlayButton}>▶</button>
                  </div>
                  <div className={styles.playlistInfo}>
                    <h3 className={styles.playlistTitle}>{playlist.title}</h3>
                    <p className={styles.playlistDescription}>
                      {playlist.description}
                    </p>
                    <p className={styles.playlistTracks}>{playlist.tracks}</p>
                  </div>
                </div>
              ))}
            </div>

            {/* Weekly Discoveries */}
            <h3 className={styles.subsectionTitle}>Weekly Discoveries</h3>

            <div className={styles.discoveriesGrid}>
              {weeklyDiscoveries.map((track, index) => (
                <div key={index} className={styles.trackCard}>
                  <div className={styles.trackImageContainer}>
                    <img
                      src="placeholder.jpg"
                      alt={track}
                      className={styles.trackImage}
                    />
                    <div className={styles.trackOverlay}></div>
                    <button className={styles.trackPlayButton}>▶</button>
                  </div>
                  <h3 className={styles.trackTitle}>{track}</h3>
                  <p className={styles.trackArtist}>AstroGlow Originals</p>
                </div>
              ))}
            </div>
          </section>
        </main>
      </div>

      {/* Now Playing Bar */}
      {currentlyPlaying && (
      <div className={styles.nowPlayingBar}>
        <div className={styles.nowPlayingContent}>
          <div className={styles.nowPlayingInfo}>
              <div className={styles.nowPlayingImage}>
                {musicList.find(m => m.musicId === currentlyPlaying)?.title.charAt(0)}
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

      <Modal
        isOpen={showUploadModal}
        onClose={handleCloseModal}
        onConfirm={isPasswordVerified ? handleUpload : verifyPassword}
        title={isPasswordVerified ? "Upload Music" : "Verify Password"}
        message={
          <div className={styles.uploadModalContent}>
            {!isPasswordVerified ? (
              <>
                <div className={styles.passwordInputContainer}>
                  <input
                    type={showPassword ? "text" : "password"}
                    placeholder="Enter your account password"
                    value={enteredPassword}
                    onChange={(e) => setEnteredPassword(e.target.value)}
                    className={styles.passwordInput}
                    onKeyDown={(e) => {
                      if (e.key === 'Enter') {
                        verifyPassword();
                      }
                    }}
                  />
                  <button
                    type="button"
                    className={styles.togglePasswordButton}
                    onClick={() => setShowPassword(!showPassword)}
                    aria-label={showPassword ? "Hide password" : "Show password"}
                  >
                    {showPassword ? (
                      <svg
                        xmlns="http://www.w3.org/2000/svg"
                        width="20"
                        height="20"
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="currentColor"
                        strokeWidth="2"
                        strokeLinecap="round"
                        strokeLinejoin="round"
                      >
                        <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
                        <line x1="1" y1="1" x2="23" y2="23"></line>
                      </svg>
                    ) : (
                      <svg
                        xmlns="http://www.w3.org/2000/svg"
                        width="20"
                        height="20"
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="currentColor"
                        strokeWidth="2"
                        strokeLinecap="round"
                        strokeLinejoin="round"
                      >
                        <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                        <circle cx="12" cy="12" r="3"></circle>
                      </svg>
                    )}
                  </button>
                </div>
                {passwordError && <p className={styles.errorMessage}>{passwordError}</p>}
              </>
            ) : (
              <>
                <p className={styles.verifiedMessage}>Password verified successfully!</p>
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
                <input
                  type="file"
                  accept="audio/mpeg"
                  onChange={handleFileChange}
                  className={styles.fileInput}
                />
                {selectedFile && (
                  <p className={styles.selectedFile}>Selected: {selectedFile.name}</p>
                )}
                {selectedFileInfo && (
                  <p className={styles.selectedFileInfo}>Size: {selectedFileInfo.size}</p>
                )}
                {uploadError && <p className={styles.errorMessage}>{uploadError}</p>}
                {isUploading && <p className={styles.uploadingMessage}>Uploading...</p>}
              </>
            )}
          </div>
        }
      />
    </div>
  );
};

export default HomePage;
