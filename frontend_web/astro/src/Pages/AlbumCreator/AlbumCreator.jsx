import React, { useState, useEffect, useRef } from "react";
import { Link, useNavigate } from "react-router-dom";
import NavBar from "../../components/NavBar/NavBar";
import { useUser } from "../../context/UserContext";
import { useFavorites } from "../../context/FavoritesContext";
import { useAudioPlayer } from "../../context/AudioPlayerContext";
import styles from "./AlbumCreator.module.css";

export const AlbumCreator = () => {
  const navigate = useNavigate();
  const { user } = useUser();
  const { favorites } = useFavorites();
  const { 
    playMusic, 
    togglePlayPause, 
    currentlyPlaying, 
    isPlaying, 
    handleDoubleClick,
    getImageUrl 
  } = useAudioPlayer();
  const userName = user?.userName || "Guest";
  const [musicList, setMusicList] = useState([]);
  const [selectedSongs, setSelectedSongs] = useState([]);
  const [albumName, setAlbumName] = useState("");
  const [albumDescription, setAlbumDescription] = useState("");
  const [filterArtist, setFilterArtist] = useState("");
  const [filterGenre, setFilterGenre] = useState("");
  const [uniqueArtists, setUniqueArtists] = useState([]);
  const [uniqueGenres, setUniqueGenres] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [isCreating, setIsCreating] = useState(false);
  const [albumCreated, setAlbumCreated] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  // Add refs for tracking double clicks
  const lastClickTimeRef = useRef({});
  const doubleClickThreshold = 300; // milliseconds

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
        
        // Extract unique artists and genres
        const artists = new Set();
        const genres = new Set();
        
        data.forEach(music => {
          if (music.artist) artists.add(music.artist);
          if (music.genre) genres.add(music.genre);
        });
        
        setUniqueArtists(Array.from(artists));
        setUniqueGenres(Array.from(genres));
      }
    } catch (error) {
      console.error('Error fetching music list:', error);
    }
  };

  const handleSongClick = (musicId) => {
    setSelectedSongs(prevSelected => {
      if (prevSelected.includes(musicId)) {
        // Remove from selection
        return prevSelected.filter(id => id !== musicId);
      } else {
        // Add to selection
        return [...prevSelected, musicId];
      }
    });
  };

  const handleCreateAlbum = async () => {
    if (!albumName) {
      setErrorMessage("Please enter an album name");
      return;
    }
    
    if (selectedSongs.length === 0) {
      setErrorMessage("Please select at least one song");
      return;
    }
    
    setIsCreating(true);
    setErrorMessage("");
    
    try {
      // Create album data object
      const albumData = {
        name: albumName,
        description: albumDescription,
        songs: selectedSongs.map(id => {
          const song = musicList.find(s => s.musicId === id);
          return {
            id: song.musicId,
            title: song.title,
            artist: song.artist,
            genre: song.genre,
            imageUrl: song.imageUrl, // Include the image URL
            audioUrl: song.audioUrl // Include the audio URL
          };
        }),
        createdBy: user?.userId || "guest",
        createdAt: new Date().toISOString()
      };
      
      // Save to localStorage
      const existingAlbums = localStorage.getItem('albums');
      let allAlbums = [];
      
      if (existingAlbums) {
        allAlbums = JSON.parse(existingAlbums);
      }
      
      allAlbums.push(albumData);
      localStorage.setItem('albums', JSON.stringify(allAlbums));
      
      console.log("Album created and saved:", albumData);
      
      // Reset form and show success message
      setAlbumCreated(true);
      setSelectedSongs([]);
      setAlbumName("");
      setAlbumDescription("");
      setFilterArtist("");
      setFilterGenre("");
      setSearchTerm("");
      
      // Navigate to home page after 1.5 seconds to see the new album
      setTimeout(() => {
        navigate('/home');
      }, 1500);
    } catch (error) {
      console.error("Error creating album:", error);
      setErrorMessage("Failed to create album. Please try again.");
    } finally {
      setIsCreating(false);
    }
  };

  // Handle click on music card play button with double-click detection
  const handleMusicPlayClick = (e, musicId) => {
    e.stopPropagation();
    
    const now = Date.now();
    const lastClickTime = lastClickTimeRef.current[musicId] || 0;
    
    // Check if this is a double click
    if (now - lastClickTime < doubleClickThreshold) {
      // It's a double click, try to go to the next song
      handleDoubleClick('next', musicList, []);
      // Reset the timestamp
      lastClickTimeRef.current[musicId] = 0;
    } else {
      // It's a single click, toggle play/pause
      if (currentlyPlaying === musicId) {
        togglePlayPause(musicId);
      } else {
        playMusic(musicId);
      }
      // Save the timestamp
      lastClickTimeRef.current[musicId] = now;
    }
  };
  
  // Handle double-click on music card to go to previous song
  const handleMusicCardDoubleClick = (e, musicId) => {
    e.stopPropagation();
    
    // If current song is playing, try to go to previous song on double-click
    if (currentlyPlaying === musicId) {
      handleDoubleClick('previous', musicList, []);
    } else {
      // If not the current song, just play it
      playMusic(musicId);
    }
  };

  return (
    <div className={styles.body}>
      <div className={styles.container}>
        {/* Sidebar */}
        <aside className={styles.sidebar}>
          <div className={styles.sidebarHeader}>
            <h2>AstroGlow</h2>
          </div>
          <nav className={styles.sidebarNav}>
            <ul className={styles.sidebarMenu}>
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
              <li>
                <Link to="/album-creator" className={`${styles.sidebarLink} ${styles.active}`}>
                  Create Album
                </Link>
              </li>
            </ul>
          </nav>
        </aside>

        {/* Main content area */}
        <main className={styles.mainContent}>
          <div className={styles.headerSection}>
            <h1 className={styles.pageTitle}>Create Your Album</h1>
            <div className={styles.userWelcome}>
              Welcome, {userName}
            </div>
          </div>

          {/* Album creation form */}
          <section className={styles.albumCreationSection}>
            <div className={styles.albumForm}>
              <div className={styles.formField}>
                <label htmlFor="albumName">Album Name</label>
                <input
                  id="albumName"
                  type="text"
                  value={albumName}
                  onChange={(e) => setAlbumName(e.target.value)}
                  placeholder="Enter album name"
                  className={styles.textInput}
                />
              </div>

              <div className={styles.formField}>
                <label htmlFor="albumDescription">Description (optional)</label>
                <textarea
                  id="albumDescription"
                  value={albumDescription}
                  onChange={(e) => setAlbumDescription(e.target.value)}
                  placeholder="Enter album description"
                  className={styles.textArea}
                  rows={3}
                />
              </div>

              <button
                className={styles.createButton}
                onClick={handleCreateAlbum}
                disabled={isCreating || selectedSongs.length === 0 || !albumName}
              >
                {isCreating ? "Creating..." : "Create Album"}
              </button>

              {errorMessage && (
                <p className={styles.errorMessage}>{errorMessage}</p>
              )}

              {albumCreated && (
                <p className={styles.successMessage}>
                  Album created successfully!
                </p>
              )}
            </div>

            {/* Selected songs count */}
            <div className={styles.selectedCount}>
              <span>{selectedSongs.length} songs selected</span>
            </div>

            {/* Song selection filters */}
            <div className={styles.filterContainer}>
              <div className={styles.searchField}>
                <input
                  type="text"
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  placeholder="Search songs..."
                  className={styles.searchInput}
                />
              </div>

              <div className={styles.filterControls}>
                <div className={styles.filterField}>
                  <label className={styles.filterLabel}>Artist</label>
                  <select
                    value={filterArtist}
                    onChange={(e) => setFilterArtist(e.target.value)}
                    className={styles.selectInput}
                  >
                    <option value="">All Artists</option>
                    {uniqueArtists.map(artist => (
                      <option key={artist} value={artist}>{artist}</option>
                    ))}
                  </select>
                </div>

                <div className={styles.filterField}>
                  <label className={styles.filterLabel}>Genre</label>
                  <select
                    value={filterGenre}
                    onChange={(e) => setFilterGenre(e.target.value)}
                    className={styles.selectInput}
                  >
                    <option value="">All Genres</option>
                    {uniqueGenres.map(genre => (
                      <option key={genre} value={genre}>{genre}</option>
                    ))}
                  </select>
                </div>
              </div>
            </div>

            {/* Song library */}
            <div className={styles.musicGrid}>
              {musicList.filter(music => {
                // Apply search term filter
                const searchMatch = !searchTerm || 
                  music.title?.toLowerCase().includes(searchTerm.toLowerCase()) ||
                  music.artist?.toLowerCase().includes(searchTerm.toLowerCase()) ||
                  music.genre?.toLowerCase().includes(searchTerm.toLowerCase());
                
                // Apply artist filter
                const artistMatch = !filterArtist || music.artist === filterArtist;
                
                // Apply genre filter
                const genreMatch = !filterGenre || music.genre === filterGenre;
                
                return searchMatch && artistMatch && genreMatch;
              }).length > 0 ? (
                musicList.filter(music => {
                  // Apply search term filter
                  const searchMatch = !searchTerm || 
                    music.title?.toLowerCase().includes(searchTerm.toLowerCase()) ||
                    music.artist?.toLowerCase().includes(searchTerm.toLowerCase()) ||
                    music.genre?.toLowerCase().includes(searchTerm.toLowerCase());
                  
                  // Apply artist filter
                  const artistMatch = !filterArtist || music.artist === filterArtist;
                  
                  // Apply genre filter
                  const genreMatch = !filterGenre || music.genre === filterGenre;
                  
                  return searchMatch && artistMatch && genreMatch;
                }).map(music => {
                  // Process the image URL with improved handler
                  const imageUrl = getImageUrl(music.imageUrl);
                  const isFavorite = favorites.includes(music.musicId);
                  const isSelected = selectedSongs.includes(music.musicId);
                  
                  return (
                    <div
                      key={music.musicId}
                      className={`${styles.musicCard} ${isSelected ? styles.selected : ''} ${currentlyPlaying === music.musicId ? 
                          (isPlaying ? styles.currentlyPlayingCard : styles.pausedCard) : ''}`}
                      onClick={() => handleSongClick(music.musicId)}
                      onDoubleClick={(e) => handleMusicCardDoubleClick(e, music.musicId)}
                    >
                      <div className={styles.musicImageContainer}>
                        {imageUrl ? (
                          <img
                            src={imageUrl}
                            alt={music.title}
                            className={styles.musicImage}
                            onError={(e) => {
                              e.target.onerror = null;
                              e.target.style.display = "none";
                              const placeholder = e.target.parentNode.querySelector(
                                `.${styles.musicPlaceholder}`
                              );
                              if (placeholder) {
                                placeholder.style.display = "flex";
                              }
                            }}
                          />
                        ) : null}
                        <div
                          className={styles.musicPlaceholder}
                          style={{
                            display: imageUrl ? "none" : "flex"
                          }}
                        >
                          <span>
                            {music.title ? music.title.charAt(0).toUpperCase() : "♪"}
                          </span>
                        </div>
                        <div className={styles.musicOverlay} />
                        
                        {isFavorite && (
                          <div className={styles.favoriteIndicator}>★</div>
                        )}
                        
                        {isSelected && (
                          <div className={styles.selectedCheckmark}>✓</div>
                        )}
                        
                        <button
                          className={styles.musicPlayButton}
                          onClick={(e) => {
                            handleMusicPlayClick(e, music.musicId);
                          }}
                        >
                          {currentlyPlaying === music.musicId && isPlaying ? '❚❚' : '▶'}
                        </button>
                      </div>
                      <div className={styles.musicInfo}>
                        <h3 className={styles.musicTitle}>{music.title}</h3>
                        <p className={styles.musicArtist}>{music.artist}</p>
                        {music.genre && (
                          <p className={styles.musicGenre}>{music.genre}</p>
                        )}
                      </div>
                    </div>
                  );
                })
              ) : (
                <div className={styles.noResults}>
                  <p>No songs match your filters</p>
                </div>
              )}
            </div>
          </section>
        </main>
      </div>
    </div>
  );
};

export default AlbumCreator; 