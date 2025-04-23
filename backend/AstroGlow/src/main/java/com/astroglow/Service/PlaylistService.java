package com.astroglow.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.ArrayList;

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
        // Check if the music exists first
        if (!musicRepo.existsById(musicId)) {
            throw new EntityNotFoundException("Music with ID " + musicId + " not found");
        }
        return playlistRepo.findByMusicId(musicId);
    }

    // Create a new playlist
    @Transactional
    public PlaylistEntity createPlaylist(int userId, String name) {
        // Check if the user exists
        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with ID " + userId + " not found"));

        // Create a new playlist
        PlaylistEntity playlist = new PlaylistEntity();
        playlist.setUser(user);
        playlist.setName(name != null && !name.trim().isEmpty() ? name.trim() : "New Playlist");
        playlist.setMusic(new ArrayList<>());
        
        return playlistRepo.save(playlist);
    }

    // Add a playlist entry (legacy method kept for backward compatibility)
    @Transactional
    public PlaylistEntity addPlaylistEntry(PlaylistEntity playlist) {
        // Check if the user exists
        UserEntity user = userRepo.findById(playlist.getUser().getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User with ID " + playlist.getUser().getUserId() + " not found"));

        // Create a new playlist if one doesn't exist
        PlaylistEntity newPlaylist;
        if (playlist.getPlaylistId() > 0) {
            // Use existing playlist
            newPlaylist = playlistRepo.findById(playlist.getPlaylistId())
                    .orElseThrow(() -> new EntityNotFoundException("Playlist with ID " + playlist.getPlaylistId() + " not found"));
        } else {
            // Create a new playlist
            newPlaylist = new PlaylistEntity();
            newPlaylist.setUser(user);
            String name = playlist.getName() != null ? playlist.getName() : "New Playlist";
            newPlaylist.setName(name);
            newPlaylist = playlistRepo.save(newPlaylist);
        }

        return newPlaylist;
    }

    // Add a music to a playlist
    @Transactional
    public PlaylistEntity addToPlaylist(int playlistId, int musicId) {
        // Check if the playlist exists
        PlaylistEntity playlist = playlistRepo.findById(playlistId)
                .orElseThrow(() -> new EntityNotFoundException("Playlist with ID " + playlistId + " not found"));

        // Check if the music exists
        MusicEntity music = musicRepo.findById(musicId)
                .orElseThrow(() -> new EntityNotFoundException("Music with ID " + musicId + " not found"));

        // Check if the music is already in the playlist
        if (playlist.getMusic().stream().anyMatch(m -> m.getMusicId() == musicId)) {
            throw new IllegalStateException("This music is already in the playlist");
        }

        // Add the music to the playlist
        playlist.addMusic(music);
        return playlistRepo.save(playlist);
    }

    // Add a music to a user's playlist (convenience method)
    @Transactional
    public PlaylistEntity addToPlaylist(int userId, int musicId, int playlistId) {
        // Check if the user exists
        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with ID " + userId + " not found"));

        // Check if the playlist exists and belongs to the user
        PlaylistEntity playlist = playlistRepo.findById(playlistId)
                .orElseThrow(() -> new EntityNotFoundException("Playlist with ID " + playlistId + " not found"));

        if (playlist.getUser().getUserId() != userId) {
            throw new IllegalStateException("This playlist doesn't belong to the specified user");
        }

        // Check if the music exists
        MusicEntity music = musicRepo.findById(musicId)
                .orElseThrow(() -> new EntityNotFoundException("Music with ID " + musicId + " not found"));

        // Check if the music is already in the playlist
        if (playlist.getMusic().stream().anyMatch(m -> m.getMusicId() == musicId)) {
            throw new IllegalStateException("This music is already in the playlist");
        }

        // Add the music to the playlist
        playlist.addMusic(music);
        return playlistRepo.save(playlist);
    }

    // Remove a music from a playlist
    @Transactional
    public PlaylistEntity removeFromPlaylist(int playlistId, int musicId) {
        // Check if the playlist exists
        PlaylistEntity playlist = playlistRepo.findById(playlistId)
                .orElseThrow(() -> new EntityNotFoundException("Playlist with ID " + playlistId + " not found"));

        // Check if the music exists
        MusicEntity music = musicRepo.findById(musicId)
                .orElseThrow(() -> new EntityNotFoundException("Music with ID " + musicId + " not found"));

        // Check if the music is in the playlist
        if (playlist.getMusic().stream().noneMatch(m -> m.getMusicId() == musicId)) {
            throw new IllegalStateException("This music is not in the playlist");
        }

        // Remove the music from the playlist
        playlist.removeMusic(music);
        return playlistRepo.save(playlist);
    }

    // Update a playlist (name)
    @Transactional
    public PlaylistEntity updatePlaylist(int playlistId, String newName) {
        PlaylistEntity playlist = playlistRepo.findById(playlistId)
                .orElseThrow(() -> new EntityNotFoundException("Playlist with ID " + playlistId + " not found"));

        // Update the name if provided
        if (newName != null && !newName.trim().isEmpty()) {
            playlist.setName(newName.trim());
        }

        return playlistRepo.save(playlist);
    }

    // Delete a playlist
    @Transactional
    public String deletePlaylist(int playlistId) {
        if (playlistRepo.existsById(playlistId)) {
            playlistRepo.deleteById(playlistId);
            return "Playlist successfully deleted";
        } else {
            return "Playlist with ID " + playlistId + " not found";
        }
    }

    // Check if a music is in a playlist
    public boolean isInPlaylist(int playlistId, int musicId) {
        try {
            PlaylistEntity playlist = playlistRepo.findById(playlistId)
                    .orElseThrow(() -> new EntityNotFoundException("Playlist with ID " + playlistId + " not found"));
            
            return playlist.getMusic().stream().anyMatch(m -> m.getMusicId() == musicId);
        } catch (EntityNotFoundException e) {
            return false;
        }
    }

    // Find music by title/filename (used for Firebase music files)
    public List<MusicEntity> findMusicByTitle(String title) {
        return musicRepo.findByTitleContainingIgnoreCase(title);
    }
    
    // Find music by audio URL (used for Firebase music files)
    public List<MusicEntity> findMusicByAudioUrl(String url) {
        // Ideally, this would query the database for music with the given URL
        // Since there's no repository method for this yet, we'll query all music and filter
        List<MusicEntity> allMusic = musicRepo.findAll();
        return allMusic.stream()
            .filter(music -> music.getAudioUrl() != null && 
                   music.getAudioUrl().contains(url))
            .collect(java.util.stream.Collectors.toList());
    }

    public List<PlaylistEntity> getUserPlaylists(int userId) {
        return playlistRepo.findByUserUserId(userId);
    }

    public void addSongToPlaylist(int playlistId, int musicId) {
        PlaylistEntity playlist = playlistRepo.findById(playlistId)
            .orElseThrow(() -> new RuntimeException("Playlist not found"));
        
        MusicEntity music = musicRepo.findById(musicId)
            .orElseThrow(() -> new RuntimeException("Music not found"));
        
        playlist.getMusic().add(music);
        playlistRepo.save(playlist);
    }

    public void removeSongFromPlaylist(int playlistId, int musicId) {
        PlaylistEntity playlist = playlistRepo.findById(playlistId)
            .orElseThrow(() -> new RuntimeException("Playlist not found"));
        
        MusicEntity music = musicRepo.findById(musicId)
            .orElseThrow(() -> new RuntimeException("Music not found"));
        
        playlist.getMusic().remove(music);
        playlistRepo.save(playlist);
    }

    public void deletePlaylistById(int playlistId) {
        PlaylistEntity playlist = playlistRepo.findById(playlistId)
            .orElseThrow(() -> new RuntimeException("Playlist not found"));
        
        playlistRepo.delete(playlist);
    }
}