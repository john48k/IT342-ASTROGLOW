import React, { useState, useCallback } from 'react';
import { storage } from '../firebase';
import { ref, uploadBytesResumable, getDownloadURL } from 'firebase/storage';

const ImageUploader = ({ onImageUploaded, initialImageUrl = null }) => {
  const [imageFile, setImageFile] = useState(null);
  const [imageUrlInput, setImageUrlInput] = useState(initialImageUrl || '');
  const [uploadProgress, setUploadProgress] = useState(0);
  const [isUploading, setIsUploading] = useState(false);
  const [error, setError] = useState('');
  const [previewUrl, setPreviewUrl] = useState(initialImageUrl);

  // Handle image file selection
  const handleFileChange = (e) => {
    const file = e.target.files[0];
    if (!file) return;

    if (!file.type.startsWith('image/')) {
      setError('Please select an image file (JPEG, PNG, GIF, etc.).');
      return;
    }
    
    // Limit file size (e.g., 5MB)
    if (file.size > 5 * 1024 * 1024) {
        setError('Image file size should not exceed 5MB.');
        return;
    }

    setError('');
    setImageFile(file);
    setPreviewUrl(URL.createObjectURL(file)); // Show local preview
    setImageUrlInput(''); // Clear URL input if file is selected
    handleUpload(file);
  };

  // Handle image URL input
  const handleUrlInputChange = (e) => {
    const url = e.target.value;
    setImageUrlInput(url);
    setImageFile(null); // Clear file input if URL is entered
    setPreviewUrl(url); // Show preview from URL
    // Notify parent immediately if URL is valid (basic check)
    if (url && (url.startsWith('http://') || url.startsWith('https://'))) {
       onImageUploaded(url); // Pass the URL directly
    } else if (!url) {
       onImageUploaded(null); // Clear if URL is empty
    }
  };

  // Handle the actual upload process
  const handleUpload = useCallback((file) => {
    if (!file) return;

    setIsUploading(true);
    setUploadProgress(0);
    setError('');

    const metadata = { contentType: file.type };
    const storageRef = ref(storage, `images/${Date.now()}_${file.name}`); // Unique name
    const uploadTask = uploadBytesResumable(storageRef, file, metadata);

    uploadTask.on(
      'state_changed',
      (snapshot) => {
        const progress = (snapshot.bytesTransferred / snapshot.totalBytes) * 100;
        setUploadProgress(progress);
      },
      (uploadError) => {
        console.error('Image upload failed:', uploadError);
        setError(`Image upload failed: ${uploadError.message}`);
        setIsUploading(false);
      },
      () => {
        getDownloadURL(uploadTask.snapshot.ref).then((downloadURL) => {
          console.log('Image uploaded successfully. URL:', downloadURL);
          setIsUploading(false);
          setUploadProgress(100);
          if (onImageUploaded) {
            onImageUploaded(downloadURL); // Pass the final Firebase URL
          }
        }).catch(err => {
          console.error("Error getting image download URL:", err);
          setError(`Error getting image URL: ${err.message}`);
          setIsUploading(false);
        });
      }
    );
  }, [onImageUploaded]);

  return (
    <div style={{ marginBottom: '15px' }}>
      <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Cover Image</label>
      
      {/* Image Preview */}
      {previewUrl && (
        <div style={{ marginBottom: '10px', maxWidth: '150px', maxHeight: '150px', border: '1px solid #ccc', padding: '5px' }}>
          <img 
            src={previewUrl} 
            alt="Cover preview" 
            style={{ width: '100%', height: 'auto', display: 'block' }} 
            onError={(e) => { 
              // Handle broken image links for URLs
              e.target.style.display = 'none'; 
              if (imageUrlInput) setError('Invalid image URL or unable to load preview.');
            }}
          />
        </div>
      )}

      {/* File Input */}
      <input
        type="file"
        accept="image/*"
        onChange={handleFileChange}
        style={{ display: 'none' }}
        id="image-upload-input"
        disabled={isUploading}
      />
      <label 
        htmlFor="image-upload-input" 
        style={{
            display: 'inline-block',
            padding: '5px 15px',
            backgroundColor: '#eee',
            border: '1px solid #ccc',
            borderRadius: '4px',
            cursor: isUploading ? 'not-allowed' : 'pointer',
            marginRight: '10px',
            opacity: isUploading ? 0.6 : 1,
        }}
      >
        {imageFile ? 'Change Image File' : 'Upload Image File'}
      </label>

      <span style={{ margin: '0 10px' }}>OR</span>

      {/* URL Input */}
      <input
        type="text"
        placeholder="Paste Image URL"
        value={imageUrlInput}
        onChange={handleUrlInputChange}
        disabled={isUploading}
        style={{
            padding: '5px 10px',
            border: '1px solid #ccc',
            borderRadius: '4px',
            width: 'calc(100% - 220px)', // Adjust width as needed
            minWidth: '150px'
        }}
      />

      {/* Upload Progress */}
      {isUploading && (
        <div style={{ marginTop: '8px' }}>
          <progress value={uploadProgress} max="100" style={{ width: '100%' }} />
          <span> Uploading Image: {uploadProgress.toFixed(0)}%</span>
        </div>
      )}

      {error && <p style={{ color: 'red', marginTop: '5px', fontSize: '14px' }}>{error}</p>}
    </div>
  );
};

export default ImageUploader; 