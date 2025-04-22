import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { useUser } from './UserContext';

const FavoritesContext = createContext();

export const FavoritesProvider = ({ children }) => {
  const { user } = useUser();
  // Initialize from localStorage before any API calls
  const [favorites, setFavorites] = useState(() => {
    // Initialize from localStorage if available
    const savedFavorites = localStorage.getItem('firebaseFavorites');
    return savedFavorites ? JSON.parse(savedFavorites) : [];
  });
  
  // Save to localStorage whenever favorites change
  useEffect(() => {
    if (favorites && favorites.length > 0) {
      console.log(`Saving ${favorites.length} favorites to localStorage`);
      localStorage.setItem('firebaseFavorites', JSON.stringify(favorites));
    } else if (favorites && favorites.length === 0) {
      // Don't clear localStorage if there are no favorites in state
      // This could happen during initial loading before backend data arrives
      const savedFavorites = localStorage.getItem('firebaseFavorites');
      if (!savedFavorites || JSON.parse(savedFavorites).length === 0) {
        localStorage.setItem('firebaseFavorites', JSON.stringify([]));
      }
    }
  }, [favorites]);

  // Load favorites from backend when component mounts or user changes
  useEffect(() => {
    const loadFavorites = async () => {
      // If we have a user, load favorites from backend and local storage
      if (user?.userId) {
        try {
          console.log(`Loading favorites for user ID: ${user.userId}`);
          
          // Get existing localStorage favorites first to retain during loading
          const savedFavorites = localStorage.getItem('firebaseFavorites');
          const parsedSavedFavorites = savedFavorites ? JSON.parse(savedFavorites) : [];
          
          // Filter to only keep this user's favorites
          const userFavorites = parsedSavedFavorites.filter(fav => 
            fav.userId === user.userId
          );
          
          // Set these immediately to have something to show
          if (userFavorites.length > 0) {
            console.log(`Setting ${userFavorites.length} favorites from localStorage while backend loads`);
            setFavorites(userFavorites);
          }
          
          // Then fetch from backend
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
            
            // Only update database favorites, keep Firebase favorites
            // This ensures we maintain user-specific favorites
            setFavorites(prevFavorites => {
              // Get Firebase favorites specific to this user
              const firebaseFavorites = [
                ...prevFavorites.filter(fav => 
                  typeof fav.music?.filename === 'string' && 
                  fav.music.filename.startsWith('firebase-') &&
                  fav.userId === user.userId
                ),
                ...userFavorites.filter(fav =>
                  typeof fav.music?.filename === 'string' && 
                  fav.music.filename.startsWith('firebase-')
                )
              ];
              
              // Remove duplicates
              const uniqueFirebaseFavorites = firebaseFavorites.filter((fav, index, self) =>
                index === self.findIndex((f) => 
                  f.music?.filename === fav.music?.filename && f.userId === fav.userId
                )
              );
              
              // Tag the database favorites with the user ID for future reference
              const taggedDbFavorites = data.map(item => ({
                ...item,
                userId: user.userId
              }));
              
              const combinedFavorites = [...taggedDbFavorites, ...uniqueFirebaseFavorites];
              console.log(`Combined ${taggedDbFavorites.length} DB and ${uniqueFirebaseFavorites.length} Firebase favorites`);
              
              // Save the combined list to localStorage immediately
              localStorage.setItem('firebaseFavorites', JSON.stringify(combinedFavorites));
              
              return combinedFavorites;
            });
          } else {
            console.error(`Error loading favorites: ${response.status} ${response.statusText}`);
          }
        } catch (error) {
          console.error('Error loading favorites:', error);
          
          // If API call fails, still load from localStorage
          const savedFavorites = localStorage.getItem('firebaseFavorites');
          if (savedFavorites) {
            const parsedFavorites = JSON.parse(savedFavorites);
            const userFavorites = parsedFavorites.filter(fav => fav.userId === user.userId);
            if (userFavorites.length > 0) {
              console.log(`Falling back to ${userFavorites.length} localStorage favorites due to API error`);
              setFavorites(userFavorites);
            }
          }
        }
      } else {
        // If no user is logged in, keep the favorites in localStorage but clear the state
        setFavorites([]);
      }
    };
    
    loadFavorites();
  }, [user?.userId]);

  // Memoize refreshFavorites to prevent unnecessary re-renders
  const refreshFavorites = useCallback(async () => {
    if (user?.userId) {
      try {
        console.log(`Refreshing favorites for user ID: ${user.userId}`);
        
        // First, ensure we have any existing localStorage favorites
        const savedFavorites = localStorage.getItem('firebaseFavorites');
        const parsedSavedFavorites = savedFavorites ? JSON.parse(savedFavorites) : [];
        
        // Get only this user's firebase favorites
        const userFirebaseFavorites = parsedSavedFavorites.filter(fav => 
          typeof fav.music?.filename === 'string' && 
          fav.music.filename.startsWith('firebase-') &&
          fav.userId === user.userId
        );
        
        // Then get backend data
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
          
          // Tag database favorites with user ID
          const taggedDbFavorites = data.map(item => ({
            ...item,
            userId: user.userId
          }));
          
          // Combine both sources with any existing state
          setFavorites(prevFavorites => {
            // Also get any Firebase favorites from current state
            const stateFirebaseFavorites = prevFavorites.filter(fav => 
              typeof fav.music?.filename === 'string' && 
              fav.music.filename.startsWith('firebase-') &&
              fav.userId === user.userId
            );
            
            // Combine all Firebase favorites sources
            const allFirebaseFavorites = [
              ...stateFirebaseFavorites,
              ...userFirebaseFavorites
            ];
            
            // Remove duplicates
            const uniqueFirebaseFavorites = allFirebaseFavorites.filter((fav, index, self) =>
              index === self.findIndex((f) => 
                f.music?.filename === fav.music?.filename && f.userId === fav.userId
              )
            );
            
            // Create the complete list
            const combinedFavorites = [...taggedDbFavorites, ...uniqueFirebaseFavorites];
            
            // Save immediately to localStorage
            localStorage.setItem('firebaseFavorites', JSON.stringify(combinedFavorites));
            
            return combinedFavorites;
          });
        } else {
          console.error(`Error refreshing favorites: ${response.status} ${response.statusText}`);
          
          // If API fails, at least ensure we have localStorage favorites in state
          if (userFirebaseFavorites.length > 0) {
            setFavorites(prevFavorites => {
              // Keep any existing DB favorites in state
              const dbFavorites = prevFavorites.filter(fav => 
                !fav.music?.filename || !fav.music.filename.startsWith('firebase-')
              );
              return [...dbFavorites, ...userFirebaseFavorites];
            });
          }
        }
      } catch (error) {
        console.error('Error refreshing favorites:', error);
        
        // On error, make sure we have localStorage favorites
        const savedFavorites = localStorage.getItem('firebaseFavorites');
        if (savedFavorites) {
          const parsedFavorites = JSON.parse(savedFavorites);
          const userFavorites = parsedFavorites.filter(fav => fav.userId === user.userId);
          if (userFavorites.length > 0) {
            console.log(`Using ${userFavorites.length} localStorage favorites on refresh error`);
            setFavorites(prevFavorites => {
              // Keep any non-Firebase favorites
              const nonFirebaseFavs = prevFavorites.filter(fav => 
                !fav.music?.filename || !fav.music.filename.startsWith('firebase-')
              );
              return [...nonFirebaseFavs, ...userFavorites];
            });
          }
        }
      }
    }
  }, [user?.userId]);

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
            // Check if the music is already in favorites
            const isAlreadyFavorited = prevFavorites.some(fav => 
              fav.music?.filename === musicId && fav.userId === user.userId
            );

            if (isAlreadyFavorited) {
              // If already favorited, remove it
              return prevFavorites.filter(fav => !(fav.music?.filename === musicId && fav.userId === user.userId));
            } else {
              // If not favorited, add it with user ID tag
              return [...prevFavorites, { 
                music: { filename: musicId },
                userId: user.userId 
              }];
            }
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
  }, [user?.userId, refreshFavorites]);

  const isFavorite = useCallback((musicId) => {
    if (!user?.userId) return false;
    
    // Handle both Firebase and database music IDs
    const isFirebaseMusic = typeof musicId === 'string' && musicId.startsWith('firebase-');
    
    // First check in the current state
    const isInState = favorites.some(fav => {
      if (isFirebaseMusic) {
        return fav.music?.filename === musicId && fav.userId === user.userId;
      }
      return Number(fav.music?.musicId) === Number(musicId) && fav.userId === user.userId;
    });
    
    if (isInState) return true;
    
    // If not in state, check localStorage as fallback
    try {
      const savedFavorites = localStorage.getItem('firebaseFavorites');
      if (savedFavorites) {
        const parsedFavorites = JSON.parse(savedFavorites);
        const isInLocalStorage = parsedFavorites.some(fav => {
          if (isFirebaseMusic) {
            return fav.music?.filename === musicId && fav.userId === user.userId;
          }
          return fav.music?.musicId === musicId && fav.userId === user.userId;
        });
        return isInLocalStorage;
      }
    } catch (error) {
      console.error('Error checking localStorage for favorites:', error);
    }
    
    return false;
  }, [favorites, user?.userId]);

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