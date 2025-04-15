import React, { useState, useEffect, useCallback } from 'react';
import Modal from './Modal';
import AudioUploader from './AudioUploader';
import ImageUploader from './ImageUploader';
import styles from './UploadModal.module.css'; // We'll create this CSS module

const UploadModal = ({ isOpen, onClose, onUploadComplete }) => {
  const [step, setStep] = useState(1); // 1: Audio Upload, 2: Metadata & Image
  const [audioFileUrl, setAudioFileUrl] = useState(null);
  const [audioFileName, setAudioFileName] = useState('');
  const [title, setTitle] = useState('');
  const [artist, setArtist] = useState('');
  const [genre, setGenre] = useState('');
  const [imageUrl, setImageUrl] = useState(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState('');

  // Reset state when modal is opened or closed
  useEffect(() => {
    if (isOpen) {
      // Reset all fields when modal opens
      setStep(1);
      setAudioFileUrl(null);
      setAudioFileName('');
      setTitle('');
      setArtist('');
      setGenre('');
      setImageUrl(null);
      setIsSubmitting(false);
      setError('');
    } else {
      // Optional: Add a small delay before resetting if you want animations
    }
  }, [isOpen]);

  // Callback when audio file is uploaded
  const handleAudioUploaded = useCallback((fileName, url) => {
    console.log('UploadModal: Audio Uploaded -', fileName, url);
    setAudioFileUrl(url);
    setAudioFileName(fileName);

    // Auto-parse initial metadata from filename
    let initialArtist = "";
    let initialTitle = fileName.replace(/\.[^/.]+$/, ""); // Remove extension
    let initialGenre = "";

    const parts = fileName.split(' - ');
    if (parts.length >= 2) {
      initialArtist = parts[0].trim();
      initialTitle = parts[1].replace(/\.[^/.]+$/, "").trim(); // Remove extension from title part

      // Extract genre from title if present in brackets
      const genreMatch = initialTitle.match(/\[(.*?)\]/);
      if (genreMatch && genreMatch[1]) {
        initialGenre = genreMatch[1].trim();
        initialTitle = initialTitle.replace(/\[.*?\]/, '').trim();
      }
    }
    setTitle(initialTitle);
    setArtist(initialArtist);
    setGenre(initialGenre);

    // Move to the next step
    setStep(2);
  }, []);

  // Callback when image is uploaded or URL is set
  const handleImageUploaded = useCallback((url) => {
    console.log('UploadModal: Image URL Set -', url);
    setImageUrl(url);
  }, []);

  // Handle final submission
  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!audioFileUrl || !title || !artist) {
      setError('Please ensure audio is uploaded and title/artist fields are filled.');
      return;
    }
    setIsSubmitting(true);
    setError('');

    try {
      const uploadData = {
        title,
        artist,
        genre: genre || 'Music', // Default genre if empty
        audioUrl: audioFileUrl,
        imageUrl: imageUrl, // Can be null if no image provided
        audioFileName: audioFileName // Keep original filename if needed
      };

      console.log('Submitting Upload Data:', uploadData);

      // --- IMPORTANT --- 
      // HERE YOU WOULD CALL YOUR BACKEND API TO SAVE THIS DATA
      // For example:
      // const response = await fetch('/api/music/upload', {
      //   method: 'POST',
      //   headers: { 'Content-Type': 'application/json' },
      //   body: JSON.stringify(uploadData),
      // });
      // if (!response.ok) {
      //   throw new Error('Failed to save music data');
      // }
      // const savedMusic = await response.json();
      // console.log('Music saved:', savedMusic);
      // -----------------

      // Simulate API call delay
      await new Promise(resolve => setTimeout(resolve, 1000));

      // Notify parent component (e.g., HomePage) about the successful upload
      if (onUploadComplete) {
        onUploadComplete(uploadData); // Pass the complete data back
      }

      handleClose(); // Close modal on success
    } catch (err) {
      console.error('Submission failed:', err);
      setError(`Submission failed: ${err.message}`);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleClose = () => {
    // Reset internal state before calling parent onClose
    setStep(1);
    setAudioFileUrl(null);
    // ... reset other states ...
    onClose(); // Call the parent's close handler
  }

  return (
    <Modal isOpen={isOpen} onClose={handleClose} title={step === 1 ? "Step 1: Upload Audio" : "Step 2: Add Details"}>
      {error && <p className={styles.errorText}>{error}</p>}

      {step === 1 && (
        <AudioUploader onFileUploaded={handleAudioUploaded} />
      )}

      {step === 2 && (
        <form onSubmit={handleSubmit} className={styles.form}>
          <div className={styles.fieldGroup}>
            <label htmlFor="title">Title</label>
            <input
              type="text"
              id="title"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              required
            />
          </div>
          <div className={styles.fieldGroup}>
            <label htmlFor="artist">Artist</label>
            <input
              type="text"
              id="artist"
              value={artist}
              onChange={(e) => setArtist(e.target.value)}
              required
            />
          </div>
          <div className={styles.fieldGroup}>
            <label htmlFor="genre" className="bg-gradient-to-r from-black to-[#653895] text-white px-3 py-1 rounded-md inline-block mb-2">Genre</label>
            <select
              id="genre"
              value={genre}
              onChange={(e) => setGenre(e.target.value)}
              required
            >
              <option value="">Select a genre</option>
              <option value="Rap">Rap</option>
              <option value="Pop">Pop</option>
              <option value="K-pop">K-pop</option>
              <option value="Hip Hop">Hip Hop</option>
              <option value="Rock">Rock</option>
              <option value="Indie">Indie</option>
              <option value="EDM">EDM</option>
            </select>
          </div>

          <ImageUploader
            onImageUploaded={handleImageUploaded}
            initialImageUrl={imageUrl}
          />

          <div className={styles.buttonGroup}>
            <button type="button" onClick={() => setStep(1)} className={styles.backButton} disabled={isSubmitting}>
              Back
            </button>
            <button type="submit" className={styles.submitButton} disabled={isSubmitting}>
              {isSubmitting ? 'Submitting...' : 'Complete Upload'}
            </button>
          </div>
        </form>
      )}
    </Modal>
  );
};

export default UploadModal; 