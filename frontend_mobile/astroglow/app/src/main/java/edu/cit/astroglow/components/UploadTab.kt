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
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ArrowDropDown
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

@Composable
fun UploadTab() {
    var title by remember { mutableStateOf("") }
    var artist by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var audioUrl by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showAudioPreview by remember { mutableStateOf(false) }
    var showImagePreview by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
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
    
    // Function to play or pause audio
    fun togglePlayback() {
        if (audioUrl.isBlank()) return
        
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
                        mediaPlayer.setDataSource(audioUrl)
                        
                        // Set up listeners before preparing
                        mediaPlayer.setOnPreparedListener {
                            mediaPlayer.start()
                            isPlaying = true
                        }
                        
                        mediaPlayer.setOnCompletionListener {
                            isPlaying = false
                        }
                        
                        // Use a variable to store error message
                        var errorMessage: String? = null
                        
                        mediaPlayer.setOnErrorListener { _, what, extra ->
                            Log.e("UploadTab", "MediaPlayer error: $what, $extra")
                            errorMessage = "Error playing audio: $what"
                            isPlaying = false
                            true
                        }
                        
                        // Prepare and start playback
                        mediaPlayer.prepareAsync()
                        
                        // Check for errors after a short delay
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

        OutlinedTextField(
            value = audioUrl,
            onValueChange = { 
                audioUrl = it
                showAudioPreview = it.isNotBlank()
            },
            label = { Text("Audio URL", color = Color.White) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            trailingIcon = {
                if (audioUrl.isNotBlank()) {
                    IconButton(onClick = { showAudioPreview = !showAudioPreview }) {
                        Icon(
                            imageVector = Icons.Default.AudioFile,
                            contentDescription = "Preview Audio",
                            tint = Color.White
                        )
                    }
                }
            }
        )

        // Audio Preview
        if (showAudioPreview && audioUrl.isNotBlank()) {
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

        OutlinedTextField(
            value = imageUrl,
            onValueChange = { 
                imageUrl = it
                showImagePreview = it.isNotBlank()
            },
            label = { Text("Image URL", color = Color.White) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            trailingIcon = {
                if (imageUrl.isNotBlank()) {
                    IconButton(onClick = { showImagePreview = !showImagePreview }) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Preview Image",
                            tint = Color.White
                        )
                    }
                }
            }
        )

        // Image Preview
        if (showImagePreview && imageUrl.isNotBlank()) {
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
                            .data(imageUrl)
                            .crossfade(true)
                            .build()
                    ),
                    contentDescription = "Album Cover",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Button(
            onClick = {
                isLoading = true
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val musicRequest = MusicUploadRequest(
                            title = title,
                            artist = artist,
                            genre = genre,
                            time = time.toIntOrNull() ?: 0,
                            audioUrl = audioUrl.takeIf { it.isNotBlank() },
                            imageUrl = imageUrl.takeIf { it.isNotBlank() },
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
                                audioUrl = ""
                                imageUrl = ""
                                showAudioPreview = false
                                showImagePreview = false
                                isPlaying = false
                                if (mediaPlayer.isPlaying) {
                                    mediaPlayer.stop()
                                }
                            } else {
                                Toast.makeText(context, "Failed to upload music: ${response.message()}", Toast.LENGTH_SHORT).show()
                            }
                            isLoading = false
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            isLoading = false
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0050D0)),
            enabled = !isLoading && title.isNotBlank() && artist.isNotBlank() && genre.isNotBlank() && time.isNotBlank() && audioUrl.isNotBlank() && imageUrl.isNotBlank()
        ) {
            if (isLoading) {
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