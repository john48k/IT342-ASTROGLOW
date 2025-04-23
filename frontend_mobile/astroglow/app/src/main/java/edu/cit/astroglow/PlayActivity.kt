package edu.cit.astroglow

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import edu.cit.astroglow.ui.theme.AstroglowTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.compose.runtime.DisposableEffect
import kotlinx.coroutines.delay

class PlayActivity : ComponentActivity() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // TODO: Retrieve song details from intent
        val songId = intent.getIntExtra("SONG_ID", -1)
        val songTitle = intent.getStringExtra("SONG_TITLE") ?: "Unknown Title"
        val songArtist = intent.getStringExtra("SONG_ARTIST") ?: "Unknown Artist"
        val songImageUrl = intent.getStringExtra("SONG_IMAGE_URL")
        
        // Get userId from SharedPreferences
        val sharedPreferences = getSharedPreferences("auth", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getLong("user_id", -1)

        if (songId == -1 || userId == -1L) {
            // Handle error: Invalid song or user ID
            Toast.makeText(this, "Error: Invalid song or user.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        setContent {
            AstroglowTheme {
                PlayScreen(
                    userId = userId,
                    songId = songId,
                    songTitle = songTitle,
                    songArtist = songArtist,
                    songImageUrl = songImageUrl,
                    client = client, // Pass the OkHttpClient
                    onBack = { finish() } // Close activity on back press
                )
            }
        }
    }
}

@Composable
fun PlayScreen(
    userId: Long,
    songId: Int,
    songTitle: String,
    songArtist: String,
    songImageUrl: String?,
    client: OkHttpClient,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // --- ExoPlayer State ---
    // Remember the ExoPlayer instance
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build()
    }
    var isPlayerReady by remember { mutableStateOf(false) }
    var audioUrl by remember { mutableStateOf<String?>(null) }
    var fetchError by remember { mutableStateOf<String?>(null) }

    // --- UI State (some controlled by ExoPlayer now) ---
    var isPlaying by remember { mutableStateOf(exoPlayer.isPlaying) }
    var isFavorite by remember { mutableStateOf(false) }
    var isLoadingFavorite by remember { mutableStateOf(true) }
    var totalDurationMillis by remember { mutableStateOf(0L) }
    var currentPositionMillis by remember { mutableStateOf(0L) }
    var shuffleEnabled by remember { mutableStateOf(false) }
    var repeatMode by remember { mutableStateOf(RepeatMode.Off) }
    var isInQueue by remember { mutableStateOf(false) }
    var isLoadingQueue by remember { mutableStateOf(true) }

    // Check if song is in queue
    LaunchedEffect(key1 = songId, key2 = userId) {
        if (userId != -1L && songId != -1) {
            isLoadingQueue = true
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val checkUrl = "${Constants.BASE_URL}/api/playlists/user/$userId/music/$songId/check"
                    val request = Request.Builder().url(checkUrl).get().build()
                    val response = client.newCall(request).execute()
                    Log.d("PlayScreen", "isInQueue check response code: ${response.code}")
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        val inQueue = responseBody?.toBooleanStrictOrNull() ?: false
                        Log.d("PlayScreen", "isInQueue check result: $inQueue")
                        withContext(Dispatchers.Main) { isInQueue = inQueue }
                    } else {
                        Log.w("PlayScreen", "Failed to check queue status (assuming false): ${response.code}")
                        withContext(Dispatchers.Main) { isInQueue = false }
                    }
                } catch (e: Exception) {
                    Log.e("PlayScreen", "Error checking queue status", e)
                    withContext(Dispatchers.Main) { isInQueue = false }
                } finally {
                    withContext(Dispatchers.Main) { isLoadingQueue = false }
                }
            }
        } else {
            Log.w("PlayScreen", "Skipping queue check due to invalid IDs (User: $userId, Song: $songId)")
            isLoadingQueue = false
        }
    }

    // --- Fetch Audio URL ---
    LaunchedEffect(songId) {
        Log.d("PlayScreen", "Fetching audio URL for songId: $songId")
        isPlayerReady = false // Reset ready state
        fetchError = null
        audioUrl = null // Clear previous URL
        if (songId != -1) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val request = Request.Builder()
                        .url("${Constants.BASE_URL}/api/music/getMusic/$songId") // Fetch full music details
                        .get()
                        .build()
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        if (responseBody != null) {
                            val jsonObject = JSONObject(responseBody)
                            val url = jsonObject.optString("audioUrl", null) // Get audioUrl
                            Log.d("PlayScreen", "Fetched audioUrl: $url")
                            withContext(Dispatchers.Main) {
                                audioUrl = url
                                if (url == null) {
                                    fetchError = "Audio URL not found"
                                }
                            }
                        } else {
                             withContext(Dispatchers.Main) { fetchError = "Empty response body" }
                        }
                    } else {
                         Log.e("PlayScreen", "Failed to fetch music details: ${response.code}")
                         withContext(Dispatchers.Main) { fetchError = "Failed to load audio details" }
                    }
                } catch (e: Exception) {
                    Log.e("PlayScreen", "Error fetching audio URL", e)
                    withContext(Dispatchers.Main) { fetchError = "Error loading audio" }
                }
            }
        } else {
             fetchError = "Invalid Song ID"
        }
    }

    // Update current position periodically while playing
    LaunchedEffect(isPlaying, isPlayerReady) {
        while (isPlaying && isPlayerReady) {
            currentPositionMillis = exoPlayer.currentPosition
            delay(1000) // Update every second
        }
    }

    // --- ExoPlayer Setup & Lifecycle ---
    DisposableEffect(audioUrl) { // Re-run when audioUrl changes
        var listener: Player.Listener? = null // Define listener here
        if (audioUrl != null) {
            Log.d("PlayScreen", "Setting up ExoPlayer with URL: $audioUrl")
            val mediaItem = MediaItem.fromUri(audioUrl!!)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()

            // Assign to the listener variable
            listener = object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                         Log.d("PlayScreen", "ExoPlayer State: READY")
                         isPlayerReady = true
                         totalDurationMillis = exoPlayer.duration.coerceAtLeast(0L)
                    } else if (playbackState == Player.STATE_ENDED) {
                         Log.d("PlayScreen", "ExoPlayer State: ENDED")
                         exoPlayer.seekTo(0)
                         exoPlayer.playWhenReady = false
                    } else {
                         isPlayerReady = false
                    }
                }

                override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                     Log.d("PlayScreen", "ExoPlayer isPlaying changed: $isPlayingNow")
                     isPlaying = isPlayingNow
                     if(isPlayerReady) currentPositionMillis = exoPlayer.currentPosition
                }

                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    Log.e("PlayScreen", "ExoPlayer Error: ${error.message}", error)
                    Toast.makeText(context, "Playback Error: ${error.message}", Toast.LENGTH_LONG).show()
                     fetchError = "Playback Error"
                     isPlayerReady = false
                }
            }
            exoPlayer.addListener(listener)

        } else {
            // If audioUrl becomes null, stop player and clear items
             exoPlayer.stop()
             exoPlayer.clearMediaItems()
             isPlayerReady = false
        }

        // Return the onDispose lambda as the result of the DisposableEffect
        onDispose {
             Log.d("PlayScreen", "Disposing ExoPlayer listener (if added)")
             listener?.let { exoPlayer.removeListener(it) } // Safely remove the listener
             // We don't release the player here; it's handled by the other DisposableEffect
        }
    }
    
    // Separate DisposableEffect for releasing the player when the composable leaves composition
    // This ensures release even if audioUrl was never set or became null.
     DisposableEffect(Unit) {
        onDispose {
            Log.d("PlayScreen", "Releasing ExoPlayer instance.")
            exoPlayer.release()
        }
    }

    // --- Favorite Status Check (Keep existing logic) ---
    LaunchedEffect(key1 = songId, key2 = userId) {
         Log.d("PlayScreen", "LaunchedEffect: Checking favorite status for song $songId, user $userId")
        if (userId != -1L && songId != -1) {
            isLoadingFavorite = true
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val checkUrl = "${Constants.BASE_URL}/api/favorites/user/$userId/music/$songId/check"
                    val request = Request.Builder().url(checkUrl).get().build()
                    val response = client.newCall(request).execute()
                    Log.d("PlayScreen", "isFavorite check response code: ${response.code}")
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        val isFav = responseBody?.toBooleanStrictOrNull() ?: false
                        Log.d("PlayScreen", "isFavorite check result: $isFav")
                        withContext(Dispatchers.Main) { isFavorite = isFav }
                    } else {
                         Log.w("PlayScreen", "Failed to check favorite status (assuming false): ${response.code}")
                         withContext(Dispatchers.Main) { isFavorite = false }
                    }
                } catch (e: Exception) {
                    Log.e("PlayScreen", "Error checking favorite status", e)
                     withContext(Dispatchers.Main) {
                         isFavorite = false
                        Toast.makeText(context, "Error checking favorite status", Toast.LENGTH_SHORT).show()
                    }
                } finally {
                    withContext(Dispatchers.Main) { isLoadingFavorite = false }
                }
            }
        } else {
             Log.w("PlayScreen", "Skipping favorite check due to invalid IDs (User: $userId, Song: $songId)")
             isLoadingFavorite = false
        }
    }

    // --- UI Calculations ---
    // Calculate progress for the Slider (0.0 to 1.0)
    val sliderPosition = remember(currentPositionMillis, totalDurationMillis) {
        if (totalDurationMillis > 0) {
            (currentPositionMillis.toFloat() / totalDurationMillis.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
    }
    val formattedCurrentTime = remember(currentPositionMillis) { formatTimeMillis(currentPositionMillis) }
    val formattedTotalTime = remember(totalDurationMillis) { formatTimeMillis(totalDurationMillis) }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF6A1B9A), Color(0xFF283593))
    )

    Column(
        modifier = Modifier.fillMaxSize().background(backgroundBrush).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Bar with padding
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
            }
            
            // Add to Queue Button
            IconButton(
                onClick = {
                    if (!isInQueue) {
                        // Add to queue
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val addUrl = "${Constants.BASE_URL}/api/playlists/postPlaylist/user/$userId/music/$songId"
                                val request = Request.Builder()
                                    .url(addUrl)
                                    .post(okhttp3.RequestBody.create(null, ByteArray(0)))
                                    .build()
                                
                                val response = client.newCall(request).execute()
                                
                                if (response.isSuccessful) {
                                    withContext(Dispatchers.Main) {
                                        isInQueue = true
                                        Toast.makeText(context, "Added to queue", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Log.e("PlayScreen", "Failed to add to queue: ${response.code}")
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, "Failed to add to queue", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("PlayScreen", "Error adding to queue", e)
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Error adding to queue", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } else {
                        // Remove from queue
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val removeUrl = "${Constants.BASE_URL}/api/playlists/deletePlaylist/user/$userId/music/$songId"
                                val request = Request.Builder()
                                    .url(removeUrl)
                                    .delete()
                                    .build()
                                
                                val response = client.newCall(request).execute()
                                
                                if (response.isSuccessful) {
                                    withContext(Dispatchers.Main) {
                                        isInQueue = false
                                        Toast.makeText(context, "Removed from queue", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Log.e("PlayScreen", "Failed to remove from queue: ${response.code}")
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, "Failed to remove from queue", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("PlayScreen", "Error removing from queue", e)
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Error removing from queue", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                },
                enabled = !isLoadingQueue && songId != -1
            ) {
                Icon(
                    imageVector = if (isInQueue) Icons.Default.QueueMusic else Icons.Default.QueueMusic,
                    contentDescription = if (isInQueue) "Remove from Queue" else "Add to Queue",
                    tint = if (isLoadingQueue) Color.Gray else if (isInQueue) Color(0xFFE91E63) else Color.White
                )
            }
        }

        // Song Artwork Area (No changes)
         Box(
             modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(16.dp))
                 .background(Color.DarkGray.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            if (songImageUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(songImageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Song Artwork",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = rememberVectorPainter(Icons.Filled.MusicNote)
                )
            } else {
                 // Placeholder Icon if no image
                Icon(
                    imageVector = Icons.Filled.MusicNote, // Use Material Icon
                    contentDescription = "Song Artwork Placeholder",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(100.dp)
                )
                 // Example Waveform Placeholder (Replace with actual implementation if desired)
                 /* Image(
                     painter = painterResource(id = R.drawable.waveform_placeholder), // Add a placeholder waveform drawable
                     contentDescription = "Waveform visualization",
                     modifier = Modifier.fillMaxSize(),
                     contentScale = ContentScale.Crop
                 ) */
            }
        }

         // Show fetch error if any
        if (fetchError != null) {
            Text(
                text = fetchError!!,
                color = Color.Red,
                modifier = Modifier.padding(vertical = 8.dp),
                textAlign = TextAlign.Center
            )
        } else {
            Spacer(modifier = Modifier.height(24.dp)) // Keep spacing consistent
        }

        // Progress Bar and Time
        Column(modifier = Modifier.fillMaxWidth()) {
            Slider(
                value = sliderPosition,
                enabled = isPlayerReady,
                onValueChange = { newSliderValue ->
                    // Update the displayed time and slider position immediately during drag
                    if (isPlayerReady && totalDurationMillis > 0) {
                        currentPositionMillis = (newSliderValue * totalDurationMillis).toLong()
                    }
                },
                 onValueChangeFinished = { // Seek when user finishes dragging
                    if (isPlayerReady) {
                        // Use the final position reflected in currentPositionMillis
                        exoPlayer.seekTo(currentPositionMillis)
                         Log.d("PlayScreen", "Slider seek finished: Seeking to $currentPositionMillis ms")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color(0xFFE91E63),
                    inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(formattedCurrentTime, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, fontFamily = interLightFontFamily)
                Text(formattedTotalTime, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, fontFamily = interLightFontFamily)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Song Title and Artist with Favorite Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = songTitle,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = interFontFamily,
                    maxLines = 1
                )
                Text(
                    text = songArtist,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 16.sp,
                    fontFamily = interLightFontFamily,
                    maxLines = 1
                )
            }
            IconButton(onClick = {
                val targetFavoriteState = !isFavorite
                isFavorite = targetFavoriteState // Optimistic UI update

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        if (targetFavoriteState) {
                            // POST to add
                            Log.d("PlayScreen", "Attempting to ADD favorite: User $userId, Song $songId")
                            val addUrl = "${Constants.BASE_URL}/api/favorites/postFavorites"
                            val jsonBody = JSONObject().apply {
                                put("user", JSONObject().put("userId", userId))
                                put("music", JSONObject().put("musicId", songId))
                            }.toString()
                            val requestBody = jsonBody.toRequestBody("application/json".toMediaType())
                            val request = Request.Builder().url(addUrl).post(requestBody).build()
                            val response = client.newCall(request).execute()
                            if (response.isSuccessful || response.code == 409) {
                                Log.i("PlayScreen", "Successfully added favorite (or it already existed)")
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Added to favorites", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Log.e("PlayScreen", "Failed to add favorite: ${response.code} - ${response.body?.string()}")
                                withContext(Dispatchers.Main) {
                                    isFavorite = false // Revert
                                    Toast.makeText(context, "Failed to add favorite", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            // DELETE to remove
                            Log.d("PlayScreen", "Attempting to REMOVE favorite: User $userId, Song $songId")
                            val deleteUrl = "${Constants.BASE_URL}/api/favorites/user/$userId/music/$songId"
                            val request = Request.Builder().url(deleteUrl).delete().build()
                            val response = client.newCall(request).execute()
                            if (response.isSuccessful || response.code == 404) {
                                Log.i("PlayScreen", "Successfully removed favorite (or it was already gone)")
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Removed from favorites", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Log.e("PlayScreen", "Failed to remove favorite: ${response.code} - ${response.body?.string()}")
                                withContext(Dispatchers.Main) {
                                    isFavorite = true // Revert
                                    Toast.makeText(context, "Failed to remove favorite", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("PlayScreen", "Error toggling favorite status", e)
                        withContext(Dispatchers.Main) {
                            isFavorite = !targetFavoriteState // Revert
                            Toast.makeText(context, "Error updating favorite", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }, enabled = !isLoadingFavorite ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isLoadingFavorite) Color.Gray else if (isFavorite) Color(0xFFE91E63) else Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Playback Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(onClick = { /* TODO: Previous */ }) {
                Icon(Icons.Default.SkipPrevious, "Previous", tint = Color.White, modifier = Modifier.size(40.dp))
            }
            IconButton(
                onClick = {
                     if (isPlayerReady) { // Only allow play/pause if player is ready
                        if (exoPlayer.isPlaying) {
                            exoPlayer.pause()
                        } else {
                            exoPlayer.play()
                        }
                    } else if (audioUrl != null && fetchError == null) {
                         // If not ready but URL is available, try preparing again? Or just wait?
                         Log.w("PlayScreen", "Play clicked but player not ready yet.")
                         // Maybe trigger prepare again? exoPlayer.prepare()
                         // Or show a loading indicator on the button?
                    } else {
                        Log.e("PlayScreen", "Play clicked but no audio URL or fetch error occurred.")
                         Toast.makeText(context, fetchError ?: "Audio not loaded", Toast.LENGTH_SHORT).show()
                    }
                },
                 modifier = Modifier.size(72.dp).background(Color.White, shape = RoundedCornerShape(36.dp)),
                 enabled = fetchError == null // Disable play if there was an error loading audio
            ) {
                // Show progress indicator on button if preparing but not ready? (Optional)
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = if (fetchError == null) Color(0xFF6A1B9A) else Color.Gray, // Gray out if error
                    modifier = Modifier.size(48.dp)
                )
            }
            IconButton(onClick = { /* TODO: Next */ }) {
                Icon(Icons.Default.SkipNext, "Next", tint = Color.White, modifier = Modifier.size(40.dp))
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

enum class RepeatMode {
    Off, One, All
}

// Update formatTime to accept milliseconds
fun formatTimeMillis(millis: Long): String {
    val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(millis)
    val minutes = TimeUnit.SECONDS.toMinutes(totalSeconds)
    val remainingSeconds = totalSeconds - TimeUnit.MINUTES.toSeconds(minutes)
    return String.format("%02d:%02d", minutes, remainingSeconds)
}

// --- Add necessary imports and font definitions if not already present globally ---
// Make sure interFontFamily and interLightFontFamily are defined or imported.
// Ensure necessary Material 3 imports are present.
// Add drawable resources for placeholder icons (ic_music_note, waveform_placeholder) if needed.

