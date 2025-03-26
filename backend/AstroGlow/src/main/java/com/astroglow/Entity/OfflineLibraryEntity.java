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
}
