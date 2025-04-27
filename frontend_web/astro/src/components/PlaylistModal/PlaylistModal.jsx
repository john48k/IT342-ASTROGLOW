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
    removeSongFromPlaylist,
    deletePlaylist,
    refreshPlaylists
  } = usePlaylist();
  
  const [newPlaylistName, setNewPlaylistName] = useState('');
  const [isCreating, setIsCreating] = useState(false);
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  const [expandedPlaylist, setExpandedPlaylist] = useState(null);
  const [editingPlaylistId, setEditingPlaylistId] = useState(null);
  const [editingPlaylistName, setEditingPlaylistName] = useState('');
  const [isEditing, setIsEditing] = useState(false);
  const [showDeleteConfirmation, setShowDeleteConfirmation] = useState(false);
  const [playlistToDelete, setPlaylistToDelete] = useState(null);

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
      // Check if the song is already in the selected playlist
      const selectedPlaylist = playlists.find(p => p.playlistId === playlistId);
      
      if (selectedPlaylist) {
        // For Firebase files, the ID might be string-based 
        const isMusicInPlaylist = selectedPlaylist.music && selectedPlaylist.music.some(song => {
          // Check if either the musicId matches or for Firebase items, the audioUrl matches
          return String(song.musicId) === String(selectedMusicId) || 
                 (song.audioUrl && selectedMusicId.startsWith('firebase-') && 
                  song.audioUrl === selectedMusicId);
        });
        
        if (isMusicInPlaylist) {
          // Song already exists in playlist, show a message
          setSuccessMessage('This song is already in the playlist!');
          
          // Clear success message after 3 seconds
          setTimeout(() => {
            setSuccessMessage('');
          }, 3000);
          
          return;
        }
      }
      
      // If we get here, the song is not in the playlist, proceed with adding it
      const result = await addSongToPlaylist(playlistId, selectedMusicId);
      
      if (result.success) {
        if (result.alreadyExists) {
          setSuccessMessage('This song is already in the playlist!');
        } else {
          setSuccessMessage('Song added to playlist successfully!');
        }
        
        // Clear success message after 3 seconds
        setTimeout(() => {
          setSuccessMessage('');
        }, 3000);
      } else {
        setError(result.error || 'Failed to add song to playlist');
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

  const handleEditPlaylist = (playlist) => {
    setEditingPlaylistId(playlist.playlistId);
    setEditingPlaylistName(playlist.name);
    setIsEditing(true);
  };

  const savePlaylistEdit = async () => {
    if (!editingPlaylistName.trim()) {
      setError('Please enter a playlist name');
      return;
    }

    try {
      // Since there's no direct update playlist name endpoint, we'll simulate it by:
      // 1. Creating a new playlist with the updated name
      // 2. Adding all songs from old playlist to the new one
      // 3. Deleting the old playlist
      
      const newPlaylist = await createPlaylist(editingPlaylistName);
      
      if (newPlaylist) {
        // Find the old playlist
        const oldPlaylist = playlists.find(p => p.playlistId === editingPlaylistId);
        
        // Add all songs from old playlist to new one
        if (oldPlaylist && oldPlaylist.music && oldPlaylist.music.length > 0) {
          for (const song of oldPlaylist.music) {
            await addSongToPlaylist(newPlaylist.playlistId, song.musicId);
          }
        }
        
        // Delete old playlist
        await deletePlaylist(editingPlaylistId);
        
        setSuccessMessage('Playlist updated successfully!');
        setTimeout(() => {
          setSuccessMessage('');
        }, 3000);
      }
    } catch (err) {
      setError('An error occurred while updating the playlist');
      console.error(err);
    } finally {
      setEditingPlaylistId(null);
      setEditingPlaylistName('');
      setIsEditing(false);
      await refreshPlaylists();
    }
  };

  const cancelEdit = () => {
    setEditingPlaylistId(null);
    setEditingPlaylistName('');
    setIsEditing(false);
  };

  const confirmDeletePlaylist = (playlist) => {
    setPlaylistToDelete(playlist);
    setShowDeleteConfirmation(true);
  };

  const handleDeletePlaylist = async () => {
    if (!playlistToDelete) return;
    
    try {
      const deleted = await deletePlaylist(playlistToDelete.playlistId);
      if (deleted) {
        setSuccessMessage('Playlist deleted successfully!');
        setTimeout(() => {
          setSuccessMessage('');
        }, 3000);
      } else {
        setError('Failed to delete playlist');
      }
    } catch (err) {
      setError('An error occurred while deleting the playlist');
      console.error(err);
    } finally {
      setShowDeleteConfirmation(false);
      setPlaylistToDelete(null);
    }
  };

  const cancelDelete = () => {
    setShowDeleteConfirmation(false);
    setPlaylistToDelete(null);
  };

  if (!isPlaylistModalOpen) return null;

  return (
    <div className={styles.modalOverlay}>
      <div className={styles.modalContent}>
        <div className={styles.modalHeader}>
          <h2>{selectedMusicId ? 'Add to Playlist' : 'Manage Playlists'}</h2>
          <button className={styles.closeButton} onClick={closePlaylistModal}>×</button>
        </div>
        
        {error && <div className={styles.errorMessage}>{error}</div>}
        {successMessage && <div className={styles.successMessage}>{successMessage}</div>}
        
        {showDeleteConfirmation && (
          <div className={styles.confirmationBox}>
            <p>Are you sure you want to delete the playlist "{playlistToDelete?.name}"?</p>
            <div className={styles.confirmationActions}>
              <button 
                className={styles.cancelButton} 
                onClick={cancelDelete}
              >
                Cancel
              </button>
              <button 
                className={styles.deleteButton} 
                onClick={handleDeletePlaylist}
              >
                Delete
              </button>
            </div>
          </div>
        )}
        
        <div className={styles.playlistsContainer}>
          <h3>Your Playlists</h3>
          {playlists.length === 0 ? (
            <p className={styles.noPlaylists}>You don't have any playlists yet</p>
          ) : (
            <ul className={styles.playlistList}>
              {playlists.map(playlist => (
                <li key={playlist.playlistId} className={styles.playlistItem}>
                  {editingPlaylistId === playlist.playlistId ? (
                    <div className={styles.editPlaylistForm}>
                      <input 
                        type="text" 
                        value={editingPlaylistName}
                        onChange={(e) => setEditingPlaylistName(e.target.value)}
                        className={styles.editPlaylistInput}
                      />
                      <div className={styles.editPlaylistActions}>
                        <button 
                          className={styles.saveButton}
                          onClick={savePlaylistEdit}
                        >
                          Save
                        </button>
                        <button 
                          className={styles.cancelButton}
                          onClick={cancelEdit}
                        >
                          Cancel
                        </button>
                      </div>
                    </div>
                  ) : (
                    <div className={styles.playlistHeader}>
                      <div className={styles.playlistInfo}>
                        <span className={styles.playlistName}>{playlist.name}</span>
                        <span className={styles.songCount}>
                          {playlist.music?.length || 0} songs
                        </span>
                      </div>
                      <div className={styles.playlistActions}>
                        {selectedMusicId && (
                          <button 
                            className={styles.addButton}
                            onClick={() => handleAddToPlaylist(playlist.playlistId)}
                          >
                            Add
                          </button>
                        )}
                        <button 
                          className={styles.editButton}
                          onClick={() => handleEditPlaylist(playlist)}
                          title="Edit playlist"
                        >
                          ✎
                        </button>
                        <button 
                          className={styles.deleteButton}
                          onClick={() => confirmDeletePlaylist(playlist)}
                          title="Delete playlist"
                        >
                          🗑️
                        </button>
                        <button 
                          className={styles.expandButton}
                          onClick={() => togglePlaylistExpand(playlist.playlistId)}
                        >
                          {expandedPlaylist === playlist.playlistId ? '▼' : '▶'}
                        </button>
                      </div>
                    </div>
                  )}
                  
                  {expandedPlaylist === playlist.playlistId && playlist.music && (
                    <ul className={styles.musicList}>
                      {playlist.music.length > 0 ? (
                        playlist.music.map((music, index) => (
                          <li key={`playlist-music-${playlist.playlistId}-${music.musicId}-${index}`} className={styles.musicItem}>
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
                        ))
                      ) : (
                        <li className={styles.emptyPlaylist}>This playlist is empty</li>
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