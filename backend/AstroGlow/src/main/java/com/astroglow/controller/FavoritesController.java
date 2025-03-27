package com.astroglow.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.astroglow.Entity.FavoritesEntity;
import com.astroglow.Service.FavoritesService;

import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/api/favorites")
public class FavoritesController {

    @Autowired
    private FavoritesService favoritesService;

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
    public ResponseEntity<List<FavoritesEntity>> getFavoritesByUserId(@PathVariable("userId") int userId) {
        try {
            List<FavoritesEntity> favorites = favoritesService.getFavoritesByUserId(userId);
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

    // Add a favorite (using path variables)
    @PostMapping("/user/{userId}/music/{musicId}")
    public ResponseEntity<?> addFavorite(@PathVariable("userId") int userId, @PathVariable("musicId") int musicId) {
        try {
            FavoritesEntity favorite = favoritesService.addFavorite(userId, musicId);
            return new ResponseEntity<>(favorite, HttpStatus.CREATED);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
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
    public ResponseEntity<?> removeFromFavorites(@PathVariable("userId") int userId, @PathVariable("musicId") int musicId) {
        try {
            String message = favoritesService.removeFromFavorites(userId, musicId);
            return new ResponseEntity<>(message, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    // Check if a music is in a user's favorites
    @GetMapping("/user/{userId}/music/{musicId}/check")
    public ResponseEntity<Boolean> isFavorite(@PathVariable("userId") int userId, @PathVariable("musicId") int musicId) {
        try {
            boolean isFavorite = favoritesService.isFavorite(userId, musicId);
            return new ResponseEntity<>(isFavorite, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}