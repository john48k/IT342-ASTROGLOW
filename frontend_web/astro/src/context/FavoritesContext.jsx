import React, { createContext, useContext, useState, useEffect, useCallback, useMemo } from 'react';
import { useUser } from './UserContext';

const FavoritesContext = createContext();

export const FavoritesProvider = ({ children }) => {
  const [favorites, setFavorites] = useState(() => {
    // Initialize empty to avoid showing other users' favorites at startup
    return [];
  });
  const { user } = useUser();

  // Helper function to get the user-specific storage key
  const getUserStorageKey = useCallback((userId) => {
    return userId ? `favorites_user_${userId}` : 'firebaseFavorites';
  }, []);

  // Save to localStorage whenever favorites change
  useEffect(() => {
    const saveFavoritesToStorage = (favoritesData) => {
      if (favoritesData && favoritesData.length > 0) {
        // Store all favorites in the global storage for backward compatibility
        const favoritesString = JSON.stringify(favoritesData);
        console.log(`Saving ${favoritesData.length} favorites to localStorage`);
        localStorage.setItem('firebaseFavorites', favoritesString);
        localStorage.setItem('persistentFavorites', favoritesString);

        // Group favorites by user ID and save to user-specific storage
        const favoritesByUser = {};
        favoritesData.forEach(fav => {
          const userId = fav.userId || 'anonymous';
          if (!favoritesByUser[userId]) {
            favoritesByUser[userId] = [];
          }
          favoritesByUser[userId].push(fav);
        });

        // Save each user's favorites to their own storage key
        Object.entries(favoritesByUser).forEach(([userId, userFavorites]) => {
          if (userId !== 'anonymous') {
            const userKey = getUserStorageKey(userId);
            console.log(`Saving ${userFavorites.length} favorites for user ${userId} to ${userKey}`);
            localStorage.setItem(userKey, JSON.stringify(userFavorites));
          }
        });
      } else if (favoritesData.length === 0 && 
                (localStorage.getItem('firebaseFavorites') || localStorage.getItem('persistentFavorites'))) {
        // Don't clear localStorage if we have an empty favorites array but saved data exists
        // This prevents losing data on component remounts
        console.log('Keeping existing favorites in localStorage');
      } else {
        // Only clear if we explicitly have an empty array and no previous data
        localStorage.setItem('firebaseFavorites', JSON.stringify([]));
        // Don't clear persistent storage though
      }
    };

    saveFavoritesToStorage(favorites);
  }, [favorites, getUserStorageKey]);

  // Load favorites from backend when component mounts or user changes
  useEffect(() => {
    const loadFavorites = async () => {
      // If we don't have a user ID, don't load any favorites
      if (!user?.userId) {
        console.log('No user logged in, clearing favorites');
        setFavorites([]);
        return;
      }
      
      console.log(`Loading favorites for user ID: ${user.userId}`);
      
      // Try to load from user-specific storage first
      const userKey = getUserStorageKey(user.userId);
      const userFavorites = localStorage.getItem(userKey);
      
      if (userFavorites) {
        try {
          const parsedUserFavorites = JSON.parse(userFavorites);
          console.log(`Loaded ${parsedUserFavorites.length} favorites from user-specific storage for ${user.userId}`);
          
          // Make sure all favorites have the current user ID
          const validUserFavorites = parsedUserFavorites.map(fav => ({
            ...fav,
            userId: user.userId
          }));
          
          // Set favorites directly from user storage
          setFavorites(validUserFavorites);
        } catch (e) {
          console.error('Error parsing user favorites:', e);
        }
      }
      
      // Fetch from backend regardless to ensure we have the latest
      try {
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
          console.log(`Loaded ${data.length} favorites from backend for user ID: ${user.userId}`);
          
          // Tag all backend favorites with the current user ID
          const taggedDbFavorites = data.map(item => ({
            ...item,
            userId: user.userId
          }));
          
          // Update state with backend favorites, preserving Firebase favorites
          setFavorites(prevFavorites => {
            // Keep only Firebase favorites for the current user
            const firebaseFavorites = prevFavorites.filter(fav => 
              typeof fav.music?.filename === 'string' && 
              fav.music.filename.startsWith('firebase-') &&
              fav.userId === user.userId
            );
            
            // Combine with backend favorites
            const combinedFavorites = [...taggedDbFavorites, ...firebaseFavorites];
            
            // Remove duplicates
            const uniqueFavorites = combinedFavorites.filter((fav, index, self) =>
              index === self.findIndex((f) => {
                if (typeof fav.music?.filename === 'string' && fav.music.filename.startsWith('firebase-')) {
                  return f.music?.filename === fav.music?.filename && f.userId === fav.userId;
                }
                return f.music?.musicId === fav.music?.musicId && f.userId === fav.userId;
              })
            );
            
            // Save to user-specific storage
            localStorage.setItem(userKey, JSON.stringify(uniqueFavorites));
            
            return uniqueFavorites;
          });
        } else {
          console.error(`Error loading favorites: ${response.status} ${response.statusText}`);
        }
      } catch (error) {
        console.error('Error loading favorites:', error);
      }
    };
    
    loadFavorites();
  }, [user?.userId, getUserStorageKey]);

  // Save favorites when the user refreshes or closes the page
  useEffect(() => {
    const handleBeforeUnload = () => {
      if (favorites.length > 0) {
        // Convert to string once to avoid duplicate work
        const favoritesString = JSON.stringify(favorites);
        console.log(`Page unloading - Saving ${favorites.length} favorites to storage`);
        localStorage.setItem('firebaseFavorites', favoritesString);
        localStorage.setItem('persistentFavorites', favoritesString);
        
        // Also save to user-specific storage if a user is logged in
        if (user?.userId) {
          const userKey = getUserStorageKey(user.userId);
          const userFavorites = favorites.filter(fav => fav.userId === user.userId);
          localStorage.setItem(userKey, JSON.stringify(userFavorites));
        }
      }
    };

    window.addEventListener('beforeunload', handleBeforeUnload);

    return () => {
      window.removeEventListener('beforeunload', handleBeforeUnload);
    };
  }, [favorites, user, getUserStorageKey]);

  // Memoize refreshFavorites to prevent unnecessary re-renders
  const refreshFavorites = useCallback(async () => {
    if (!user?.userId) {
      console.warn('Cannot refresh favorites: No user logged in');
      return;
    }

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
        console.log(`Refreshed ${data.length} favorites from backend for user ID: ${user.userId}`);
        
        // Tag all database favorites with the current user ID
        const taggedDbFavorites = data.map(item => ({
          ...item,
          userId: user.userId
        }));
        
        // Update state with backend favorites, preserving Firebase favorites
        setFavorites(prevFavorites => {
          // Get favorites from other users (keep them separate)
          const otherUsersFavorites = prevFavorites.filter(fav => 
            fav.userId !== user.userId
          );
          
          // Get Firebase favorites for current user only
          const firebaseFavorites = prevFavorites.filter(fav => 
            typeof fav.music?.filename === 'string' && 
            fav.music.filename.startsWith('firebase-') &&
            fav.userId === user.userId
          );
          
          // Combine the current user's favorites
          const currentUserFavorites = [...taggedDbFavorites, ...firebaseFavorites];
          
          // Remove duplicates within current user's favorites
          const uniqueUserFavorites = currentUserFavorites.filter((fav, index, self) =>
            index === self.findIndex((f) => {
              if (typeof fav.music?.filename === 'string' && fav.music.filename.startsWith('firebase-')) {
                return f.music?.filename === fav.music?.filename;
              }
              return f.music?.musicId === fav.music?.musicId;
            })
          );
          
          // Save current user's favorites to user-specific storage
          const userKey = getUserStorageKey(user.userId);
          localStorage.setItem(userKey, JSON.stringify(uniqueUserFavorites));
          
          // Return all favorites (other users + current user)
          const allFavorites = [...otherUsersFavorites, ...uniqueUserFavorites];
          return allFavorites;
        });
      } else {
        console.error(`Error refreshing favorites: ${response.status} ${response.statusText}`);
      }
    } catch (error) {
      console.error('Error refreshing favorites:', error);
    }
  }, [user?.userId, getUserStorageKey]);

  const toggleFavorite = useCallback(async (musicId) => {
    if (!user?.userId) {
      console.warn('Cannot toggle favorite: No user logged in');
      return;
    }

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
        console.log(`Removing favorite: user=${user.userId}, music=${musicId}`);
        response = await fetch(`http://localhost:8080/api/favorites/user/${user.userId}/music/${encodeURIComponent(musicId)}`, {
          method: 'DELETE',
          credentials: 'include'
        });
      } else {
        // Add to favorites
        console.log(`Adding favorite: user=${user.userId}, music=${musicId}`);
        response = await fetch(`http://localhost:8080/api/favorites/user/${user.userId}/music/${encodeURIComponent(musicId)}`, {
          method: 'POST',
          credentials: 'include'
        });
      }

      if (response.ok) {
        // For Firebase music, update the UI state with user-specific tag
        if (isFirebaseMusic) {
          setFavorites(prevFavorites => {
            // Keep favorites from other users unchanged
            const otherUsersFavorites = prevFavorites.filter(fav => 
              fav.userId !== user.userId
            );
            
            // Get current user's favorites
            const currentUserFavorites = prevFavorites.filter(fav => 
              fav.userId === user.userId
            );
            
            // Check if the music is already in favorites for this user
            const isAlreadyFavorited = currentUserFavorites.some(fav => 
              fav.music?.filename === musicId
            );

            let updatedUserFavorites;
            if (isAlreadyFavorited) {
              // If already favorited, remove it
              updatedUserFavorites = currentUserFavorites.filter(fav => 
                !(fav.music?.filename === musicId)
              );
              console.log(`Removed Firebase favorite for user ${user.userId}`);
            } else {
              // If not favorited, add it with user ID tag
              updatedUserFavorites = [
                ...currentUserFavorites, 
                { 
                  music: { filename: musicId },
                  userId: user.userId 
                }
              ];
              console.log(`Added Firebase favorite for user ${user.userId}`);
            }
            
            // Save current user's favorites to user-specific storage only
            const userKey = getUserStorageKey(user.userId);
            localStorage.setItem(userKey, JSON.stringify(updatedUserFavorites));
            
            // Return complete favorites (other users + updated current user)
            return [...otherUsersFavorites, ...updatedUserFavorites];
          });
        } else {
          // For database music, refresh the favorites list
          await refreshFavorites();
        }
      } else {
        console.error(`Error toggling favorite: ${response.status} ${response.statusText}`);
      }
    } catch (error) {
      console.error('Error toggling favorite:', error);
    }
  }, [user?.userId, refreshFavorites, getUserStorageKey]);

  const isFavorite = useCallback((musicId) => {
    if (!user?.userId) return false;
    
    // Handle both Firebase and database music IDs
    const isFirebaseMusic = typeof musicId === 'string' && musicId.startsWith('firebase-');
    
    // First check user-specific storage for better reliability
    const userKey = getUserStorageKey(user.userId);
    const userFavorites = localStorage.getItem(userKey);
    if (userFavorites) {
      try {
        const parsedUserFavorites = JSON.parse(userFavorites);
        // Check in user's specific favorites first
        const found = parsedUserFavorites.some(fav => {
          if (isFirebaseMusic) {
            return fav.music?.filename === musicId;
          }
          return Number(fav.music?.musicId) === Number(musicId);
        });
        
        if (found) return true;
      } catch (e) {
        console.error('Error parsing user favorites:', e);
      }
    }
    
    // Fall back to checking the in-memory state
    return favorites.some(fav => {
      if (isFirebaseMusic) {
        return fav.music?.filename === musicId && fav.userId === user.userId;
      }
      return Number(fav.music?.musicId) === Number(musicId) && fav.userId === user.userId;
    });
  }, [favorites, user?.userId, getUserStorageKey]);

  // Filter favorites to only show those belonging to the current user
  const visibleFavorites = useMemo(() => {
    if (!user?.userId) return [];
    
    // Only return favorites that belong to the current user
    return favorites.filter(fav => fav.userId === user.userId);
  }, [favorites, user?.userId]);

  return (
    <FavoritesContext.Provider value={{ 
      favorites: visibleFavorites, 
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