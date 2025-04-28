package com.astroglow.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.astroglow.entity.FavoritesEntity;
import com.astroglow.entity.MusicEntity;
import com.astroglow.entity.UserEntity;
import com.astroglow.repository.FavoritesRepository;
import com.astroglow.repository.MusicRepository;
import com.astroglow.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class FavoritesService {

    @Autowired
    private FavoritesRepository favoritesRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private MusicRepository musicRepo;

    public FavoritesService() {
        super();
    }

    // Get all favorites
    public List<FavoritesEntity> getAllFavorites() {
        return favoritesRepo.findAll();
    }

    // Get favorite by ID
    public FavoritesEntity getFavoriteById(int id) {
        return favoritesRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Favorite with ID " + id + " not found"));
    }

    // Get favorites by user ID - Ensure this method properly filters by user
    public List<FavoritesEntity> getFavoritesByUserId(int userId) {
        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with ID " + userId + " not found"));
        
        // This query ensures we only get favorites for the specified user
        return favoritesRepo.findByUser(user);
    }

    // Get favorites by music ID
    public List<FavoritesEntity> getFavoritesByMusicId(int musicId) {
        MusicEntity music = musicRepo.findById(musicId)
                .orElseThrow(() -> new EntityNotFoundException("Music with ID " + musicId + " not found"));
        return favoritesRepo.findByMusic(music);
    }

    // Add a favorite - Make sure we check this is for the specific user
    @Transactional
    public FavoritesEntity addFavorite(int userId, int musicId) {
        // Check if the user exists
        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with ID " + userId + " not found"));

        // Check if the music exists
        MusicEntity music = musicRepo.findById(musicId)
                .orElseThrow(() -> new EntityNotFoundException("Music with ID " + musicId + " not found"));

        // Check if the favorite already exists for THIS USER
        if (favoritesRepo.existsByUserAndMusic(user, music)) {
            throw new IllegalStateException("This music is already in the user's favorites");
        }

        // Create a new favorite
        FavoritesEntity favorite = new FavoritesEntity();
        favorite.setUser(user);
        favorite.setMusic(music);

        return favoritesRepo.save(favorite);
    }

    // Add a favorite (alternative method using entity)
    @Transactional
    public FavoritesEntity addFavorite(FavoritesEntity favorite) {
        // Check if the user exists
        UserEntity user = userRepo.findById(favorite.getUser().getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User with ID " + favorite.getUser().getUserId() + " not found"));

        // Check if the music exists
        MusicEntity music = musicRepo.findById(favorite.getMusic().getMusicId())
                .orElseThrow(() -> new EntityNotFoundException("Music with ID " + favorite.getMusic().getMusicId() + " not found"));

        // Check if the favorite already exists for THIS USER
        if (favoritesRepo.existsByUserAndMusic(user, music)) {
            throw new IllegalStateException("This music is already in the user's favorites");
        }

        // Set the proper entities (to ensure we have the complete entity, not just ID)
        favorite.setUser(user);
        favorite.setMusic(music);

        return favoritesRepo.save(favorite);
    }

    // Update a favorite
    @Transactional
    public FavoritesEntity updateFavorite(int favoriteId, FavoritesEntity newFavoriteDetails) {
        FavoritesEntity favorite;

        try {
            favorite = favoritesRepo.findById(favoriteId).get();

            // Check if the user exists
            if (newFavoriteDetails.getUser() != null) {
                UserEntity user = userRepo.findById(newFavoriteDetails.getUser().getUserId())
                        .orElseThrow(() -> new EntityNotFoundException("User with ID " + newFavoriteDetails.getUser().getUserId() + " not found"));
                favorite.setUser(user);
            }

            // Check if the music exists
            if (newFavoriteDetails.getMusic() != null) {
                MusicEntity music = musicRepo.findById(newFavoriteDetails.getMusic().getMusicId())
                        .orElseThrow(() -> new EntityNotFoundException("Music with ID " + newFavoriteDetails.getMusic().getMusicId() + " not found"));
                favorite.setMusic(music);
            }

        } catch (NoSuchElementException nex) {
            throw new EntityNotFoundException("Favorite with ID " + favoriteId + " not found");
        }

        return favoritesRepo.save(favorite);
    }

    // Delete a favorite
    @Transactional
    public String deleteFavorite(int favoriteId) {
        if (favoritesRepo.existsById(favoriteId)) {
            favoritesRepo.deleteById(favoriteId);
            return "Favorite successfully removed";
        } else {
            return "Favorite with ID " + favoriteId + " not found";
        }
    }

    // Remove a music from a user's favorites
    @Transactional
    public String removeFromFavorites(int userId, int musicId) {
        // Check if the user exists
        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with ID " + userId + " not found"));

        // Check if the music exists
        MusicEntity music = musicRepo.findById(musicId)
                .orElseThrow(() -> new EntityNotFoundException("Music with ID " + musicId + " not found"));

        // Find the favorite for THIS USER
        FavoritesEntity favorite = favoritesRepo.findByUserAndMusic(user, music)
                .orElseThrow(() -> new EntityNotFoundException("This music is not in the user's favorites"));

        // Delete the favorite
        favoritesRepo.deleteById(favorite.getFavoriteId());

        return "Music successfully removed from favorites";
    }

    // Check if a music is in a user's favorites
    public boolean isFavorite(int userId, int musicId) {
        // Check if the user exists
        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with ID " + userId + " not found"));

        // Check if the music exists
        MusicEntity music = musicRepo.findById(musicId)
                .orElseThrow(() -> new EntityNotFoundException("Music with ID " + musicId + " not found"));

        // Check if this user has favorited this music
        return favoritesRepo.existsByUserAndMusic(user, music);
    }
}