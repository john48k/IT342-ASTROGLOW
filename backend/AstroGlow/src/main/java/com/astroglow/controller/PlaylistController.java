package com.astroglow.controller;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.astroglow.Entity.PlaylistEntity;
import com.astroglow.Entity.UserEntity;
import com.astroglow.Entity.MusicEntity;
import com.astroglow.Service.PlaylistService;
import com.astroglow.Service.MusicService;

import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/api/playlists")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174"}, allowCredentials = "true")
public class PlaylistController {
    private static final Logger logger = Logger.getLogger(PlaylistController.class.getName());

    @Autowired
    private PlaylistService playlistService;

    @Autowired
    private MusicService musicService;

    // Get all playlists
    @GetMapping("/getAllPlaylist")
    public ResponseEntity<List<PlaylistEntity>> getAllPlaylists() {
        logger.info("GET /getAllPlaylist - Getting all playlists");
        List<PlaylistEntity> playlists = playlistService.getAllPlaylists();
        logger.info("Found " + playlists.size() + " playlists");
        return new ResponseEntity<>(playlists, HttpStatus.OK);
    }

    // Get playlist by ID
    @GetMapping("/getPlaylist/{id}")
    public ResponseEntity<PlaylistEntity> getPlaylistById(@PathVariable("id") int id) {
        logger.info("GET /getPlaylist/" + id + " - Getting playlist by ID");
        try {
            PlaylistEntity playlist = playlistService.getPlaylistById(id);
            logger.info("Found playlist: " + playlist.getName());
            return new ResponseEntity<>(playlist, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            logger.warning("Playlist not found with ID: " + id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Get playlists by user ID (original)
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PlaylistEntity>> getPlaylistsByUserId(@PathVariable("userId") int userId) {
        logger.info("GET /user/" + userId + " - Getting playlists by user ID");
        try {
            List<PlaylistEntity> playlists = playlistService.getPlaylistsByUserId(userId);
            logger.info("Found " + playlists.size() + " playlists for user ID: " + userId);
            return new ResponseEntity<>(playlists, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            logger.warning("User not found with ID: " + userId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Get playlists by user ID (new path to match frontend)
    @GetMapping("/getPlaylist/user/{userId}")
    public ResponseEntity<List<PlaylistEntity>> getPlaylistsByUserIdPath(@PathVariable("userId") int userId) {
        logger.info("GET /getPlaylist/user/" + userId + " - Getting playlists by user ID (alternative path)");
        try {
            List<PlaylistEntity> playlists = playlistService.getPlaylistsByUserId(userId);
            logger.info("Found " + playlists.size() + " playlists for user ID: " + userId);
            return new ResponseEntity<>(playlists, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            logger.warning("User not found with ID: " + userId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Get playlists containing a specific music
    @GetMapping("/music/{musicId}")
    public ResponseEntity<List<PlaylistEntity>> getPlaylistsByMusicId(@PathVariable("musicId") int musicId) {
        logger.info("GET /music/" + musicId + " - Getting playlists by music ID");
        try {
            List<PlaylistEntity> playlists = playlistService.getPlaylistsByMusicId(musicId);
            logger.info("Found " + playlists.size() + " playlists containing music ID: " + musicId);
            return new ResponseEntity<>(playlists, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            logger.warning("Music not found with ID: " + musicId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Create a new playlist
    @PostMapping("/postPlaylist")
    public ResponseEntity<?> createPlaylist(@RequestBody Map<String, Object> request) {
        logger.info("POST /postPlaylist - Creating a new playlist");
        try {
            // Extract user ID and playlist name from request
            int userId;
            String name = null;
            
            if (request.containsKey("user") && request.get("user") instanceof Map) {
                Map<String, Object> userMap = (Map<String, Object>) request.get("user");
                if (userMap.containsKey("userId")) {
                    userId = Integer.parseInt(userMap.get("userId").toString());
                    logger.info("User ID from request: " + userId);
                } else {
                    logger.warning("User ID is required but not provided");
                    return new ResponseEntity<>("User ID is required", HttpStatus.BAD_REQUEST);
                }
            } else {
                logger.warning("User information is required but not provided");
                return new ResponseEntity<>("User information is required", HttpStatus.BAD_REQUEST);
            }
            
            if (request.containsKey("name")) {
                name = request.get("name").toString();
                logger.info("Playlist name from request: " + name);
            } else {
                logger.info("No playlist name provided, will use default");
            }
            
            // Check if this is a legacy request with a music field
            if (request.containsKey("music")) {
                logger.info("Legacy request with music field detected");
                // Handle as a playlist entry with the addPlaylistEntry method
                PlaylistEntity playlistEntity = new PlaylistEntity();
                UserEntity userEntity = new UserEntity();
                userEntity.setUserId(userId);
                playlistEntity.setUser(userEntity);
                playlistEntity.setName(name);
                
                // Initialize music list to prevent NullPointerException
                playlistEntity.setMusic(new ArrayList<>());
                
                PlaylistEntity savedPlaylist = playlistService.addPlaylistEntry(playlistEntity);
                logger.info("Successfully created playlist entry with ID: " + savedPlaylist.getPlaylistId());
                return new ResponseEntity<>(savedPlaylist, HttpStatus.CREATED);
            } else {
                // Use the standard createPlaylist method for new requests
                PlaylistEntity playlist = playlistService.createPlaylist(userId, name);
                logger.info("Successfully created playlist with ID: " + playlist.getPlaylistId() + " and name: " + playlist.getName());
                return new ResponseEntity<>(playlist, HttpStatus.CREATED);
            }
        } catch (EntityNotFoundException e) {
            logger.warning("Entity not found: " + e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.severe("Error creating playlist: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Add a music to a playlist
    @PostMapping("/playlist/{playlistId}/music/{musicId}")
    public ResponseEntity<?> addMusicToPlaylist(
            @PathVariable("playlistId") int playlistId,
            @PathVariable("musicId") int musicId) {
        logger.info("POST /playlist/" + playlistId + "/music/" + musicId + " - Adding music to playlist");
        try {
            PlaylistEntity playlist = playlistService.addToPlaylist(playlistId, musicId);
            logger.info("Successfully added music ID: " + musicId + " to playlist ID: " + playlistId);
            return new ResponseEntity<>(playlist, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            logger.warning("Entity not found: " + e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalStateException e) {
            logger.warning("Illegal state: " + e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        } catch (Exception e) {
            logger.severe("Error adding music to playlist: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Add a music to a user's playlist
    @PostMapping("/user/{userId}/music/{musicId}")
    public ResponseEntity<?> addToUserPlaylist(
            @PathVariable("userId") int userId,
            @PathVariable("musicId") int musicId,
            @RequestParam(value = "playlistId", required = true) int playlistId) {
        logger.info("POST /user/" + userId + "/music/" + musicId + " - Adding music to user's playlist (playlistId: " + playlistId + ")");
        try {
            PlaylistEntity playlist = playlistService.addToPlaylist(userId, musicId, playlistId);
            logger.info("Successfully added music ID: " + musicId + " to playlist ID: " + playlistId + " for user ID: " + userId);
            return new ResponseEntity<>(playlist, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            logger.warning("Entity not found: " + e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalStateException e) {
            logger.warning("Illegal state: " + e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        } catch (Exception e) {
            logger.severe("Error adding music to user's playlist: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Remove a music from a playlist
    @DeleteMapping("/playlist/{playlistId}/music/{musicId}")
    public ResponseEntity<?> removeMusicFromPlaylist(
            @PathVariable("playlistId") int playlistId,
            @PathVariable("musicId") int musicId) {
        logger.info("DELETE /playlist/" + playlistId + "/music/" + musicId + " - Removing music from playlist");
        try {
            PlaylistEntity playlist = playlistService.removeFromPlaylist(playlistId, musicId);
            logger.info("Successfully removed music ID: " + musicId + " from playlist ID: " + playlistId);
            return new ResponseEntity<>(playlist, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            logger.warning("Entity not found: " + e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalStateException e) {
            logger.warning("Illegal state: " + e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        } catch (Exception e) {
            logger.severe("Error removing music from playlist: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Update playlist name
    @PutMapping("/putPlaylist/{id}")
    public ResponseEntity<?> updatePlaylist(
            @PathVariable("id") int id,
            @RequestBody Map<String, String> request) {
        logger.info("PUT /putPlaylist/" + id + " - Updating playlist");
        try {
            String name = request.get("name");
            if (name == null || name.trim().isEmpty()) {
                logger.warning("Playlist name is required but not provided");
                return new ResponseEntity<>("Playlist name is required", HttpStatus.BAD_REQUEST);
            }
            
            logger.info("Updating playlist ID: " + id + " with new name: " + name);
            PlaylistEntity playlist = playlistService.updatePlaylist(id, name);
            logger.info("Successfully updated playlist ID: " + id + " with name: " + playlist.getName());
            return new ResponseEntity<>(playlist, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            logger.warning("Entity not found: " + e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.severe("Error updating playlist: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Delete a playlist
    @DeleteMapping("/deletePlaylist/{id}")
    public ResponseEntity<String> deletePlaylist(@PathVariable("id") int id) {
        logger.info("DELETE /deletePlaylist/" + id + " - Deleting playlist");
        String message = playlistService.deletePlaylist(id);
        if (message.contains("not found")) {
            logger.warning("Playlist not found with ID: " + id);
            return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
        }
        logger.info("Successfully deleted playlist ID: " + id);
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    // Add a playlist entry (used in legacy code path)
    @PostMapping("/addPlaylistEntry")
    public ResponseEntity<?> addPlaylistEntry(@RequestBody PlaylistEntity playlist) {
        logger.info("POST /addPlaylistEntry - Adding a playlist entry");
        try {
            if (playlist.getUser() == null) {
                logger.warning("User information is required");
                return new ResponseEntity<>("User information is required", HttpStatus.BAD_REQUEST);
            }
            
            if (playlist.getMusic() == null) {
                // Initialize the music list if it's null to avoid NullPointerException
                playlist.setMusic(new ArrayList<>());
            }
            
            PlaylistEntity savedPlaylist = playlistService.addPlaylistEntry(playlist);
            logger.info("Successfully added playlist entry with ID: " + savedPlaylist.getPlaylistId());
            return new ResponseEntity<>(savedPlaylist, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            logger.warning("Entity not found: " + e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.severe("Error adding playlist entry: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Check if a music is in a playlist
    @GetMapping("/playlist/{playlistId}/music/{musicId}/check")
    public ResponseEntity<Boolean> isInPlaylist(
            @PathVariable("playlistId") int playlistId,
            @PathVariable("musicId") int musicId) {
        logger.info("GET /playlist/" + playlistId + "/music/" + musicId + "/check - Checking if music is in playlist");
        try {
            boolean isInPlaylist = playlistService.isInPlaylist(playlistId, musicId);
            logger.info("Music ID: " + musicId + " is " + (isInPlaylist ? "" : "not ") + "in playlist ID: " + playlistId);
            return new ResponseEntity<>(isInPlaylist, HttpStatus.OK);
        } catch (Exception e) {
            logger.warning("Error checking if music is in playlist: " + e.getMessage());
            return new ResponseEntity<>(false, HttpStatus.OK);
        }
    }

    // Add a music to a playlist (using a string music ID for Firebase files)
    @PostMapping("/playlist/{playlistId}/music/firebase")
    public ResponseEntity<?> addFirebaseMusicToPlaylist(
            @PathVariable("playlistId") int playlistId,
            @RequestParam("musicId") String musicId) {
        logger.info("POST /playlist/" + playlistId + "/music/firebase?musicId=" + musicId + " - Adding Firebase music to playlist");
        try {
            // First try to parse the musicId as an integer (it might be a database ID)
            try {
                int numericMusicId = Integer.parseInt(musicId);
                logger.info("Treating musicId as numeric: " + numericMusicId);
                
                // Check if the music exists in the database
                MusicEntity music = musicService.getMusicById(numericMusicId);
                PlaylistEntity playlist = playlistService.addToPlaylist(playlistId, numericMusicId);
                
                logger.info("Successfully added music ID: " + numericMusicId + " to playlist ID: " + playlistId);
                return new ResponseEntity<>(playlist, HttpStatus.OK);
            } catch (NumberFormatException | EntityNotFoundException e) {
                // Not a numeric ID or music not found, continue with string-based lookup
                logger.info("MusicId is not numeric or not found, trying string-based lookup: " + musicId);
            }
            
            // Clean up the musicId by removing the "firebase-" prefix if present
            String searchTitle = musicId;
            if (searchTitle.startsWith("firebase-")) {
                searchTitle = searchTitle.substring("firebase-".length());
            }
            
            // Remove file extension if present (e.g., .mp3)
            int extensionIndex = searchTitle.lastIndexOf('.');
            if (extensionIndex > 0) {
                searchTitle = searchTitle.substring(0, extensionIndex);
            }
            
            logger.info("Searching for music with title similar to: " + searchTitle);
            
            // First, try to find the music by title/filename
            List<MusicEntity> matchingMusic = playlistService.findMusicByTitle(searchTitle);
            
            // If no matches by title, let's try by audio URL if the musicId seems to be a URL
            if (matchingMusic.isEmpty() && musicId.toLowerCase().contains("http")) {
                logger.info("No match by title, trying to match by audio URL");
                matchingMusic = playlistService.findMusicByAudioUrl(musicId);
            }
            
            if (matchingMusic.isEmpty()) {
                // If still not found, let's create a new music entry from Firebase
                logger.info("No music found with title similar to: " + searchTitle + ", attempting to create new entry");
                
                // Create a basic MusicEntity from the Firebase music ID
                MusicEntity newMusic = new MusicEntity();
                newMusic.setTitle(searchTitle);
                newMusic.setArtist("Firebase Music");
                newMusic.setGenre("Unknown");
                newMusic.setAudioUrl(musicId);
                
                // Save the new music entity
                MusicEntity savedMusic = musicService.postMusic(newMusic);
                logger.info("Created new music entry with ID: " + savedMusic.getMusicId());
                
                // Add it to the playlist
                PlaylistEntity playlist = playlistService.addToPlaylist(playlistId, savedMusic.getMusicId());
                logger.info("Successfully added new Firebase music: " + musicId + " to playlist ID: " + playlistId);
                return new ResponseEntity<>(playlist, HttpStatus.OK);
            }
            
            // Use the first matching music
            MusicEntity music = matchingMusic.get(0);
            logger.info("Found matching music: " + music.getTitle() + " (ID: " + music.getMusicId() + ")");
            
            PlaylistEntity playlist = playlistService.addToPlaylist(playlistId, music.getMusicId());
            
            logger.info("Successfully added Firebase music: " + musicId + " to playlist ID: " + playlistId);
            return new ResponseEntity<>(playlist, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            logger.warning("Entity not found: " + e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalStateException e) {
            logger.warning("Illegal state: " + e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        } catch (Exception e) {
            logger.severe("Error adding Firebase music to playlist: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}