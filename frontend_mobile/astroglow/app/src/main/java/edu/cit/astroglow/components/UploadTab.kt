package edu.cit.astroglow.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.cit.astroglow.interFontFamily
import edu.cit.astroglow.data.api.RetrofitClient
import edu.cit.astroglow.data.api.MusicUploadRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import android.media.MediaPlayer
import android.util.Log
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun UploadTab() {
    var title by remember { mutableStateOf("") }
    var artist by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var isPlaying by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    val viewModel: UploadViewModel = viewModel()
    val uploadState by viewModel.uploadState.collectAsState()
    
    // Set context in ViewModel
    LaunchedEffect(Unit) {
        viewModel.setContext(context)
    }
    
    // Predefined genre options
    val genres = listOf(
        "None", "Pop", "Rock", "OPM", "Hip Hop", "R&B", "Electronic", 
        "Jazz", "Classical", "Country", "Folk", "Metal",
        "Blues", "Reggae", "Latin", "World", "Other"
    )
    
    // Create a MediaPlayer instance
    val mediaPlayer = remember { MediaPlayer() }
    
    // Handle lifecycle events to release MediaPlayer resources
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                }
                mediaPlayer.release()
            }
        }
        
        lifecycleOwner.lifecycle.addObserver(observer)
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            mediaPlayer.release()
        }
    }
    
    // File picker launchers
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.onEvent(UploadEvent.AudioFileSelected(it))
        }
    }
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.onEvent(UploadEvent.ImageFileSelected(it))
        }
    }
    
    // Function to play or pause audio
    fun togglePlayback() {
        if (uploadState.audioUrl.isBlank()) return
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (isPlaying) {
                    withContext(Dispatchers.Main) {
                        mediaPlayer.pause()
                        isPlaying = false
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        if (mediaPlayer.isPlaying) {
                            mediaPlayer.stop()
                        }
                        
                        mediaPlayer.reset()
                        mediaPlayer.setDataSource(uploadState.audioUrl)
                        
                        mediaPlayer.setOnPreparedListener {
                            mediaPlayer.start()
                            isPlaying = true
                        }
                        
                        mediaPlayer.setOnCompletionListener {
                            isPlaying = false
                        }
                        
                        var errorMessage: String? = null
                        
                        mediaPlayer.setOnErrorListener { _, what, extra ->
                            Log.e("UploadTab", "MediaPlayer error: $what, $extra")
                            errorMessage = "Error playing audio: $what"
                            isPlaying = false
                            true
                        }
                        
                        mediaPlayer.prepareAsync()
                        
                        kotlinx.coroutines.delay(1000)
                        if (errorMessage != null) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("UploadTab", "Error playing audio", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error playing audio: ${e.message}", Toast.LENGTH_SHORT).show()
                    isPlaying = false
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Upload Music",
            fontFamily = interFontFamily,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title", color = Color.White) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        OutlinedTextField(
            value = artist,
            onValueChange = { artist = it },
            label = { Text("Artist", color = Color.White) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        // Genre Dropdown
        Column(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = genre,
                onValueChange = { },
                label = { Text("Genre", color = Color.White) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select Genre",
                            tint = Color.White
                        )
                    }
                }
            )
            
            if (expanded) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color(0xFF1E1E1E), RoundedCornerShape(4.dp))
                        .border(1.dp, Color.White, RoundedCornerShape(4.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        genres.forEach { genreOption ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        genre = genreOption
                                        expanded = false
                                    }
                                    .background(
                                        if (genreOption == genre) Color(0xFF0050D0).copy(alpha = 0.3f)
                                        else Color.Transparent
                                    )
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = genreOption,
                                    color = Color.White,
                                    fontFamily = interFontFamily
                                )
                            }
                        }
                    }
                }
            }
        }

        OutlinedTextField(
            value = time,
            onValueChange = { time = it },
            label = { Text("Duration (seconds)", color = Color.White) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        // Audio File Upload Button
        Button(
            onClick = { audioPickerLauncher.launch("audio/*") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0050D0))
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AudioFile, contentDescription = "Select Audio")
                Text("Select Audio File")
            }
        }

        // Audio Upload Progress
        if (uploadState.isUploading && uploadState.uploadProgress > 0f) {
            LinearProgressIndicator(
                progress = uploadState.uploadProgress / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = Color(0xFF0050D0)
            )
            Text(
                text = "Uploading audio: ${uploadState.uploadProgress.toInt()}%",
                color = Color.White,
                fontSize = 14.sp
            )
        }

        // Audio Preview
        if (uploadState.showAudioPreview && uploadState.audioUrl.isNotBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E1E1E))
                    .border(1.dp, Color.White, RoundedCornerShape(8.dp))
                    .padding(8.dp)
                    .clickable { togglePlayback() },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        text = if (isPlaying) "Playing..." else "Tap to Play",
                        color = Color.White,
                        fontFamily = interFontFamily,
                        fontSize = 16.sp
                    )
                }
            }
        }

        // Image File Upload Button
        Button(
            onClick = { imagePickerLauncher.launch("image/*") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0050D0))
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Image, contentDescription = "Select Image")
                Text("Select Cover Image")
            }
        }

        // Image Upload Progress
        if (uploadState.isUploading && uploadState.uploadProgress > 0f) {
            LinearProgressIndicator(
                progress = uploadState.uploadProgress / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = Color(0xFF0050D0)
            )
            Text(
                text = "Uploading image: ${uploadState.uploadProgress.toInt()}%",
                color = Color.White,
                fontSize = 14.sp
            )
        }

        // Image Preview
        if (uploadState.showImagePreview && uploadState.imageUrl.isNotBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, Color.White, RoundedCornerShape(8.dp))
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(uploadState.imageUrl)
                            .crossfade(true)
                            .build()
                    ),
                    contentDescription = "Album Cover",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        // Error message
        uploadState.error?.let { error ->
            Text(
                text = error,
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Button(
            onClick = {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val musicRequest = MusicUploadRequest(
                            title = title,
                            artist = artist,
                            genre = genre,
                            time = time.toIntOrNull() ?: 0,
                            audioUrl = uploadState.audioUrl.takeIf { it.isNotBlank() },
                            imageUrl = uploadState.imageUrl.takeIf { it.isNotBlank() },
                            playlists = emptyList(),
                            offlineLibraries = emptyList(),
                            favorites = emptyList()
                        )

                        val response = RetrofitClient.api.uploadMusic(musicRequest)

                        withContext(Dispatchers.Main) {
                            if (response.isSuccessful) {
                                Toast.makeText(context, "Music uploaded successfully!", Toast.LENGTH_SHORT).show()
                                title = ""
                                artist = ""
                                genre = ""
                                time = ""
                                viewModel.onEvent(UploadEvent.ResetUpload)
                                if (mediaPlayer.isPlaying) {
                                    mediaPlayer.stop()
                                }
                            } else {
                                Toast.makeText(context, "Failed to upload music: ${response.message()}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0050D0)),
            enabled = !uploadState.isUploading && title.isNotBlank() && artist.isNotBlank() && genre.isNotBlank() && time.isNotBlank() && uploadState.audioUrl.isNotBlank() && uploadState.imageUrl.isNotBlank()
        ) {
            if (uploadState.isUploading) {
                CircularProgressIndicator(color = Color.White)
            } else {
                Text(
                    text = "Upload Music",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
} 