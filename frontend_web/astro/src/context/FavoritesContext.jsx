import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { useUser } from './UserContext';

const FavoritesContext = createContext();

export const FavoritesProvider = ({ children }) => {
  const [favorites, setFavorites] = useState([]);
  const { user } = useUser();

  // Load favorites from localStorage when component mounts or user changes
  useEffect(() => {
    const loadFavorites = () => {
      const storedFavorites = localStorage.getItem('favorites');
      if (storedFavorites) {
        setFavorites(JSON.parse(storedFavorites));
      }
    };
    
    loadFavorites();
  }, [user?.userId]); // Only reload when user changes

  // Memoize refreshFavorites to prevent unnecessary re-renders
  const refreshFavorites = useCallback(() => {
    const storedFavorites = localStorage.getItem('favorites');
    if (storedFavorites) {
      setFavorites(JSON.parse(storedFavorites));
    }
  }, []); // Empty dependency array since it doesn't depend on any props or state

  const toggleFavorite = useCallback((musicId) => {
    setFavorites(prevFavorites => {
      // Check if the music is already in favorites
      const isFavorited = prevFavorites.includes(musicId);
      
      // Create new favorites array
      const newFavorites = isFavorited
        ? prevFavorites.filter(id => id !== musicId)
        : [...prevFavorites, musicId];

      // Update localStorage
      localStorage.setItem('favorites', JSON.stringify(newFavorites));
      
      return newFavorites;
    });
  }, []); // Empty dependency array since it doesn't depend on any props or state

  const isFavorite = useCallback((musicId) => {
    return favorites.includes(musicId);
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