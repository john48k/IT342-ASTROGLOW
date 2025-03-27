package com.astroglow.Controller;

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

import com.astroglow.Entity.MusicEntity;
import com.astroglow.Service.MusicService;

import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/api/music")
public class MusicController {

    @Autowired
    private MusicService musicService;

    // Create new music
    @PostMapping("/postMusic")
    public ResponseEntity<MusicEntity> createMusic(@RequestBody MusicEntity music) {
        MusicEntity savedMusic = musicService.postMusic(music);
        return new ResponseEntity<>(savedMusic, HttpStatus.CREATED);
    }

    // Get all music
    @GetMapping("/getAllMusic")
    public ResponseEntity<List<MusicEntity>> getAllMusic() {
        List<MusicEntity> musicList = musicService.getAllMusic();
        return new ResponseEntity<>(musicList, HttpStatus.OK);
    }

    // Get music by ID
    @GetMapping("/getMusic/{id}")
    public ResponseEntity<MusicEntity> getMusicById(@PathVariable("id") int id) {
        try {
            MusicEntity music = musicService.getMusicById(id);
            return new ResponseEntity<>(music, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
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
}