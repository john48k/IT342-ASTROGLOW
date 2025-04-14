import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { useUser } from './UserContext';

const FavoritesContext = createContext();

export const FavoritesProvider = ({ children }) => {
  const [favorites, setFavorites] = useState([]);
  const { user } = useUser();

  // Load favorites from backend when component mounts or user changes
  useEffect(() => {
    const loadFavorites = async () => {
      if (user?.userId) {
        try {
          console.log(`Loading favorites for user ID: ${user.userId}`);
          const response = await fetch(`http://localhost:8080/api/favorites/user/${user.userId}`, {
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
            console.log(`Loaded ${data.length} favorites for user ID: ${user.userId}`);
            setFavorites(data);
          } else {
            console.error(`Error loading favorites: ${response.status} ${response.statusText}`);
          }
        } catch (error) {
          console.error('Error loading favorites:', error);
        }
      }
    };
    
    loadFavorites();
  }, [user?.userId]);

  // Memoize refreshFavorites to prevent unnecessary re-renders
  const refreshFavorites = useCallback(async () => {
    if (user?.userId) {
      try {
        console.log(`Refreshing favorites for user ID: ${user.userId}`);
        const response = await fetch(`http://localhost:8080/api/favorites/user/${user.userId}`, {
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
          console.log(`Refreshed ${data.length} favorites for user ID: ${user.userId}`);
          setFavorites(data);
        } else {
          console.error(`Error refreshing favorites: ${response.status} ${response.statusText}`);
        }
      } catch (error) {
        console.error('Error refreshing favorites:', error);
      }
    }
  }, [user?.userId]);

  const toggleFavorite = useCallback(async (musicId) => {
    if (!user?.userId) return;

    try {
      // First check if the music is already favorited
      const checkResponse = await fetch(`http://localhost:8080/api/favorites/user/${user.userId}/music/${musicId}/check`);
      const isFavorited = await checkResponse.json();

      let response;
      if (isFavorited) {
        // Remove from favorites
        response = await fetch(`http://localhost:8080/api/favorites/user/${user.userId}/music/${musicId}`, {
          method: 'DELETE'
        });
      } else {
        // Add to favorites
        response = await fetch(`http://localhost:8080/api/favorites/user/${user.userId}/music/${musicId}`, {
          method: 'POST'
        });
      }

      if (response.ok) {
        // Refresh the favorites list
        await refreshFavorites();
      }
    } catch (error) {
      console.error('Error toggling favorite:', error);
    }
  }, [user?.userId, refreshFavorites]);

  const isFavorite = useCallback((musicId) => {
    return favorites.some(fav => Number(fav.music?.musicId) === Number(musicId));
  }, [favorites]);

  return (
    <FavoritesContext.Provider value={{ 
      favorites, 
      toggleFavorite, 
      isFavorite,
      refreshFavorites 
    }}>
      {children}
    </FavoritesContext.Provider>
  );
};

export const useFavorites = () => {
  return useContext(FavoritesContext);
};

export default FavoritesContext;