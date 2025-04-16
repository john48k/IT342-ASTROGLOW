// src/components/AudioUploader.jsx
import React, { useState } from "react";
import { storage } from "../firebase";
import { ref, uploadBytesResumable, getDownloadURL } from "firebase/storage";

const AudioUploader = ({ onFileUploaded }) => {
    const [audioFile, setAudioFile] = useState(null);
    const [uploadProgress, setUploadProgress] = useState(0);
    const [downloadURL, setDownloadURL] = useState("");
    const [error, setError] = useState("");
    const [uploadSuccess, setUploadSuccess] = useState(false);

    const handleFileChange = (e) => {
        const file = e.target.files[0];
        if (!file) return;
        
        setError("");
        setUploadSuccess(false);
        setDownloadURL("");
        setUploadProgress(0);
        
        // Check if file is an audio file
        if (!file.type.startsWith('audio/')) {
            setError("Please select an audio file.");
            return;
        }
        
        setAudioFile(file);
        handleUpload(file);
    };

    const handleUpload = (file) => {
        if (!file) {
            setError("Please select a file first.");
            return;
        }
        
        // Create metadata with the correct content type
        const metadata = {
            contentType: file.type || 'audio/mpeg'
        };
        
        // Make sure to preserve the full original filename
        const originalFileName = file.name;
        console.log('Uploading file with original name:', originalFileName);

        // Reference to storage location
        const storageRef = ref(storage, `audios/${file.name}`);
        
        // Create upload task with metadata
        const uploadTask = uploadBytesResumable(storageRef, file, metadata);

        uploadTask.on(
            "state_changed",
            (snapshot) => {
                const progress = (snapshot.bytesTransferred / snapshot.totalBytes) * 100;
                setUploadProgress(progress);
            },
            (error) => {
                console.error("Upload failed:", error);
                setError(`Upload failed: ${error.message}`);
            },
            () => {
                getDownloadURL(uploadTask.snapshot.ref).then((url) => {
                    console.log("File uploaded successfully. URL:", url);
                    setDownloadURL(url);
                    setUploadSuccess(true);
                    setUploadProgress(100);
                    
                    // Notify parent component that a file was uploaded
                    if (onFileUploaded) {
                        // Pass both the file name and the download URL
                        onFileUploaded(file.name, url);
                        
                        // Log for debugging
                        console.log('AudioUploader: File uploaded successfully!');
                        console.log('AudioUploader: File name:', file.name);
                        console.log('AudioUploader: URL:', url);
                        console.log('AudioUploader: Content type:', metadata.contentType);
                    }
                }).catch(err => {
                    console.error("Error getting download URL:", err);
                    setError(`Error getting download URL: ${err.message}`);
                });
            }
        );
    };

    return (
        <div>
            <input
                type="file"
                accept="audio/*"
                onChange={handleFileChange}
                style={{ display: 'none' }}
                id="audio-upload"
            />
            <label htmlFor="audio-upload" style={{
                display: 'inline-block',
                padding: '6px 19px',
                background: 'linear-gradient(to right, #8b5cf6, #ec4899)',
                color: 'white',
                cursor: 'pointer',
                borderRadius: '12px',
                transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)'
            }} className="hover:scale-103">
                Upload Music
            </label>
            
            {error && <p style={{ color: 'red', marginTop: '8px' }}>{error}</p>}
            
            {uploadProgress > 0 && uploadProgress < 100 && (
                <div style={{ marginTop: '10px', width: '100%', maxWidth: '300px' }}>
                    <div style={{ 
                        display: 'flex',
                        alignItems: 'center',
                        marginBottom: '4px'
                    }}>
                        <div style={{ 
                            flex: '1',
                            height: '8px',
                            backgroundColor: '#edf2f7',
                            borderRadius: '4px',
                            overflow: 'hidden'
                        }}>
                            <div 
                                style={{
                                    width: `${uploadProgress}%`,
                                    height: '100%',
                                    backgroundColor: 'rgb(139, 92, 246)',
                                    backgroundImage: 'linear-gradient(to right, #8b5cf6, #ec4899)',
                                    borderRadius: '4px',
                                    transition: 'width 0.3s ease'
                                }}
                            />
                        </div>
                        <span style={{ 
                            marginLeft: '8px',
                            fontSize: '14px',
                            color: '#4a5568'
                        }}>
                            {uploadProgress.toFixed(0)}%
                        </span>
                    </div>
                    <p style={{ 
                        margin: '0',
                        fontSize: '14px',
                        color: '#4a5568'
                    }}>
                        Uploading: {audioFile?.name}
                    </p>
                </div>
            )}
            
            {uploadSuccess && (
                <div style={{ marginTop: '10px' }}>
                    <p style={{ 
                        color: '#48bb78',
                        display: 'flex',
                        alignItems: 'center',
                        margin: '0 0 4px 0',
                        fontWeight: 'bold'
                    }}>
                        <span style={{ marginRight: '6px' }}>✓</span>
                        Upload Complete!
                    </p>
                    <p style={{ 
                        margin: '0',
                        fontSize: '14px',
                        color: '#4a5568'
                    }}>
                        Your music is now available in the library
                    </p>
                </div>
            )}
        </div>
    );
};

export default AudioUploader;
