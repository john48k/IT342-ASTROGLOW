package edu.cit.astroglow.components

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.BrokenImage
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

@Composable
fun FavoritesTab() {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    val userId = sharedPreferences.getLong("user_id", -1)

    var favoriteSongs by remember { mutableStateOf<List<FavoriteSong>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // Fetch favorites when the tab is composed or userId changes
    LaunchedEffect(userId) {
        if (userId == -1L) {
            error = "User not logged in"
            isLoading = false
            return@LaunchedEffect
        }

        isLoading = true
        error = null
        Log.d("FavoritesTab", "Fetching favorites for user: $userId")

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
                Log.d("FavoritesTab", "Favorites fetch response code: ${response.code}")

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
                             // Construct a fallback URL (adjust this if your backend has a specific image endpoint)
                             // Example: Maybe fetch image from music endpoint directly if possible?
                             // For now, setting to null if not directly available or placeholder text
                             Log.w("FavoritesTab", "ImageUrl is null, empty, or placeholder text for musicId: ${songObject.getInt("musicId")}. Setting to null.")
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
                     Log.e("FavoritesTab", "Failed to fetch favorites: ${response.code}")
                    withContext(Dispatchers.Main) {
                        error = "Failed to load favorites"
                        isLoading = false
                    }
                }
            } catch (e: Exception) {
                 Log.e("FavoritesTab", "Error fetching favorites", e)
                withContext(Dispatchers.Main) {
                    error = "Error: ${e.message}"
                    isLoading = false
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        // Removed verticalScroll here as LazyVerticalGrid handles scrolling
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Favorites",
            fontFamily = interFontFamily,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        when {
            isLoading -> {
                Spacer(Modifier.height(50.dp))
                CircularProgressIndicator(color = Color.White)
            }
            error != null -> {
                Spacer(Modifier.height(50.dp))
                Text(
                    text = error ?: "Unknown error",
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp),
                    fontFamily = interLightFontFamily
                )
            }
            favoriteSongs.isEmpty() -> {
                 Icon(
                    imageVector = Icons.Outlined.Favorite,
                    contentDescription = "Favorites Placeholder",
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(80.dp).padding(bottom = 16.dp)
                )
                Text(
                    text = "Your favorite songs will appear here",
                    fontFamily = interFontFamily,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            }
            else -> {
                // Display favorites in a 2-column grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(favoriteSongs) { song ->
                        FavoriteGridItem(song = song)
                    }
                }
            }
        }
    }
}

// Composable for displaying a single favorite song item in the grid
@Composable
fun FavoriteGridItem(song: FavoriteSong) {
    val context = LocalContext.current

    // State to hold the final image URL, potentially fetched in a secondary request
    val finalImageUrl by produceState<String?>(initialValue = song.imageUrl, key1 = song.musicId) {
        // This block runs if the initial song.imageUrl needs processing or is null
        if (value == null) { // Check if initial imageUrl was null
            Log.d("FavoriteGridItem", "Initial imageUrl is null for ${song.title}, fetching details...")
            // Launch network request to get full music details
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val client = OkHttpClient.Builder().build() // Use a client
                    val request = Request.Builder()
                        .url("${Constants.BASE_URL}/api/music/getMusic/${song.musicId}")
                        .get()
                        .build()
                    val response = client.newCall(request).execute()

                    if (response.isSuccessful) {
                        val musicDetailsJson = response.body?.string()
                        if (musicDetailsJson != null) {
                            val musicObject = JSONObject(musicDetailsJson)
                            // Extract the actual image URL from the full music details
                            val actualUrl = musicObject.optString("imageUrl", null)
                             Log.d("FavoriteGridItem", "Fetched actual imageUrl: ${actualUrl?.take(50)}...")
                            // Update the state with the fetched URL
                             value = actualUrl?.takeIf { it.isNotEmpty() } // Set state to the fetched URL
                        } else {
                             Log.w("FavoriteGridItem", "Fetched music details body is null for ${song.musicId}")
                             value = null // Keep null if body is null
                        }
                    } else {
                        Log.e("FavoriteGridItem", "Failed to fetch music details for ${song.musicId}: ${response.code}")
                        value = "error" // Use a special value to indicate error
                    }
                } catch (e: Exception) {
                    Log.e("FavoriteGridItem", "Error fetching music details for ${song.musicId}", e)
                    value = "error" // Use a special value to indicate error
                }
            }
        }
        // If initial song.imageUrl was not null, produceState completes immediately with that value.
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { // Click logic remains the same
                 val intent = Intent(context, PlayActivity::class.java).apply {
                     putExtra("SONG_ID", song.musicId)
                     putExtra("SONG_TITLE", song.title)
                     putExtra("SONG_ARTIST", song.artist)
                     // Pass the potentially fetched URL if available, otherwise the original might be needed?
                     // For simplicity, let PlayActivity handle fetching its own image if needed.
                     // We only pass the basic info needed to identify the song.
                     // putExtra("SONG_IMAGE_URL", finalImageUrl ?: song.imageUrl) // Be careful passing fetched URL
                 }
                 context.startActivity(intent)
             }
            .background(
                 brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF3A3A3A), Color(0xFF1E1E1E))
                )
            )
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Image - Use finalImageUrl from produceState
         Box( // Wrap image in a Box to show loading/error states
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.DarkGray), // Background for loading/error states
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
                else -> { // Image URL is ready (either initial or fetched)
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(finalImageUrl) // Use the state value
                            .crossfade(true)
                            .build(),
                        contentDescription = song.title,
                        modifier = Modifier.fillMaxSize(), // Fill the Box
                        contentScale = ContentScale.Crop,
                        error = rememberVectorPainter(Icons.Filled.MusicNote)
                    )
                }
            }
        }

        // Title
        Text(
            text = song.title,
            color = Color.White,
            fontFamily = interFontFamily,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        // Artist
        Text(
            text = song.artist,
            color = Color.Gray,
            fontFamily = interLightFontFamily,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
} 