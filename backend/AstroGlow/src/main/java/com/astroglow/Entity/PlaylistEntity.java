package com.astroglow.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "PLAYLIST")
public class PlaylistEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "playlist_id")
    private Long playlistId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "music_id", nullable = false)
    private MusicEntity music;

    public PlaylistEntity() {
    }

    public PlaylistEntity(Long playlistId, UserEntity user, MusicEntity music) {
        this.playlistId = playlistId;
        this.user = user;
        this.music = music;
    }

    public Long getPlaylistId() {
        return playlistId;
    }

    public void setPlaylistId(Long playlistId) {
        this.playlistId = playlistId;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public MusicEntity getMusic() {
        return music;
    }

    public void setMusic(MusicEntity music) {
        this.music = music;
    }
}