package edu.cit.astroglow

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.cit.astroglow.ui.theme.AstroglowTheme
import edu.cit.astroglow.R
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment
import edu.cit.astroglow.components.BottomNavBar
import edu.cit.astroglow.interFontFamily
import edu.cit.astroglow.interLightFontFamily
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import android.content.Context
import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import android.provider.MediaStore
import android.widget.Toast
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import coil.compose.rememberImagePainter
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.lint.kotlin.metadata.Visibility

class HomeActivity : ComponentActivity() {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        showSignOutDialog()
    }

    private fun showSignOutDialog() {
        setContent {
            AstroglowTheme {
                // Get user data from SharedPreferences
                val sharedPreferences = getSharedPreferences("auth", MODE_PRIVATE)
                val userName = sharedPreferences.getString("user_name", "") ?: ""
                var profileImage by remember { mutableStateOf<Uri?>(null) }
                var showDialog by remember { mutableStateOf(true) }

                // Show the HomeScreen
                HomeScreen(
                    userName = userName,
                    initialProfileImage = profileImage
                )

                // Show the dialog
                if (showDialog) {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = {
                            Text(
                                text = "Sign Out",
                                fontFamily = interFontFamily,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        text = {
                            Text(
                                text = "Are you sure you want to sign out?",
                                fontFamily = interFontFamily,
                                fontSize = 16.sp
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    // Clear shared preferences
                                    sharedPreferences.edit().clear().apply()
                                    
                                    // Redirect to login
                                    val intent = Intent(this@HomeActivity, LoginActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                    finish()
                                },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = Color(0xFF9C27B0)
                                )
                            ) {
                                Text(
                                    "Sign Out",
                                    fontFamily = interFontFamily,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { showDialog = false },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = Color.Gray
                                )
                            ) {
                                Text(
                                    "Cancel",
                                    fontFamily = interFontFamily
                                )
                            }
                        },
                        containerColor = Color(0xFF1E1E1E),
                        titleContentColor = Color.White,
                        textContentColor = Color.White
                    )
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check if user is already logged in
        val sharedPreferences = getSharedPreferences("auth", MODE_PRIVATE)
                val userId = sharedPreferences.getLong("user_id", -1)

                if (userId == -1L) {
            // Only redirect to login if there's no valid user ID
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
            return
        }
        
        setContent {
            AstroglowTheme {
                // Get user data from SharedPreferences
                val userName = sharedPreferences.getString("user_name", "") ?: ""
                val userEmail = sharedPreferences.getString("user_email", "") ?: ""

                // Fetch profile picture immediately
                var profileImage by remember { mutableStateOf<Uri?>(null) }
                
                LaunchedEffect(userId) {
                    if (userId != -1L) {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val url = "${Constants.BASE_URL}/api/user/profile-picture/$userId"
                                val request = Request.Builder()
                                    .url(url)
                                    .get()
                                    .build()
                                
                                val response = client.newCall(request).execute()
                                
                                if (response.isSuccessful) {
                                    val responseBody = response.body?.string()
                                    val jsonObject = JSONObject(responseBody)
                                    val status = jsonObject.getString("status")
                                    
                                    if (status == "success") {
                                        val profilePicture = jsonObject.optString("profilePicture", "")
                                        
                                        if (profilePicture.isNotEmpty()) {
                                            try {
                                                val imageBytes = Base64.decode(profilePicture, Base64.DEFAULT)
                                                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                                
                                                if (bitmap != null) {
                                                    val cachePath = File(cacheDir, "images")
                                                    cachePath.mkdirs()
                                                    val file = File(cachePath, "profile_$userId.jpg")
                                                    
                                                    if (file.exists()) {
                                                        file.delete()
                                                    }
                                                    
                                                    file.createNewFile()
                                                    val outputStream = FileOutputStream(file)
                                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                                                    outputStream.close()
                                                    
                                                    withContext(Dispatchers.Main) {
                                                        profileImage = Uri.fromFile(file)
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }

                HomeScreen(
                    userName = userName,
                    initialProfileImage = profileImage
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                // Convert image to base64
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val inputStream = contentResolver.openInputStream(uri)
                        val bytes = inputStream?.readBytes()
                        inputStream?.close()
                        
                        if (bytes != null) {
                            val base64Image = Base64.encodeToString(bytes, Base64.DEFAULT)
                            
                            // Get user ID from SharedPreferences
                            val sharedPreferences = getSharedPreferences("auth", MODE_PRIVATE)
                            val userId = sharedPreferences.getLong("user_id", -1)
                            
                            if (userId != -1L) {
                                // Update profile picture
                                val url = "${Constants.BASE_URL}/api/user/update-profile-picture/$userId"
                                
                                val requestBody = JSONObject().apply {
                                    put("profilePicture", base64Image)
                                }.toString()
                                
                                val request = Request.Builder()
                                    .url(url)
                                    .put(requestBody.toRequestBody("application/json".toMediaType()))
                                    .build()
                                
                                try {
                                    val response = client.newCall(request).execute()
                                    
                                    if (response.isSuccessful) {
                                        // Update UI on main thread
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(
                                                this@HomeActivity,
                                                "Profile picture updated successfully",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                } else {
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(
                                                this@HomeActivity,
                                                "Failed to update profile picture: ${response.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                } catch (e: SocketTimeoutException) {
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(
                                            this@HomeActivity,
                                            "Connection timeout. Please check if the backend server is running.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                } catch (e: IOException) {
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(
                                            this@HomeActivity,
                                            "Cannot connect to the server. Please ensure the backend is running.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@HomeActivity,
                                "Error updating profile picture: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(userName: String, initialProfileImage: Uri? = null) {
    var selectedTab by remember { mutableStateOf(0) }
    var showHomeTab by remember { mutableStateOf(true) }
    var showProfileTab by remember { mutableStateOf(false) }
    var profileImage by remember { mutableStateOf(initialProfileImage) }
    var currentUserName by remember { mutableStateOf(userName) }
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    val userId = sharedPreferences.getLong("user_id", -1)
    var isDarkMode by remember { 
        mutableStateOf(sharedPreferences.getBoolean("dark_mode", false)) 
    }

    // Fetch profile picture when component is created
    LaunchedEffect(userId) {
        if (userId != -1L) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val url = "${Constants.BASE_URL}/api/user/profile-picture/$userId"
                    android.util.Log.d("HomeScreen", "Fetching profile picture from: $url")
                    
                    val client = OkHttpClient.Builder()
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .build()
                    
                    val request = Request.Builder()
                        .url(url)
                        .get()
                        .build()
                    
                    val response = client.newCall(request).execute()
                    android.util.Log.d("HomeScreen", "Response code: ${response.code}")
                    
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        val jsonObject = JSONObject(responseBody)
                        val status = jsonObject.getString("status")
                        
                        if (status == "success") {
                            val profilePicture = jsonObject.optString("profilePicture", "")
                            
                            if (profilePicture.isNotEmpty()) {
                                try {
                                    val imageBytes = Base64.decode(profilePicture, Base64.DEFAULT)
                                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                    
                                    if (bitmap != null) {
                                        val cachePath = File(context.cacheDir, "images")
                                        cachePath.mkdirs()
                                        val file = File(cachePath, "profile_$userId.jpg")
                                        
                                        if (file.exists()) {
                                            file.delete()
                                        }
                                        
                                        file.createNewFile()
                                        val outputStream = FileOutputStream(file)
                                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                                        outputStream.close()
                                        
                                        withContext(Dispatchers.Main) {
                                            profileImage = Uri.fromFile(file)
                                        }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(
                                            context,
                                            "Error processing profile picture: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "Error loading profile picture: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
    
    // Animation for gradient colors
    val infiniteTransition = rememberInfiniteTransition()
    
    // Animate first color
    val firstColor by infiniteTransition.animateColor(
        initialValue = Color(0xFFE81EDE), // Pink
        targetValue = Color(0xFF9C27B0),  // Purple
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    // Animate second color
    val secondColor by infiniteTransition.animateColor(
        initialValue = Color(0xFF0050D0), // Blue
        targetValue = Color(0xFF2196F3),  // Light Blue
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { 
                            showHomeTab = true
                            showProfileTab = false
                            selectedTab = 0
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.mipmap.logo_foreground),
                            contentDescription = "AstroGlow Logo",
                            modifier = Modifier.size(47.dp),
                            tint = Color.Unspecified,
                        )
                        Text(
                            text = "AstroGlow",
                            color = Color.White,
                            fontFamily = interFontFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                },
                navigationIcon = { },
                actions = {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF6A1B9A))
                            .clickable { 
                        showHomeTab = false
                        showProfileTab = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (profileImage != null) {
                            Image(
                                painter = rememberImagePainter(profileImage),
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = currentUserName.take(1).uppercase(),
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = interFontFamily
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = if (isDarkMode) Color.Black else Color(0xFF1E1E1E)
                )
            )
        },
        bottomBar = {
            // Only show bottom nav if not on Profile screen
            if (!showProfileTab) {
                BottomNavBar(
                    selectedTab = selectedTab,
                    onTabSelected = {
                        selectedTab = it
                        showHomeTab = true
                    }
                )
            }
        }
    ) { paddingValues ->
        // The main content based on selected tab or special screens
        Box(
            modifier = if (isDarkMode) {
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.Black)
            } else {
                Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(firstColor, secondColor)
                    )
                )
            }
        ) {
            when {
                showProfileTab -> ProfileTab(
                    onProfileImageChanged = { newImage ->
                        profileImage = newImage
                    },
                    onUsernameChanged = { newUsername ->
                        currentUserName = newUsername
                    }
                )
                else -> {
                    when (selectedTab) {
                        0 -> HomeTabWithSearch(currentUserName)
                        1 -> FavoritesTab()
                        2 -> PlaylistTab()
                        3 -> SettingsTab(
                            isDarkMode = isDarkMode,
                            onDarkModeChange = { newDarkMode ->
                                isDarkMode = newDarkMode
                                // Save preference to SharedPreferences
                                sharedPreferences.edit().putBoolean("dark_mode", newDarkMode).apply()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileTab(
    onProfileImageChanged: (Uri?) -> Unit,
    onUsernameChanged: (String) -> Unit
) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    val userEmail = sharedPreferences.getString("user_email", "") ?: ""
    var userName by remember { mutableStateOf(sharedPreferences.getString("user_name", "") ?: "") }
    val userId = sharedPreferences.getLong("user_id", -1)
    
    var showImagePicker by remember { mutableStateOf(false) }
    var profileImage by remember { mutableStateOf<Uri?>(null) }
    var isEditingUsername by remember { mutableStateOf(false) }
    var isEditingEmail by remember { mutableStateOf(false) }
    
    // Fetch profile picture when component is created
    LaunchedEffect(userId) {
        if (userId != -1L) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val url = "${Constants.BASE_URL}/api/user/profile-picture/$userId"
                    android.util.Log.d("ProfileTab", "Fetching profile picture from: $url")
                    android.util.Log.d("ProfileTab", "User ID: $userId")
                    
                    val client = OkHttpClient.Builder()
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .build()
                    
                    val request = Request.Builder()
                        .url(url)
                        .get()
                        .build()
                    
                    val response = client.newCall(request).execute()
                    android.util.Log.d("ProfileTab", "Response code: ${response.code}")
                    android.util.Log.d("ProfileTab", "Response message: ${response.message}")
                    
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        android.util.Log.d("ProfileTab", "Response body: $responseBody")
                        
                        val jsonObject = JSONObject(responseBody)
                        val status = jsonObject.getString("status")
                        android.util.Log.d("ProfileTab", "Status: $status")
                        
                        if (status == "success") {
                            val profilePicture = jsonObject.optString("profilePicture", "")
                            android.util.Log.d("ProfileTab", "Profile picture length: ${profilePicture.length}")
                            android.util.Log.d("ProfileTab", "Profile picture first 50 chars: ${profilePicture.take(50)}")
                            
                            if (profilePicture.isNotEmpty()) {
                                try {
                                    // Convert base64 to bitmap
                                    val imageBytes = Base64.decode(profilePicture, Base64.DEFAULT)
                                    android.util.Log.d("ProfileTab", "Image bytes length: ${imageBytes.size}")
                                    
                                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                    android.util.Log.d("ProfileTab", "Bitmap created: ${bitmap != null}")
                                    
                                    if (bitmap != null) {
                                        // Save bitmap to cache and get URI
                                        val cachePath = File(context.cacheDir, "images")
                                        cachePath.mkdirs()
                                        val file = File(cachePath, "profile_$userId.jpg")
                                        
                                        // Delete existing file if it exists
                                        if (file.exists()) {
                                            file.delete()
                                        }
                                        
                                        file.createNewFile()
                                        val outputStream = FileOutputStream(file)
                                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                                        outputStream.close()
                                        
                                        withContext(Dispatchers.Main) {
                                            val newUri = Uri.fromFile(file)
                                            profileImage = newUri
                                            onProfileImageChanged(newUri)
                                            android.util.Log.d("ProfileTab", "Profile image URI set: $newUri")
                                            android.util.Log.d("ProfileTab", "File exists: ${file.exists()}")
                                            android.util.Log.d("ProfileTab", "File size: ${file.length()}")
                                        }
                                    } else {
                                        android.util.Log.e("ProfileTab", "Failed to decode bitmap from base64")
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(
                                                context,
                                                "Failed to decode profile picture",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("ProfileTab", "Error processing profile picture: ${e.message}")
                                    e.printStackTrace()
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(
                                            context,
                                            "Error processing profile picture: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            } else {
                                android.util.Log.d("ProfileTab", "No profile picture found in response")
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        context,
                                        "No profile picture found",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } else {
                            val message = jsonObject.optString("message", "Failed to load profile picture")
                            android.util.Log.e("ProfileTab", "Error status: $message")
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    context,
                                    message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else {
                        android.util.Log.e("ProfileTab", "Unsuccessful response: ${response.code}")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                "Failed to load profile picture: ${response.code}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("ProfileTab", "Error loading profile picture: ${e.message}")
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "Error loading profile picture: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    // Image Picker Dialog
    if (showImagePicker) {
        AlertDialog(
            onDismissRequest = { showImagePicker = false },
            title = { Text("Select Profile Picture", fontFamily = interFontFamily) },
            text = {
                Column {
                    Button(
                        onClick = {
                            // Launch image picker
                            val intent = Intent(Intent.ACTION_PICK).apply {
                                type = "image/*"
                            }
                            (context as ComponentActivity).startActivityForResult(intent, 1)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0))
                    ) {
                        Text("Choose from Gallery", fontFamily = interFontFamily)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showImagePicker = false }) {
                    Text("Cancel", fontFamily = interFontFamily)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Profile",
            fontFamily = interFontFamily,
            color = Color.White,
            fontSize = 46.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Profile Picture with edit functionality
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(60.dp))
                    .background(Color(0xFF6A1B9A))
                    .clickable { showImagePicker = true },
                contentAlignment = Alignment.Center
            ) {
                if (profileImage != null) {
                    Image(
                        painter = rememberImagePainter(profileImage),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(60.dp))
                            .background(Color(0xFF6A1B9A)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                Text(
                    text = userName.take(1).uppercase(),
                    color = Color.White,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = interFontFamily
                )
                }
                
                // Edit icon overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                        .padding(8.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile Picture",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Profile Information Section
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Email Account Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Email",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (isEditingEmail) {
                    OutlinedTextField(
                        value = userEmail,
                        onValueChange = { /* Email is not editable */ },
                        label = { Text("Email", fontFamily = interFontFamily) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF1E1E1E),
                            unfocusedContainerColor = Color(0xFF1E1E1E),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.White,
                            focusedPlaceholderColor = Color.Gray,
                            unfocusedPlaceholderColor = Color.Gray
                        ),
                        enabled = false
                    )
                } else {
                    Text(
                        text = userEmail,
                        fontFamily = interLightFontFamily,
                        color = Color.White,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Name Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isEditingUsername) {
                    OutlinedTextField(
                        value = userName,
                        onValueChange = { userName = it },
                        label = { Text("Username", fontFamily = interFontFamily) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF1E1E1E),
                            unfocusedContainerColor = Color(0xFF1E1E1E),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.White,
                            focusedPlaceholderColor = Color.Gray,
                            unfocusedPlaceholderColor = Color.Gray
                        ),
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    // Save username
                                    CoroutineScope(Dispatchers.IO).launch {
                                        try {
                                            val url = "${Constants.BASE_URL}/api/user/putUser?id=$userId"
                                            val client = OkHttpClient.Builder()
                                                .connectTimeout(30, TimeUnit.SECONDS)
                                                .readTimeout(30, TimeUnit.SECONDS)
                                                .writeTimeout(30, TimeUnit.SECONDS)
                                                .build()
                                            
                                            val requestBody = JSONObject().apply {
                                                put("userName", userName)
                                                put("userEmail", userEmail)
                                            }.toString()
                                            
                                            val request = Request.Builder()
                                                .url(url)
                                                .put(requestBody.toRequestBody("application/json".toMediaType()))
                                                .build()
                                            
                                            val response = client.newCall(request).execute()
                                            
                                            if (response.isSuccessful) {
                                                // Update local storage
                                                sharedPreferences.edit().putString("user_name", userName).apply()
                                                withContext(Dispatchers.Main) {
                                                    isEditingUsername = false
                                                    onUsernameChanged(userName)
                                                    Toast.makeText(
                                                        context,
                                                        "Username updated successfully",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(
                                                    context,
                                                    "Error updating username: ${e.message}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Save",
                                    tint = Color(0xFF9C27B0)
                                )
                            }
                        }
                    )
                } else {
                    Text(
                        text = userName,
                        fontFamily = interLightFontFamily,
                        color = Color.White,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { isEditingUsername = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Username",
                            tint = Color.White
                        )
                    }
                }
            }

            // Password Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                var showPasswordFields by remember { mutableStateOf(false) }
                var oldPassword by remember { mutableStateOf("") }
                var newPassword by remember { mutableStateOf("") }
                var confirmPassword by remember { mutableStateOf("") }
                var passwordError by remember { mutableStateOf<String?>(null) }
                var showOldPassword by remember { mutableStateOf(false) }
                var showNewPassword by remember { mutableStateOf(false) }
                var showConfirmPassword by remember { mutableStateOf(false) }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "************",
                        fontFamily = interLightFontFamily,
                        color = Color.White,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { showPasswordFields = !showPasswordFields }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Change Password",
                            tint = Color.White
                        )
                    }
                }

                if (showPasswordFields) {
                    OutlinedTextField(
                        value = oldPassword,
                        onValueChange = { 
                            oldPassword = it
                            passwordError = null
                        },
                        label = { Text("Current Password", fontFamily = interFontFamily) },
                        placeholder = { Text("Enter your current password", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF1E1E1E),
                            unfocusedContainerColor = Color(0xFF1E1E1E),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.White,
                            focusedPlaceholderColor = Color.Gray,
                            unfocusedPlaceholderColor = Color.Gray
                        ),
                        visualTransformation = if (showOldPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        trailingIcon = {
                            IconButton(
                                onClick = { showOldPassword = !showOldPassword }
                            ) {
                                Icon(
                                    imageVector = if (showOldPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (showOldPassword) "Hide password" else "Show password",
                                    tint = Color.White
                                )
                            }
                        },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { 
                            newPassword = it
                            passwordError = null
                        },
                        label = { Text("New Password", fontFamily = interFontFamily) },
                        placeholder = { Text("Enter your new password", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF1E1E1E),
                            unfocusedContainerColor = Color(0xFF1E1E1E),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.White,
                            focusedPlaceholderColor = Color.Gray,
                            unfocusedPlaceholderColor = Color.Gray
                        ),
                        visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        trailingIcon = {
                            IconButton(
                                onClick = { showNewPassword = !showNewPassword }
                            ) {
                                Icon(
                                    imageVector = if (showNewPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (showNewPassword) "Hide password" else "Show password",
                                    tint = Color.White
                                )
                            }
                        },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { 
                            confirmPassword = it
                            passwordError = null
                        },
                        label = { Text("Confirm Password", fontFamily = interFontFamily) },
                        placeholder = { Text("Confirm your new password", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF1E1E1E),
                            unfocusedContainerColor = Color(0xFF1E1E1E),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.White,
                            focusedPlaceholderColor = Color.Gray,
                            unfocusedPlaceholderColor = Color.Gray
                        ),
                        visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        trailingIcon = {
                            IconButton(
                                onClick = { showConfirmPassword = !showConfirmPassword }
                            ) {
                                Icon(
                                    imageVector = if (showConfirmPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (showConfirmPassword) "Hide password" else "Show password",
                                    tint = Color.White
                                )
                            }
                        },
                        singleLine = true
                    )

                    if (passwordError != null) {
                        Text(
                            text = passwordError!!,
                            color = Color.Red,
                            fontFamily = interFontFamily,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Button(
                        onClick = {
                            if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                                passwordError = "All fields are required"
                                return@Button
                            }
                            if (newPassword != confirmPassword) {
                                passwordError = "New passwords do not match"
                                return@Button
                            }
                            if (newPassword.length < 6) {
                                passwordError = "Password must be at least 6 characters"
                                return@Button
                            }

                            // Call the change password API
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val url = "${Constants.BASE_URL}/api/user/changePassword/$userId"
                                    val client = OkHttpClient.Builder()
                                        .connectTimeout(30, TimeUnit.SECONDS)
                                        .readTimeout(30, TimeUnit.SECONDS)
                                        .writeTimeout(30, TimeUnit.SECONDS)
                                        .build()
                                    
                                    val requestBody = JSONObject().apply {
                                        put("currentPassword", oldPassword)
                                        put("newPassword", newPassword)
                                    }.toString()
                                    
                                    val request = Request.Builder()
                                        .url(url)
                                        .post(requestBody.toRequestBody("application/json".toMediaType()))
                                        .build()
                                    
                                    val response = client.newCall(request).execute()
                                    val responseBody = response.body?.string()
                                    val jsonResponse = JSONObject(responseBody ?: "{}")
                                    
                                    withContext(Dispatchers.Main) {
                                        if (response.isSuccessful) {
                                            // Clear fields and show success message
                                            oldPassword = ""
                                            newPassword = ""
                                            confirmPassword = ""
                                            showPasswordFields = false
                                            Toast.makeText(
                                                context,
                                                jsonResponse.optString("message", "Password changed successfully"),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            passwordError = jsonResponse.optString("message", "Failed to change password")
                                        }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    withContext(Dispatchers.Main) {
                                        passwordError = "Error: ${e.message}"
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF9C27B0)
                        )
                    ) {
                        Text(
                            text = "Change Password",
                            fontFamily = interFontFamily,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Sign Out Button
        Button(
            onClick = { 
                context.getSharedPreferences("auth", Context.MODE_PRIVATE)
                    .edit()
                    .clear()
                    .apply()
                
                val intent = Intent(context, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF9C27B0)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "Sign Out",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontFamily = interFontFamily
            )
        }
    }
}

@Composable
fun ProfileDetailItem(
    icon: ImageVector?,
    label: String,
    value: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Label Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = label,
                fontFamily = interLightFontFamily,
                color = Color.White,
                fontSize = 16.sp
            )
        }

        // Value Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF6A1B9A),
                            Color(0xFF283593)
                        )
                    )
                ),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = value,
                fontFamily = interLightFontFamily,
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTabWithSearch(userName: String) {
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Welcome Section with adjusted sizes
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Welcome,",
                fontFamily = interFontFamily,
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Normal
            )
            
            Text(
                text = userName,
                fontFamily = interFontFamily,
                color = Color.White,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = "Listen to our music for free and browse through our library to see our latest creation.",
                fontFamily = interLightFontFamily,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 16.sp,
                lineHeight = 24.sp,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        // Search bar with adjusted spacing
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { 
                Text(
                    "Enter a song",
                    color = Color.Gray,
                    fontFamily = interLightFontFamily
                )
            },
            leadingIcon = { 
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.Gray
                )
            },
            trailingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_forward),
                    contentDescription = "Search",
                    tint = Color.Gray,
                    modifier = Modifier.clickable {
                        val intent = Intent(context, SearchActivity::class.java).apply {
                            putExtra("search_query", searchQuery)
                        }
                        context.startActivity(intent)
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
                .height(56.dp)
                .clickable {
                    val intent = Intent(context, SearchActivity::class.java).apply {
                        putExtra("search_query", searchQuery)
                    }
                    context.startActivity(intent)
                },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = Color.DarkGray,
                unfocusedTextColor = Color.DarkGray
            ),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontFamily = interFontFamily,
                fontSize = 16.sp
            ),
            singleLine = true
        )

        // Sample song list
        SongGrid()

        Spacer(modifier = Modifier.height(24.dp))

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
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(3) { index ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                    Box(
                        modifier = Modifier
                            .width(160.dp)
                            .height(140.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                        )
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
                            Text(
                                text = "Song ${index + 1}",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontFamily = interFontFamily,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.width(160.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Favorites section with title and different arrow - positioned RIGHT
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Title and star icon for favorites
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Favorites",
                    fontFamily = interFontFamily,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                // Changed to favorite/star icon
                Icon(
                    imageVector = Icons.Outlined.Favorite,
                    contentDescription = "Favorites",
                    tint = Color.White,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(24.dp)
                )
            }
        }

        // Favorites carousel with gradient background matching app theme (reversed gradient)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .padding(vertical = 8.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF0050D0).copy(alpha = 0.85f),  // Blue
                            Color(0xFFE81EDE).copy(alpha = 0.85f)   // Pink
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(3) { index ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(160.dp)
                                .height(140.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                        )
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
                    Text(
                                text = "Favorite ${index + 1}",
                                color = Color.White,
                        fontSize = 14.sp,
                                fontFamily = interFontFamily,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.width(160.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SongGrid() {
    Column {
        // Row 1
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            SongItem(title = "Song 1", modifier = Modifier.weight(1f))
            SongItem(title = "Song 2", modifier = Modifier.weight(1f))
        }

        // Row 2
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            SongItem(title = "Song 3", modifier = Modifier.weight(1f))
            SongItem(title = "Song 4", modifier = Modifier.weight(1f))
        }

        // Row 3
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            SongItem(title = "Song 5", modifier = Modifier.weight(1f))
            SongItem(title = "Song 6", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun SongItem(title: String, modifier: Modifier = Modifier) {
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
                .background(Color.LightGray)
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
                text = title,
                color = Color.White,
                fontFamily = interLightFontFamily,
                modifier = Modifier.padding(start = 12.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun FavoritesTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
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

        Icon(
            imageVector = Icons.Outlined.Favorite,
            contentDescription = "Favorites",
            tint = Color.White,
            modifier = Modifier.size(80.dp)
        )

        Text(
            text = "Your favorite songs will appear here",
            fontFamily = interFontFamily,
            color = Color.White,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
fun PlaylistTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Playlists",
            fontFamily = interFontFamily,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { /* Create playlist */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0050D0)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Create Playlist")
            }

            Button(
                onClick = { /* Import playlist */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0050D0)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Import")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Your playlists will appear here",
            fontFamily = interFontFamily,
            color = Color.White,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SettingsTab(
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Large "Settings" title
        Text(
            text = "Settings",
            fontFamily = interFontFamily,
            color = Color.White,
            fontSize = 46.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Dark Mode Settings Item
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF8E24AA), Color(622790))
                    )
                )
                .clickable { onDarkModeChange(!isDarkMode) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side: Icon Box with gradient
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(100.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF42A5F5), Color(0xFF8E24AA))
                        ),
                        shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isDarkMode) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = "Dark Mode",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
            
            // Right side: Text Box with gradient
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF8E24AA), Color(0xFF42A5F5))
                        ),
                        shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
                    )
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Column {
                    Text(
                        text = "DARK MODE",
                        color = Color.White,
                        fontFamily = interFontFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = if (isDarkMode) "Dark mode is enabled" else "Dark mode is disabled",
                        color = Color.White.copy(alpha = 0.8f),
                        fontFamily = interLightFontFamily,
                        fontSize = 14.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Settings Items
        SettingsListItem(
            icon = Icons.Outlined.Fingerprint,
            title = "BIOMETRICS",
            description = "Enable fingerprint for account security and protect your library for a secure experience."
        )
        Spacer(modifier = Modifier.height(16.dp))
        SettingsListItem(
            icon = Icons.Outlined.Face,
            title = "FACE ID",
            description = "Register face id to unlock application faster and provide secure access to all your music."
        )
        Spacer(modifier = Modifier.height(16.dp))
        SettingsListItem(
            icon = Icons.Outlined.Info,
            title = "Our Team",
            description = "Get to know the team of developers who developed AstroGlow.",
            onClick = { 
                val intent = Intent(context, OurTeamActivity::class.java)
                context.startActivity(intent)
            }
        )
    }
}

/**
 * Composable for individual settings items, matching the design image.
 */
@Composable
fun SettingsListItem(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: (() -> Unit)? = null // Make onClick optional
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF8E24AA), Color(622790)) // Gradient colors
                )
            )
            .clickable { onClick?.invoke() }, // Make pressable
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left side: Icon Box with gradient
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(100.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF42A5F5), Color(0xFF8E24AA))
                    ),
                    shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White, // White icon tint
                modifier = Modifier.size(40.dp)
            )
        }
        
        // Right side: Text Box with gradient
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF8E24AA), Color(0xFF42A5F5)) // Gradient colors
                    ),
                    shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
                )
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontFamily = interFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = description,
                    color = Color.White.copy(alpha = 0.8f),
                    fontFamily = interLightFontFamily, // Use light font for description
                    fontSize = 14.sp,
                    lineHeight = 18.sp // Adjust line height for better readability
                )
            }
        }
    }
}

