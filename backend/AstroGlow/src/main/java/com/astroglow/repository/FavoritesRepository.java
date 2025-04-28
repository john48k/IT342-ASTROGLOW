package com.astroglow.repository;

import com.astroglow.entity.FavoritesEntity;
import com.astroglow.entity.MusicEntity;
import com.astroglow.entity.UserEntity;
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
