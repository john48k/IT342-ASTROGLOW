package edu.cit.astroglow

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import edu.cit.astroglow.ui.theme.AstroglowTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class Song(
    val id: Int,
    val title: String,
    val artist: String,
    val imageUrl: String?
)

class SearchResultsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val searchType = intent.getStringExtra("SEARCH_TYPE") ?: "title"
        val searchQuery = intent.getStringExtra("SEARCH_QUERY") ?: ""
        
        setContent {
            AstroglowTheme {
                SearchResultsScreen(
                    searchType = searchType,
                    searchQuery = searchQuery,
                    onBack = { finish() }
                )
            }
        }
    }
}

@Composable
fun SearchResultsScreen(
    searchType: String,
    searchQuery: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    val userId = sharedPreferences.getLong("user_id", -1)
    val isDarkMode = sharedPreferences.getBoolean("dark_mode", false)
    
    // Create OkHttpClient
    val client = remember {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    var searchResults by remember { mutableStateOf<List<Song>>(emptyList()) }
    var filteredResults by remember { mutableStateOf<List<Song>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Genre filter state
    var selectedGenre by remember { mutableStateOf<String?>(null) }
    var showGenreDropdown by remember { mutableStateOf(false) }
    
    // List of available genres
    val genres = remember {
        listOf(
            "All Genres",
                "None", "Pop", "Rock", "OPM", "Hip Hop", "R&B", "Electronic",
                "Jazz", "Classical", "Country", "Folk", "Metal",
                "Blues", "Reggae", "Latin", "World", "Other"
        )
    }
    
    // Load search results
    LaunchedEffect(searchType, searchQuery) {
        isLoading = true
        errorMessage = null
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val endpoint = when (searchType) {
                    "title" -> "${Constants.BASE_URL}/api/music/search/title?title=$searchQuery"
                    "artist" -> "${Constants.BASE_URL}/api/music/search/artist?artist=$searchQuery"
                    "genre" -> "${Constants.BASE_URL}/api/music/search/genre?genre=$searchQuery"
                    else -> "${Constants.BASE_URL}/api/music/search/title?title=$searchQuery"
                }
                
                val request = Request.Builder()
                    .url(endpoint)
                    .get()
                    .build()
                
                val response = client.newCall(request).execute()
                
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    if (responseBody != null) {
                        val jsonArray = JSONArray(responseBody)
                        val results = mutableListOf<Song>()
                        
                        for (i in 0 until jsonArray.length()) {
                            val songObject = jsonArray.getJSONObject(i)
                            val song = Song(
                                id = songObject.getInt("musicId"),
                                title = songObject.getString("title"),
                                artist = songObject.getString("artist"),
                                imageUrl = songObject.optString("imageUrl", null)
                            )
                            results.add(song)
                        }
                        
                        withContext(Dispatchers.Main) {
                            searchResults = results
                            filteredResults = results
                            isLoading = false
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            errorMessage = "No results found"
                            isLoading = false
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        errorMessage = "Failed to load search results: ${response.code}"
                        isLoading = false
                    }
                }
            } catch (e: Exception) {
                Log.e("SearchResultsScreen", "Error loading search results", e)
                withContext(Dispatchers.Main) {
                    errorMessage = "Error: ${e.message}"
                    isLoading = false
                }
            }
        }
    }
    
    // Apply genre filter when selectedGenre changes
    LaunchedEffect(selectedGenre) {
        if (selectedGenre != null) {
            isLoading = true
            errorMessage = null
            
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Make a new API call with the selected genre
                    val endpoint = "${Constants.BASE_URL}/api/music/search/genre?genre=$selectedGenre"
                    
                    val request = Request.Builder()
                        .url(endpoint)
                        .get()
                        .build()
                    
                    val response = client.newCall(request).execute()
                    
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        if (responseBody != null) {
                            val jsonArray = JSONArray(responseBody)
                            val results = mutableListOf<Song>()
                            
                            for (i in 0 until jsonArray.length()) {
                                val songObject = jsonArray.getJSONObject(i)
                                val song = Song(
                                    id = songObject.getInt("musicId"),
                                    title = songObject.getString("title"),
                                    artist = songObject.getString("artist"),
                                    imageUrl = songObject.optString("imageUrl", null)
                                )
                                results.add(song)
                            }
                            
                            withContext(Dispatchers.Main) {
                                filteredResults = results
                                isLoading = false
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                errorMessage = "No results found for genre: $selectedGenre"
                                isLoading = false
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            errorMessage = "Failed to load genre results: ${response.code}"
                            isLoading = false
                        }
                    }
                } catch (e: Exception) {
                    Log.e("SearchResultsScreen", "Error loading genre results", e)
                    withContext(Dispatchers.Main) {
                        errorMessage = "Error: ${e.message}"
                        isLoading = false
                    }
                }
            }
        } else {
            // If no genre is selected, show all search results
            filteredResults = searchResults
        }
    }
    
    val backgroundBrush = if (isDarkMode) {
        Brush.verticalGradient(
            colors = listOf(Color.Black, Color(0xFF1A1A1A))
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(Color(0xFF6A1B9A), Color(0xFF283593))
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .padding(horizontal = 16.dp)
            .padding(top = 64.dp, bottom = 16.dp)
    ) {
        // Top Bar with modern styling
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.1f))
            ) {
                Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
            }
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = "Search Results",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = interFontFamily
                )
                
                Text(
                    text = "\"$searchQuery\"",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 16.sp,
                    fontFamily = interLightFontFamily,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        
        // Genre filter
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            OutlinedButton(
                onClick = { showGenreDropdown = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedGenre ?: "Filter by Genre",
                        fontFamily = interFontFamily,
                        fontSize = 16.sp
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Select Genre",
                        modifier = Modifier.graphicsLayer(rotationZ = 90f)
                    )
                }
            }
            
            DropdownMenu(
                expanded = showGenreDropdown,
                onDismissRequest = { showGenreDropdown = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E1E1E))
            ) {
                genres.forEach { genre ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = genre,
                                fontFamily = interFontFamily,
                                color = Color.White
                            )
                        },
                        onClick = {
                            selectedGenre = if (genre == "All Genres") null else genre
                            showGenreDropdown = false
                        },
                        leadingIcon = {
                            if (selectedGenre == genre || (genre == "All Genres" && selectedGenre == null)) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color(0xFF9C27B0)
                                )
                            }
                        }
                    )
                }
            }
        }
        
        // Loading indicator or error message
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
        } else if (errorMessage != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Error",
                        tint = Color.Red,
                        modifier = Modifier.size(48.dp)
                    )
                    
                    Text(
                        text = errorMessage!!,
                        color = Color.Red,
                        textAlign = TextAlign.Center,
                        fontFamily = interFontFamily
                    )
                }
            }
        } else if (filteredResults.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = "No Results",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(64.dp)
                    )
                    
                    Text(
                        text = "No results found for \"$searchQuery\"",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        fontFamily = interFontFamily,
                        fontSize = 18.sp
                    )
                    
                    Text(
                        text = "Try a different search term",
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        fontFamily = interLightFontFamily,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            // Search results list with modern styling
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredResults) { song ->
                    SongItem(
                        song = song,
                        onClick = {
                            // Launch PlayActivity with song details
                            val intent = Intent(context, PlayActivity::class.java).apply {
                                putExtra("SONG_ID", song.id)
                                putExtra("SONG_TITLE", song.title)
                                putExtra("SONG_ARTIST", song.artist)
                                putExtra("SONG_IMAGE_URL", song.imageUrl)
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SongItem(
    song: Song,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    val isDarkMode = sharedPreferences.getBoolean("dark_mode", false)
    val userId = sharedPreferences.getLong("user_id", -1)
    
    var isAddingToPlaylist by remember { mutableStateOf(false) }
    var isInQueue by remember { mutableStateOf(false) }
    var isLoadingQueue by remember { mutableStateOf(true) }
    
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
                    Log.d("SongItem", "isInQueue check response code: ${response.code}")
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        val inQueue = responseBody?.toBooleanStrictOrNull() ?: false
                        Log.d("SongItem", "isInQueue check result: $inQueue")
                        withContext(Dispatchers.Main) { isInQueue = inQueue }
                    } else {
                        Log.w("SongItem", "Failed to check queue status (assuming false): ${response.code}")
                        withContext(Dispatchers.Main) { isInQueue = false }
                    }
                } catch (e: Exception) {
                    Log.e("SongItem", "Error checking queue status", e)
                    withContext(Dispatchers.Main) { isInQueue = false }
                } finally {
                    withContext(Dispatchers.Main) { isLoadingQueue = false }
                }
            }
        } else {
            Log.w("SongItem", "Skipping queue check due to invalid IDs (User: $userId, Song: ${song.id})")
            isLoadingQueue = false
        }
    }
    
    // Add an additional check when the SongItem is first created
    LaunchedEffect(Unit) {
        // This will run once when the composable is first created
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
                    Log.d("SongItem", "Initial isInQueue check response code: ${response.code}")
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        val inQueue = responseBody?.toBooleanStrictOrNull() ?: false
                        Log.d("SongItem", "Initial isInQueue check result: $inQueue")
                        withContext(Dispatchers.Main) { isInQueue = inQueue }
                    }
                } catch (e: Exception) {
                    Log.e("SongItem", "Error in initial queue check", e)
                }
            }
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
                        Log.e("SongItem", "Error in periodic queue check", e)
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
    
    val itemBackgroundColor = if (isDarkMode) {
        Color(0xFF2A2A2A)
    } else {
        Color.White.copy(alpha = 0.1f)
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(itemBackgroundColor)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Song artwork
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.DarkGray.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            if (song.imageUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(song.imageUrl)
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
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Song details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = interFontFamily,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = song.artist,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                fontFamily = interLightFontFamily,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        // Add to queue button
        IconButton(
            onClick = { toggleQueue() },
            enabled = !isAddingToPlaylist && !isLoadingQueue
        ) {
            if (isAddingToPlaylist) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.QueueMusic,
                    contentDescription = if (isInQueue) "Remove from Queue" else "Add to Queue",
                    tint = if (isLoadingQueue) Color.Gray else if (isInQueue) Color(0xFFE91E63) else Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
} 