package edu.cit.astroglow.components

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import java.io.InputStream

class UploadViewModel : ViewModel() {
    private val _uploadState = MutableStateFlow(UploadState())
    val uploadState: StateFlow<UploadState> = _uploadState.asStateFlow()

    private var storage: com.google.firebase.storage.StorageReference? = null
    private lateinit var context: Context

    fun setContext(context: Context) {
        this.context = context
        try {
            // Initialize storage reference
            storage = Firebase.storage.reference
        } catch (e: Exception) {
            Log.e("UploadViewModel", "Error initializing Firebase Storage", e)
            _uploadState.update { it.copy(error = "Failed to initialize Firebase Storage: ${e.message}") }
        }
    }

    fun onEvent(event: UploadEvent) {
        when (event) {
            is UploadEvent.AudioFileSelected -> handleAudioUpload(event.uri)
            is UploadEvent.ImageFileSelected -> handleImageUpload(event.uri)
            is UploadEvent.UploadProgress -> updateProgress(event.progress)
            is UploadEvent.UploadSuccess -> handleUploadSuccess(event.type, event.url)
            is UploadEvent.UploadError -> handleError(event.error)
            is UploadEvent.ResetUpload -> resetUpload()
        }
    }

    private fun handleAudioUpload(uri: Uri) {
        viewModelScope.launch {
            try {
                if (storage == null) {
                    onEvent(UploadEvent.UploadError("Firebase Storage not initialized"))
                    return@launch
                }

                _uploadState.update { it.copy(isUploading = true, error = null) }
                val fileName = "${UUID.randomUUID()}.mp3"
                val audioRef = storage!!.child("audios/$fileName")
                
                val uploadTask = audioRef.putStream(context.contentResolver.openInputStream(uri)!!)
                
                uploadTask.addOnProgressListener { taskSnapshot ->
                    val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toFloat()
                    onEvent(UploadEvent.UploadProgress(progress))
                }.addOnSuccessListener {
                    audioRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        onEvent(UploadEvent.UploadSuccess("audio", downloadUrl.toString()))
                    }.addOnFailureListener { e ->
                        onEvent(UploadEvent.UploadError("Failed to get audio URL: ${e.message}"))
                    }
                }.addOnFailureListener { e ->
                    onEvent(UploadEvent.UploadError("Failed to upload audio: ${e.message}"))
                }
            } catch (e: Exception) {
                onEvent(UploadEvent.UploadError("Error uploading audio: ${e.message}"))
            }
        }
    }

    private fun handleImageUpload(uri: Uri) {
        viewModelScope.launch {
            try {
                if (storage == null) {
                    onEvent(UploadEvent.UploadError("Firebase Storage not initialized"))
                    return@launch
                }

                _uploadState.update { it.copy(isUploading = true, error = null) }
                val fileName = "${UUID.randomUUID()}.jpg"
                val imageRef = storage!!.child("images/$fileName")
                
                val uploadTask = imageRef.putStream(context.contentResolver.openInputStream(uri)!!)
                
                uploadTask.addOnProgressListener { taskSnapshot ->
                    val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toFloat()
                    onEvent(UploadEvent.UploadProgress(progress))
                }.addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        onEvent(UploadEvent.UploadSuccess("image", downloadUrl.toString()))
                    }.addOnFailureListener { e ->
                        onEvent(UploadEvent.UploadError("Failed to get image URL: ${e.message}"))
                    }
                }.addOnFailureListener { e ->
                    onEvent(UploadEvent.UploadError("Failed to upload image: ${e.message}"))
                }
            } catch (e: Exception) {
                onEvent(UploadEvent.UploadError("Error uploading image: ${e.message}"))
            }
        }
    }

    private fun updateProgress(progress: Float) {
        _uploadState.update { it.copy(uploadProgress = progress) }
    }

    private fun handleUploadSuccess(type: String, url: String) {
        _uploadState.update { state ->
            when (type) {
                "audio" -> state.copy(
                    audioUrl = url,
                    showAudioPreview = true,
                    isUploading = false,
                    uploadProgress = 0f
                )
                "image" -> state.copy(
                    imageUrl = url,
                    showImagePreview = true,
                    isUploading = false,
                    uploadProgress = 0f
                )
                else -> state
            }
        }
    }

    private fun handleError(error: String) {
        _uploadState.update { 
            it.copy(
                isUploading = false,
                uploadProgress = 0f,
                error = error
            )
        }
    }

    private fun resetUpload() {
        viewModelScope.launch {
            try {
                // Delete audio file if it exists
                if (_uploadState.value.audioUrl.isNotBlank()) {
                    val audioRef = storage?.child("audios/${_uploadState.value.audioUrl.substringAfterLast("/")}")
                    audioRef?.delete()?.await()
                }
                
                // Delete image file if it exists
                if (_uploadState.value.imageUrl.isNotBlank()) {
                    val imageRef = storage?.child("images/${_uploadState.value.imageUrl.substringAfterLast("/")}")
                    imageRef?.delete()?.await()
                }
            } catch (e: Exception) {
                Log.e("UploadViewModel", "Error deleting files", e)
            }
            
            _uploadState.update { 
                it.copy(
                    isUploading = false,
                    uploadProgress = 0f,
                    error = null,
                    audioUrl = "",
                    imageUrl = "",
                    showAudioPreview = false,
                    showImagePreview = false
                )
            }
        }
    }
} 