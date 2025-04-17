package com.astroglow.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.astroglow.Entity.OfflineLibraryEntity;
import com.astroglow.Entity.MusicEntity;
import com.astroglow.Entity.UserEntity;

@Repository
public interface OfflineLibraryRepository extends JpaRepository<OfflineLibraryEntity, Integer> {

    // Find by user
    List<OfflineLibraryEntity> findByUser(UserEntity user);

    // Find by music
    List<OfflineLibraryEntity> findByMusic(MusicEntity music);

    // Find by user and music
    Optional<OfflineLibraryEntity> findByUserAndMusic(UserEntity user, MusicEntity music);

    // Check if exists by user and music
    boolean existsByUserAndMusic(UserEntity user, MusicEntity music);

    // Count by user
    long countByUser(UserEntity user);

    // Find by file path containing
    List<OfflineLibraryEntity> findByFilePathContaining(String filePath);
}
//Test Commit