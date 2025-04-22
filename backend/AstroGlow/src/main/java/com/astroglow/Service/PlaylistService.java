package com.astroglow.Service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.astroglow.Entity.PlaylistEntity;
import com.astroglow.Entity.MusicEntity;
import com.astroglow.Entity.UserEntity;
import com.astroglow.Repository.PlaylistRepository;
import com.astroglow.Repository.MusicRepository;
import com.astroglow.Repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class PlaylistService {

    @Autowired
    private PlaylistRepository playlistRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private MusicRepository musicRepo;

    public PlaylistService() {
        super();
    }

    // Get all playlists
    public List<PlaylistEntity> getAllPlaylists() {
        return playlistRepo.findAll();
    }

    // Get playlist by ID
    public PlaylistEntity getPlaylistById(int id) {
        return playlistRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Playlist with ID " + id + " not found"));
    }

    // Get playlists by user ID
    public List<PlaylistEntity> getPlaylistsByUserId(int userId) {
        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with ID " + userId + " not found"));
        return playlistRepo.findByUser(user);
    }

    // Get playlists containing a specific music
    public List<PlaylistEntity> getPlaylistsByMusicId(int musicId) {
        MusicEntity music = musicRepo.findById(musicId)
                .orElseThrow(() -> new EntityNotFoundException("Music with ID " + musicId + " not found"));
        return playlistRepo.findByMusic(music);
    }

    // Add a music to a user's playlist
    @Transactional
    public PlaylistEntity addToPlaylist(int userId, int musicId) {
        // Check if the user exists
        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with ID " + userId + " not found"));

        // Check if the music exists
        MusicEntity music = musicRepo.findById(musicId)
                .orElseThrow(() -> new EntityNotFoundException("Music with ID " + musicId + " not found"));

        // Check if the music is already in the user's playlist
        if (playlistRepo.existsByUserAndMusic(user, music)) {
            throw new IllegalStateException("This music is already in the user's playlist");
        }

        // Create a new playlist entry
        PlaylistEntity playlist = new PlaylistEntity();
        playlist.setUser(user);
        playlist.setMusic(music);

        return playlistRepo.save(playlist);
    }

    // Add a playlist entry (using entity)
    @Transactional
    public PlaylistEntity addPlaylistEntry(PlaylistEntity playlist) {
        // Check if the user exists
        UserEntity user = userRepo.findById(playlist.getUser().getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User with ID " + playlist.getUser().getUserId() + " not found"));

        // Check if the music exists
        MusicEntity music = musicRepo.findById(playlist.getMusic().getMusicId())
                .orElseThrow(() -> new EntityNotFoundException("Music with ID " + playlist.getMusic().getMusicId() + " not found"));

        // Check if the music is already in the user's playlist
        if (playlistRepo.existsByUserAndMusic(user, music)) {
            throw new IllegalStateException("This music is already in the user's playlist");
        }

        // Set the proper entities (to ensure we have the complete entity, not just ID)
        playlist.setUser(user);
        playlist.setMusic(music);

        return playlistRepo.save(playlist);
    }

    // Update a playlist entry
    @Transactional
    public PlaylistEntity updatePlaylistEntry(int playlistId, PlaylistEntity newPlaylistDetails) {
        PlaylistEntity playlist;

        try {
            playlist = playlistRepo.findById(playlistId).get();

            // Check if the user exists if provided
            if (newPlaylistDetails.getUser() != null) {
                UserEntity user = userRepo.findById(newPlaylistDetails.getUser().getUserId())
                        .orElseThrow(() -> new EntityNotFoundException("User with ID " + newPlaylistDetails.getUser().getUserId() + " not found"));
                playlist.setUser(user);
            }

            // Check if the music exists if provided
            if (newPlaylistDetails.getMusic() != null) {
                MusicEntity music = musicRepo.findById(newPlaylistDetails.getMusic().getMusicId())
                        .orElseThrow(() -> new EntityNotFoundException("Music with ID " + newPlaylistDetails.getMusic().getMusicId() + " not found"));
                playlist.setMusic(music);
            }

        } catch (NoSuchElementException nex) {
            throw new EntityNotFoundException("Playlist entry with ID " + playlistId + " not found");
        }

        return playlistRepo.save(playlist);
    }

    // Delete a playlist entry
    @Transactional
    public String deletePlaylistEntry(int playlistId) {
        if (playlistRepo.existsById(playlistId)) {
            playlistRepo.deleteById(playlistId);
            return "Playlist entry successfully removed";
        } else {
            return "Playlist entry with ID " + playlistId + " not found";
        }
    }

    // Remove a music from a user's playlist
    @Transactional
    public String removeFromPlaylist(int userId, int musicId) {
        // Check if the user exists
        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with ID " + userId + " not found"));

        // Check if the music exists
        MusicEntity music = musicRepo.findById(musicId)
                .orElseThrow(() -> new EntityNotFoundException("Music with ID " + musicId + " not found"));

        // Find the playlist entry
        PlaylistEntity playlist = playlistRepo.findByUserAndMusic(user, music)
                .orElseThrow(() -> new EntityNotFoundException("This music is not in the user's playlist"));

        // Delete the playlist entry
        playlistRepo.deleteById(playlist.getPlaylistId());

        return "Music successfully removed from playlist";
    }

    // Check if a music is in a user's playlist
    public boolean isInPlaylist(int userId, int musicId) {
        // Check if the user exists
        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with ID " + userId + " not found"));

        // Check if the music exists
        MusicEntity music = musicRepo.findById(musicId)
                .orElseThrow(() -> new EntityNotFoundException("Music with ID " + musicId + " not found"));

        return playlistRepo.existsByUserAndMusic(user, music);
    }
}