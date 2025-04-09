package com.astroglow.dto;

import com.astroglow.Entity.MusicEntity;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class MusicDTO {
    private int musicId;
    private String title;
    private String artist;
    private String genre;
    private Integer time;
    private List<Integer> playlistIds = new ArrayList<>();
    private List<Integer> offlineLibraryIds = new ArrayList<>();
    private List<Integer> favoriteIds = new ArrayList<>();
    
    // Constructor
    public MusicDTO() {
    }
    
    // Constructor from MusicEntity
    public MusicDTO(MusicEntity music) {
        this.musicId = music.getMusicId();
        this.title = music.getTitle();
        this.artist = music.getArtist();
        this.genre = music.getGenre();
        this.time = music.getTime();
        
        // Extract IDs only from relationships
        if (music.getPlaylists() != null) {
            this.playlistIds = music.getPlaylists().stream()
                .map(playlist -> playlist.getPlaylistId())
                .collect(Collectors.toList());
        }
        
        if (music.getOfflineLibraries() != null) {
            this.offlineLibraryIds = music.getOfflineLibraries().stream()
                .map(offlineLibrary -> offlineLibrary.getOfflinelibId())
                .collect(Collectors.toList());
        }
        
        if (music.getFavorites() != null) {
            this.favoriteIds = music.getFavorites().stream()
                .map(favorite -> favorite.getFavoriteId())
                .collect(Collectors.toList());
        }
    }
    
    // Static method to convert a list of entities to DTOs
    public static List<MusicDTO> fromEntities(List<MusicEntity> entities) {
        return entities.stream()
                .map(MusicDTO::new)
                .collect(Collectors.toList());
    }
    
    // Getters and setters
    public int getMusicId() {
        return musicId;
    }
    
    public void setMusicId(int musicId) {
        this.musicId = musicId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getArtist() {
        return artist;
    }
    
    public void setArtist(String artist) {
        this.artist = artist;
    }
    
    public String getGenre() {
        return genre;
    }
    
    public void setGenre(String genre) {
        this.genre = genre;
    }
    
    public Integer getTime() {
        return time;
    }
    
    public void setTime(Integer time) {
        this.time = time;
    }
    
    public List<Integer> getPlaylistIds() {
        return playlistIds;
    }
    
    public void setPlaylistIds(List<Integer> playlistIds) {
        this.playlistIds = playlistIds;
    }
    
    public List<Integer> getOfflineLibraryIds() {
        return offlineLibraryIds;
    }
    
    public void setOfflineLibraryIds(List<Integer> offlineLibraryIds) {
        this.offlineLibraryIds = offlineLibraryIds;
    }
    
    public List<Integer> getFavoriteIds() {
        return favoriteIds;
    }
    
    public void setFavoriteIds(List<Integer> favoriteIds) {
        this.favoriteIds = favoriteIds;
    }
} 