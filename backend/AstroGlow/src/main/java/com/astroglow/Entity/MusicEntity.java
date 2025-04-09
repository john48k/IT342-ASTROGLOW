package com.astroglow.Entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

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

    @JsonManagedReference(value = "music-playlist")
    @OneToMany(mappedBy = "music", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<PlaylistEntity> playlists;

    @JsonManagedReference(value = "music-offline")
    @OneToMany(mappedBy = "music", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<OfflineLibraryEntity> offlineLibraries;

    @JsonManagedReference(value = "music-favorites")
    @OneToMany(mappedBy = "music", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<FavoritesEntity> favorites;

    public MusicEntity() {
    }

    public MusicEntity(int musicId, String title, String artist, String genre, Integer time, String audioData, List<PlaylistEntity> playlists, List<OfflineLibraryEntity> offlineLibraries, List<FavoritesEntity> favorites) {
        this.musicId = musicId;
        this.title = title;
        this.artist = artist;
        this.genre = genre;
        this.time = time;
        this.audioData = audioData;
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
}