package com.astroglow.controller;

import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

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

    // Get all favorites
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

    // Get favorites by user ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FavoritesEntity>> getFavoritesByUserId(@PathVariable("userId") String userId) {
        try {
            // First try to find user by OAuth ID
            UserEntity user = userService.findByOauthId(userId);
            if (user == null) {
                // If not found by OAuth ID, try numeric user ID
                try {
                    int numericUserId = Integer.parseInt(userId);
                    user = userService.findById(numericUserId);
                } catch (NumberFormatException e) {
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                }
            }
            
            List<FavoritesEntity> favorites = favoritesService.getFavoritesByUserId(user.getUserId());
            return new ResponseEntity<>(favorites, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
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
            // First try to find user by OAuth ID
            UserEntity user = userService.findByOauthId(userId);
            if (user == null) {
                // If not found by OAuth ID, try numeric user ID
                try {
                    int numericUserId = Integer.parseInt(userId);
                    user = userService.findById(numericUserId);
                } catch (NumberFormatException e) {
                    return new ResponseEntity<>("User not found with ID: " + userId, HttpStatus.NOT_FOUND);
                }
            }
            
            // Check if musicId is a Firebase ID
            if (musicId.startsWith("firebase-")) {
                // For Firebase music, we'll return a success response but not actually store it
                return new ResponseEntity<>("Firebase music cannot be favorited", HttpStatus.OK);
            }
            
            // For database music, try to parse as integer
            try {
                int numericMusicId = Integer.parseInt(musicId);
                FavoritesEntity favorite = favoritesService.addFavorite(user.getUserId(), numericMusicId);
                return new ResponseEntity<>(favorite, HttpStatus.CREATED);
            } catch (NumberFormatException e) {
                return new ResponseEntity<>("Invalid music ID: " + musicId, HttpStatus.BAD_REQUEST);
            } catch (EntityNotFoundException e) {
                return new ResponseEntity<>("Music not found with ID: " + musicId, HttpStatus.NOT_FOUND);
            } catch (IllegalStateException e) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
            }
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>("User not found with ID: " + userId, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
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
            // First try to find user by OAuth ID
            UserEntity user = userService.findByOauthId(userId);
            if (user == null) {
                // If not found by OAuth ID, try numeric user ID
                try {
                    int numericUserId = Integer.parseInt(userId);
                    user = userService.findById(numericUserId);
                } catch (NumberFormatException e) {
                    return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
                }
            }
            
            String message = favoritesService.removeFromFavorites(user.getUserId(), musicId);
            return new ResponseEntity<>(message, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    // Get all favorite music details for a user
    @GetMapping("/user/{userId}/music-details")
    public ResponseEntity<?> getFavoritesMusicByUserId(@PathVariable String userId) {
        try {
            // First try to find user by OAuth ID
            UserEntity user = userService.findByOauthId(userId);
            if (user == null) {
                // If not found by OAuth ID, try numeric user ID
                try {
                    int numericUserId = Integer.parseInt(userId);
                    user = userService.findById(numericUserId);
                } catch (NumberFormatException e) {
                    return ResponseEntity.notFound().build();
                }
            }
            
            List<FavoritesEntity> favorites = favoritesService.getFavoritesByUserId(user.getUserId());

            if (favorites.isEmpty()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("[]");
            }

            // Create a list to hold the music details
            List<Map<String, Object>> musicDetails = new ArrayList<>();

            // Process each favorite and extract music details
            for (FavoritesEntity favorite : favorites) {
                MusicEntity music = favorite.getMusic();
                if (music != null) {
                    Map<String, Object> details = new HashMap<>();
                    details.put("musicId", music.getMusicId());
                    details.put("title", music.getTitle());
                    details.put("artist", music.getArtist());
                    details.put("genre", music.getGenre());
                    details.put("time", music.getTime());
                    details.put("audioUrl", music.getAudioUrl());
                    details.put("favoriteId", favorite.getFavoriteId());
                    details.put("createdAt", favorite.getCreatedAt());
                    musicDetails.add(details);
                }
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(musicDetails);

        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace(); // Log the error for debugging
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}