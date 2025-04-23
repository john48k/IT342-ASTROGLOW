package com.astroglow.controller;

import java.util.List;
import java.util.Base64;
import java.io.IOException;
import java.util.stream.Collectors;
import java.net.URL;
import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Map;

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
import org.springframework.web.multipart.MultipartFile;

import com.astroglow.Entity.MusicEntity;
import com.astroglow.Entity.UserEntity;
import com.astroglow.Repository.UserRepository;
import com.astroglow.Service.MusicService;
import com.astroglow.Service.FavoritesService;

import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/api/music")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174"})
public class MusicController {

    private final MusicService musicService;
    private final UserRepository userRepository;
    private final FavoritesService favoritesService;

    @Autowired
    public MusicController(MusicService musicService, UserRepository userRepository, FavoritesService favoritesService) {
        this.musicService = musicService;
        this.userRepository = userRepository;
        this.favoritesService = favoritesService;
    }

    // Create new music
    @PostMapping("/postMusic")
    public ResponseEntity<MusicEntity> createMusic(@RequestBody MusicEntity music) {
        MusicEntity savedMusic = musicService.postMusic(music);
        return new ResponseEntity<>(savedMusic, HttpStatus.CREATED);
    }

    // Upload MP3 file
    @PostMapping("/upload")
    public ResponseEntity<MusicEntity> uploadMusicFile(@RequestParam(value = "file", required = false) MultipartFile file,
                                                      @RequestParam(value = "title", required = true) String title,
                                                      @RequestParam(value = "artist", required = true) String artist,
                                                      @RequestParam(value = "genre", required = true) String genre,
                                                      @RequestParam(value = "audioUrl", required = false) String audioUrl,
                                                      @RequestParam(value = "imageUrl", required = false) String imageUrl,
                                                      @RequestParam(value = "imageBase64", required = false) String imageBase64,
                                                      @RequestParam(value = "userId", required = false) Integer userId) {
        try {
            System.out.println("Received upload request with title: " + title + ", artist: " + artist);
            if (audioUrl != null) {
                System.out.println("Received audioUrl: " + audioUrl);
            }
            if (imageUrl != null) {
                System.out.println("Received imageUrl: " + (imageUrl.length() > 50 ? imageUrl.substring(0, 50) + "..." : imageUrl));
            }
            
            // Sanitize inputs to avoid null or empty string issues
            audioUrl = (audioUrl != null && !audioUrl.trim().isEmpty()) ? audioUrl.trim() : null;
            imageUrl = (imageUrl != null && !imageUrl.trim().isEmpty()) ? imageUrl.trim() : null;
            imageBase64 = (imageBase64 != null && !imageBase64.trim().isEmpty()) ? imageBase64.trim() : null;
            
            // Process image data to ensure it's not too large
            if (imageUrl != null) {
                imageUrl = processImageData(imageUrl);
            }
            if (imageBase64 != null) {
                imageBase64 = processImageData(imageBase64);
            }
            
            validateAudioAndImageUrls(file, audioUrl, imageUrl, imageBase64);

            MusicEntity musicEntity = new MusicEntity();
            musicEntity.setTitle(title);
            musicEntity.setArtist(artist);
            musicEntity.setGenre(genre);
            musicEntity.setTime(0); // Default duration in seconds

            // Set owner if userId is provided
            if (userId != null) {
                Optional<UserEntity> userOptional = userRepository.findById(userId);
                if (userOptional.isPresent()) {
                    musicEntity.setOwner(userOptional.get());
                } else {
                    return ResponseEntity.badRequest().body(null);
                }
            }

            // Set audio data
            if (file != null && !file.isEmpty()) {
                System.out.println("Processing uploaded file of type: " + file.getContentType());
                // Convert byte[] to Base64 string for storage
                String base64Audio = Base64.getEncoder().encodeToString(file.getBytes());
                musicEntity.setAudioData(base64Audio);
            } else if (audioUrl != null) {
                System.out.println("Setting audio URL: " + audioUrl);
                musicEntity.setAudioUrl(audioUrl);
            } else {
                System.out.println("No audio file or URL provided");
                return ResponseEntity.badRequest().body(null);
            }

            // Set image URL or base64
            if (imageUrl != null) {
                System.out.println("Setting image URL of length: " + imageUrl.length());
                musicEntity.setImageUrl(imageUrl);
            } else if (imageBase64 != null) {
                System.out.println("Setting image base64 data of length: " + imageBase64.length());
                musicEntity.setImageUrl(imageBase64);
            }

            MusicEntity savedMusic = musicService.postMusic(musicEntity);
            System.out.println("Successfully saved music with ID: " + savedMusic.getMusicId());
            return ResponseEntity.ok(savedMusic);
        } catch (IOException e) {
            System.out.println("IOException during file processing: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid argument: " + e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Get all music
    @GetMapping("/getAllMusic")
    public ResponseEntity<List<MusicEntity>> getAllMusic() {
        List<MusicEntity> musicList = musicService.getAllMusic();
        return new ResponseEntity<>(musicList, HttpStatus.OK);
    }

    // Get music by user ID (owner)
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<MusicEntity>> getMusicByUserId(@PathVariable("userId") int userId) {
        List<MusicEntity> musicList = musicService.getMusicByOwnerId(userId);
        return new ResponseEntity<>(musicList, HttpStatus.OK);
    }

    // Get music by ID
    @GetMapping("/getMusic/{id}")
    public ResponseEntity<?> getMusicById(
            @PathVariable("id") int id,
            @RequestParam(value = "includeAudioData", required = false, defaultValue = "false") boolean includeAudioData) {
        try {
            MusicEntity music = musicService.getMusicById(id);
            
            // If audio data is specifically requested, create a custom response
            if (includeAudioData) {
                // Create a map to include audioData explicitly
                java.util.Map<String, Object> response = new java.util.HashMap<>();
                response.put("musicId", music.getMusicId());
                response.put("title", music.getTitle());
                response.put("artist", music.getArtist());
                response.put("genre", music.getGenre());
                response.put("time", music.getTime());
                response.put("audioData", music.getAudioData());
                if (music.getAudioUrl() != null) {
                    response.put("audioUrl", music.getAudioUrl());
                }
                if (music.getImageUrl() != null) {
                    response.put("imageUrl", music.getImageUrl());
                }
                
                return new ResponseEntity<>(response, HttpStatus.OK);
            }
            
            // Return the entity directly
            return new ResponseEntity<>(music, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Get audio data by music ID
    @GetMapping("/audio/{id}")
    public ResponseEntity<?> getAudioData(@PathVariable("id") int id) {
        try {
            System.out.println("Fetching audio data for music ID: " + id);
            
            MusicEntity music = musicService.getMusicById(id);
            System.out.println("Found music: " + music.getTitle() + " by " + music.getArtist());
            
            String audioData = music.getAudioData();
            if (audioData == null) {
                System.out.println("Audio data is null for music ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No audio data found for this music");
            }
            
            if (audioData.isEmpty()) {
                System.out.println("Audio data is empty for music ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Audio data is empty for this music");
            }
            
            System.out.println("Returning audio data with length: " + audioData.length());
            
            // Return just the base64 data as text
            return ResponseEntity.ok()
                    .header("Content-Type", "text/plain")
                    .body(audioData);
        } catch (EntityNotFoundException e) {
            System.out.println("Music not found with ID: " + id);
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Music not found");
        } catch (Exception e) {
            System.out.println("Error retrieving audio data for music ID: " + id);
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving audio data: " + e.getMessage());
        }
    }

    // Update music
    @PutMapping("/putMusic/{id}")
    public ResponseEntity<MusicEntity> updateMusic(@PathVariable("id") int id, @RequestBody MusicEntity music) {
        try {
            MusicEntity updatedMusic = musicService.putMusic(id, music);
            return new ResponseEntity<>(updatedMusic, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Delete music
    @DeleteMapping("/deleteMusic/{id}")
    public ResponseEntity<String> deleteMusic(@PathVariable("id") int id) {
        String message = musicService.deleteMusic(id);
        if (message.contains("not found")) {
            return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    // Search by title
    @GetMapping("/search/title")
    public ResponseEntity<List<MusicEntity>> searchByTitle(@RequestParam String title) {
        List<MusicEntity> musicList = musicService.findByTitle(title);
        return new ResponseEntity<>(musicList, HttpStatus.OK);
    }

    // Search by artist
    @GetMapping("/search/artist")
    public ResponseEntity<List<MusicEntity>> searchByArtist(@RequestParam String artist) {
        List<MusicEntity> musicList = musicService.findByArtist(artist);
        return new ResponseEntity<>(musicList, HttpStatus.OK);
    }

    // Search by genre
    @GetMapping("/search/genre")
    public ResponseEntity<List<MusicEntity>> searchByGenre(@RequestParam String genre) {
        List<MusicEntity> musicList = musicService.findByGenre(genre);
        return new ResponseEntity<>(musicList, HttpStatus.OK);
    }

    // Search by duration less than or equal to
    @GetMapping("/search/duration/max")
    public ResponseEntity<List<MusicEntity>> searchByMaxDuration(@RequestParam Integer maxTime) {
        List<MusicEntity> musicList = musicService.findByMaxDuration(maxTime);
        return new ResponseEntity<>(musicList, HttpStatus.OK);
    }

    // Search by duration greater than or equal to
    @GetMapping("/search/duration/min")
    public ResponseEntity<List<MusicEntity>> searchByMinDuration(@RequestParam Integer minTime) {
        List<MusicEntity> musicList = musicService.findByMinDuration(minTime);
        return new ResponseEntity<>(musicList, HttpStatus.OK);
    }

    // Add music with URL
    @PostMapping("/addMusicWithUrl")
    public ResponseEntity<?> addMusicWithUrl(
            @RequestParam("title") String title,
            @RequestParam("artist") String artist,
            @RequestParam(value = "genre", required = false) String genre,
            @RequestParam("audioUrl") String audioUrl,
            @RequestParam(value = "imageUrl", required = false) String imageUrl,
            @RequestParam(value = "time", required = false) Integer time) {
        try {
            System.out.println("Received addMusicWithUrl request - Title: " + title + ", Artist: " + artist);
            System.out.println("AudioUrl: " + audioUrl);
            
            // Sanitize inputs
            audioUrl = (audioUrl != null && !audioUrl.trim().isEmpty()) ? audioUrl.trim() : null;
            imageUrl = (imageUrl != null && !imageUrl.trim().isEmpty()) ? imageUrl.trim() : null;
            
            // Process image URL to ensure it's not too large
            if (imageUrl != null) {
                imageUrl = processImageData(imageUrl);
            }
            
            // Validate the audio URL
            if (audioUrl == null) {
                return ResponseEntity.badRequest().body("Audio URL cannot be empty");
            }
            
            if (!isValidUrl(audioUrl) && !isDataUri(audioUrl)) {
                System.out.println("Invalid audio URL format: " + audioUrl);
                return ResponseEntity.badRequest().body("Invalid audio URL format: " + audioUrl);
            }
            
            // Check if a song with the same title and artist already exists
            if (musicService.existsByTitleAndArtist(title, artist)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("A song with this title and artist already exists. Please use a different title or artist.");
            }
            
            // Create a new music entity
            MusicEntity music = new MusicEntity();
            music.setTitle(title);
            music.setArtist(artist);
            if (genre != null && !genre.isEmpty()) {
                music.setGenre(genre);
            } else {
                music.setGenre("Unknown");
            }
            music.setAudioUrl(audioUrl);
            
            // Set image URL if provided
            if (imageUrl != null) {
                if (!isValidUrl(imageUrl) && !isDataUri(imageUrl)) {
                    System.out.println("Invalid image URL format: " + imageUrl);
                    return ResponseEntity.badRequest().body("Invalid image URL format: " + imageUrl);
                }
                music.setImageUrl(imageUrl);
                System.out.println("Image URL set: " + (imageUrl.length() > 50 ? imageUrl.substring(0, 50) + "..." : imageUrl));
            }
            
            // Set time if provided
            if (time != null) {
                music.setTime(time);
            }
            
            // Save the music entity
            MusicEntity savedMusic = musicService.postMusic(music);
            System.out.println("Saved music with ID: " + savedMusic.getMusicId());
            
            // Create a response with just the essential information
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("status", "success");
            response.put("message", "Music with URL added successfully");
            response.put("musicId", savedMusic.getMusicId());
            response.put("title", savedMusic.getTitle());
            response.put("artist", savedMusic.getArtist());
            response.put("genre", savedMusic.getGenre());
            response.put("audioUrl", savedMusic.getAudioUrl());
            if (savedMusic.getImageUrl() != null) {
                response.put("imageUrl", "Image data available");
            }
            if (savedMusic.getTime() != null) {
                response.put("time", savedMusic.getTime());
            }
            
            // Return the response
            return new ResponseEntity<>(response, HttpStatus.CREATED);
            
        } catch (Exception e) {
            System.out.println("Error in addMusicWithUrl: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to add music: " + e.getMessage());
        }
    }

    // Add a new endpoint for adding music with base64 image
    @PostMapping("/addMusicWithBase64Image")
    public ResponseEntity<?> addMusicWithBase64Image(
            @RequestParam("title") String title,
            @RequestParam("artist") String artist,
            @RequestParam(value = "genre", required = false) String genre,
            @RequestParam("audioUrl") String audioUrl,
            @RequestParam(value = "imageBase64", required = false) String imageBase64,
            @RequestParam(value = "time", required = false) Integer time) {
        try {
            System.out.println("Received addMusicWithBase64Image request - Title: " + title + ", Artist: " + artist);
            
            // Sanitize inputs
            audioUrl = (audioUrl != null && !audioUrl.trim().isEmpty()) ? audioUrl.trim() : null;
            imageBase64 = (imageBase64 != null && !imageBase64.trim().isEmpty()) ? imageBase64.trim() : null;
            
            // Process image data to ensure it's not too large
            if (imageBase64 != null) {
                imageBase64 = processImageData(imageBase64);
            }
            
            // Validate the audio URL
            if (audioUrl == null) {
                return ResponseEntity.badRequest().body("Audio URL cannot be empty");
            }
            
            if (!isValidUrl(audioUrl) && !isDataUri(audioUrl)) {
                System.out.println("Invalid audio URL format: " + audioUrl);
                return ResponseEntity.badRequest().body("Invalid audio URL format: " + audioUrl);
            }
            
            // Check if a song with the same title and artist already exists
            if (musicService.existsByTitleAndArtist(title, artist)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("A song with this title and artist already exists. Please use a different title or artist.");
            }
            
            // Create a new MusicEntity
            MusicEntity music = new MusicEntity();
            music.setTitle(title);
            music.setArtist(artist);
            if (genre != null && !genre.isEmpty()) {
                music.setGenre(genre);
            } else {
                music.setGenre("Unknown");
            }
            music.setAudioUrl(audioUrl);
            
            // Set image base64 data if provided
            if (imageBase64 != null) {
                // Ensure it's a proper data URI
                if (!imageBase64.startsWith("data:")) {
                    imageBase64 = "data:image/jpeg;base64," + imageBase64;
                }
                music.setImageUrl(imageBase64);
                System.out.println("Image Base64 data set (length: " + imageBase64.length() + ")");
            }
            
            // Set time if provided
            if (time != null) {
                music.setTime(time);
            }
            
            // Save the music entity
            MusicEntity savedMusic = musicService.postMusic(music);
            System.out.println("Saved music with ID: " + savedMusic.getMusicId());
            
            // Create a response with just the essential information
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("status", "success");
            response.put("message", "Music with base64 image added successfully");
            response.put("musicId", savedMusic.getMusicId());
            response.put("title", savedMusic.getTitle());
            response.put("artist", savedMusic.getArtist());
            response.put("genre", savedMusic.getGenre());
            response.put("audioUrl", savedMusic.getAudioUrl());
            if (savedMusic.getImageUrl() != null) {
                // Only include information about the image, not the full data
                response.put("imageUrl", "Image data available");
            }
            if (savedMusic.getTime() != null) {
                response.put("time", savedMusic.getTime());
            }
            
            // Return the response
            return new ResponseEntity<>(response, HttpStatus.CREATED);
            
        } catch (Exception e) {
            System.out.println("Error in addMusicWithBase64Image: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to add music: " + e.getMessage());
        }
    }

    // Upload music with URL
    @PostMapping("/uploadWithUrl")
    public ResponseEntity<MusicEntity> uploadMusicWithUrl(@RequestBody Map<String, Object> requestBody) {
        try {
            String title = (String) requestBody.get("title");
            String artist = (String) requestBody.get("artist");
            String genre = (String) requestBody.get("genre");
            String audioUrl = (String) requestBody.get("audioUrl");
            String imageUrl = (String) requestBody.get("imageUrl");
            Integer userId = (Integer) requestBody.get("userId");

            System.out.println("Received URL upload request with title: " + title + ", artist: " + artist);
            if (audioUrl != null) {
                System.out.println("Received audioUrl: " + audioUrl);
            }
            if (imageUrl != null) {
                System.out.println("Received imageUrl: " + (imageUrl.length() > 50 ? imageUrl.substring(0, 50) + "..." : imageUrl));
            }
            
            // Sanitize inputs to avoid null or empty string issues
            audioUrl = (audioUrl != null && !audioUrl.trim().isEmpty()) ? audioUrl.trim() : null;
            imageUrl = (imageUrl != null && !imageUrl.trim().isEmpty()) ? imageUrl.trim() : null;
            
            // Process image data to ensure it's not too large
            if (imageUrl != null) {
                imageUrl = processImageData(imageUrl);
            }
            
            MusicEntity musicEntity = new MusicEntity();
            musicEntity.setTitle(title);
            musicEntity.setArtist(artist);
            musicEntity.setGenre(genre);
            musicEntity.setTime(0); // Default duration in seconds
            musicEntity.setAudioUrl(audioUrl);
            musicEntity.setImageUrl(imageUrl);

            // Set owner if userId is provided
            if (userId != null) {
                Optional<UserEntity> userOptional = userRepository.findById(userId);
                if (userOptional.isPresent()) {
                    musicEntity.setOwner(userOptional.get());
                } else {
                    return ResponseEntity.badRequest().body(null);
                }
            }

            MusicEntity savedMusic = musicService.postMusic(musicEntity);
            return ResponseEntity.ok(savedMusic);
        } catch (Exception e) {
            System.err.println("Error processing URL upload: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Helper method to validate audio and image URLs
    private void validateAudioAndImageUrls(MultipartFile file, String audioUrl, String imageUrl, String imageBase64) {
        // Check if either file or audioUrl is provided
        if ((file == null || file.isEmpty()) && audioUrl == null) {
            throw new IllegalArgumentException("Please upload a file or provide a valid audio URL");
        }
        
        // Validate audioUrl if provided
        if (audioUrl != null) {
            if (!isValidUrl(audioUrl) && !isDataUri(audioUrl)) {
                System.out.println("Audio URL validation failed: " + audioUrl);
                throw new IllegalArgumentException("Invalid audio URL format: " + audioUrl);
            } else {
                System.out.println("Audio URL validated successfully: " + audioUrl);
            }
        }
        
        // Validate imageUrl if provided
        if (imageUrl != null) {
            if (!isValidUrl(imageUrl) && !isDataUri(imageUrl)) {
                System.out.println("Image URL validation failed: " + imageUrl);
                throw new IllegalArgumentException("Invalid image URL format: " + imageUrl);
            } else {
                System.out.println("Image URL validated successfully: " + imageUrl);
            }
        }
        
        // Validate file if provided
        if (file != null && !file.isEmpty()) {
            if (file.getContentType() == null) {
                throw new IllegalArgumentException("File content type cannot be determined");
            }
            
            // Check for audio MIME types
            String contentType = file.getContentType().toLowerCase();
            if (!contentType.equals("audio/mpeg") && 
                !contentType.equals("audio/mp3") && 
                !contentType.equals("audio/wav") && 
                !contentType.contains("audio")) {
                throw new IllegalArgumentException("Only audio files are supported. Received: " + file.getContentType());
            }
            
            System.out.println("File validated successfully: " + file.getOriginalFilename() + " (" + file.getContentType() + ")");
        }
    }

    // Helper method to check if a string is a valid URL
    private boolean isValidUrl(String urlString) {
        try {
            // First trim the URL to avoid whitespace issues
            String trimmedUrl = urlString.trim();
            
            // Check for common protocols
            if (!trimmedUrl.startsWith("http://") && !trimmedUrl.startsWith("https://") && 
                !trimmedUrl.startsWith("ftp://") && !trimmedUrl.startsWith("file://")) {
                
                // Try adding https:// if no protocol is specified
                trimmedUrl = "https://" + trimmedUrl;
            }
            
            // Create and validate the URL
            URL url = new URL(trimmedUrl);
            url.toURI(); // This will throw an exception for malformed URIs
            return true;
        } catch (Exception e) {
            System.out.println("Invalid URL: " + urlString + " - Error: " + e.getMessage());
            return false;
        }
    }
    
    // Helper method to check if a string is a data URI
    private boolean isDataUri(String uri) {
        if (uri == null) {
            return false;
        }
        
        String trimmedUri = uri.trim();
        return trimmedUri.startsWith("data:");
    }

    // Add this helper method to check and potentially limit image data size
    private String processImageData(String imageData) {
        if (imageData == null) {
            return null;
        }
        
        // Log the size of the incoming image data
        System.out.println("Processing image data of length: " + imageData.length());
        
        // If it's a data URI, check its size
        if (isDataUri(imageData)) {
            // Very large data URI (over 5MB) - consider if we need to truncate
            if (imageData.length() > 5 * 1024 * 1024) {
                System.out.println("Warning: Image data exceeds 5MB, might cause issues with database storage");
            }
        }
        
        return imageData;
    }

    // Add the new favorite endpoints:

    // Check if a song is favorited by a user
    @GetMapping("/favorites/isFavorite/{userId}/{musicId}")
    public ResponseEntity<Boolean> isFavorite(@PathVariable int userId, @PathVariable int musicId) {
        try {
            // Assume favoritesService has this method
            boolean isFav = favoritesService.isFavorite(userId, musicId); 
            return ResponseEntity.ok(isFav);
        } catch (Exception e) {
            System.err.println("Error checking favorite status for user " + userId + ", music " + musicId + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

    // Delete a favorite - REMOVE THIS METHOD
    /*
    @DeleteMapping("/favorites/{userId}/{musicId}")
    public ResponseEntity<Void> deleteFavorite(@PathVariable int userId, @PathVariable int musicId) {
        try {
             // Assume favoritesService has this method
            boolean deleted = favoritesService.deleteFavorite(userId, musicId);
            if (deleted) {
                return ResponseEntity.ok().build(); // Successfully deleted
            } else {
                // Favorite might not have existed
                return ResponseEntity.notFound().build(); 
            }
        } catch (Exception e) {
             System.err.println("Error deleting favorite for user " + userId + ", music " + musicId + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    */
}