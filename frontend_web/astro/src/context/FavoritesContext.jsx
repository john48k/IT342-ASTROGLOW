import React, { createContext, useContext, useState, useEffect } from 'react';
import { useUser } from './UserContext';

const FavoritesContext = createContext(null);

export const FavoritesProvider = ({ children }) => {
  const [favorites, setFavorites] = useState([]);
  const [favoriteObjects, setFavoriteObjects] = useState([]); // Store the full favorite objects
  const { user, isAuthenticated } = useUser();

  // Load favorites when user changes or on initial load
  useEffect(() => {
    // Try to load from localStorage first for immediate display
    const storedFavorites = localStorage.getItem('favorites');
    const storedFavoriteObjects = localStorage.getItem('favoriteObjects');
    
    if (storedFavorites) {
      try {
        setFavorites(JSON.parse(storedFavorites));
      } catch (error) {
        console.error('Error parsing stored favorites:', error);
      }
    }
    
    if (storedFavoriteObjects) {
      try {
        setFavoriteObjects(JSON.parse(storedFavoriteObjects));
      } catch (error) {
        console.error('Error parsing stored favorite objects:', error);
      }
    }

    // If user is logged in, fetch from server and override local
    if (isAuthenticated && user && user.userId) {
      fetchFavoritesFromServer();
    }
  }, [user, isAuthenticated]);

  const fetchFavoritesFromServer = async () => {
    if (!isAuthenticated || !user || !user.userId) {
      console.log('Not authenticated or missing user ID, skipping server fetch');
      return;
    }
    
    try {
      // Use the user-specific endpoint directly for reliability
      const response = await fetch(`http://localhost:8080/api/favorites/user/${user.userId}`);
      
      if (response.ok) {
        const data = await response.json();
        console.log('Received favorites data:', data);
        
        // Handle case where API returns array of favorite objects
        if (Array.isArray(data) && data.length > 0) {
          // Store the complete favorite objects
          setFavoriteObjects(data);
          localStorage.setItem('favoriteObjects', JSON.stringify(data));
          
          // Get all favoriteIds and musicIds
          const favoriteIds = data.map(fav => fav.favoriteId).filter(Boolean);
          
          // Get the music IDs if they exist (backwards compatibility)
          let musicIds = [];
          try {
            // Extract music IDs if they exist in the response
            musicIds = data.map(fav => fav.music && fav.music.musicId).filter(Boolean);
          } catch (err) {
            console.log('No music objects in favorites response, using favoriteIds only');
          }
          
          // Combine both sets of IDs without duplicates
          const combinedIds = [...new Set([...favoriteIds, ...musicIds])];
          
          setFavorites(combinedIds);
          // Also update localStorage
          localStorage.setItem('favorites', JSON.stringify(combinedIds));
          console.log(`Loaded ${combinedIds.length} favorites for user ${user.userId}`);
        } else if (response.status === 200 && (!data || data.length === 0)) {
          // API returned success but empty data - clear favorites
          setFavorites([]);
          setFavoriteObjects([]);
          localStorage.setItem('favorites', JSON.stringify([]));
          localStorage.setItem('favoriteObjects', JSON.stringify([]));
          console.log(`No favorites found for user ${user.userId}`);
        }
      } else if (response.status === 404) {
        // Handle case where user has no favorites yet
        setFavorites([]);
        setFavoriteObjects([]);
        localStorage.setItem('favorites', JSON.stringify([]));
        localStorage.setItem('favoriteObjects', JSON.stringify([]));
        console.log(`No favorites found for user ${user.userId}`);
      }
    } catch (error) {
      console.error('Error fetching favorites:', error);
      // Keep using localStorage favorites on error
    }
  };

  const toggleFavorite = async (musicId) => {
    let newFavorites;
    
    if (favorites.includes(musicId)) {
      // Find the favoriteId if it exists in our favoriteObjects
      const favoriteObject = favoriteObjects.find(fav => 
        (fav.music && fav.music.musicId === musicId) || fav.favoriteId === musicId
      );
      
      // Remove from favorites
      newFavorites = favorites.filter(id => id !== musicId);
      const newFavoriteObjects = favoriteObjects.filter(fav => 
        !((fav.music && fav.music.musicId === musicId) || fav.favoriteId === musicId)
      );
      
      // If user is logged in, also update on server
      if (isAuthenticated && user && user.userId) {
        try {
          // Use the DELETE method to remove from favorites
          await fetch(`http://localhost:8080/api/favorites/user/${user.userId}/music/${musicId}`, {
            method: 'DELETE'
          });
          console.log(`Removed music ${musicId} from favorites for user ${user.userId}`);
          
          // Update local state and storage
          setFavorites(newFavorites);
          setFavoriteObjects(newFavoriteObjects);
          localStorage.setItem('favorites', JSON.stringify(newFavorites));
          localStorage.setItem('favoriteObjects', JSON.stringify(newFavoriteObjects));
          
          // Refresh from server to ensure we're in sync
          fetchFavoritesFromServer();
        } catch (error) {
          console.error('Error removing from favorites on server:', error);
        }
      }
    } else {
      // Add to favorites
      newFavorites = [...favorites, musicId];
      
      // If user is logged in, also update on server
      if (isAuthenticated && user && user.userId) {
        try {
          // Use the POST method to add to favorites
          const response = await fetch(`http://localhost:8080/api/favorites/user/${user.userId}/music/${musicId}`, {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json'
            }
          });
          
          // Get the new favorite object from response if available
          if (response.ok) {
            try {
              const newFavoriteData = await response.json();
              console.log('New favorite created:', newFavoriteData);
              
              // Add the new favorite to our objects list
              const updatedFavoriteObjects = [...favoriteObjects, newFavoriteData];
              setFavoriteObjects(updatedFavoriteObjects);
              localStorage.setItem('favoriteObjects', JSON.stringify(updatedFavoriteObjects));
            } catch (e) {
              console.log('No response data from add favorite, will refresh from server');
            }
          }
          
          console.log(`Added music ${musicId} to favorites for user ${user.userId}`);
          
          // Update local state
          setFavorites(newFavorites);
          localStorage.setItem('favorites', JSON.stringify(newFavorites));
          
          // Refresh from server to ensure we're in sync
          fetchFavoritesFromServer();
        } catch (error) {
          console.error('Error adding to favorites on server:', error);
        }
      }
    }
    
    return newFavorites;
  };

  // Function to check if a music is favorited, accommodating both favoriteId and musicId
  const isFavorite = (musicId) => {
    // Check if it's in our simple favorites array
    if (favorites.includes(musicId)) return true;
    
    // Or check if it's in the objects array
    return favoriteObjects.some(fav => fav.music && fav.music.musicId === musicId);
  };

  return (
    <FavoritesContext.Provider value={{ 
      favorites, 
      favoriteObjects,
      toggleFavorite, 
      fetchFavoritesFromServer,
      isFavorite
    }}>
      {children}
    </FavoritesContext.Provider>
  );
};

export const useFavorites = () => {
  const context = useContext(FavoritesContext);
  if (!context) {
    throw new Error('useFavorites must be used within a FavoritesProvider');
  }
  return context;
};

export default FavoritesContext; 