package edu.cit.astroglow.components

data class UploadState(
    val isUploading: Boolean = false,
    val uploadProgress: Float = 0f,
    val audioUrl: String = "",
    val imageUrl: String = "",
    val error: String? = null,
    val showAudioPreview: Boolean = false,
    val showImagePreview: Boolean = false
)

sealed class UploadEvent {
    data class AudioFileSelected(val uri: android.net.Uri) : UploadEvent()
    data class ImageFileSelected(val uri: android.net.Uri) : UploadEvent()
    data class UploadProgress(val progress: Float) : UploadEvent()
    data class UploadSuccess(val type: String, val url: String) : UploadEvent()
    data class UploadError(val error: String) : UploadEvent()
    object ResetUpload : UploadEvent()
} 