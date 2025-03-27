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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.astroglow.Entity.OfflineLibraryEntity;
import com.astroglow.Service.OfflineLibraryService;

import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/api/offline-library")
public class OfflineLibraryController {

    @Autowired
    private OfflineLibraryService offlineLibraryService;

    // Get all offline library entries
    @GetMapping
    public ResponseEntity<List<OfflineLibraryEntity>> getAllOfflineLibraries() {
        List<OfflineLibraryEntity> offlineLibraries = offlineLibraryService.getAllOfflineLibraries();
        return new ResponseEntity<>(offlineLibraries, HttpStatus.OK);
    }

    // Get offline library entry by ID
    @GetMapping("/{id}")
    public ResponseEntity<OfflineLibraryEntity> getOfflineLibraryById(@PathVariable("id") int id) {
        try {
            OfflineLibraryEntity offlineLibrary = offlineLibraryService.getOfflineLibraryById(id);
            return new ResponseEntity<>(offlineLibrary, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Get offline library entries by user ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OfflineLibraryEntity>> getOfflineLibrariesByUserId(@PathVariable("userId") int userId) {
        try {
            List<OfflineLibraryEntity> offlineLibraries = offlineLibraryService.getOfflineLibrariesByUserId(userId);
            return new ResponseEntity<>(offlineLibraries, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Get offline library entries containing a specific music
    @GetMapping("/music/{musicId}")
    public ResponseEntity<List<OfflineLibraryEntity>> getOfflineLibrariesByMusicId(@PathVariable("musicId") int musicId) {
        try {
            List<OfflineLibraryEntity> offlineLibraries = offlineLibraryService.getOfflineLibrariesByMusicId(musicId);
            return new ResponseEntity<>(offlineLibraries, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Add a music to a user's offline library
    @PostMapping("/user/{userId}/music/{musicId}")
    public ResponseEntity<?> addToOfflineLibrary(
            @PathVariable("userId") int userId,
            @PathVariable("musicId") int musicId,
            @RequestParam("filePath") String filePath) {
        try {
            OfflineLibraryEntity offlineLibrary = offlineLibraryService.addToOfflineLibrary(userId, musicId, filePath);
            return new ResponseEntity<>(offlineLibrary, HttpStatus.CREATED);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // Add an offline library entry (using request body)
    @PostMapping
    public ResponseEntity<?> addOfflineLibraryEntry(@RequestBody OfflineLibraryEntity offlineLibrary) {
        try {
            OfflineLibraryEntity savedOfflineLibrary = offlineLibraryService.addOfflineLibraryEntry(offlineLibrary);
            return new ResponseEntity<>(savedOfflineLibrary, HttpStatus.CREATED);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // Update an offline library entry
    @PutMapping("/{id}")
    public ResponseEntity<?> updateOfflineLibraryEntry(@PathVariable("id") int id, @RequestBody OfflineLibraryEntity offlineLibrary) {
        try {
            OfflineLibraryEntity updatedOfflineLibrary = offlineLibraryService.updateOfflineLibraryEntry(id, offlineLibrary);
            return new ResponseEntity<>(updatedOfflineLibrary, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // Delete an offline library entry
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteOfflineLibraryEntry(@PathVariable("id") int id) {
        String message = offlineLibraryService.deleteOfflineLibraryEntry(id);
        if (message.contains("not found")) {
            return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    // Remove a music from a user's offline library
    @DeleteMapping("/user/{userId}/music/{musicId}")
    public ResponseEntity<?> removeFromOfflineLibrary(@PathVariable("userId") int userId, @PathVariable("musicId") int musicId) {
        try {
            String message = offlineLibraryService.removeFromOfflineLibrary(userId, musicId);
            return new ResponseEntity<>(message, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    // Check if a music is in a user's offline library
    @GetMapping("/user/{userId}/music/{musicId}/check")
    public ResponseEntity<Boolean> isInOfflineLibrary(@PathVariable("userId") int userId, @PathVariable("musicId") int musicId) {
        try {
            boolean isInOfflineLibrary = offlineLibraryService.isInOfflineLibrary(userId, musicId);
            return new ResponseEntity<>(isInOfflineLibrary, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Search offline library entries by file path
    @GetMapping("/search")
    public ResponseEntity<List<OfflineLibraryEntity>> searchByFilePath(@RequestParam("filePath") String filePath) {
        List<OfflineLibraryEntity> offlineLibraries = offlineLibraryService.searchByFilePath(filePath);
        return new ResponseEntity<>(offlineLibraries, HttpStatus.OK);
    }
}