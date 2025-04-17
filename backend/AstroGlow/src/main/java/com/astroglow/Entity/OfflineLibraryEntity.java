package com.astroglow.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "OFFLINE_LIBRARY")
public class OfflineLibraryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "offlinelib_id")
    private int offlinelibId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "userId", nullable = false)
    @JsonBackReference(value = "user-offline")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "music_id", referencedColumnName = "musicId", nullable = false)
    @JsonBackReference(value = "music-offline")
    private MusicEntity music;

    @Column(name = "file_path", length = 255, nullable = false)
    private String filePath;

    public OfflineLibraryEntity() {
    }

    public OfflineLibraryEntity(int offlinelibId, UserEntity user, MusicEntity music, String filePath) {
        this.offlinelibId = offlinelibId;
        this.user = user;
        this.music = music;
        this.filePath = filePath;
    }

    public int getOfflinelibId() {
        return offlinelibId;
    }

    public void setOfflinelibId(int offlinelibId) {
        this.offlinelibId = offlinelibId;
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

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
//Test Commit