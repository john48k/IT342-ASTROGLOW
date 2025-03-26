package com.astroglow.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "FAVORITES")
public class FavoritesEntity {

    @Id

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "favorite_id")
    private Long favoriteId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "music_id", nullable = false)
    private MusicEntity music;

    public FavoritesEntity() {
    }

    public FavoritesEntity(Long favoriteId, UserEntity user, MusicEntity music) {
        this.favoriteId = favoriteId;
        this.user = user;
        this.music = music;
    }

    public Long getFavoriteId() {
        return favoriteId;
    }

    public void setFavoriteId(Long favoriteId) {
        this.favoriteId = favoriteId;
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