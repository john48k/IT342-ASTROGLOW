import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { useUser } from './UserContext';

const PlaylistContext = createContext();

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

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
          const response = await fetch(`${API_BASE_URL}/playlists/getAllPlaylist`, {
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
        const response = await fetch(`${API_BASE_URL}/playlists/getAllPlaylist`, {
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

  // Check if a song is in a playlist
  const checkSongInPlaylist = useCallback(async (musicId) => {
    if (!user?.userId) return false;

    try {
      const response = await fetch(`${API_BASE_URL}/playlists/user/${user.userId}/music/${musicId}/check`, {
        headers: {
          'Accept': 'application/json',
          'Content-Type': 'application/json'
        },
        credentials: 'include'
      });

      if (response.ok) {
        return await response.json();
      }
      return false;
    } catch (error) {
      console.error('Error checking song in playlist:', error);
      return false;
    }
  }, [user?.userId]);

  // Create a new playlist
  const createPlaylist = useCallback(async (playlistName) => {
    if (!user?.userId) return null;

    try {
      console.log(`Creating new playlist`);
      const response = await fetch(`${API_BASE_URL}/playlists/postPlaylist`, {
        method: 'POST',
        headers: {
          'Accept': 'application/json',
          'Content-Type': 'application/json',
          'Cache-Control': 'no-cache, no-store, must-revalidate',
          'Pragma': 'no-cache'
        },
        body: JSON.stringify({
          user: {
            userId: parseInt(user.userId)
          }
        }),
        credentials: 'include'
      });

      if (response.ok) {
        const newPlaylist = await response.json();
        console.log(`Successfully created playlist`);
        await refreshPlaylists();
        return newPlaylist;
      } else {
        const errorData = await response.text();
        console.error(`Error creating playlist: ${response.status}`, errorData);
        return null;
      }
    } catch (error) {
      console.error('Error creating playlist:', error);
      return null;
    }
  }, [user?.userId, refreshPlaylists]);

  // Add a song to a playlist
  const addSongToPlaylist = useCallback(async (playlistId, musicId) => {
    if (!user?.userId) return false;

    try {
      console.log(`Adding song ${musicId} to playlist ${playlistId}`);
      const response = await fetch(`${API_BASE_URL}/playlists/user/${user.userId}/music/${musicId}`, {
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
        console.log(`Successfully added song ${musicId} to playlist`);
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
  const removeSongFromPlaylist = useCallback(async (musicId) => {
    if (!user?.userId) return false;

    try {
      const response = await fetch(`${API_BASE_URL}/playlists/deletePlaylist/user/${user.userId}/music/${musicId}`, {
        method: 'DELETE',
        headers: {
          'Accept': 'application/json',
          'Content-Type': 'application/json'
        },
        credentials: 'include'
      });

      if (response.ok) {
        await refreshPlaylists();
        return true;
      }
      return false;
    } catch (error) {
      console.error('Error removing song from playlist:', error);
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
      checkSongInPlaylist,
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