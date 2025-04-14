// src/components/AudioUploader.tsx
import React, { useState } from "react";
import { storage } from "../firebase";
import { ref, uploadBytesResumable, getDownloadURL } from "firebase/storage";

interface AudioUploaderProps {
    onFileUploaded?: (fileName: string, fileUrl: string) => void;
}

const AudioUploader: React.FC<AudioUploaderProps> = ({ onFileUploaded }) => {
    const [audioFile, setAudioFile] = useState<File | null>(null);
    const [uploadProgress, setUploadProgress] = useState<number>(0);
    const [downloadURL, setDownloadURL] = useState<string>("");

    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files && e.target.files.length > 0) {
            const file = e.target.files[0];
            setAudioFile(file);
            if (file) {
                handleUpload(file);
            }
        }
    };

    const handleUpload = (file: File) => {
        if (!file) {
            alert("Please select a file first.");
            return;
        }
        const storageRef = ref(storage, `audios/${file.name}`);
        const uploadTask = uploadBytesResumable(storageRef, file);

        uploadTask.on(
            "state_changed",
            (snapshot) => {
                const progress = (snapshot.bytesTransferred / snapshot.totalBytes) * 100;
                setUploadProgress(progress);
            },
            (error) => {
                console.error("Upload failed:", error);
            },
            () => {
                getDownloadURL(uploadTask.snapshot.ref).then((url) => {
                    setDownloadURL(url);
                    // Notify parent component that a file was uploaded
                    if (onFileUploaded) {
                        onFileUploaded(file.name, url);
                    }
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
                Upload Music (This connects to firebase)
            </label>
            {uploadProgress > 0 && <p>Uploading: {uploadProgress.toFixed(0)}%</p>}
            {downloadURL && (
                <div>
                    <p>Uploaded! 🎉</p>
                    <audio controls src={downloadURL}></audio>
                    <p><a href={downloadURL} target="_blank" rel="noopener noreferrer">Download Link</a></p>
                </div>
            )}
        </div>
    );
};

export default AudioUploader;
