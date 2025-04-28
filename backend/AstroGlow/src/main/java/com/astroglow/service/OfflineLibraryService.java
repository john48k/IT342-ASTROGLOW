package com.astroglow.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.astroglow.entity.OfflineLibraryEntity;
import com.astroglow.entity.MusicEntity;
import com.astroglow.entity.UserEntity;
import com.astroglow.repository.OfflineLibraryRepository;
import com.astroglow.repository.MusicRepository;
import com.astroglow.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class OfflineLibraryService {

    @Autowired
    private OfflineLibraryRepository offlineLibraryRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private MusicRepository musicRepo;

    public OfflineLibraryService() {
        super();
    }

    // Get all offline library entries
    public List<OfflineLibraryEntity> getAllOfflineLibraries() {
        return offlineLibraryRepo.findAll();
    }

    // Get offline library entry by ID
    public OfflineLibraryEntity getOfflineLibraryById(int id) {
        return offlineLibraryRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Offline library entry with ID " + id + " not found"));
    }

    // Get offline library entries by user ID
    public List<OfflineLibraryEntity> getOfflineLibrariesByUserId(int userId) {
        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with ID " + userId + " not found"));
        return offlineLibraryRepo.findByUser(user);
    }

    // Get offline library entries containing a specific music
    public List<OfflineLibraryEntity> getOfflineLibrariesByMusicId(int musicId) {
        MusicEntity music = musicRepo.findById(musicId)
                .orElseThrow(() -> new EntityNotFoundException("Music with ID " + musicId + " not found"));
        return offlineLibraryRepo.findByMusic(music);
    }

    // Add a music to a user's offline library
    @Transactional
    public OfflineLibraryEntity addToOfflineLibrary(int userId, int musicId, String filePath) {
        // Check if the user exists
        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with ID " + userId + " not found"));

        // Check if the music exists
        MusicEntity music = musicRepo.findById(musicId)
                .orElseThrow(() -> new EntityNotFoundException("Music with ID " + musicId + " not found"));

        // Check if the music is already in the user's offline library
        if (offlineLibraryRepo.existsByUserAndMusic(user, music)) {
            throw new IllegalStateException("This music is already in the user's offline library");
        }

        // Create a new offline library entry
        OfflineLibraryEntity offlineLibrary = new OfflineLibraryEntity();
        offlineLibrary.setUser(user);
        offlineLibrary.setMusic(music);
        offlineLibrary.setFilePath(filePath);

        return offlineLibraryRepo.save(offlineLibrary);
    }

    // Add an offline library entry (using entity)
    @Transactional
    public OfflineLibraryEntity addOfflineLibraryEntry(OfflineLibraryEntity offlineLibrary) {
        // Check if the user exists
        UserEntity user = userRepo.findById(offlineLibrary.getUser().getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User with ID " + offlineLibrary.getUser().getUserId() + " not found"));

        // Check if the music exists
        MusicEntity music = musicRepo.findById(offlineLibrary.getMusic().getMusicId())
                .orElseThrow(() -> new EntityNotFoundException("Music with ID " + offlineLibrary.getMusic().getMusicId() + " not found"));

        // Check if the music is already in the user's offline library
        if (offlineLibraryRepo.existsByUserAndMusic(user, music)) {
            throw new IllegalStateException("This music is already in the user's offline library");
        }

        // Set the proper entities (to ensure we have the complete entity, not just ID)
        offlineLibrary.setUser(user);
        offlineLibrary.setMusic(music);

        // Validate file path
        if (offlineLibrary.getFilePath() == null || offlineLibrary.getFilePath().trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be empty");
        }

        return offlineLibraryRepo.save(offlineLibrary);
    }

    // Update an offline library entry
    @Transactional
    public OfflineLibraryEntity updateOfflineLibraryEntry(int offlineLibraryId, OfflineLibraryEntity newOfflineLibraryDetails) {
        OfflineLibraryEntity offlineLibrary;

        try {
            offlineLibrary = offlineLibraryRepo.findById(offlineLibraryId).get();

            // Update file path if provided
            if (newOfflineLibraryDetails.getFilePath() != null && !newOfflineLibraryDetails.getFilePath().trim().isEmpty()) {
                offlineLibrary.setFilePath(newOfflineLibraryDetails.getFilePath());
            }

            // Check if the user exists if provided
            if (newOfflineLibraryDetails.getUser() != null) {
                UserEntity user = userRepo.findById(newOfflineLibraryDetails.getUser().getUserId())
                        .orElseThrow(() -> new EntityNotFoundException("User with ID " + newOfflineLibraryDetails.getUser().getUserId() + " not found"));
                offlineLibrary.setUser(user);
            }

            // Check if the music exists if provided
            if (newOfflineLibraryDetails.getMusic() != null) {
                MusicEntity music = musicRepo.findById(newOfflineLibraryDetails.getMusic().getMusicId())
                        .orElseThrow(() -> new EntityNotFoundException("Music with ID " + newOfflineLibraryDetails.getMusic().getMusicId() + " not found"));
                offlineLibrary.setMusic(music);
            }

        } catch (NoSuchElementException nex) {
            throw new EntityNotFoundException("Offline library entry with ID " + offlineLibraryId + " not found");
        }

        return offlineLibraryRepo.save(offlineLibrary);
    }

    // Delete an offline library entry
    @Transactional
    public String deleteOfflineLibraryEntry(int offlineLibraryId) {
        if (offlineLibraryRepo.existsById(offlineLibraryId)) {
            offlineLibraryRepo.deleteById(offlineLibraryId);
            return "Offline library entry successfully removed";
        } else {
            return "Offline library entry with ID " + offlineLibraryId + " not found";
        }
    }

    // Remove a music from a user's offline library
    @Transactional
    public String removeFromOfflineLibrary(int userId, int musicId) {
        // Check if the user exists
        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with ID " + userId + " not found"));

        // Check if the music exists
        MusicEntity music = musicRepo.findById(musicId)
                .orElseThrow(() -> new EntityNotFoundException("Music with ID " + musicId + " not found"));

        // Find the offline library entry
        OfflineLibraryEntity offlineLibrary = offlineLibraryRepo.findByUserAndMusic(user, music)
                .orElseThrow(() -> new EntityNotFoundException("This music is not in the user's offline library"));

        // Delete the offline library entry
        offlineLibraryRepo.deleteById(offlineLibrary.getOfflinelibId());

        return "Music successfully removed from offline library";
    }

    // Check if a music is in a user's offline library
    public boolean isInOfflineLibrary(int userId, int musicId) {
        // Check if the user exists
        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with ID " + userId + " not found"));

        // Check if the music exists
        MusicEntity music = musicRepo.findById(musicId)
                .orElseThrow(() -> new EntityNotFoundException("Music with ID " + musicId + " not found"));

        return offlineLibraryRepo.existsByUserAndMusic(user, music);
    }

    // Search offline library entries by file path
    public List<OfflineLibraryEntity> searchByFilePath(String filePath) {
        return offlineLibraryRepo.findByFilePathContaining(filePath);
    }
}