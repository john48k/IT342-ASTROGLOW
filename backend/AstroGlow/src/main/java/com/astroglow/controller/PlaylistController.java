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

import com.astroglow.Entity.PlaylistEntity;
import com.astroglow.Service.PlaylistService;

import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistController {

    @Autowired
    private PlaylistService playlistService;

    // Get all playlists
    @GetMapping("/getAllPlaylist")
    public ResponseEntity<List<PlaylistEntity>> getAllPlaylists() {
        List<PlaylistEntity> playlists = playlistService.getAllPlaylists();
        return new ResponseEntity<>(playlists, HttpStatus.OK);
    }

    // Get playlist by ID
    @GetMapping("getPlaylist/{id}")
    public ResponseEntity<PlaylistEntity> getPlaylistById(@PathVariable("id") int id) {
        try {
            PlaylistEntity playlist = playlistService.getPlaylistById(id);
            return new ResponseEntity<>(playlist, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Get playlists by user ID
    @GetMapping("getPlaylist/user/{userId}")
    public ResponseEntity<List<PlaylistEntity>> getPlaylistsByUserId(@PathVariable("userId") int userId) {
        try {
            List<PlaylistEntity> playlists = playlistService.getPlaylistsByUserId(userId);
            return new ResponseEntity<>(playlists, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Get playlists containing a specific music
    @GetMapping("/music/{musicId}")
    public ResponseEntity<List<PlaylistEntity>> getPlaylistsByMusicId(@PathVariable("musicId") int musicId) {
        try {
            List<PlaylistEntity> playlists = playlistService.getPlaylistsByMusicId(musicId);
            return new ResponseEntity<>(playlists, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Add a music to a user's playlist
    @PostMapping("/postPlaylist/user/{userId}/music/{musicId}")
    public ResponseEntity<?> addToPlaylist(@PathVariable("userId") int userId, @PathVariable("musicId") int musicId) {
        try {
            PlaylistEntity playlist = playlistService.addToPlaylist(userId, musicId);
            return new ResponseEntity<>(playlist, HttpStatus.CREATED);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    // Add a playlist entry (using request body)
    @PostMapping("postPlaylist")
    public ResponseEntity<?> addPlaylistEntry(@RequestBody PlaylistEntity playlist) {
        try {
            PlaylistEntity savedPlaylist = playlistService.addPlaylistEntry(playlist);
            return new ResponseEntity<>(savedPlaylist, HttpStatus.CREATED);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    // Update a playlist entry
    @PutMapping("putPlaylist/{id}")
    public ResponseEntity<?> updatePlaylistEntry(@PathVariable("id") int id, @RequestBody PlaylistEntity playlist) {
        try {
            PlaylistEntity updatedPlaylist = playlistService.updatePlaylistEntry(id, playlist);
            return new ResponseEntity<>(updatedPlaylist, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    // Delete a playlist entry
    @DeleteMapping("deletePlaylist/{id}")
    public ResponseEntity<String> deletePlaylistEntry(@PathVariable("id") int id) {
        String message = playlistService.deletePlaylistEntry(id);
        if (message.contains("not found")) {
            return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    // Remove a music from a user's playlist
    @DeleteMapping("deletePlaylist/user/{userId}/music/{musicId}")
    public ResponseEntity<?> removeFromPlaylist(@PathVariable("userId") int userId, @PathVariable("musicId") int musicId) {
        try {
            String message = playlistService.removeFromPlaylist(userId, musicId);
            return new ResponseEntity<>(message, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    // Check if a music is in a user's playlist
    @GetMapping("/user/{userId}/music/{musicId}/check")
    public ResponseEntity<Boolean> isInPlaylist(@PathVariable("userId") int userId, @PathVariable("musicId") int musicId) {
        try {
            boolean isInPlaylist = playlistService.isInPlaylist(userId, musicId);
            return new ResponseEntity<>(isInPlaylist, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}