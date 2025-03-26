package com.astroglow.Entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "MUSIC")
public class MusicEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "music_id")
    private Long musicId;

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Column(name = "artist", length = 255, nullable = false)
    private String artist;

    @Column(name = "genre", length = 255)
    private String genre;

    @Column(name = "time")
    private Integer time;

    @OneToMany(mappedBy = "music", cascade = CascadeType.ALL)
    private List<PlaylistEntity> playlists;

    @OneToMany(mappedBy = "music", cascade = CascadeType.ALL)
    private List<OfflineLibraryEntity> offlineLibraries;

    @OneToMany(mappedBy = "music", cascade = CascadeType.ALL)
    private List<FavoritesEntity> favorites;

    public MusicEntity() {
    }

    public MusicEntity(Long musicId, String title, String artist, String genre, Integer time, List<PlaylistEntity> playlists, List<OfflineLibraryEntity> offlineLibraries, List<FavoritesEntity> favorites) {
        this.musicId = musicId;
        this.title = title;
        this.artist = artist;
        this.genre = genre;
        this.time = time;
        this.playlists = playlists;
        this.offlineLibraries = offlineLibraries;
        this.favorites = favorites;
    }

    public Long getMusicId() {
        return musicId;
    }

    public void setMusicId(Long musicId) {
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