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
    var isPlaying by remember { mutableStateOf(false) }
    var isFavorite by remember { mutableStateOf(false) }
    var isLoadingFavorite by remember { mutableStateOf(true) }
    var currentPosition by remember { mutableStateOf(0f) }
    var totalDuration by remember { mutableStateOf(224L) }
    var shuffleEnabled by remember { mutableStateOf(false) }
    var repeatMode by remember { mutableStateOf(RepeatMode.Off) }

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
                        withContext(Dispatchers.Main) {
                            isFavorite = isFav
                        }
                    } else {
                         Log.w("PlayScreen", "Failed to check favorite status (assuming false): ${response.code}")
                         withContext(Dispatchers.Main) {
                            isFavorite = false
                         }
                    }
                } catch (e: Exception) {
                    Log.e("PlayScreen", "Error checking favorite status", e)
                     withContext(Dispatchers.Main) {
                         isFavorite = false
                        Toast.makeText(context, "Error checking favorite status", Toast.LENGTH_SHORT).show()
                    }
                } finally {
                    withContext(Dispatchers.Main) {
                        isLoadingFavorite = false
                    }
                }
            }
        } else {
             Log.w("PlayScreen", "Skipping favorite check due to invalid IDs (User: $userId, Song: $songId)")
             isLoadingFavorite = false
        }
    }

    val formattedCurrentTime = formatTime( (currentPosition * totalDuration).toLong() )
    val formattedTotalTime = formatTime(totalDuration)

    // Example gradient background matching the provided image
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF6A1B9A), Color(0xFF283593)) // Purple to Dark Blue
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween // Pushes controls to bottom
    ) {
        // Top Bar (Optional - simple back button)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            // Spacer to push potential title or actions to the right if needed
            Spacer(modifier = Modifier.weight(1f))
            // IconButton for Share (optional)
             IconButton(onClick = { /* TODO: Implement Share */ }) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = Color.White
                )
            }
        }

        // Song Artwork Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f) // Square aspect ratio
                .clip(RoundedCornerShape(16.dp))
                .background(Color.DarkGray.copy(alpha = 0.5f)), // Placeholder background
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

        Spacer(modifier = Modifier.height(24.dp))

        // Progress Bar and Time
        Column(modifier = Modifier.fillMaxWidth()) {
            Slider(
                value = currentPosition,
                onValueChange = { currentPosition = it },
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color(0xFFE91E63), // Pink accent
                    inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formattedCurrentTime,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    fontFamily = interLightFontFamily
                )
                Text(
                    text = formattedTotalTime,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    fontFamily = interLightFontFamily
                )
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
                            // --- Add Favorite (POST) --- 
                            Log.d("PlayScreen", "Attempting to ADD favorite: User $userId, Song $songId")
                            val addUrl = "${Constants.BASE_URL}/api/favorites/postFavorites"
                            val jsonBody = JSONObject().apply {
                                put("user", JSONObject().put("userId", userId))
                                put("music", JSONObject().put("musicId", songId))
                            }.toString()
                            val requestBody = jsonBody.toRequestBody("application/json".toMediaType())
                            val request = Request.Builder().url(addUrl).post(requestBody).build()
                            
                            val response = client.newCall(request).execute()
                            Log.d("PlayScreen", "Add favorite response code: ${response.code}")

                            if (response.isSuccessful || response.code == 409) { // Success or Already Exists
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
                             // --- Remove Favorite (DELETE) --- 
                            Log.d("PlayScreen", "Attempting to REMOVE favorite: User $userId, Song $songId")
                            // Correct DELETE endpoint from FavoritesController
                            val deleteUrl = "${Constants.BASE_URL}/api/favorites/user/$userId/music/$songId"
                            
                            val request = Request.Builder().url(deleteUrl).delete().build()
                            val response = client.newCall(request).execute()
                            Log.d("PlayScreen", "Remove favorite response code: ${response.code}")

                            if (response.isSuccessful || response.code == 404) { // Success or Not Found
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
            horizontalArrangement = Arrangement.SpaceAround // Distribute controls evenly
        ) {
            // Shuffle Button
            IconButton(onClick = { shuffleEnabled = !shuffleEnabled }) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (shuffleEnabled) Color(0xFFE91E63) else Color.White // Pink accent when active
                )
            }

            // Previous Button
            IconButton(onClick = { /* TODO: Implement Previous Track */ }) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            // Play/Pause Button (Larger)
            IconButton(
                onClick = { isPlaying = !isPlaying },
                modifier = Modifier
                    .size(72.dp)
                    .background(Color.White, shape = RoundedCornerShape(36.dp))
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color(0xFF6A1B9A), // Purple icon color
                    modifier = Modifier.size(48.dp)
                )
            }

            // Next Button
            IconButton(onClick = { /* TODO: Implement Next Track */ }) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            // Repeat Button
            IconButton(onClick = {
                repeatMode = when (repeatMode) {
                    RepeatMode.Off -> RepeatMode.All
                    RepeatMode.All -> RepeatMode.One
                    RepeatMode.One -> RepeatMode.Off
                }
            }) {
                Icon(
                    imageVector = when (repeatMode) {
                        RepeatMode.Off -> Icons.Default.Repeat
                        RepeatMode.All -> Icons.Default.Repeat
                        RepeatMode.One -> Icons.Default.RepeatOne
                    },
                    contentDescription = "Repeat",
                    tint = if (repeatMode != RepeatMode.Off) Color(0xFFE91E63) else Color.White // Pink accent when active
                )
            }
        }

         Spacer(modifier = Modifier.height(24.dp)) // Add some padding at the bottom
    }
}

enum class RepeatMode {
    Off, One, All
}

// Helper function to format time in seconds to MM:SS
fun formatTime(seconds: Long): String {
    val minutes = TimeUnit.SECONDS.toMinutes(seconds)
    val remainingSeconds = seconds - TimeUnit.MINUTES.toSeconds(minutes)
    return String.format("%02d:%02d", minutes, remainingSeconds)
}

// --- Add necessary imports and font definitions if not already present globally ---
// Make sure interFontFamily and interLightFontFamily are defined or imported.
// Ensure necessary Material 3 imports are present.
// Add drawable resources for placeholder icons (ic_music_note, waveform_placeholder) if needed.

