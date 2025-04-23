package edu.cit.astroglow.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import edu.cit.astroglow.Constants
import edu.cit.astroglow.interFontFamily
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
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
    
    // Fetch random songs when the component is created
    LaunchedEffect(Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Switch to IO dispatcher for network operations
                val fetchedSongs = withContext(Dispatchers.IO) {
                    val client = OkHttpClient.Builder()
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .build()
                    
                    val request = Request.Builder()
                        .url("${Constants.BASE_URL}/api/music/getAllMusic")
                        .get()
                        .build()
                    
                    val response = client.newCall(request).execute()
                    
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string() ?: "[]"
                        val jsonArray = JSONArray(responseBody)
                        
                        // Get a random subset of songs (up to 5)
                        val songList = mutableListOf<RecommendationSong>()
                        val totalSongs = jsonArray.length()
                        
                        if (totalSongs > 0) {
                            // Get up to 5 random songs
                            val numSongsToShow = minOf(5, totalSongs)
                            val usedIndices = mutableSetOf<Int>()
                            
                            while (songList.size < numSongsToShow && usedIndices.size < totalSongs) {
                                val randomIndex = (0 until totalSongs).random()
                                if (usedIndices.add(randomIndex)) {
                                    val songObject = jsonArray.getJSONObject(randomIndex)
                                    val song = RecommendationSong(
                                        id = songObject.getInt("musicId"),
                                        title = songObject.getString("title"),
                                        artist = songObject.getString("artist"),
                                        imageUrl = songObject.optString("imageUrl", null).takeIf { it.isNotEmpty() }
                                    )
                                    songList.add(song)
                                }
                            }
                        }
                        
                        songList
                    } else {
                        Log.e("RecommendationsSection", "Error loading songs: ${response.code}")
                        null
                    }
                }
                
                // Update UI state based on the result
                if (fetchedSongs != null) {
                    songs = fetchedSongs
                    isLoading = false
                } else {
                    error = "Failed to load recommendations"
                    isLoading = false
                }
            } catch (e: Exception) {
                error = "Error: ${e.message}"
                isLoading = false
                Log.e("RecommendationsSection", "Exception loading songs", e)
            }
        }
    }
    
    Column {
        // Recommendations section with label and arrow side by side - positioned LEFT
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Title and arrow side by side
            Row(
                verticalAlignment = Alignment.CenterVertically
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
                    contentDescription = "See more",
                    tint = Color.White,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(24.dp)
                )
            }
        }

        // Recommendations carousel with gradient background matching app theme
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .padding(vertical = 8.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF9C27B0).copy(alpha = 0.85f),  // Purple
                            Color(0xFF0050D0).copy(alpha = 0.85f)   // Blue
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
        ) {
            if (isLoading) {
                // Show loading state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Loading recommendations...",
                        color = Color.White,
                        fontFamily = interFontFamily
                    )
                }
            } else if (error != null) {
                // Show error state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = error ?: "Unknown error",
                        color = Color.White,
                        fontFamily = interFontFamily,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else if (songs.isEmpty()) {
                // Show empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No recommendations available",
                        color = Color.White,
                        fontFamily = interFontFamily,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                // Display songs in a horizontal scrollable list
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(songs) { song ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Song thumbnail
                            Box(
                                modifier = Modifier
                                    .width(160.dp)
                                    .height(140.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White)
                            ) {
                                if (song.imageUrl != null) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(song.imageUrl)
                                            .crossfade(true)
                                            .size(width = 320, height = 280)
                                            .build(),
                                        contentDescription = "Song thumbnail",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                }
                            }
                            
                            // Song title and artist
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
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
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
                                        fontFamily = interFontFamily,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.width(150.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
} 