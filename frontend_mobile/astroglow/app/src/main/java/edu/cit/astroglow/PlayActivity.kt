package edu.cit.astroglow

import android.os.Bundle
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
import java.util.concurrent.TimeUnit

class PlayActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // TODO: Retrieve song details from intent
        val songId = intent.getIntExtra("SONG_ID", -1)
        val songTitle = intent.getStringExtra("SONG_TITLE") ?: "Unknown Title"
        val songArtist = intent.getStringExtra("SONG_ARTIST") ?: "Unknown Artist"
        val songImageUrl = intent.getStringExtra("SONG_IMAGE_URL")
        
        setContent {
            AstroglowTheme {
                PlayScreen(
                    songTitle = songTitle,
                    songArtist = songArtist,
                    songImageUrl = songImageUrl,
                    onBack = { finish() } // Close activity on back press
                )
            }
        }
    }
}

@Composable
fun PlayScreen(
    songTitle: String,
    songArtist: String,
    songImageUrl: String?,
    onBack: () -> Unit
) {
    var isPlaying by remember { mutableStateOf(false) }
    var isFavorite by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(0f) } // 0.0f to 1.0f
    var totalDuration by remember { mutableStateOf(224L) } // Example duration in seconds (3:44)
    var shuffleEnabled by remember { mutableStateOf(false) }
    var repeatMode by remember { mutableStateOf(RepeatMode.Off) } // Off, One, All

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
            IconButton(onClick = { isFavorite = !isFavorite }) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) Color(0xFFE91E63) else Color.White, // Pink when favorite
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

