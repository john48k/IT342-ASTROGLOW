package edu.cit.astroglow.components

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import edu.cit.astroglow.Constants
import edu.cit.astroglow.R
import edu.cit.astroglow.interFontFamily
import edu.cit.astroglow.interLightFontFamily
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
import edu.cit.astroglow.components.Song

// Create a persistent player manager that survives across tab switches
object PlayerManager {
    private var exoPlayer: ExoPlayer? = null
    private var currentSong: Song? = null
    private var isPlaying: Boolean = false
    private var playbackProgress: Float = 0f
    private var totalDuration: Long = 0L
    private var currentPosition: Long = 0L
    private var playQueue: List<Song> = emptyList()
    private var playerListener: Player.Listener? = null
    private var repeatMode: Int = Player.REPEAT_MODE_OFF
    private var autoPlayNext: Boolean = false
    
    // Add a callback mechanism for UI updates
    private val stateChangeCallbacks = mutableListOf<() -> Unit>()
    
    fun addStateChangeCallback(callback: () -> Unit) {
        stateChangeCallbacks.add(callback)
    }
    
    fun removeStateChangeCallback(callback: () -> Unit) {
        stateChangeCallbacks.remove(callback)
    }
    
    private fun notifyStateChange() {
        stateChangeCallbacks.forEach { it.invoke() }
    }
    
    fun initializePlayer(context: Context) {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context).build()
            
            // Set up the player listener
            playerListener = object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        // Auto-play next song when current song ends
                        if (playQueue.isNotEmpty() && currentSong != null && autoPlayNext) {
                            val currentIndex = playQueue.indexOf(currentSong)
                            if (currentIndex < playQueue.size - 1) {
                                // Play next song
                                val nextSong = playQueue[currentIndex + 1]
                                currentSong = nextSong
                                loadAudioForSong(nextSong, context, true)
                                notifyStateChange()
                            } else if (currentIndex == playQueue.size - 1 && playQueue.size > 0) {
                                // If it's the last song, loop back to the first one
                                val firstSong = playQueue[0]
                                currentSong = firstSong
                                loadAudioForSong(firstSong, context, true)
                                notifyStateChange()
                            }
                        }
                    } else if (playbackState == Player.STATE_READY) {
                        // Update duration when player is ready
                        val duration = exoPlayer?.duration?.coerceAtLeast(0L) ?: 0L
                        setTotalDuration(duration)
                    }
                }
                
                override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                    isPlaying = isPlayingNow
                    notifyStateChange()
                }
                
                // Remove the problematic method and use a different approach
                override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                    isPlaying = playWhenReady
                    notifyStateChange()
                }
                
                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    Log.e("PlayerTab", "Player error: ${error.message}")
                    notifyStateChange()
                }
            }
            
            exoPlayer?.addListener(playerListener!!)
        }
    }
    
    fun releasePlayer() {
        playerListener?.let { exoPlayer?.removeListener(it) }
        exoPlayer?.release()
        exoPlayer = null
        playerListener = null
    }
    
    fun getPlayer(): ExoPlayer? = exoPlayer
    
    fun getCurrentSong(): Song? = currentSong
    
    fun setCurrentSong(song: Song?) {
        currentSong = song
        notifyStateChange()
    }
    
    fun isPlaying(): Boolean = isPlaying
    
    fun setPlaying(playing: Boolean) {
        isPlaying = playing
        if (playing) {
            exoPlayer?.play()
        } else {
            exoPlayer?.pause()
        }
        notifyStateChange()
    }
    
    fun getPlaybackProgress(): Float = playbackProgress
    
    fun setPlaybackProgress(progress: Float) {
        playbackProgress = progress
        notifyStateChange()
    }
    
    fun getTotalDuration(): Long = totalDuration
    
    fun setTotalDuration(duration: Long) {
        totalDuration = duration
        notifyStateChange()
    }
    
    fun getCurrentPosition(): Long = currentPosition
    
    fun setCurrentPosition(position: Long) {
        currentPosition = position
        if (totalDuration > 0) {
            playbackProgress = position.toFloat() / totalDuration.toFloat()
        }
        notifyStateChange()
    }
    
    fun getPlayQueue(): List<Song> = playQueue
    
    fun setPlayQueue(queue: List<Song>) {
        playQueue = queue
        notifyStateChange()
    }
    
    fun setRepeatMode(mode: Int) {
        repeatMode = mode
        exoPlayer?.repeatMode = mode
        notifyStateChange()
    }
    
    fun getRepeatMode(): Int = repeatMode
    
    fun setAutoPlayNext(autoPlay: Boolean) {
        autoPlayNext = autoPlay
        notifyStateChange()
    }
    
    fun isAutoPlayNext(): Boolean = autoPlayNext
    
    fun loadAudioForSong(song: Song, context: Context, autoPlay: Boolean = false) {
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
            
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Get the audio URL for the song
                val request = Request.Builder()
                    .url("${Constants.BASE_URL}/api/music/getMusic/${song.id}")
                    .get()
                    .build()
                
                val response = client.newCall(request).execute()
                
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    if (responseBody != null) {
                        val jsonObject = JSONObject(responseBody)
                        val audioUrl = jsonObject.optString("audioUrl", null)
                        
                        if (audioUrl != null) {
                            withContext(Dispatchers.Main) {
                                // Set the media item and prepare the player
                                val mediaItem = MediaItem.fromUri(audioUrl)
                                exoPlayer?.setMediaItem(mediaItem)
                                exoPlayer?.repeatMode = repeatMode
                                exoPlayer?.prepare()
                                
                                // Only play if autoPlay is true
                                if (autoPlay) {
                                    exoPlayer?.play()
                                    setPlaying(true)
                                } else {
                                    // Reset position to beginning
                                    exoPlayer?.seekTo(0)
                                    setPlaying(false)
                                }
                                
                                // Notify UI of state change
                                notifyStateChange()
                            }
                        } else {
                            Log.e("PlayerTab", "Audio URL not found for song: ${song.id}")
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Audio not available", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Log.e("PlayerTab", "Failed to get audio URL: ${response.code}")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Failed to load audio", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("PlayerTab", "Error loading audio", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error loading audio: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    fun seekTo(position: Long) {
        exoPlayer?.seekTo(position)
        currentPosition = position
        if (totalDuration > 0) {
            playbackProgress = position.toFloat() / totalDuration.toFloat()
        }
        notifyStateChange()
    }
    
    // Add a method to update the current position and progress
    fun updatePosition() {
        val player = exoPlayer ?: return
        val newPosition = player.currentPosition
        val newDuration = player.duration.coerceAtLeast(0L)
        
        if (newPosition != currentPosition || newDuration != totalDuration) {
            currentPosition = newPosition
            totalDuration = newDuration
            
            if (newDuration > 0) {
                playbackProgress = newPosition.toFloat() / newDuration.toFloat()
            }
            
            notifyStateChange()
        }
    }
}

@Composable
fun PlayerTab() {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    val userId = sharedPreferences.getLong("user_id", -1)
    
    // Create OkHttpClient
    val client = remember {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    // Initialize the persistent player if not already initialized
    LaunchedEffect(Unit) {
        PlayerManager.initializePlayer(context)
    }
    
    // Get state from the persistent player manager
    var currentSong by remember { mutableStateOf(PlayerManager.getCurrentSong()) }
    var isPlaying by remember { mutableStateOf(PlayerManager.isPlaying()) }
    var playbackProgress by remember { mutableStateOf(PlayerManager.getPlaybackProgress()) }
    var totalDuration by remember { mutableStateOf(PlayerManager.getTotalDuration()) }
    var currentPosition by remember { mutableStateOf(PlayerManager.getCurrentPosition()) }
    var playQueue by remember { mutableStateOf(PlayerManager.getPlayQueue()) }
    var repeatMode by remember { mutableStateOf(PlayerManager.getRepeatMode()) }
    var autoPlayNext by remember { mutableStateOf(PlayerManager.isAutoPlayNext()) }
    
    // Set up a callback to update the UI when the player state changes
    DisposableEffect(Unit) {
        val callback = {
            currentSong = PlayerManager.getCurrentSong()
            isPlaying = PlayerManager.isPlaying()
            playbackProgress = PlayerManager.getPlaybackProgress()
            totalDuration = PlayerManager.getTotalDuration()
            currentPosition = PlayerManager.getCurrentPosition()
            playQueue = PlayerManager.getPlayQueue()
            repeatMode = PlayerManager.getRepeatMode()
            autoPlayNext = PlayerManager.isAutoPlayNext()
        }
        
        PlayerManager.addStateChangeCallback(callback)
        
        onDispose {
            PlayerManager.removeStateChangeCallback(callback)
        }
    }
    
    // Update progress periodically
    LaunchedEffect(isPlaying) {
        while (true) {
            if (isPlaying) {
                PlayerManager.updatePosition()
            }
            delay(500) // Update more frequently (every 500ms)
        }
    }
    
    // Load queue on initial composition
    LaunchedEffect(userId) {
        if (userId != -1L) {
            loadQueue(userId, client) { loadedQueue ->
                playQueue = loadedQueue
                PlayerManager.setPlayQueue(loadedQueue)
                
                // Set the first song as current if available
                if (loadedQueue.isNotEmpty() && currentSong == null) {
                    currentSong = loadedQueue[0]
                    PlayerManager.setCurrentSong(currentSong)
                    // Load the audio for the first song but don't auto-play
                    PlayerManager.loadAudioForSong(currentSong!!, context, false)
                }
            }
        }
    }
    
    // Create a scrollable column for the entire content
    val scrollState = rememberScrollState()
    
    // Single column layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Title
        Text(
            text = "Now Playing",
            fontFamily = interFontFamily,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Song Artwork
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.DarkGray.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            if (currentSong?.imageUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(currentSong?.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Song Artwork",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = rememberVectorPainter(Icons.Filled.MusicNote)
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.MusicNote,
                    contentDescription = "Song Artwork Placeholder",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(100.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Song Info
        Text(
            text = currentSong?.title ?: "No Song Selected",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = interFontFamily,
            maxLines = 1
        )
        
        Text(
            text = currentSong?.artist ?: "Select a song to play",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 16.sp,
            fontFamily = interLightFontFamily,
            maxLines = 1
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Progress Bar and Time
        Column(modifier = Modifier.fillMaxWidth()) {
            Slider(
                value = playbackProgress,
                enabled = isPlaying,
                onValueChange = { newSliderValue ->
                    // Update the displayed time and slider position immediately during drag
                    if (totalDuration > 0) {
                        currentPosition = (newSliderValue * totalDuration).toLong()
                        PlayerManager.setCurrentPosition(currentPosition)
                    }
                },
                onValueChangeFinished = { // Seek when user finishes dragging
                    if (totalDuration > 0) {
                        // Use the final position reflected in currentPosition
                        PlayerManager.seekTo(currentPosition)
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
                Text(
                    text = formatTimeMillis(currentPosition),
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    fontFamily = interLightFontFamily
                )
                Text(
                    text = formatTimeMillis(totalDuration),
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    fontFamily = interLightFontFamily
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Playback Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(onClick = { 
                // Play previous song
                if (playQueue.isNotEmpty() && currentSong != null) {
                    val currentIndex = playQueue.indexOf(currentSong)
                    if (currentIndex > 0) {
                        val previousSong = playQueue[currentIndex - 1]
                        currentSong = previousSong
                        PlayerManager.setCurrentSong(previousSong)
                        PlayerManager.loadAudioForSong(previousSong, context, true)
                    }
                }
            }) {
                Icon(Icons.Default.SkipPrevious, "Previous", tint = Color.White, modifier = Modifier.size(40.dp))
            }
            
            IconButton(
                onClick = { 
                    // Toggle play/pause
                    if (currentSong != null) {
                        isPlaying = !isPlaying
                        PlayerManager.setPlaying(isPlaying)
                    }
                },
                modifier = Modifier
                    .size(72.dp)
                    .background(Color.White, shape = RoundedCornerShape(36.dp)),
                enabled = currentSong != null // Disabled when no song is selected
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = if (currentSong != null) Color(0xFF6A1B9A) else Color.Gray, // Grayed out when disabled
                    modifier = Modifier.size(48.dp)
                )
            }
            
            IconButton(onClick = { 
                // Play next song
                if (playQueue.isNotEmpty() && currentSong != null) {
                    val currentIndex = playQueue.indexOf(currentSong)
                    if (currentIndex < playQueue.size - 1) {
                        val nextSong = playQueue[currentIndex + 1]
                        currentSong = nextSong
                        PlayerManager.setCurrentSong(nextSong)
                        PlayerManager.loadAudioForSong(nextSong, context, true)
                    }
                }
            }) {
                Icon(Icons.Default.SkipNext, "Next", tint = Color.White, modifier = Modifier.size(40.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Add auto-play toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Auto-play next song",
                color = Color.White,
                fontFamily = interFontFamily,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = autoPlayNext,
                onCheckedChange = { checked ->
                    autoPlayNext = checked
                    PlayerManager.setAutoPlayNext(checked)
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFFE91E63),
                    checkedTrackColor = Color(0xFFE91E63).copy(alpha = 0.5f)
                )
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Play Queue Section
        Text(
            text = "Play Queue",
            fontFamily = interFontFamily,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        if (playQueue.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color.DarkGray.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No songs in queue",
                    color = Color.White.copy(alpha = 0.7f),
                    fontFamily = interLightFontFamily
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                playQueue.forEachIndexed { index, song ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (song == currentSong) Color(0xFF6A1B9A).copy(alpha = 0.7f) 
                                else Color.DarkGray.copy(alpha = 0.3f), 
                                RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp)
                            .clickable {
                                // Play this song
                                currentSong = song
                                PlayerManager.setCurrentSong(song)
                                PlayerManager.loadAudioForSong(song, context, true)
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${index + 1}",
                            color = Color.White,
                            fontFamily = interFontFamily,
                            modifier = Modifier.width(24.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = song.title,
                                    color = Color.White,
                                    fontFamily = interFontFamily,
                                    maxLines = 1
                                )
                                Text(
                                    text = song.artist,
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontFamily = interLightFontFamily,
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                            }
                        }
                        
                        IconButton(onClick = { 
                            // Remove from queue
                            removeFromQueue(userId, song.id, client) {
                                // Update the queue after removal
                                loadQueue(userId, client) { updatedQueue ->
                                    playQueue = updatedQueue
                                    PlayerManager.setPlayQueue(updatedQueue)
                                    // If we removed the current song, select the first one
                                    if (song == currentSong) {
                                        currentSong = if (updatedQueue.isNotEmpty()) updatedQueue[0] else null
                                        PlayerManager.setCurrentSong(currentSong)
                                        isPlaying = false
                                        PlayerManager.setPlaying(false)
                                        PlayerManager.getPlayer()?.stop()
                                    }
                                }
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove from queue",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
        
        // Add bottom padding
        Spacer(modifier = Modifier.height(24.dp))
    }
}

// Function to load the queue from the backend
private fun loadQueue(userId: Long, client: OkHttpClient, onQueueLoaded: (List<Song>) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            // First, get the song IDs from the user's playlist
            val songIdsRequest = Request.Builder()
                .url("${Constants.BASE_URL}/api/playlists/user/$userId/songs")
                .get()
                .build()
            
            val songIdsResponse = client.newCall(songIdsRequest).execute()
            
            if (songIdsResponse.isSuccessful) {
                val songIdsBody = songIdsResponse.body?.string()
                if (songIdsBody != null) {
                    val songIdsArray = org.json.JSONArray(songIdsBody)
                    val songs = mutableListOf<Song>()
                    
                    // For each song ID, fetch the song details
                    for (i in 0 until songIdsArray.length()) {
                        val songId = songIdsArray.getInt(i)
                        val songRequest = Request.Builder()
                            .url("${Constants.BASE_URL}/api/music/getMusic/$songId")
                            .get()
                            .build()
                        
                        val songResponse = client.newCall(songRequest).execute()
                        
                        if (songResponse.isSuccessful) {
                            val songBody = songResponse.body?.string()
                            if (songBody != null) {
                                val songObject = org.json.JSONObject(songBody)
                                
                                val song = Song(
                                    id = songObject.getInt("musicId"),
                                    title = songObject.getString("title"),
                                    artist = songObject.getString("artist"),
                                    imageUrl = songObject.optString("imageUrl", null)
                                )
                                songs.add(song)
                            }
                        }
                    }
                    
                    withContext(Dispatchers.Main) {
                        onQueueLoaded(songs)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onQueueLoaded(emptyList())
                    }
                }
            } else {
                Log.e("PlayerTab", "Failed to load song IDs: ${songIdsResponse.code}")
                withContext(Dispatchers.Main) {
                    onQueueLoaded(emptyList())
                }
            }
        } catch (e: Exception) {
            Log.e("PlayerTab", "Error loading queue", e)
            withContext(Dispatchers.Main) {
                onQueueLoaded(emptyList())
            }
        }
    }
}

// Function to add a song to the queue
fun addToQueue(userId: Long, songId: Int, client: OkHttpClient, onSuccess: () -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val request = Request.Builder()
                .url("${Constants.BASE_URL}/api/playlists/postPlaylist/user/$userId/music/$songId")
                .post(okhttp3.RequestBody.create(null, ByteArray(0)))
                .build()
            
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } else {
                Log.e("PlayerTab", "Failed to add to queue: ${response.code}")
            }
        } catch (e: Exception) {
            Log.e("PlayerTab", "Error adding to queue", e)
        }
    }
}

// Function to remove a song from the queue
private fun removeFromQueue(userId: Long, songId: Int, client: OkHttpClient, onSuccess: () -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val request = Request.Builder()
                .url("${Constants.BASE_URL}/api/playlists/deletePlaylist/user/$userId/music/$songId")
                .delete()
                .build()
            
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } else {
                Log.e("PlayerTab", "Failed to remove from queue: ${response.code}")
            }
        } catch (e: Exception) {
            Log.e("PlayerTab", "Error removing from queue", e)
        }
    }
}

// Update formatTime to accept milliseconds
fun formatTimeMillis(millis: Long): String {
    val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(millis)
    val minutes = TimeUnit.SECONDS.toMinutes(totalSeconds)
    val remainingSeconds = totalSeconds - TimeUnit.MINUTES.toSeconds(minutes)
    return String.format("%02d:%02d", minutes, remainingSeconds)
} 