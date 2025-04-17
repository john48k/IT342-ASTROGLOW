package com.astroglow.Entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "MUSIC")
public class MusicEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int musicId;

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Column(name = "artist", length = 255, nullable = false)
    private String artist;

    @Column(name = "genre", length = 255)
    private String genre;

    @Column(name = "time")
    private Integer time;

    @Lob
    @Column(name = "audio_data", columnDefinition = "LONGTEXT")
    @JsonIgnore
    private String audioData;

    @Column(name = "audio_url", length = 1024)
    private String audioUrl;

    @Lob
    @Column(name = "image_url", columnDefinition = "LONGTEXT")
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "userId")
    @JsonBackReference(value = "user-music")
    private UserEntity owner;

    @JsonManagedReference(value = "music-playlist")
    @OneToMany(mappedBy = "music", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<PlaylistEntity> playlists;

    @JsonManagedReference(value = "music-offline")
    @OneToMany(mappedBy = "music", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<OfflineLibraryEntity> offlineLibraries;

    @JsonManagedReference(value = "music-favorites")
    @OneToMany(mappedBy = "music", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FavoritesEntity> favorites = new ArrayList<>();

    public MusicEntity() {
    }

    public MusicEntity(int musicId, String title, String artist, String genre, Integer time, String audioData, String audioUrl, String imageUrl, UserEntity owner, List<PlaylistEntity> playlists, List<OfflineLibraryEntity> offlineLibraries, List<FavoritesEntity> favorites) {
        this.musicId = musicId;
        this.title = title;
        this.artist = artist;
        this.genre = genre;
        this.time = time;
        this.audioData = audioData;
        this.audioUrl = audioUrl;
        this.imageUrl = imageUrl;
        this.owner = owner;
        this.playlists = playlists;
        this.offlineLibraries = offlineLibraries;
        this.favorites = favorites;
    }

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

    public String getAudioData() {
        return audioData;
    }

    public void setAudioData(String audioData) {
        this.audioData = audioData;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<PlaylistEntity> getPlaylists() {
        return playlists;
    }

    public void setPlaylists(List<PlaylistEntity> playlists) {
        this.playlists = playlists;
    }

    public List<OfflineLibraryEntity> getOfflineLibraries() {
        return offlineLibraries;
    }

    public void setOfflineLibraries(List<OfflineLibraryEntity> offlineLibraries) {
        this.offlineLibraries = offlineLibraries;
    }

    public List<FavoritesEntity> getFavorites() {
        return favorites;
    }

    public void setFavorites(List<FavoritesEntity> favorites) {
        this.favorites = favorites;
    }
    
    // Helper methods for working with favorites

    /**
     * Check if this music is favorited by a specific user
     * @param userId The ID of the user to check
     * @return true if favorited, false otherwise
     */
    public boolean isFavoritedBy(int userId) {
        return favorites.stream()
            .anyMatch(fav -> fav.getUser() != null && fav.getUser().getUserId() == userId);
    }

    /**
     * Add this music to a user's favorites without needing a DTO
     * @param user The user who is favoriting this music
     * @return The created FavoritesEntity
     */
    public FavoritesEntity addToFavorites(UserEntity user) {
        // Check if already favorited by this user
        if (isFavoritedBy(user.getUserId())) {
            return favorites.stream()
                .filter(fav -> fav.getUser() != null && fav.getUser().getUserId() == user.getUserId())
                .findFirst()
                .orElse(null);
        }
        
        // Create new favorite
        FavoritesEntity favorite = new FavoritesEntity();
        favorite.setUser(user);
        favorite.setMusic(this);
        favorites.add(favorite);
        return favorite;
    }

    /**
     * Remove this music from a user's favorites
     * @param userId The ID of the user
     * @return true if removed, false if not found
     */
    public boolean removeFromFavorites(int userId) {
        List<FavoritesEntity> userFavorites = favorites.stream()
            .filter(fav -> fav.getUser() != null && fav.getUser().getUserId() == userId)
            .collect(Collectors.toList());
            
        return favorites.removeAll(userFavorites);
    }

    /**
     * Get the list of users who favorited this music
     * @return List of UserEntity objects
     */
    @JsonIgnore
    public List<UserEntity> getFavoritedByUsers() {
        return favorites.stream()
            .map(FavoritesEntity::getUser)
            .collect(Collectors.toList());
    }

    public UserEntity getOwner() {
        return owner;
    }

    public void setOwner(UserEntity owner) {
        this.owner = owner;
    }
}
//Test Commit