import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { useUser } from './UserContext';

const FavoritesContext = createContext();

export const FavoritesProvider = ({ children }) => {
  const [favorites, setFavorites] = useState(() => {
    // Initialize from localStorage if available
    const savedFavorites = localStorage.getItem('firebaseFavorites');
    return savedFavorites ? JSON.parse(savedFavorites) : [];
  });
  const { user } = useUser();

  // Save to localStorage whenever favorites change
  useEffect(() => {
    localStorage.setItem('firebaseFavorites', JSON.stringify(favorites));
  }, [favorites]);

  // Load favorites from backend when component mounts or user changes
  useEffect(() => {
    const loadFavorites = async () => {
      if (user?.userId) {
        try {
          console.log(`Loading favorites for user ID: ${user.userId}`);
          const response = await fetch(`http://localhost:8080/api/favorites/user/${user.userId}/music-details`, {
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
            // Only update database favorites, keep Firebase favorites
            setFavorites(prevFavorites => {
              const firebaseFavorites = prevFavorites.filter(fav => 
                typeof fav.music?.filename === 'string' && 
                fav.music.filename.startsWith('firebase-')
              );
              return [...data, ...firebaseFavorites];
            });
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
        const response = await fetch(`http://localhost:8080/api/favorites/user/${user.userId}/music-details`, {
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
      // Handle Firebase music IDs differently
      const isFirebaseMusic = typeof musicId === 'string' && musicId.startsWith('firebase-');
      
      // First check if the music is already favorited
      const checkResponse = await fetch(`http://localhost:8080/api/favorites/user/${user.userId}/music/${encodeURIComponent(musicId)}/check`, {
        credentials: 'include'
      });
      const isFavorited = await checkResponse.json();

      let response;
      if (isFavorited) {
        // Remove from favorites
        response = await fetch(`http://localhost:8080/api/favorites/user/${user.userId}/music/${encodeURIComponent(musicId)}`, {
          method: 'DELETE',
          credentials: 'include'
        });
      } else {
        // Add to favorites
        response = await fetch(`http://localhost:8080/api/favorites/user/${user.userId}/music/${encodeURIComponent(musicId)}`, {
          method: 'POST',
          credentials: 'include'
        });
      }

      if (response.ok) {
        // For Firebase music, we'll just update the UI state
        if (isFirebaseMusic) {
          setFavorites(prevFavorites => {
            if (isFavorited) {
              return prevFavorites.filter(fav => fav.music?.filename !== musicId);
            } else {
              return [...prevFavorites, { music: { filename: musicId } }];
            }
          });
        } else {
          // For database music, refresh the favorites list
          await refreshFavorites();
        }
      }
    } catch (error) {
      console.error('Error toggling favorite:', error);
    }
  }, [user?.userId, refreshFavorites]);

  const isFavorite = useCallback((musicId) => {
    // Handle both Firebase and database music IDs
    const isFirebaseMusic = typeof musicId === 'string' && musicId.startsWith('firebase-');
    return favorites.some(fav => {
      if (isFirebaseMusic) {
        return fav.music?.filename === musicId;
      }
      return Number(fav.music?.musicId) === Number(musicId);
    });
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