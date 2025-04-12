// src/components/AudioUploader.jsx
import React, { useState } from "react";
import { storage } from "../firebase";
import { ref, uploadBytesResumable, getDownloadURL } from "firebase/storage";

const AudioUploader = () => {
    const [audioFile, setAudioFile] = useState(null);
    const [uploadProgress, setUploadProgress] = useState(0);
    const [downloadURL, setDownloadURL] = useState("");

    const handleFileChange = (e) => {
        setAudioFile(e.target.files[0]);
    };

    const handleUpload = () => {
        console.log("Uploading file:", audioFile); // Debugging line
        if (!audioFile) {
            alert("Please select a file first.");
            return;
        }
        console.log("Uploading file 2:", audioFile); // Debugging line
        const storageRef = ref(storage, `audios/${audioFile.name}`);
        const uploadTask = uploadBytesResumable(storageRef, audioFile);

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
                });
            }
        );
    };

    return (
        <div>
            <input type="file" accept="audio/*" onChange={handleFileChange} />
            <button onClick={handleUpload}>Upload Audio</button>
            {uploadProgress > 0 && <p>Uploading: {uploadProgress.toFixed(0)}%</p>}
            {downloadURL && (
                <div>
                    <p>Uploaded! 🎉</p>
                    <audio controls src={downloadURL}></audio>
                    <p><a href={downloadURL} target="_blank">Download Link</a></p>
                </div>
            )}
        </div>
    );
};

export default AudioUploader;
