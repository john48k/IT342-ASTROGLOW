package com.astroglow.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.astroglow.Entity.PlaylistEntity;
import com.astroglow.Entity.MusicEntity;
import com.astroglow.Entity.UserEntity;

@Repository
public interface PlaylistRepository extends JpaRepository<PlaylistEntity, Integer> {

    // Find by user
    List<PlaylistEntity> findByUser(UserEntity user);

    // Find by music
    List<PlaylistEntity> findByMusic(MusicEntity music);

    // Find by user and music
    Optional<PlaylistEntity> findByUserAndMusic(UserEntity user, MusicEntity music);

    // Check if exists by user and music
    boolean existsByUserAndMusic(UserEntity user, MusicEntity music);

    // Count by user
    long countByUser(UserEntity user);

    // Count by music
    long countByMusic(MusicEntity music);
}
//Test Commit