import React, { useState, useEffect } from 'react';
import { usePlaylist } from '../../context/PlaylistContext';
import styles from './PlaylistModal.module.css';

const PlaylistModal = () => {
  const { 
    playlists, 
    addSongToPlaylist, 
    createPlaylist, 
    isPlaylistModalOpen, 
    closePlaylistModal, 
    selectedMusicId,
    refreshPlaylists
  } = usePlaylist();
  
  const [newPlaylistName, setNewPlaylistName] = useState('');
  const [isCreating, setIsCreating] = useState(false);
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  const [expandedPlaylist, setExpandedPlaylist] = useState(null);

  // Log when the modal is opened and what music ID is selected
  useEffect(() => {
    if (isPlaylistModalOpen) {
      console.log('Playlist modal opened. Selected music ID:', selectedMusicId);
      // Refresh playlists when modal is opened
      refreshPlaylists().then(() => {
        console.log('Playlists refreshed. Current playlists:', playlists);
      });
    }
  }, [isPlaylistModalOpen, selectedMusicId, refreshPlaylists]);

  // Log playlists when they change
  useEffect(() => {
    console.log('Playlists updated:', playlists);
  }, [playlists]);

  const handleCreatePlaylist = async (e) => {
    e.preventDefault();
    
    // Validate playlist name
    if (!newPlaylistName.trim()) {
      setError('Please enter a playlist name');
      return;
    }
    
    setIsCreating(true);
    setError('');
    
    try {
      console.log(`Attempting to create playlist: "${newPlaylistName}"`);
      const newPlaylist = await createPlaylist(newPlaylistName);
      
      if (newPlaylist) {
        console.log('Successfully created playlist:', newPlaylist);
        setNewPlaylistName('');
        setSuccessMessage(`"${newPlaylistName}" playlist created successfully!`);
        
        // If a song is selected, add it to the new playlist
        if (selectedMusicId) {
          console.log(`Attempting to add song ${selectedMusicId} to new playlist ${newPlaylist.playlistId}`);
          try {
            const added = await addSongToPlaylist(newPlaylist.playlistId, selectedMusicId);
            if (added) {
              console.log(`Successfully added song ${selectedMusicId} to playlist ${newPlaylist.playlistId}`);
              setSuccessMessage(`Song added to "${newPlaylistName}" playlist!`);
            } else {
              console.error(`Failed to add song ${selectedMusicId} to playlist ${newPlaylist.playlistId}`);
              setError('Failed to add song to playlist');
            }
          } catch (error) {
            console.error('Error adding song to playlist:', error);
          }
        }
        
        // Clear success message after 3 seconds
        setTimeout(() => {
          setSuccessMessage('');
        }, 3000);
      } else {
        console.error('Failed to create playlist - returned null');
        setError('Failed to create playlist. Please try again later.');
      }
    } catch (err) {
      console.error('An error occurred while creating the playlist:', err);
      setError('An unexpected error occurred. Please try again later.');
    } finally {
      setIsCreating(false);
    }
  };

  const handleAddToPlaylist = async (playlistId, playlistName) => {
    if (!selectedMusicId) {
      console.error('No music selected to add to playlist');
      setError('No music selected to add to playlist');
      return;
    }
    
    setError('');
    
    try {
      console.log(`Attempting to add song ${selectedMusicId} to playlist ${playlistId}`);
      
      // Display a loading message
      setSuccessMessage('Adding song to playlist...');
      
      // Determine if this is a Firebase music (string) or database music (number)
      const isMusicIdString = typeof selectedMusicId === 'string';
      const isFirebaseMusic = isMusicIdString && selectedMusicId.startsWith('firebase-');
      
      console.log(`Music type: ${isFirebaseMusic ? 'Firebase' : 'Database'}, ID: ${selectedMusicId}`);
      
      const added = await addSongToPlaylist(playlistId, selectedMusicId);
      
      if (added) {
        console.log(`Successfully added song ${selectedMusicId} to playlist ${playlistId}`);
        setSuccessMessage(`Song added to "${playlistName}" successfully!`);
        
        // Refresh the playlists to show the updated content
        await refreshPlaylists();
        
        // Clear success message after 3 seconds
        setTimeout(() => {
          setSuccessMessage('');
        }, 3000);
      } else {
        console.error(`Failed to add song ${selectedMusicId} to playlist ${playlistId}`);
        setError('Failed to add song to playlist. Please try again.');
      }
    } catch (err) {
      console.error('Error adding song to playlist:', err);
      setError(`Error: ${err.message || 'Something went wrong adding the song to the playlist'}`);
    }
  };

  const togglePlaylistExpand = (playlistId) => {
    if (expandedPlaylist === playlistId) {
      setExpandedPlaylist(null);
    } else {
      setExpandedPlaylist(playlistId);
    }
  };

  if (!isPlaylistModalOpen) return null;

  return (
    <div className={styles.modalOverlay}>
      <div className={styles.modalContent}>
        <div className={styles.modalHeader}>
          <h2>Add to Playlist</h2>
          <button className={styles.closeButton} onClick={closePlaylistModal}>×</button>
        </div>
        
        {error && <div className={styles.errorMessage}>{error}</div>}
        {successMessage && <div className={styles.successMessage}>{successMessage}</div>}
        
        <div className={styles.playlistsContainer}>
          <h3>Your Playlists</h3>
          {playlists.length === 0 ? (
            <p className={styles.noPlaylists}>You don't have any playlists yet</p>
          ) : (
            <ul className={styles.playlistList}>
              {playlists.map(playlist => (
                <li key={playlist.playlistId} className={styles.playlistItem}>
                  <div className={styles.playlistHeader}>
                    <div className={styles.playlistInfo}>
                      <span className={styles.playlistName}>{playlist.name || 'Unnamed Playlist'}</span>
                      <span className={styles.songCount}>
                        {playlist.music?.length || 0} songs
                      </span>
                    </div>
                    <div className={styles.playlistActions}>
                      <button 
                        className={styles.expandButton}
                        onClick={() => togglePlaylistExpand(playlist.playlistId)}
                      >
                        {expandedPlaylist === playlist.playlistId ? '▼' : '▶'}
                      </button>
                      <button 
                        className={styles.addButton}
                        onClick={() => handleAddToPlaylist(playlist.playlistId, playlist.name)}
                      >
                        Add
                      </button>
                    </div>
                  </div>
                  {expandedPlaylist === playlist.playlistId && playlist.music && (
                    <ul className={styles.musicList}>
                      {playlist.music.length > 0 ? (
                        playlist.music.map(music => (
                          <li key={music.musicId} className={styles.musicItem}>
                            <img 
                              src={music.imageUrl} 
                              alt={music.title} 
                              className={styles.musicThumbnail}
                              onError={(e) => {
                                e.target.onerror = null;
                                e.target.src = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='40' height='40' viewBox='0 0 40 40'%3E%3Crect width='40' height='40' fill='%23333'/%3E%3Ctext x='50%25' y='50%25' font-size='20' text-anchor='middle' fill='%23666' dy='.3em'%3E♪%3C/text%3E%3C/svg%3E";
                              }}
                            />
                            <div className={styles.musicInfo}>
                              <span className={styles.musicTitle}>{music.title || 'Untitled'}</span>
                              <span className={styles.musicArtist}>{music.artist || 'Unknown Artist'}</span>
                            </div>
                          </li>
                        ))
                      ) : (
                        <li className={styles.emptyPlaylist}>No songs in this playlist yet</li>
                      )}
                    </ul>
                  )}
                </li>
              ))}
            </ul>
          )}
        </div>
        
        <div className={styles.createPlaylistContainer}>
          <h3>Create New Playlist</h3>
          <form onSubmit={handleCreatePlaylist} className={styles.createPlaylistForm}>
            <input
              type="text"
              value={newPlaylistName}
              onChange={(e) => setNewPlaylistName(e.target.value)}
              placeholder="Enter playlist name"
              className={styles.playlistInput}
              required
            />
            <button 
              type="submit" 
              className={styles.createButton}
              disabled={isCreating || !newPlaylistName.trim()}
            >
              {isCreating ? 'Creating...' : 'Create'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};

export default PlaylistModal; 