package com.astroglow.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.astroglow.Entity.MusicEntity;

@Repository
public interface MusicRepository extends JpaRepository<MusicEntity, Integer> {

    // Find by title containing the search term (case insensitive)
    List<MusicEntity> findByTitleContainingIgnoreCase(String title);

    // Find by artist containing the search term (case insensitive)
    List<MusicEntity> findByArtistContainingIgnoreCase(String artist);

    // Find by genre containing the search term (case insensitive)
    List<MusicEntity> findByGenreContainingIgnoreCase(String genre);

    // Find by title and artist
    List<MusicEntity> findByTitleAndArtist(String title, String artist);

    // Find by time less than or equal to specified duration
    List<MusicEntity> findByTimeLessThanEqual(Integer time);

    // Find by time greater than or equal to specified duration
    List<MusicEntity> findByTimeGreaterThanEqual(Integer time);
}