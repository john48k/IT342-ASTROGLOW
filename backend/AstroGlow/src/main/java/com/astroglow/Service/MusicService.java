package com.astroglow.Service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.astroglow.Entity.MusicEntity;
import com.astroglow.Repository.MusicRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class MusicService {
    @Autowired
    MusicRepository mrepo;

    public MusicService() {
        super();
        // TODO Auto-generated constructor stub
    }

    public MusicEntity postMusic(MusicEntity music) {
        return mrepo.save(music);
    }

    public List<MusicEntity> getAllMusic() {
        return mrepo.findAll();
    }

    @SuppressWarnings("finally")
    public MusicEntity putMusic(int musicId, MusicEntity newMusicDetails) {
        MusicEntity music = new MusicEntity();

        try {
            music = mrepo.findById(musicId).get();

            music.setTitle(newMusicDetails.getTitle());
            music.setArtist(newMusicDetails.getArtist());
            music.setGenre(newMusicDetails.getGenre());
            music.setTime(newMusicDetails.getTime());
            // You may decide if you want to update the relationships as well
            // music.setPlaylists(newMusicDetails.getPlaylists());
            // music.setOfflineLibraries(newMusicDetails.getOfflineLibraries());
            // music.setFavorites(newMusicDetails.getFavorites());

        } catch(NoSuchElementException nex) {
            throw new EntityNotFoundException("Music " + musicId + " not found!");
        }
        finally {
            return mrepo.save(music);
        }
    }

    public String deleteMusic(int id) {
        String msg = "";
        if(mrepo.findById(id).isPresent()) {
            mrepo.deleteById(id);
            msg = "Music successfully deleted.";
        } else {
            msg = id + " not found.";
        }
        return msg;
    }

    // Find music by title
    public List<MusicEntity> findByTitle(String title) {
        return mrepo.findByTitleContainingIgnoreCase(title);
    }

    // Find music by artist
    public List<MusicEntity> findByArtist(String artist) {
        return mrepo.findByArtistContainingIgnoreCase(artist);
    }

    // Find music by genre
    public List<MusicEntity> findByGenre(String genre) {
        return mrepo.findByGenreContainingIgnoreCase(genre);
    }

    // Find music by exact title match
    public List<MusicEntity> findByExactTitle(String title) {
        return mrepo.findByTitle(title);
    }

    // Find music by exact artist match
    public List<MusicEntity> findByExactArtist(String artist) {
        return mrepo.findByArtist(artist);
    }

    // Find music by exact genre match
    public List<MusicEntity> findByExactGenre(String genre) {
        return mrepo.findByGenre(genre);
    }

    // Get a single music by ID
    public MusicEntity getMusicById(int id) {
        return mrepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Music with ID " + id + " not found"));
    }

    // Get all music by owner (user)
    public List<MusicEntity> getMusicByOwnerId(int userId) {
        return mrepo.findByOwnerUserId(userId);
    }

    // Find music by maximum duration
    public List<MusicEntity> findByMaxDuration(Integer maxTime) {
        return mrepo.findByTimeLessThanEqual(maxTime);
    }

    // Find music by minimum duration
    public List<MusicEntity> findByMinDuration(Integer minTime) {
        return mrepo.findByTimeGreaterThanEqual(minTime);
    }

    // Check if a song with the same title and artist already exists
    public boolean existsByTitleAndArtist(String title, String artist) {
        List<MusicEntity> existingMusic = mrepo.findByTitleAndArtist(title, artist);
        return !existingMusic.isEmpty();
    }
}