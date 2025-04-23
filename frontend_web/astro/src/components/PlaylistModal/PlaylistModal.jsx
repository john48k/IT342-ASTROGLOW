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
    removeSongFromPlaylist
  } = usePlaylist();
  
  const [newPlaylistName, setNewPlaylistName] = useState('');
  const [isCreating, setIsCreating] = useState(false);
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  const [expandedPlaylist, setExpandedPlaylist] = useState(null);

  const handleCreatePlaylist = async (e) => {
    e.preventDefault();
    
    if (!newPlaylistName.trim()) {
      setError('Please enter a playlist name');
      return;
    }
    
    setIsCreating(true);
    setError('');
    setSuccessMessage('');
    
    try {
      const newPlaylist = await createPlaylist(newPlaylistName);
      
      if (newPlaylist) {
        setNewPlaylistName('');
        setSuccessMessage('New playlist created successfully!');
        
        // If a song is selected, add it to the new playlist
        if (selectedMusicId) {
          try {
            const added = await addSongToPlaylist(newPlaylist.playlistId, selectedMusicId);
            if (added) {
              setSuccessMessage('Song added to the new playlist!');
            } else {
              setError('Failed to add song to playlist');
            }
          } catch (err) {
            setError('Failed to add song to playlist: ' + err.message);
          }
        }
        
        // Clear success message after 3 seconds
        setTimeout(() => {
          setSuccessMessage('');
        }, 3000);
      }
    } catch (err) {
      setError(err.message || 'An error occurred while creating the playlist');
      console.error('Error creating playlist:', err);
    } finally {
      setIsCreating(false);
    }
  };

  const handleAddToPlaylist = async (playlistId) => {
    if (!selectedMusicId) return;
    
    setError('');
    
    try {
      const added = await addSongToPlaylist(playlistId, selectedMusicId);
      
      if (added) {
        setSuccessMessage('Song added to playlist successfully!');
        
        // Clear success message after 3 seconds
        setTimeout(() => {
          setSuccessMessage('');
        }, 3000);
      } else {
        setError('Failed to add song to playlist');
      }
    } catch (err) {
      setError('An error occurred while adding the song to the playlist');
      console.error(err);
    }
  };

  const handleRemoveFromPlaylist = async (playlistId, musicId) => {
    try {
      const removed = await removeSongFromPlaylist(playlistId, musicId);
      if (removed) {
        setSuccessMessage('Song removed from playlist successfully!');
        setTimeout(() => {
          setSuccessMessage('');
        }, 3000);
      } else {
        setError('Failed to remove song from playlist');
      }
    } catch (err) {
      setError('An error occurred while removing the song from the playlist');
      console.error(err);
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
                      <span className={styles.playlistName}>{playlist.name}</span>
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
                        onClick={() => handleAddToPlaylist(playlist.playlistId)}
                      >
                        Add
                      </button>
                    </div>
                  </div>
                  {expandedPlaylist === playlist.playlistId && playlist.music && (
                    <ul className={styles.musicList}>
                      {playlist.music.map(music => (
                        <li key={music.musicId} className={styles.musicItem}>
                          <img 
                            src={music.imageUrl} 
                            alt={music.title} 
                            className={styles.musicThumbnail}
                          />
                          <div className={styles.musicInfo}>
                            <span className={styles.musicTitle}>{music.title}</span>
                            <span className={styles.musicArtist}>{music.artist}</span>
                          </div>
                          <button
                            className={styles.removeButton}
                            onClick={() => handleRemoveFromPlaylist(playlist.playlistId, music.musicId)}
                            title="Remove from playlist"
                          >
                            ×
                          </button>
                        </li>
                      ))}
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
              className={styles.playlistNameInput}
              disabled={isCreating}
            />
            <button 
              type="submit" 
              className={styles.createButton}
              disabled={isCreating || !newPlaylistName.trim()}
            >
              {isCreating ? 'Creating...' : 'Create New Playlist'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};

export default PlaylistModal; 