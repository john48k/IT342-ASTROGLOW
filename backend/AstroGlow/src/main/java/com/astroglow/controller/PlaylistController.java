package com.astroglow.Controller;

import java.util.List;
import java.util.stream.Collectors;
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

    // Get playlists by user ID
    @GetMapping("/getPlaylist/user/{userId}")
    public ResponseEntity<List<PlaylistEntity>> getPlaylistsByUserId(@PathVariable("userId") int userId) {
        logger.info("GET /getPlaylist/user/" + userId + " - Getting playlists by user ID");
        try {
            List<PlaylistEntity> playlists = playlistService.getPlaylistsByUserId(userId);
            logger.info("Found " + playlists.size() + " playlists for user ID: " + userId);
            return new ResponseEntity<>(playlists, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            logger.warning("User not found with ID: " + userId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Get song IDs from user's playlist
    @GetMapping("/user/{userId}/songs")
    public ResponseEntity<List<Integer>> getSongIdsByUserId(@PathVariable("userId") int userId) {
        try {
            List<PlaylistEntity> playlists = playlistService.getPlaylistsByUserId(userId);
            List<Integer> songIds = playlists.stream()
                .flatMap(playlist -> playlist.getMusic().stream())
                .map(music -> music.getMusicId())
                .distinct()
                .collect(Collectors.toList());
            return new ResponseEntity<>(songIds, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
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
            int userId;
            String name = null;
            
            // Handle different request formats
            if (request.containsKey("user") && request.get("user") instanceof Map) {
                Map<String, Object> userMap = (Map<String, Object>) request.get("user");
                if (userMap.containsKey("userId")) {
                    userId = Integer.parseInt(userMap.get("userId").toString());
                } else {
                    logger.warning("User ID is required but not provided");
                    return new ResponseEntity<>("User ID is required", HttpStatus.BAD_REQUEST);
                }
            } else if (request.containsKey("userId")) {
                userId = Integer.parseInt(request.get("userId").toString());
            } else {
                logger.warning("User ID is required but not provided");
                return new ResponseEntity<>("User ID is required", HttpStatus.BAD_REQUEST);
            }
            
            if (request.containsKey("name")) {
                name = request.get("name").toString();
            }
            
            PlaylistEntity playlist = playlistService.createPlaylist(userId, name);
            logger.info("Successfully created playlist with ID: " + playlist.getPlaylistId() + " and name: " + playlist.getName());
            return new ResponseEntity<>(playlist, HttpStatus.CREATED);
        } catch (NumberFormatException e) {
            logger.warning("Invalid user ID format: " + e.getMessage());
            return new ResponseEntity<>("Invalid user ID format", HttpStatus.BAD_REQUEST);
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

    // Add a music to a user's default playlist (without specifying playlistId)
    @PostMapping("/postPlaylist/user/{userId}/music/{musicId}")
    public ResponseEntity<?> addToUserDefaultPlaylist(
            @PathVariable("userId") int userId,
            @PathVariable("musicId") int musicId) {
        logger.info("POST /postPlaylist/user/" + userId + "/music/" + musicId + " - Adding music to user's default playlist");
        try {
            // Get user's playlists
            List<PlaylistEntity> playlists = playlistService.getPlaylistsByUserId(userId);
            
            if (playlists.isEmpty()) {
                // Create a default playlist if user has none
                PlaylistEntity defaultPlaylist = playlistService.createPlaylist(userId, "My Playlist");
                playlists.add(defaultPlaylist);
            }
            
            // Use the first playlist (or the newly created default playlist)
            PlaylistEntity playlist = playlists.get(0);
            int playlistId = playlist.getPlaylistId();
            
            // Add the music to the playlist
            PlaylistEntity updatedPlaylist = playlistService.addToPlaylist(userId, musicId, playlistId);
            logger.info("Successfully added music ID: " + musicId + " to default playlist ID: " + playlistId + " for user ID: " + userId);
            return new ResponseEntity<>(updatedPlaylist, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            logger.warning("Entity not found: " + e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalStateException e) {
            logger.warning("Illegal state: " + e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        } catch (Exception e) {
            logger.severe("Error adding music to user's default playlist: " + e.getMessage());
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

    // Remove a music from a user's default playlist (without specifying playlistId)
    @DeleteMapping("/deletePlaylist/user/{userId}/music/{musicId}")
    public ResponseEntity<?> removeFromUserDefaultPlaylist(
            @PathVariable("userId") int userId,
            @PathVariable("musicId") int musicId) {
        logger.info("DELETE /deletePlaylist/user/" + userId + "/music/" + musicId + " - Removing music from user's default playlist");
        try {
            // Get user's playlists
            List<PlaylistEntity> playlists = playlistService.getPlaylistsByUserId(userId);
            
            if (playlists.isEmpty()) {
                logger.warning("User has no playlists");
                return new ResponseEntity<>("User has no playlists", HttpStatus.NOT_FOUND);
            }
            
            // Find all playlists containing this music
            List<PlaylistEntity> playlistsWithMusic = playlists.stream()
                .filter(playlist -> playlist.getMusic().stream().anyMatch(music -> music.getMusicId() == musicId))
                .collect(Collectors.toList());
            
            if (playlistsWithMusic.isEmpty()) {
                logger.warning("Music not found in any of user's playlists");
                return new ResponseEntity<>("Music not found in any of user's playlists", HttpStatus.NOT_FOUND);
            }
            
            // Remove from all playlists that contain this music
            for (PlaylistEntity playlist : playlistsWithMusic) {
                playlistService.removeFromPlaylist(playlist.getPlaylistId(), musicId);
                logger.info("Removed music ID: " + musicId + " from playlist ID: " + playlist.getPlaylistId());
            }
            
            return new ResponseEntity<>("Music removed from all user playlists", HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            logger.warning("Entity not found: " + e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalStateException e) {
            logger.warning("Illegal state: " + e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        } catch (Exception e) {
            logger.severe("Error removing music from user's playlists: " + e.getMessage());
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
            try {
                int numericMusicId = Integer.parseInt(musicId);
                logger.info("Treating musicId as numeric: " + numericMusicId);
                
                MusicEntity music = musicService.getMusicById(numericMusicId);
                PlaylistEntity playlist = playlistService.addToPlaylist(playlistId, numericMusicId);
                
                logger.info("Successfully added music ID: " + numericMusicId + " to playlist ID: " + playlistId);
                return new ResponseEntity<>(playlist, HttpStatus.OK);
            } catch (NumberFormatException | EntityNotFoundException e) {
                logger.info("MusicId is not numeric or not found, trying string-based lookup: " + musicId);
            }
            
            String searchTitle = musicId;
            if (searchTitle.startsWith("firebase-")) {
                searchTitle = searchTitle.substring("firebase-".length());
            }
            
            int extensionIndex = searchTitle.lastIndexOf('.');
            if (extensionIndex > 0) {
                searchTitle = searchTitle.substring(0, extensionIndex);
            }
            
            logger.info("Searching for music with title similar to: " + searchTitle);
            
            List<MusicEntity> matchingMusic = playlistService.findMusicByTitle(searchTitle);
            
            if (matchingMusic.isEmpty() && musicId.toLowerCase().contains("http")) {
                logger.info("No match by title, trying to match by audio URL");
                matchingMusic = playlistService.findMusicByAudioUrl(musicId);
            }
            
            if (matchingMusic.isEmpty()) {
                logger.info("No music found with title similar to: " + searchTitle + ", attempting to create new entry");
                
                MusicEntity newMusic = new MusicEntity();
                newMusic.setTitle(searchTitle);
                newMusic.setArtist("Firebase Music");
                newMusic.setGenre("Unknown");
                newMusic.setAudioUrl(musicId);
                
                MusicEntity savedMusic = musicService.postMusic(newMusic);
                logger.info("Created new music entry with ID: " + savedMusic.getMusicId());
                
                PlaylistEntity playlist = playlistService.addToPlaylist(playlistId, savedMusic.getMusicId());
                logger.info("Successfully added new Firebase music: " + musicId + " to playlist ID: " + playlistId);
                return new ResponseEntity<>(playlist, HttpStatus.OK);
            }
            
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

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PlaylistEntity>> getUserPlaylists(@PathVariable int userId) {
        try {
            List<PlaylistEntity> playlists = playlistService.getUserPlaylists(userId);
            return ResponseEntity.ok(playlists);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{playlistId}/add/{musicId:.+}")
    public ResponseEntity<?> addSongToPlaylist(@PathVariable int playlistId, @PathVariable String musicId) {
        try {
            logger.info("Received request to add music to playlist. Playlist ID: " + playlistId + ", Music ID: " + musicId);
            
            // Check if musicId is a Firebase file
            if (musicId.startsWith("firebase-")) {
                // Extract the title from the Firebase filename
                String title = musicId.substring("firebase-".length());
                int extensionIndex = title.lastIndexOf('.');
                if (extensionIndex > 0) {
                    title = title.substring(0, extensionIndex);
                }
                
                // Search for existing music with similar title
                List<MusicEntity> matchingMusic = playlistService.findMusicByTitle(title);
                
                if (matchingMusic.isEmpty()) {
                    // Create new music entry for Firebase file
                    MusicEntity newMusic = new MusicEntity();
                    newMusic.setTitle(title);
                    newMusic.setArtist("Firebase Music");
                    newMusic.setGenre("Unknown");
                    newMusic.setAudioUrl(musicId);
                    
                    MusicEntity savedMusic = musicService.postMusic(newMusic);
                    logger.info("Created new music entry with ID: " + savedMusic.getMusicId());
                    playlistService.addSongToPlaylist(playlistId, savedMusic.getMusicId());
                } else {
                    // Use existing music entry
                    logger.info("Using existing music entry with ID: " + matchingMusic.get(0).getMusicId());
                    playlistService.addSongToPlaylist(playlistId, matchingMusic.get(0).getMusicId());
                }
            } else {
                // Handle numeric musicId
                try {
                    int numericMusicId = Integer.parseInt(musicId);
                    logger.info("Adding numeric music ID: " + numericMusicId);
                    playlistService.addSongToPlaylist(playlistId, numericMusicId);
                } catch (NumberFormatException e) {
                    logger.warning("Invalid music ID format: " + musicId);
                    return ResponseEntity.badRequest().body("Invalid music ID format");
                }
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.severe("Error adding song to playlist: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{playlistId}/remove/{musicId}")
    public ResponseEntity<Void> removeSongFromPlaylist(@PathVariable int playlistId, @PathVariable int musicId) {
        try {
            playlistService.removeSongFromPlaylist(playlistId, musicId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}