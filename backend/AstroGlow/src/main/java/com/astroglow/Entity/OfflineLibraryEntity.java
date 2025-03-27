package com.astroglow.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "OFFLINE_LIBRARY")
public class OfflineLibraryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "offlinelib_id")
    private Long offlinelibId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "music_id", nullable = false)
    private MusicEntity music;

    @Column(name = "file_path", length = 255, nullable = false)
    private String filePath;

    public OfflineLibraryEntity() {
    }

    public OfflineLibraryEntity(Long offlinelibId, UserEntity user, MusicEntity music, String filePath) {
        this.offlinelibId = offlinelibId;
        this.user = user;
        this.music = music;
        this.filePath = filePath;
    }

    public Long getOfflinelibId() {
        return offlinelibId;
    }

    public void setOfflinelibId(Long offlinelibId) {
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
