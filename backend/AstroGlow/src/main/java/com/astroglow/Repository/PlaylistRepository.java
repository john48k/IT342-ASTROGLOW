package com.astroglow.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.astroglow.Entity.PlaylistEntity;
import com.astroglow.Entity.MusicEntity;
import com.astroglow.Entity.UserEntity;

@Repository
public interface PlaylistRepository extends JpaRepository<PlaylistEntity, Integer> {

    // Find by user
    List<PlaylistEntity> findByUser(UserEntity user);
    
    // Find by name containing
    List<PlaylistEntity> findByNameContainingIgnoreCase(String name);
    
    // Find by user and name
    Optional<PlaylistEntity> findByUserAndNameIgnoreCase(UserEntity user, String name);

    // Find playlists containing a specific music
    @Query("SELECT p FROM PlaylistEntity p JOIN p.music m WHERE m.musicId = :musicId")
    List<PlaylistEntity> findByMusicId(@Param("musicId") int musicId);
    
    // Find by user and containing specific music
    @Query("SELECT p FROM PlaylistEntity p JOIN p.music m WHERE p.user.userId = :userId AND m.musicId = :musicId")
    List<PlaylistEntity> findByUserIdAndMusicId(@Param("userId") int userId, @Param("musicId") int musicId);
    
    // Check if playlist with name exists for user
    boolean existsByUserAndNameIgnoreCase(UserEntity user, String name);

    List<PlaylistEntity> findByUserUserId(int userId);
}