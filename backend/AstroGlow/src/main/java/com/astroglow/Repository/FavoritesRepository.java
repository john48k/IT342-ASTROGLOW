package com.astroglow.Repository;

import com.astroglow.Entity.FavoritesEntity;
import com.astroglow.Entity.MusicEntity;
import com.astroglow.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoritesRepository extends JpaRepository<FavoritesEntity, Integer> {

    // Find by user
    List<FavoritesEntity> findByUser(UserEntity user);

    // Find by music
    List<FavoritesEntity> findByMusic(MusicEntity music);

    // Find by user and music
    Optional<FavoritesEntity> findByUserAndMusic(UserEntity user, MusicEntity music);

    // Check if exists by user and music
    boolean existsByUserAndMusic(UserEntity user, MusicEntity music);

    // Count by user
    long countByUser(UserEntity user);

    // Count by music
    long countByMusic(MusicEntity music);
}
//Test Commit