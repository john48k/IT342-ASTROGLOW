package edu.cit.astroglow.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import edu.cit.astroglow.Constants
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
import androidx.compose.foundation.clickable
import android.content.Intent
import edu.cit.astroglow.PlayActivity

data class Song(
    val id: Int,
    val title: String,
    val artist: String,
    val imageUrl: String? = null
)

@Composable
fun SongGrid() {
    var songs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    
    // Fetch songs when the component is created
    LaunchedEffect(Unit) {
        // Use a coroutine scope to handle the network request
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
                        
                        val songList = mutableListOf<Song>()
                        for (i in 0 until minOf(jsonArray.length(), 6)) {
                            val songObject = jsonArray.getJSONObject(i)
                            val song = Song(
                                id = songObject.getInt("musicId"),
                                title = songObject.getString("title"),
                                artist = songObject.getString("artist"),
                                imageUrl = songObject.optString("imageUrl", null).takeIf { it.isNotEmpty() }
                            )
                            songList.add(song)
                        }
                        
                        songList
                    } else {
                        Log.e("SongGrid", "Error loading songs: ${response.code}")
                        null
                    }
                }
                
                // Update UI state based on the result
                if (fetchedSongs != null) {
                    songs = fetchedSongs
                    isLoading = false
                } else {
                    error = "Failed to load songs"
                    isLoading = false
                }
            } catch (e: Exception) {
                error = "Error: ${e.message}"
                isLoading = false
                Log.e("SongGrid", "Exception loading songs", e)
            }
        }
    }
    
    Column {
        if (isLoading) {
            // Show loading state
            repeat(3) { rowIndex ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    SongItemPlaceholder(modifier = Modifier.weight(1f))
                    SongItemPlaceholder(modifier = Modifier.weight(1f))
                }
            }
        } else if (error != null) {
            // Show error state
            Text(
                text = error ?: "Unknown error",
                color = Color.Red,
                modifier = Modifier.padding(16.dp)
            )
        } else if (songs.isEmpty()) {
            // Show empty state
            Text(
                text = "No songs available",
                color = Color.White,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            // Display songs in rows of 2
            for (i in songs.indices step 2) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    // First song in the row
                    SongItem(
                        song = songs[i],
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Second song in the row (if available)
                    if (i + 1 < songs.size) {
                        SongItem(
                            song = songs[i + 1],
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        // Empty space to maintain layout
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun SongItemPlaceholder(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .height(48.dp)
            .padding(end = 8.dp)
    ) {
        // Gray thumbnail
        Box(
            modifier = Modifier
                .width(48.dp)
                .height(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.DarkGray)
        )

        // Song info with black background
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .clip(RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
                .background(Color.Black),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "Loading...",
                color = Color.Gray,
                fontFamily = interLightFontFamily,
                modifier = Modifier.padding(start = 12.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SongItem(song: Song, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Row(
        modifier = modifier
            .height(48.dp)
            .padding(end = 8.dp)
            .clickable {
                val intent = Intent(context, PlayActivity::class.java).apply {
                    putExtra("SONG_ID", song.id)
                    putExtra("SONG_TITLE", song.title)
                    putExtra("SONG_ARTIST", song.artist)
                    putExtra("SONG_IMAGE_URL", song.imageUrl)
                }
                context.startActivity(intent)
            }
    ) {
        // Song thumbnail
        Box(
            modifier = Modifier
                .width(48.dp)
                .height(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.DarkGray)
        ) {
            if (song.imageUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(song.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Song thumbnail",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Song info with black background
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .clip(RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
                .background(Color.Black),
            contentAlignment = Alignment.CenterStart
        ) {
            Column(
                modifier = Modifier.padding(start = 12.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = song.title,
                    color = Color.White,
                    fontFamily = interLightFontFamily,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist,
                    color = Color.Gray,
                    fontFamily = interLightFontFamily,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}