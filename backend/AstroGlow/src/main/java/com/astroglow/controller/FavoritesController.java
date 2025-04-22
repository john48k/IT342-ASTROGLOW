package com.astroglow.controller;

import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.astroglow.Entity.FavoritesEntity;
import com.astroglow.Entity.MusicEntity;
import com.astroglow.Service.FavoritesService;
import com.astroglow.Entity.UserEntity;
import com.astroglow.Service.UserService;

import jakarta.persistence.EntityNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/favorites")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:5174"}, allowCredentials = "true")
public class FavoritesController {
    private static final Logger logger = Logger.getLogger(FavoritesController.class.getName());

    @Autowired
    private FavoritesService favoritesService;

    @Autowired
    private UserService userService;

    // Get music details for a favorite
    @GetMapping("/{favoriteId}/music")
    public ResponseEntity<MusicEntity> getMusicForFavorite(@PathVariable("favoriteId") int favoriteId) {
        try {
            FavoritesEntity favorite = favoritesService.getFavoriteById(favoriteId);
            if (favorite == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            MusicEntity music = favorite.getMusic();
            if (music == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(music, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Get all favorites - Admin only, not for regular users
    @GetMapping("/getAllFavorites")
    public ResponseEntity<List<FavoritesEntity>> getAllFavorites() {
        List<FavoritesEntity> favorites = favoritesService.getAllFavorites();
        return new ResponseEntity<>(favorites, HttpStatus.OK);
    }

    // Get favorite by ID
    @GetMapping("/{id}")
    public ResponseEntity<FavoritesEntity> getFavoriteById(@PathVariable("id") int id) {
        try {
            FavoritesEntity favorite = favoritesService.getFavoriteById(id);
            return new ResponseEntity<>(favorite, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Get favorites by user ID - Ensure this is working correctly
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FavoritesEntity>> getFavoritesByUserId(@PathVariable("userId") String userId) {
        try {
            // Log the request
            logger.info("Fetching favorites for user ID: " + userId);
            
            // First try to find user by OAuth ID
            UserEntity user = userService.findByOauthId(userId);
            if (user == null) {
                // If not found by OAuth ID, try numeric user ID
                try {
                    int numericUserId = Integer.parseInt(userId);
                    user = userService.findById(numericUserId);
                } catch (NumberFormatException e) {
                    logger.warning("User not found with ID: " + userId);
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                }
            }
            
            logger.info("Found user: " + user.getUserName() + " (ID: " + user.getUserId() + ")");
            
            // This will get only favorites specific to this user
            List<FavoritesEntity> favorites = favoritesService.getFavoritesByUserId(user.getUserId());
            
            logger.info("Found " + favorites.size() + " favorites for user ID: " + user.getUserId());
            
            return new ResponseEntity<>(favorites, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            logger.warning("Error fetching favorites: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Get favorites by music ID
    @GetMapping("/music/{musicId}")
    public ResponseEntity<List<FavoritesEntity>> getFavoritesByMusicId(@PathVariable("musicId") int musicId) {
        try {
            List<FavoritesEntity> favorites = favoritesService.getFavoritesByMusicId(musicId);
            return new ResponseEntity<>(favorites, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Check if a music is in a user's favorites
    @GetMapping("/user/{userId}/music/{musicId}/check")
    public ResponseEntity<Boolean> isFavorite(@PathVariable("userId") String userId, @PathVariable("musicId") String musicId) {
        try {
            // First try to find user by OAuth ID
            UserEntity user = userService.findByOauthId(userId);
            if (user == null) {
                // If not found by OAuth ID, try numeric user ID
                try {
                    int numericUserId = Integer.parseInt(userId);
                    user = userService.findById(numericUserId);
                } catch (NumberFormatException e) {
                    return new ResponseEntity<>(false, HttpStatus.OK);
                }
            }
            
            // Check if musicId is a Firebase ID
            if (musicId.startsWith("firebase-")) {
                // For Firebase music, we'll consider it not favorited since we don't store these in the database
                return new ResponseEntity<>(false, HttpStatus.OK);
            }
            
            // For database music, try to parse as integer
            try {
                int numericMusicId = Integer.parseInt(musicId);
                boolean isFavorite = favoritesService.isFavorite(user.getUserId(), numericMusicId);
                return new ResponseEntity<>(isFavorite, HttpStatus.OK);
            } catch (NumberFormatException e) {
                return new ResponseEntity<>(false, HttpStatus.OK);
            }
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(false, HttpStatus.OK);
        }
    }

    // Add a favorite (using path variables)
    @PostMapping("/user/{userId}/music/{musicId}")
    public ResponseEntity<?> addFavorite(@PathVariable("userId") String userId, @PathVariable("musicId") String musicId) {
        try {
            logger.info("Adding favorite for user ID: " + userId + ", music ID: " + musicId);
            
            // First try to find user by OAuth ID
            UserEntity user = userService.findByOauthId(userId);
            if (user == null) {
                // If not found by OAuth ID, try numeric user ID
                try {
                    int numericUserId = Integer.parseInt(userId);
                    user = userService.findById(numericUserId);
                } catch (NumberFormatException e) {
                    logger.warning("User not found with ID: " + userId);
                    return new ResponseEntity<>("User not found with ID: " + userId, HttpStatus.NOT_FOUND);
                }
            }
            
            logger.info("Found user: " + user.getUserName() + " (ID: " + user.getUserId() + ")");
            
            // Check if musicId is a Firebase ID
            if (musicId.startsWith("firebase-")) {
                // For Firebase music, we'll return a success response but not actually store it
                return new ResponseEntity<>("Firebase music cannot be favorited", HttpStatus.OK);
            }
            
            // For database music, try to parse as integer
            try {
                int numericMusicId = Integer.parseInt(musicId);
                FavoritesEntity favorite = favoritesService.addFavorite(user.getUserId(), numericMusicId);
                
                logger.info("Successfully added favorite with ID: " + favorite.getFavoriteId());
                
                return new ResponseEntity<>(favorite, HttpStatus.CREATED);
            } catch (NumberFormatException e) {
                logger.warning("Invalid music ID: " + musicId);
                return new ResponseEntity<>("Invalid music ID: " + musicId, HttpStatus.BAD_REQUEST);
            } catch (EntityNotFoundException e) {
                logger.warning("Music not found with ID: " + musicId);
                return new ResponseEntity<>("Music not found with ID: " + musicId, HttpStatus.NOT_FOUND);
            } catch (IllegalStateException e) {
                logger.warning("Error adding favorite: " + e.getMessage());
                return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
            }
        } catch (EntityNotFoundException e) {
            logger.warning("User not found with ID: " + userId);
            return new ResponseEntity<>("User not found with ID: " + userId, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.severe("Error adding favorite: " + e.getMessage());
            return new ResponseEntity<>("An error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Add a favorite (using request body)
    @PostMapping("/postFavorites")
    public ResponseEntity<?> addFavoriteEntity(@RequestBody FavoritesEntity favorite) {
        try {
            FavoritesEntity savedFavorite = favoritesService.addFavorite(favorite);
            return new ResponseEntity<>(savedFavorite, HttpStatus.CREATED);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    // Update a favorite
    @PutMapping("/putFavorites/{id}")
    public ResponseEntity<?> updateFavorite(@PathVariable("id") int id, @RequestBody FavoritesEntity favorite) {
        try {
            FavoritesEntity updatedFavorite = favoritesService.updateFavorite(id, favorite);
            return new ResponseEntity<>(updatedFavorite, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    // Delete a favorite
    @DeleteMapping("/deleteFavorites/{id}")
    public ResponseEntity<String> deleteFavorite(@PathVariable("id") int id) {
        String message = favoritesService.deleteFavorite(id);
        if (message.contains("not found")) {
            return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    // Remove a music from a user's favorites
    @DeleteMapping("/user/{userId}/music/{musicId}")
    public ResponseEntity<?> removeFromFavorites(@PathVariable("userId") String userId, @PathVariable("musicId") int musicId) {
        try {
            logger.info("Removing favorite for user ID: " + userId + ", music ID: " + musicId);
            
            // First try to find user by OAuth ID
            UserEntity user = userService.findByOauthId(userId);
            if (user == null) {
                // If not found by OAuth ID, try numeric user ID
                try {
                    int numericUserId = Integer.parseInt(userId);
                    user = userService.findById(numericUserId);
                } catch (NumberFormatException e) {
                    logger.warning("User not found with ID: " + userId);
                    return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
                }
            }
            
            logger.info("Found user: " + user.getUserName() + " (ID: " + user.getUserId() + ")");
            
            String message = favoritesService.removeFromFavorites(user.getUserId(), musicId);
            
            logger.info("Successfully removed favorite: " + message);
            
            return new ResponseEntity<>(message, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            logger.warning("Error removing favorite: " + e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    // Get all favorite music details for a user
    @GetMapping("/user/{userId}/music-details")
    public ResponseEntity<?> getFavoritesMusicByUserId(@PathVariable String userId) {
        try {
            // Log the request
            logger.info("Fetching favorite music details for user ID: " + userId);
            
            // First try to find user by OAuth ID
            UserEntity user = userService.findByOauthId(userId);
            if (user == null) {
                // If not found by OAuth ID, try numeric user ID
                try {
                    int numericUserId = Integer.parseInt(userId);
                    user = userService.findById(numericUserId);
                } catch (NumberFormatException e) {
                    logger.warning("User not found with ID: " + userId);
                    return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
                }
            }
            
            logger.info("Found user: " + user.getUserName() + " (ID: " + user.getUserId() + ")");
            
            // Get favorites for the specific user
            List<FavoritesEntity> favorites = favoritesService.getFavoritesByUserId(user.getUserId());
            
            logger.info("Found " + favorites.size() + " favorites for user ID: " + user.getUserId());
            
            // Transform to just return the music objects with an ID
            List<Map<String, Object>> musicList = favorites.stream().map(favorite -> {
                MusicEntity music = favorite.getMusic();
                if (music != null) {
                    Map<String, Object> musicMap = new HashMap<>();
                    musicMap.put("musicId", music.getMusicId());
                    musicMap.put("title", music.getTitle());
                    musicMap.put("artist", music.getArtist());
                    musicMap.put("genre", music.getGenre());
                    musicMap.put("releaseYear", music.getTime());
                    // Favorite ID is useful for reference
                    musicMap.put("favoriteId", favorite.getFavoriteId());
                    // Don't include binary data here to keep response size small
                    return musicMap;
                }
                return null;
            }).filter(m -> m != null).collect(Collectors.toList());
            
            logger.info("Returning " + musicList.size() + " music items for user ID: " + user.getUserId());
            
            return new ResponseEntity<>(musicList, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            logger.warning("Error fetching favorite music details: " + e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.severe("Unexpected error fetching favorite music details: " + e.getMessage());
            return new ResponseEntity<>("An error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}