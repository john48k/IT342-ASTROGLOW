package edu.cit.astroglow.components

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import edu.cit.astroglow.Constants
import edu.cit.astroglow.PlayActivity
import edu.cit.astroglow.interFontFamily
import edu.cit.astroglow.interLightFontFamily
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import android.widget.Toast
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

data class RecommendationSong(
    val id: Int,
    val title: String,
    val artist: String,
    val imageUrl: String? = null
)

@Composable
fun RecommendationsSection() {
    var songs by remember { mutableStateOf<List<RecommendationSong>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var refreshTrigger by remember { mutableStateOf(0) } // Add a trigger to refresh recommendations
    
    // Fetch random songs
    LaunchedEffect(refreshTrigger) { // Change dependency to refreshTrigger
        isLoading = true
        error = null
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val fetchedSongs = withContext(Dispatchers.IO) {
                    val client = OkHttpClient.Builder()
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .build()
                    
                    // Assuming recommendations come from the same endpoint
                    // Adjust if there's a specific recommendations endpoint
                    val request = Request.Builder()
                        .url("${Constants.BASE_URL}/api/music/getAllMusic") 
                        .get()
                        .build()
                    
                    val response = client.newCall(request).execute()
                    
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string() ?: "[]"
                        val jsonArray = JSONArray(responseBody)
                        
                        val songList = mutableListOf<RecommendationSong>()
                        val indices = (0 until jsonArray.length()).shuffled().take(5) // Get up to 5 random indices

                        for (i in indices) {
                             val songObject = jsonArray.getJSONObject(i)
                            // Assuming the fields match RecommendationSong data class
                            val song = RecommendationSong(
                                id = songObject.getInt("musicId"),
                                title = songObject.getString("title"),
                                artist = songObject.getString("artist"),
                                imageUrl = songObject.optString("imageUrl", null).takeIf { it.isNotEmpty() }
                            )
                            songList.add(song)
                        }
                        songList
                    } else {
                        Log.e("RecommendationsSection", "Error loading recommendations: ${response.code}")
                        null // Indicate error
                    }
                }
                
                if (fetchedSongs != null) {
                    songs = fetchedSongs
                } else {
                    error = "Failed to load recommendations"
                }
            } catch (e: Exception) {
                error = "Error: ${e.message}"
                Log.e("RecommendationsSection", "Exception loading recommendations", e)
            } finally {
                 isLoading = false
            }
        }
    }
    
    Column {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { 
                    // Increment refreshTrigger to trigger a new fetch
                    refreshTrigger++
                }
            ) {
                Text(
                    text = "Recommendations",
                    fontFamily = interFontFamily,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Refresh recommendations",
                    tint = Color.White,
                    modifier = Modifier.padding(start = 8.dp).size(24.dp)
                )
            }
        }

        // Carousel Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp) 
                .padding(vertical = 8.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF9C27B0).copy(alpha = 0.85f), 
                            Color(0xFF0050D0).copy(alpha = 0.85f)  
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
        ) {
             when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
                error != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = error ?: "Unknown error",
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp),
                            fontFamily = interLightFontFamily
                        )
                    }
                }
                songs.isEmpty() -> {
                     Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No recommendations available",
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp),
                            fontFamily = interLightFontFamily
                        )
                    }
                }
                else -> {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(songs) { song ->
                           // Use the new RecommendationItem composable
                           RecommendationItem(song = song)
                        }
                    }
                }
            }
        }
    }
}

// Composable for displaying a single recommendation item
@Composable
fun RecommendationItem(song: RecommendationSong) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    val userId = sharedPreferences.getLong("user_id", -1)
    
    var isAddingToPlaylist by remember { mutableStateOf(false) }
    var isInQueue by remember { mutableStateOf(false) }
    var isLoadingQueue by remember { mutableStateOf(true) }
    
    // Use produceState for potential secondary image fetch (similar to favorites)
    val finalImageUrl by produceState<String?>(initialValue = song.imageUrl, key1 = song.id) {
        if (value == null) {
            Log.d("RecommendationItem", "Initial imageUrl is null for song ID ${song.id} ('${song.title}'), fetching details...")
            CoroutineScope(Dispatchers.IO).launch {
                var fetchedUrl: String? = null
                var fetchStatus: String = "pending"
                try {
                    val client = OkHttpClient.Builder().build()
                    val request = Request.Builder()
                        .url("${Constants.BASE_URL}/api/music/getMusic/${song.id}")
                        .get()
                        .build()
                    val response = client.newCall(request).execute()
                    fetchStatus = "Response Code: ${response.code}"
                    if (response.isSuccessful) {
                        val musicDetailsJson = response.body?.string()
                        if (musicDetailsJson != null) {
                            val musicObject = JSONObject(musicDetailsJson)
                            fetchedUrl = musicObject.optString("imageUrl", null)?.takeIf { it.isNotEmpty() }
                            fetchStatus += ", Fetched URL: ${fetchedUrl ?: "null"}"
                            value = fetchedUrl // Update state
                        } else {
                            fetchStatus += ", Body was null"
                            value = null // Keep null if body is null
                        }
                    } else {
                        fetchStatus += ", Fetch failed"
                        value = "error" // Set error state
                    }
                } catch (e: Exception) {
                    Log.e("RecommendationItem", "Exception fetching image details for ${song.id}", e)
                    fetchStatus = "Exception: ${e.message}"
                    value = "error" // Set error state
                }
                Log.d("RecommendationItem", "Secondary fetch for song ID ${song.id} finished. Status: $fetchStatus. Final state: $value")
            }
        } else {
            Log.d("RecommendationItem", "Using initial imageUrl for song ID ${song.id}: ${value?.take(50)}...")
            // Initial value was not null, state is already set
        }
    }
    
    // Check if song is in queue
    LaunchedEffect(key1 = song.id, key2 = userId) {
        if (userId != -1L && song.id != -1) {
            isLoadingQueue = true
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val client = OkHttpClient.Builder()
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .build()
                    
                    val checkUrl = "${Constants.BASE_URL}/api/playlists/user/$userId/music/${song.id}/check"
                    val request = Request.Builder().url(checkUrl).get().build()
                    val response = client.newCall(request).execute()
                    Log.d("RecommendationItem", "isInQueue check response code: ${response.code}")
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        val inQueue = responseBody?.toBooleanStrictOrNull() ?: false
                        Log.d("RecommendationItem", "isInQueue check result: $inQueue")
                        withContext(Dispatchers.Main) { isInQueue = inQueue }
                    } else {
                        Log.w("RecommendationItem", "Failed to check queue status (assuming false): ${response.code}")
                        withContext(Dispatchers.Main) { isInQueue = false }
                    }
                } catch (e: Exception) {
                    Log.e("RecommendationItem", "Error checking queue status", e)
                    withContext(Dispatchers.Main) { isInQueue = false }
                } finally {
                    withContext(Dispatchers.Main) { isLoadingQueue = false }
                }
            }
        } else {
            Log.w("RecommendationItem", "Skipping queue check due to invalid IDs (User: $userId, Song: ${song.id})")
            isLoadingQueue = false
        }
    }
    
    // Add a periodic check to ensure queue status is always up-to-date
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000) // Check every 5 seconds
            if (userId != -1L && song.id != -1) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val client = OkHttpClient.Builder()
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(30, TimeUnit.SECONDS)
                            .writeTimeout(30, TimeUnit.SECONDS)
                            .build()
                        
                        val checkUrl = "${Constants.BASE_URL}/api/playlists/user/$userId/music/${song.id}/check"
                        val request = Request.Builder().url(checkUrl).get().build()
                        val response = client.newCall(request).execute()
                        if (response.isSuccessful) {
                            val responseBody = response.body?.string()
                            val inQueue = responseBody?.toBooleanStrictOrNull() ?: false
                            withContext(Dispatchers.Main) { isInQueue = inQueue }
                        }
                    } catch (e: Exception) {
                        Log.e("RecommendationItem", "Error in periodic queue check", e)
                    }
                }
            }
        }
    }
    
    // Function to add/remove song from queue
    fun toggleQueue() {
        if (userId <= 0) {
            Toast.makeText(context, "Error: Invalid user ID", Toast.LENGTH_SHORT).show()
            return
        }
        
        isAddingToPlaylist = true
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build()
                
                if (!isInQueue) {
                    // Add to queue
                    val url = "${Constants.BASE_URL}/api/playlists/postPlaylist/user/$userId/music/${song.id}"
                    val request = Request.Builder()
                        .url(url)
                        .post("".toRequestBody("application/json".toMediaType()))
                        .build()
                    
                    val response = client.newCall(request).execute()
                    
                    withContext(Dispatchers.Main) {
                        isAddingToPlaylist = false
                        
                        if (response.isSuccessful) {
                            isInQueue = true
                            Toast.makeText(context, "Added to queue", Toast.LENGTH_SHORT).show()
                        } else {
                            val responseBody = response.body?.string() ?: "Unknown error"
                            
                            // Check if the error is because the song is already in the playlist
                            if (response.code == 409 && responseBody.contains("already in the playlist")) {
                                isInQueue = true
                                Toast.makeText(context, "Song is already in your queue", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Failed to add to queue: ${response.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    // Remove from queue
                    val url = "${Constants.BASE_URL}/api/playlists/deletePlaylist/user/$userId/music/${song.id}"
                    val request = Request.Builder()
                        .url(url)
                        .delete()
                        .build()
                    
                    val response = client.newCall(request).execute()
                    
                    withContext(Dispatchers.Main) {
                        isAddingToPlaylist = false
                        
                        if (response.isSuccessful) {
                            isInQueue = false
                            Toast.makeText(context, "Removed from queue", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Failed to remove from queue: ${response.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isAddingToPlaylist = false
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
         modifier = Modifier.clickable { // Make item clickable
             val intent = Intent(context, PlayActivity::class.java).apply {
                 putExtra("SONG_ID", song.id)
                 putExtra("SONG_TITLE", song.title)
                 putExtra("SONG_ARTIST", song.artist)
                 // Pass the final determined image URL (can still be null if fetch failed)
                 putExtra("SONG_IMAGE_URL", finalImageUrl?.takeIf { it != "error" })
             }
             context.startActivity(intent)
         }
    ) {
        // Image Box
        Box(
            modifier = Modifier
                .width(160.dp)
                .height(140.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
             when (finalImageUrl) {
                 null -> CircularProgressIndicator(modifier = Modifier.size(32.dp), color = Color.White.copy(alpha=0.5f))
                 "error" -> Icon(Icons.Filled.BrokenImage, "Error loading image", tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(40.dp))
                 else -> AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(finalImageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Song thumbnail",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        error = rememberVectorPainter(Icons.Filled.MusicNote)
                    )
            }
        }
        // Info Box
        Box(
            modifier = Modifier
                .width(160.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF9C27B0).copy(alpha = 0.6f),
                            Color(0xFF0050D0).copy(alpha = 0.6f)
                        )
                    )
                )
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = song.title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontFamily = interFontFamily,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.width(150.dp)
                )
                Text(
                    text = song.artist,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    fontFamily = interLightFontFamily,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.width(150.dp)
                )
                
                // Add to playlist button
                IconButton(
                    onClick = { toggleQueue() },
                    enabled = !isAddingToPlaylist && !isLoadingQueue,
                    modifier = Modifier.size(24.dp)
                ) {
                    if (isAddingToPlaylist) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.PlaylistAdd,
                            contentDescription = if (isInQueue) "Remove from Queue" else "Add to Queue",
                            tint = if (isLoadingQueue) Color.Gray else if (isInQueue) Color(0xFFE91E63) else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
} 