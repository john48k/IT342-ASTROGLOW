package com.astroglow.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "FAVORITES")
public class FavoritesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "favorite_id")
    private int favoriteId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "userId", nullable = false)
    @JsonBackReference(value = "user-favorites")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "music_id", referencedColumnName = "musicId", nullable = false)
    @JsonBackReference(value = "music-favorites")    
    private MusicEntity music;
    
    // Timestamp for when this favorite was created
    @Column(name = "created_at", insertable = false, updatable = false, 
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private java.sql.Timestamp createdAt;
    
    public FavoritesEntity() {
    }

    public FavoritesEntity(int favoriteId, UserEntity user, MusicEntity music) {
        this.favoriteId = favoriteId;
        this.user = user;
        this.music = music;
    }
    
    /**
     * Create a new favorite relationship
     * @param user The user who is favoriting
     * @param music The music being favorited
     * @return A new FavoritesEntity with the relationship
     */
    public static FavoritesEntity createFavorite(UserEntity user, MusicEntity music) {
        FavoritesEntity favorite = new FavoritesEntity();
        favorite.setUser(user);
        favorite.setMusic(music);
        return favorite;
    }

    public int getFavoriteId() {
        return favoriteId;
    }

    public void setFavoriteId(int favoriteId) {
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
    
    public java.sql.Timestamp getCreatedAt() {
        return createdAt;
    }

    /**
     * Simple representation of the favorite for API responses
     * @return A string representation of the favorite
     */
    @Override
    public String toString() {
        return String.format("Favorite[id=%d, user=%s, music=%s]", 
                favoriteId, 
                user != null ? user.getUserName() : "null", 
                music != null ? music.getTitle() : "null");
    }
    
    /**
     * Equals method to compare favorites
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FavoritesEntity favorite = (FavoritesEntity) o;
        return favoriteId == favorite.favoriteId;
    }
    
    /**
     * Hash code for favorites
     */
    @Override
    public int hashCode() {
        return Objects.hash(favoriteId);
    }
}