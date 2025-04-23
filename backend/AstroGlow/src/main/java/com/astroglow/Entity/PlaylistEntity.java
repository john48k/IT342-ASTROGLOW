package com.astroglow.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "PLAYLIST")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "playlistId")
public class PlaylistEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "playlist_id")
    private int playlistId;

    @Column(name = "playlist_name", nullable = false)
    private String name;

    @JsonBackReference(value = "user-playlist")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "userId", nullable = false)
    private UserEntity user;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "PLAYLIST_MUSIC",
        joinColumns = @JoinColumn(name = "playlist_id"),
        inverseJoinColumns = @JoinColumn(name = "music_id")
    )
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private List<MusicEntity> music = new ArrayList<>();

    public PlaylistEntity() {
    }

    public PlaylistEntity(int playlistId, String name, UserEntity user) {
        this.playlistId = playlistId;
        this.name = name;
        this.user = user;
    }

    public int getPlaylistId() {
        return playlistId;
    }

    public void setPlaylistId(int playlistId) {
        this.playlistId = playlistId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public List<MusicEntity> getMusic() {
        return music;
    }

    public void setMusic(List<MusicEntity> music) {
        this.music = music;
    }
    
    public void addMusic(MusicEntity music) {
        this.music.add(music);
    }
    
    public void removeMusic(MusicEntity music) {
        this.music.remove(music);
    }
}