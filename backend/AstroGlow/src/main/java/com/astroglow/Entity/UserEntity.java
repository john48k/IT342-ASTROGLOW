package com.astroglow.Entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.util.List;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "user_name", name = "uk_user_name"),
        @UniqueConstraint(columnNames = "user_email", name = "uk_user_email")
})
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userId;

    @Column(name = "user_name", length = 255, nullable = false, unique = true)
    private String userName;

    @Column(name = "user_password", length = 255, nullable = false)
    private String userPassword;

    @Column(name = "user_email", length = 255, nullable = false, unique = true)
    private String userEmail;
    
    @Lob
    @Column(name = "profile_picture", columnDefinition = "LONGTEXT")
    private String profilePicture;

    @JsonManagedReference(value = "user-authentication")
    @OneToOne(mappedBy = "user", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private AuthenticationEntity authentication;

    @JsonManagedReference(value = "user-music")
    @OneToMany(mappedBy = "owner", cascade = CascadeType.PERSIST)
    private List<MusicEntity> music;

    @JsonManagedReference(value = "user-playlist")
    @OneToMany(mappedBy = "user", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<PlaylistEntity> playlists;

    @JsonManagedReference(value = "user-offline")
    @OneToMany(mappedBy = "user", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<OfflineLibraryEntity> offlineLibraries;

    @JsonManagedReference(value = "user-favorites")
    @OneToMany(mappedBy = "user", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<FavoritesEntity> favorites;

    // Default Constructor (Required by JPA)
    public UserEntity() {
    }

    public UserEntity(int userId, String userName, String userPassword, String userEmail, String profilePicture, AuthenticationEntity authentication, List<MusicEntity> music, List<PlaylistEntity> playlists, List<OfflineLibraryEntity> offlineLibraries, List<FavoritesEntity> favorites) {
        this.userId = userId;
        this.userName = userName;
        this.userPassword = userPassword;
        this.userEmail = userEmail;
        this.profilePicture = profilePicture;
        this.authentication = authentication;
        this.music = music;
        this.playlists = playlists;
        this.offlineLibraries = offlineLibraries;
        this.favorites = favorites;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
    
    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public AuthenticationEntity getAuthentication() {
        return authentication;
    }

    public void setAuthentication(AuthenticationEntity authentication) {
        this.authentication = authentication;
    }

    public List<MusicEntity> getMusic() {
        return music;
    }

    public void setMusic(List<MusicEntity> music) {
        this.music = music;
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