import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { useUser } from './UserContext';

const FavoritesContext = createContext();

export const FavoritesProvider = ({ children }) => {
  const { user } = useUser();
  const [favorites, setFavorites] = useState([]);

  // Initialize from localStorage when user changes
  useEffect(() => {
    if (user?.userId) {
      // Use user-specific key for localStorage
      const storageKey = `firebaseFavorites-${user.userId}`;
      const savedFavorites = localStorage.getItem(storageKey);
      
      if (savedFavorites) {
        try {
          const parsedFavorites = JSON.parse(savedFavorites);
          console.log(`Loaded ${parsedFavorites.length} Firebase favorites from localStorage for user ${user.userId}`);
          setFavorites(parsedFavorites);
        } catch (error) {
          console.error('Error parsing favorites from localStorage:', error);
          setFavorites([]);
        }
      } else {
        // No saved favorites for this user
        setFavorites([]);
      }
    } else {
      // Clear favorites when no user is logged in
      setFavorites([]);
    }
  }, [user?.userId]);

  // Save to localStorage whenever favorites change
  useEffect(() => {
    if (user?.userId) {
      // Use user-specific key for localStorage
      const storageKey = `firebaseFavorites-${user.userId}`;
      localStorage.setItem(storageKey, JSON.stringify(favorites));
      console.log(`Saved ${favorites.length} Firebase favorites to localStorage for user ${user.userId}`);
    }
  }, [favorites, user?.userId]);

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
            console.log(`Loaded ${data.length} favorites from database for user ID: ${user.userId}`);
            
            // Get Firebase favorites from localStorage
            const storageKey = `firebaseFavorites-${user.userId}`;
            const savedFavorites = localStorage.getItem(storageKey);
            const firebaseFavorites = savedFavorites ? JSON.parse(savedFavorites).filter(fav => 
              typeof fav.music?.filename === 'string' && 
              fav.music.filename.startsWith('firebase-')
            ) : [];
            
            // Combine database favorites with Firebase favorites
            setFavorites([...data, ...firebaseFavorites]);
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
          console.log(`Refreshed ${data.length} favorites from database for user ID: ${user.userId}`);
          
          // Keep existing Firebase favorites while updating database favorites
          setFavorites(prevFavorites => {
            const firebaseFavorites = prevFavorites.filter(fav => 
              typeof fav.music?.filename === 'string' && 
              fav.music.filename.startsWith('firebase-')
            );
            return [...data, ...firebaseFavorites];
          });
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
            // Check if the music is already in favorites
            const isAlreadyFavorited = prevFavorites.some(fav => 
              fav.music?.filename === musicId
            );

            if (isAlreadyFavorited) {
              // If already favorited, remove it
              return prevFavorites.filter(fav => fav.music?.filename !== musicId);
            } else {
              // If not favorited, add it
              return [...prevFavorites, { music: { filename: musicId } }];
            }
          });
          
          // Update user-specific favorites in localStorage
          const storageKey = `firebaseFavorites-${user.userId}`;
          const updatedFavorites = isFavorited 
            ? favorites.filter(fav => fav.music?.filename !== musicId)
            : [...favorites, { music: { filename: musicId } }];
          
          localStorage.setItem(storageKey, JSON.stringify(updatedFavorites));
        } else {
          // For database music, refresh the favorites list
          await refreshFavorites();
        }
      }
    } catch (error) {
      console.error('Error toggling favorite:', error);
    }
  }, [user?.userId, refreshFavorites, favorites]);

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

export const useFavorites = () => useContext(FavoritesContext);

export default FavoritesContext;