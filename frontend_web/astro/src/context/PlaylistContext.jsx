import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { useUser } from './UserContext';

const PlaylistContext = createContext();

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'https://astroglowfirebase-d2411.uc.r.appspot.com/api';

export const PlaylistProvider = ({ children }) => {
  const [playlists, setPlaylists] = useState([]);
  const [isPlaylistModalOpen, setIsPlaylistModalOpen] = useState(false);
  const [selectedMusicId, setSelectedMusicId] = useState(null);
  const { user } = useUser();

  // Load playlists from backend when component mounts or user changes
  useEffect(() => {
    const loadPlaylists = async () => {
      if (user?.userId) {
        try {
          console.log(`Loading playlists for user ID: ${user.userId}`);
          const response = await fetch(`${API_BASE_URL}/playlists/user/${user.userId}`, {
            headers: {
              'Accept': 'application/json',
              'Content-Type': 'application/json',
              'Cache-Control': 'no-cache, no-store, must-revalidate',
              'Pragma': 'no-cache'
            },
            credentials: 'include'
          });

          if (response.ok) {
            const data = await response.json();
            console.log(`Loaded ${data.length} playlists for user ID: ${user.userId}`);
            setPlaylists(data);
          } else {
            console.error(`Error loading playlists: ${response.status} ${response.statusText}`);
          }
        } catch (error) {
          console.error('Error loading playlists:', error);
        }
      }
    };

    loadPlaylists();
  }, [user?.userId]);

  // Memoize refreshPlaylists to prevent unnecessary re-renders
  const refreshPlaylists = useCallback(async () => {
    if (user?.userId) {
      try {
        console.log(`Refreshing playlists for user ID: ${user.userId}`);
        const response = await fetch(`${API_BASE_URL}/playlists/user/${user.userId}`, {
          headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            'Cache-Control': 'no-cache, no-store, must-revalidate',
            'Pragma': 'no-cache'
          },
          credentials: 'include'
        });

        if (response.ok) {
          const data = await response.json();
          console.log(`Refreshed ${data.length} playlists for user ID: ${user.userId}`);
          setPlaylists(data);
        } else {
          console.error(`Error refreshing playlists: ${response.status} ${response.statusText}`);
        }
      } catch (error) {
        console.error('Error refreshing playlists:', error);
      }
    }
  }, [user?.userId]);

  // Create a new playlist
  const createPlaylist = useCallback(async (playlistName) => {
    if (!user?.userId) {
      console.error('User ID is required to create a playlist');
      return null;
    }

    try {
      console.log(`Creating new playlist: ${playlistName}`);
      const response = await fetch(`${API_BASE_URL}/playlists/postPlaylist`, {
        method: 'POST',
        headers: {
          'Accept': 'application/json',
          'Content-Type': 'application/json',
          'Cache-Control': 'no-cache, no-store, must-revalidate',
          'Pragma': 'no-cache'
        },
        body: JSON.stringify({
          userId: parseInt(user.userId),
          name: playlistName
        }),
        credentials: 'include'
      });

      if (!response.ok) {
        const errorData = await response.text();
        console.error(`Error creating playlist: ${response.status}`, errorData);
        throw new Error(errorData || 'Failed to create playlist');
      }

      const newPlaylist = await response.json();
      console.log(`Successfully created playlist: ${newPlaylist.name}`);
      await refreshPlaylists();
      return newPlaylist;
    } catch (error) {
      console.error('Error creating playlist:', error);
      throw error;
    }
  }, [user?.userId, refreshPlaylists]);

  // Add a song to a playlist
  const addSongToPlaylist = useCallback(async (playlistId, musicId) => {
    if (!user?.userId) return false;

    try {
      console.log(`Adding song ${musicId} to playlist ${playlistId}`);
      const response = await fetch(`${API_BASE_URL}/playlists/${playlistId}/add/${musicId}`, {
        method: 'POST',
        headers: {
          'Accept': 'application/json',
          'Content-Type': 'application/json',
          'Cache-Control': 'no-cache, no-store, must-revalidate',
          'Pragma': 'no-cache'
        },
        credentials: 'include'
      });

      if (response.ok) {
        console.log(`Successfully added song ${musicId} to playlist ${playlistId}`);
        await refreshPlaylists();
        return true;
      } else {
        const errorData = await response.text();
        console.error(`Error adding song to playlist: ${response.status}`, errorData);
        return false;
      }
    } catch (error) {
      console.error('Error adding song to playlist:', error);
      return false;
    }
  }, [user?.userId, refreshPlaylists]);

  // Remove a song from a playlist
  const removeSongFromPlaylist = useCallback(async (playlistId, musicId) => {
    if (!user?.userId) return false;

    try {
      console.log(`Removing song ${musicId} from playlist ${playlistId}`);
      const response = await fetch(`${API_BASE_URL}/playlists/${playlistId}/remove/${musicId}`, {
        method: 'DELETE',
        headers: {
          'Accept': 'application/json',
          'Content-Type': 'application/json'
        },
        credentials: 'include'
      });

      if (response.ok) {
        console.log(`Successfully removed song ${musicId} from playlist ${playlistId}`);
        await refreshPlaylists();
        return true;
      } else {
        const errorData = await response.text();
        console.error(`Error removing song from playlist: ${response.status}`, errorData);
        return false;
      }
    } catch (error) {
      console.error('Error removing song from playlist:', error);
      return false;
    }
  }, [user?.userId, refreshPlaylists]);

  // Delete a playlist
  const deletePlaylist = useCallback(async (playlistId) => {
    if (!user?.userId) return false;

    try {
      console.log(`Deleting playlist ${playlistId}`);
      const response = await fetch(`${API_BASE_URL}/playlists/${playlistId}`, {
        method: 'DELETE',
        headers: {
          'Accept': 'application/json',
          'Content-Type': 'application/json'
        },
        credentials: 'include'
      });

      if (response.ok) {
        console.log(`Successfully deleted playlist ${playlistId}`);
        await refreshPlaylists();
        return true;
      } else {
        const errorData = await response.text();
        console.error(`Error deleting playlist: ${response.status}`, errorData);
        return false;
      }
    } catch (error) {
      console.error('Error deleting playlist:', error);
      return false;
    }
  }, [user?.userId, refreshPlaylists]);

  // Open the playlist modal for a specific music
  const openPlaylistModal = useCallback((musicId) => {
    setSelectedMusicId(musicId);
    setIsPlaylistModalOpen(true);
  }, []);

  // Close the playlist modal
  const closePlaylistModal = useCallback(() => {
    setIsPlaylistModalOpen(false);
    setSelectedMusicId(null);
  }, []);

  return (
    <PlaylistContext.Provider value={{
      playlists,
      addSongToPlaylist,
      createPlaylist,
      removeSongFromPlaylist,
      deletePlaylist,
      isPlaylistModalOpen,
      openPlaylistModal,
      closePlaylistModal,
      selectedMusicId,
      refreshPlaylists
    }}>
      {children}
    </PlaylistContext.Provider>
  );
};

export const usePlaylist = () => {
  return useContext(PlaylistContext);
};

export default PlaylistContext; 