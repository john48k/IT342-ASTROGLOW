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
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import androidx.compose.runtime.produceState
import androidx.compose.material.icons.filled.BrokenImage

// Data class to hold favorite song details (adjust fields based on actual API response)
data class FavoriteSong(
    val favoriteId: Int,
    val musicId: Int,
    val title: String,
    val artist: String,
    val genre: String? = null,
    val imageUrl: String? = null
    // Add other relevant fields if needed, e.g., audioUrl
)

@Composable
fun FavoritesSection() {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    val userId = sharedPreferences.getLong("user_id", -1)

    var favoriteSongs by remember { mutableStateOf<List<FavoriteSong>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userId) {
        if (userId == -1L) {
            error = "User not logged in"
            isLoading = false
            return@LaunchedEffect
        }

        isLoading = true
        error = null
        Log.d("FavoritesSection", "Fetching favorites for user: $userId")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build()

                val request = Request.Builder()
                    .url("${Constants.BASE_URL}/api/favorites/user/$userId/music-details")
                    .get()
                    .build()

                val response = client.newCall(request).execute()
                Log.d("FavoritesSection", "Favorites fetch response code: ${response.code}")

                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: "[]"
                    val jsonArray = JSONArray(responseBody)
                    val songs = mutableListOf<FavoriteSong>()
                    for (i in 0 until jsonArray.length()) {
                        val songObject = jsonArray.getJSONObject(i)

                        // Safely parse image URL
                        val rawImageUrl = songObject.optString("imageUrl", null)
                        val processedImageUrl = if (rawImageUrl != null && rawImageUrl.isNotEmpty() && rawImageUrl != "Image data available") {
                            rawImageUrl // Use the URL if it's valid and not the placeholder text
                        } else {
                             Log.w("FavoritesSection", "ImageUrl is null, empty, or placeholder text for musicId: ${songObject.getInt("musicId")}. Setting to null.")
                             null // Fallback to null if not directly usable
                        }

                        val song = FavoriteSong(
                            favoriteId = songObject.getInt("favoriteId"),
                            musicId = songObject.getInt("musicId"),
                            title = songObject.getString("title"),
                            artist = songObject.getString("artist"),
                            genre = songObject.optString("genre", null),
                            imageUrl = processedImageUrl // Assign the safely processed URL
                        )
                        songs.add(song)
                    }
                    withContext(Dispatchers.Main) {
                        favoriteSongs = songs
                        isLoading = false
                    }
                } else {
                    Log.e("FavoritesSection", "Failed to fetch favorites: ${response.code}")
                    withContext(Dispatchers.Main) {
                        error = "Failed to load favorites"
                        isLoading = false
                    }
                }
            } catch (e: Exception) {
                Log.e("FavoritesSection", "Error fetching favorites", e)
                withContext(Dispatchers.Main) {
                    error = "Error: ${e.message}"
                    isLoading = false
                }
            }
        }
    }

    Column {
        // Favorites section header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Favorites",
                    fontFamily = interFontFamily,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Outlined.Favorite,
                    contentDescription = "Favorites",
                    tint = Color.White,
                    modifier = Modifier.padding(start = 8.dp).size(24.dp)
                )
            }
        }

        // Favorites carousel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .padding(vertical = 8.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF0050D0).copy(alpha = 0.85f), // Blue
                            Color(0xFFE81EDE).copy(alpha = 0.85f)  // Pink
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
                favoriteSongs.isEmpty() -> {
                     Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No favorites added yet",
                            color = Color.White.copy(alpha = 0.8f),
                             textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp),
                            fontFamily = interLightFontFamily
                        )
                    }
                }
                else -> {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(favoriteSongs) { song ->
                           FavoriteSongItem(song = song) // Use a dedicated composable
                        }
                    }
                }
            }
        }
    }
}

// Composable for displaying a single favorite song item in the horizontal list
@Composable
fun FavoriteSongItem(song: FavoriteSong) {
    val context = LocalContext.current

    // State to hold the final image URL, potentially fetched in a secondary request
    val finalImageUrl by produceState<String?>(initialValue = song.imageUrl, key1 = song.musicId) {
        if (value == null) { // Check if initial imageUrl was null
            Log.d("FavoriteSongItem", "Initial imageUrl is null for ${song.title}, fetching details...")
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val client = OkHttpClient.Builder().build()
                    val request = Request.Builder()
                        .url("${Constants.BASE_URL}/api/music/getMusic/${song.musicId}")
                        .get()
                        .build()
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        val musicDetailsJson = response.body?.string()
                        if (musicDetailsJson != null) {
                            val musicObject = JSONObject(musicDetailsJson)
                            val actualUrl = musicObject.optString("imageUrl", null)
                            Log.d("FavoriteSongItem", "Fetched actual imageUrl: ${actualUrl?.take(50)}...")
                            value = actualUrl?.takeIf { it.isNotEmpty() }
                        } else {
                             Log.w("FavoriteSongItem", "Fetched music details body is null for ${song.musicId}")
                             value = null
                        }
                    } else {
                        Log.e("FavoriteSongItem", "Failed to fetch music details for ${song.musicId}: ${response.code}")
                        value = "error"
                    }
                } catch (e: Exception) {
                    Log.e("FavoriteSongItem", "Error fetching music details for ${song.musicId}", e)
                    value = "error"
                }
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.clickable { // Click logic remains the same
             val intent = Intent(context, PlayActivity::class.java).apply {
                 putExtra("SONG_ID", song.musicId)
                 putExtra("SONG_TITLE", song.title)
                 putExtra("SONG_ARTIST", song.artist)
                 // Don't pass fetched URL, let PlayActivity handle its own image
                 // putExtra("SONG_IMAGE_URL", finalImageUrl ?: song.imageUrl) 
             }
             context.startActivity(intent)
         }
    ) {
        // Image Box - Use finalImageUrl from produceState
        Box(
            modifier = Modifier
                .width(160.dp)
                .height(140.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            when (finalImageUrl) {
                 null -> { // Still loading the secondary request
                     CircularProgressIndicator(modifier = Modifier.size(32.dp), color = Color.White.copy(alpha=0.5f))
                }
                "error" -> { // Error during secondary fetch
                     Icon(
                        imageVector = Icons.Filled.BrokenImage, 
                        contentDescription = "Error loading image",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(40.dp)
                    )
                }
                else -> { // Image URL is ready
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(finalImageUrl)
                            .crossfade(true)
                            .size(320, 280) // Optional: size hint
                            .build(),
                        contentDescription = "Song thumbnail",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        error = rememberVectorPainter(Icons.Filled.MusicNote)
                    )
                }
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
                             Color(0xFF0050D0).copy(alpha = 0.6f),
                             Color(0xFFE81EDE).copy(alpha = 0.6f)
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
                    fontFamily = interLightFontFamily,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.width(150.dp)
                )
            }
        }
    }
} 