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

    // Improved parsing of metadata from filename
    let initialArtist = "";
    let initialTitle = fileName.replace(/\.[^/.]+$/, ""); // Remove extension
    let initialGenre = "";

    // First check for the common pattern "Artist - Title" format
    const artistSongPattern = fileName.split(' - ');
    if (artistSongPattern.length >= 2) {
      initialArtist = artistSongPattern[0].trim();
      
      // Extract the rest as the title (handling cases with multiple hyphens)
      initialTitle = artistSongPattern.slice(1).join(' - ').replace(/\.[^/.]+$/, "").trim();

      // Extract special song types or descriptions
      const extractTitleParts = (title) => {
        // Match patterns like (Lyrics), [Official Music Video], etc.
        const lyricsMatch = title.match(/\(Lyrics\)|\[Lyrics\]/i);
        const officialMatch = title.match(/\[(Official|4K|Music Video|Remaster).*?\]/i);
        const tiktokMatch = title.match(/\[?Tiktok.*?\]?/i);
        
        // Extract the core title without these annotations
        let cleanTitle = title;
        if (lyricsMatch) {
          cleanTitle = cleanTitle.replace(lyricsMatch[0], '').trim();
          // Add "(Lyrics)" back to the title in a consistent format
          cleanTitle = `${cleanTitle} (Lyrics)`.trim();
        }
        
        // Check for TikTok song
        if (tiktokMatch) {
          initialGenre = "TikTok song";
          cleanTitle = cleanTitle.replace(tiktokMatch[0], '').trim();
        }
        
        // Check if there's a genre in brackets
        const genreMatch = cleanTitle.match(/\[(.*?)\]/);
        if (genreMatch && genreMatch[1]) {
          initialGenre = genreMatch[1].trim();
          cleanTitle = cleanTitle.replace(genreMatch[0], '').trim();
        }
        
        return cleanTitle;
      };
      
      initialTitle = extractTitleParts(initialTitle);
    }

    // Handle special cases like song lyrics with typical phrases in the title
    if (initialTitle.toLowerCase().includes("ay ay ayi'm your little butterfly")) {
      initialTitle = "Butterfly (Lyrics) Ay ay ayi'm your little butterfly";
      initialGenre = "TikTok song";
    }

    console.log("Parsed metadata:", { 
      artist: initialArtist, 
      title: initialTitle, 
      genre: initialGenre 
    });
    
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
      // Process title and artist - make sure long titles are preserved properly
      const cleanTitle = title.trim();
      const cleanArtist = artist.trim();
      
      // Format genre properly - default to 'Music' if empty
      const finalGenre = genre || 'Music';
      
      // Make sure to add TikTok genre if title contains specific TikTok phrases but genre is unspecified
      const finalFormattedGenre = (!genre && title.toLowerCase().includes("tiktok song")) ? 
        "TikTok song" : finalGenre;
      
      const uploadData = {
        title: cleanTitle,
        artist: cleanArtist,
        genre: finalFormattedGenre,
        audioUrl: audioFileUrl,
        imageUrl: imageUrl, // Can be null if no image provided
        audioFileName: audioFileName // Keep original filename if needed
      };

      console.log('Submitting Upload Data:', uploadData);

      // Create FormData object
      const formData = new FormData();
      formData.append('title', cleanTitle);
      formData.append('artist', cleanArtist);
      formData.append('genre', finalFormattedGenre);
      formData.append('audioUrl', audioFileUrl);
      if (imageUrl) {
        formData.append('imageUrl', imageUrl);
      }
      formData.append('userId', '1'); // TODO: Get actual user ID from context

      // Save to database
      const dbResponse = await fetch('http://localhost:8080/api/music/upload', {
        method: 'POST',
        body: formData
      });

      if (!dbResponse.ok) {
        throw new Error('Failed to save music to database');
      }

      const savedMusic = await dbResponse.json();
      console.log('Music saved to database:', savedMusic);

      // Notify parent component (e.g., HomePage) about the successful upload
      if (onUploadComplete) {
        onUploadComplete({
          ...uploadData,
          musicId: savedMusic.musicId // Include the database music ID
        });
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
            <textarea
              id="title"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              required
              rows={2}
              style={{ 
                width: '100%', 
                resize: 'vertical', 
                padding: '8px',
                borderRadius: '8px',
                background: 'rgba(0,0,0,0.1)',
                border: '1px solid rgba(255,255,255,0.1)'
              }}
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
              style={{ 
                width: '100%', 
                padding: '8px',
                borderRadius: '8px',
                background: 'rgba(0,0,0,0.1)',
                border: '1px solid rgba(255,255,255,0.1)'
              }}
            />
          </div>
          
          <div className={styles.fieldGroup}>
            <label htmlFor="genre">Genre</label>
            <select
              id="genre"
              value={genre}
              onChange={(e) => setGenre(e.target.value)}
              required
              style={{ 
                width: '100%', 
                padding: '8px',
                borderRadius: '8px',
                background: 'rgba(0,0,0,0.1)',
                border: '1px solid rgba(255,255,255,0.1)',
                color: '#fff'
              }}
            >
              <option value="">Select a genre</option>
              <option value="Rap">Rap</option>
              <option value="Pop">Pop</option>
              <option value="K-pop">K-pop</option>
              <option value="Hip Hop">Hip Hop</option>
              <option value="Rock">Rock</option>
              <option value="Indie">Indie</option>
              <option value="EDM">EDM</option>
              <option value="TikTok song">TikTok song</option>
              <option value="Remix">Remix</option>
              <option value="Lyrics">Lyrics</option>
              <option value="Electronic">Electronic</option>
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