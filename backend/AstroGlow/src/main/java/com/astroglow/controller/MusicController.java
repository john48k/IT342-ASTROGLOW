package com.astroglow.controller;

import java.util.List;
import java.util.Base64;
import java.io.IOException;
import java.util.stream.Collectors;

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
import com.astroglow.Service.MusicService;
import com.astroglow.dto.MusicDTO;

import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/api/music")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174"})
public class MusicController {

    @Autowired
    private MusicService musicService;

    // Create new music
    @PostMapping("/postMusic")
    public ResponseEntity<MusicDTO> createMusic(@RequestBody MusicEntity music) {
        MusicEntity savedMusic = musicService.postMusic(music);
        return new ResponseEntity<>(new MusicDTO(savedMusic), HttpStatus.CREATED);
    }

    // Upload MP3 file
    @PostMapping("/upload")
    public ResponseEntity<?> uploadMusicFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("artist") String artist,
            @RequestParam(value = "genre", required = false) String genre) {
        try {
            // Check if file is empty
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Please upload a file");
            }
            
            // Log file information
            System.out.println("Uploading file: " + file.getOriginalFilename());
            System.out.println("File size: " + file.getSize() + " bytes");
            System.out.println("Content type: " + file.getContentType());
            
            // Check if file is an MP3
            if (!file.getContentType().equals("audio/mpeg")) {
                return ResponseEntity.badRequest().body("Only MP3 files are supported. Received: " + file.getContentType());
            }
            
            // Convert the file to base64
            byte[] fileContent = file.getBytes();
            String base64Audio = Base64.getEncoder().encodeToString(fileContent);
            
            // Log base64 data length
            System.out.println("Base64 data length: " + base64Audio.length());
            
            // Create a new MusicEntity
            MusicEntity music = new MusicEntity();
            music.setTitle(title);
            music.setArtist(artist);
            if (genre != null && !genre.isEmpty()) {
                music.setGenre(genre);
            } else {
                music.setGenre("Unknown");
            }
            music.setAudioData(base64Audio);
            
            // Save the music entity
            MusicEntity savedMusic = musicService.postMusic(music);
            System.out.println("Saved music with ID: " + savedMusic.getMusicId());
            
            // Create a simple response with just the essential information
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("status", "success");
            response.put("message", "Music uploaded successfully");
            response.put("musicId", savedMusic.getMusicId());
            response.put("title", savedMusic.getTitle());
            response.put("artist", savedMusic.getArtist());
            response.put("genre", savedMusic.getGenre());
            
            // Return the simple response
            return new ResponseEntity<>(response, HttpStatus.CREATED);
            
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload file: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process upload: " + e.getMessage());
        }
    }

    // Get all music
    @GetMapping("/getAllMusic")
    public ResponseEntity<List<MusicDTO>> getAllMusic() {
        List<MusicEntity> musicList = musicService.getAllMusic();
        List<MusicDTO> musicDTOList = musicList.stream()
                .map(MusicDTO::new)
                .collect(Collectors.toList());
        return new ResponseEntity<>(musicDTOList, HttpStatus.OK);
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
                
                return new ResponseEntity<>(response, HttpStatus.OK);
            }
            
            // Return the DTO without audio data
            return new ResponseEntity<>(new MusicDTO(music), HttpStatus.OK);
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
    public ResponseEntity<MusicDTO> updateMusic(@PathVariable("id") int id, @RequestBody MusicEntity music) {
        try {
            MusicEntity updatedMusic = musicService.putMusic(id, music);
            return new ResponseEntity<>(new MusicDTO(updatedMusic), HttpStatus.OK);
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
    public ResponseEntity<List<MusicDTO>> searchByTitle(@RequestParam String title) {
        List<MusicEntity> musicList = musicService.findByTitle(title);
        List<MusicDTO> musicDTOList = musicList.stream()
                .map(MusicDTO::new)
                .collect(Collectors.toList());
        return new ResponseEntity<>(musicDTOList, HttpStatus.OK);
    }

    // Search by artist
    @GetMapping("/search/artist")
    public ResponseEntity<List<MusicDTO>> searchByArtist(@RequestParam String artist) {
        List<MusicEntity> musicList = musicService.findByArtist(artist);
        List<MusicDTO> musicDTOList = musicList.stream()
                .map(MusicDTO::new)
                .collect(Collectors.toList());
        return new ResponseEntity<>(musicDTOList, HttpStatus.OK);
    }

    // Search by genre
    @GetMapping("/search/genre")
    public ResponseEntity<List<MusicDTO>> searchByGenre(@RequestParam String genre) {
        List<MusicEntity> musicList = musicService.findByGenre(genre);
        List<MusicDTO> musicDTOList = musicList.stream()
                .map(MusicDTO::new)
                .collect(Collectors.toList());
        return new ResponseEntity<>(musicDTOList, HttpStatus.OK);
    }

    // Search by duration less than or equal to
    @GetMapping("/search/duration/max")
    public ResponseEntity<List<MusicDTO>> searchByMaxDuration(@RequestParam Integer maxTime) {
        List<MusicEntity> musicList = musicService.findByMaxDuration(maxTime);
        List<MusicDTO> musicDTOList = musicList.stream()
                .map(MusicDTO::new)
                .collect(Collectors.toList());
        return new ResponseEntity<>(musicDTOList, HttpStatus.OK);
    }

    // Search by duration greater than or equal to
    @GetMapping("/search/duration/min")
    public ResponseEntity<List<MusicDTO>> searchByMinDuration(@RequestParam Integer minTime) {
        List<MusicEntity> musicList = musicService.findByMinDuration(minTime);
        List<MusicDTO> musicDTOList = musicList.stream()
                .map(MusicDTO::new)
                .collect(Collectors.toList());
        return new ResponseEntity<>(musicDTOList, HttpStatus.OK);
    }
}