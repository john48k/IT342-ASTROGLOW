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
}