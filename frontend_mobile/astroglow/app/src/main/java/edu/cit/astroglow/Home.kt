package edu.cit.astroglow

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
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
import coil.request.ImageRequest
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import edu.cit.astroglow.components.UploadTab
import edu.cit.astroglow.components.SongGrid
import edu.cit.astroglow.components.RecommendationsSection
import edu.cit.astroglow.components.FavoritesSection
import edu.cit.astroglow.components.FavoritesTab
import edu.cit.astroglow.components.PlaylistTab

class HomeActivity : FragmentActivity() {
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
        val userEmail = sharedPreferences.getString("user_email", "") ?: ""
        val userName = sharedPreferences.getString("user_name", "") ?: ""
        val isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false)
        
        Log.d("HomeActivity", "Checking user data - ID: $userId, Email: $userEmail, Name: $userName, IsLoggedIn: $isLoggedIn")

        if (!isLoggedIn || userId < 0L || userEmail.isEmpty() || userName.isEmpty()) {
            Log.d("HomeActivity", "Missing user data or not logged in, redirecting to LoginActivity")
            // Only redirect to login if there's no valid user data
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }
        
        Log.d("HomeActivity", "User data valid, proceeding to HomeScreen")
        
        setContent {
            AstroglowTheme {
                // Get user data from SharedPreferences
                val userName = sharedPreferences.getString("user_name", "") ?: ""
                val userEmail = sharedPreferences.getString("user_email", "") ?: ""
                val profilePictureUrl = sharedPreferences.getString("profile_picture_url", null)
                
                Log.d("HomeActivity", "Profile picture URL from SharedPreferences: $profilePictureUrl")

                // Fetch profile picture immediately
                var profileImage by remember { mutableStateOf<Uri?>(null) }
                
                LaunchedEffect(userId) {
                    if (userId != -1L) {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                // First try to get profile picture from server (database)
                                Log.d("HomeActivity", "Fetching profile picture from server")
                                val url = "${Constants.BASE_URL}/api/user/profile-picture/$userId"
                                val request = Request.Builder()
                                    .url(url)
                                    .get()
                                    .build()
                                
                                val response = client.newCall(request).execute()
                                Log.d("HomeActivity", "Server profile picture response code: ${response.code}")
                                
                                if (response.isSuccessful) {
                                    val responseBody = response.body?.string()
                                    val jsonObject = JSONObject(responseBody)
                                    val status = jsonObject.getString("status")
                                    Log.d("HomeActivity", "Server profile picture status: $status")
                                    
                                    if (status == "success") {
                                        val profilePicture = jsonObject.optString("profilePicture", "")
                                        Log.d("HomeActivity", "Server profile picture length: ${profilePicture.length}")
                                        
                                        if (profilePicture.isNotEmpty()) {
                                            try {
                                                val imageBytes = Base64.decode(profilePicture, Base64.DEFAULT)
                                                Log.d("HomeActivity", "Server profile picture bytes size: ${imageBytes.size}")
                                                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                                Log.d("HomeActivity", "Server profile picture bitmap created: ${bitmap != null}")
                                                
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
                                                    
                                                    val uri = Uri.fromFile(file)
                                                    Log.d("HomeActivity", "Server profile picture saved to: $uri")
                                                    
                                                    withContext(Dispatchers.Main) {
                                                        profileImage = uri
                                                        Log.d("HomeActivity", "Profile image URI set from server: $uri")
                                                    }
                                                    return@launch
                                                }
                                            } catch (e: Exception) {
                                                Log.e("HomeActivity", "Error processing server profile picture", e)
                                                e.printStackTrace()
                                            }
                                        }
                                    }
                                }
                                
                                // If server profile picture fails or is not available, try Google profile picture
                                if (profilePictureUrl != null) {
                                    Log.d("HomeActivity", "Falling back to Google profile picture from URL: $profilePictureUrl")
                                    try {
                                        val client = OkHttpClient.Builder()
                                            .connectTimeout(30, TimeUnit.SECONDS)
                                            .readTimeout(30, TimeUnit.SECONDS)
                                            .writeTimeout(30, TimeUnit.SECONDS)
                                            .build()
                                        
                                        val request = Request.Builder()
                                            .url(profilePictureUrl)
                                            .get()
                                            .build()
                                        
                                        val response = client.newCall(request).execute()
                                        Log.d("HomeActivity", "Google profile picture response code: ${response.code}")
                                        
                                        if (response.isSuccessful) {
                                            val responseBody = response.body
                                            if (responseBody != null) {
                                                val imageBytes = responseBody.bytes()
                                                Log.d("HomeActivity", "Google profile picture bytes size: ${imageBytes.size}")
                                                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                                Log.d("HomeActivity", "Google profile picture bitmap created: ${bitmap != null}")
                                                
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
                                                    
                                                    val uri = Uri.fromFile(file)
                                                    Log.d("HomeActivity", "Google profile picture saved to: $uri")
                                                    
                                                    withContext(Dispatchers.Main) {
                                                        profileImage = uri
                                                        Log.d("HomeActivity", "Profile image URI set from Google: $uri")
                                                    }
                                                }
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Log.e("HomeActivity", "Error loading Google profile picture", e)
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("HomeActivity", "Error in profile picture loading process", e)
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
                // Convert image to base64 with compression
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        // Load the image from URI
                        val inputStream = contentResolver.openInputStream(uri)
                        val originalBitmap = BitmapFactory.decodeStream(inputStream)
                        inputStream?.close()
                        
                        if (originalBitmap != null) {
                            // Calculate new dimensions (max 800px on longest side)
                            val maxDimension = 800
                            val width = originalBitmap.width
                            val height = originalBitmap.height
                            
                            val scaleFactor = if (width > height) {
                                maxDimension.toFloat() / width
                            } else {
                                maxDimension.toFloat() / height
                            }
                            
                            val newWidth = (width * scaleFactor).toInt()
                            val newHeight = (height * scaleFactor).toInt()
                            
                            // Resize the bitmap
                            val resizedBitmap = Bitmap.createScaledBitmap(
                                originalBitmap, 
                                newWidth, 
                                newHeight, 
                                true
                            )
                            
                            // Compress to JPEG with lower quality
                            val outputStream = ByteArrayOutputStream()
                            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
                            
                            // Convert to base64
                            val base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
                            
                            // Clean up bitmaps
                            originalBitmap.recycle()
                            resizedBitmap.recycle()
                            
                            // Get user ID from SharedPreferences
                            val sharedPreferences = getSharedPreferences("auth", MODE_PRIVATE)
                            val userId = sharedPreferences.getLong("user_id", -1)
                            
                            // Validate user ID
                            if (userId <= 0) {
                                Log.e("HomeActivity", "Invalid user ID: $userId")
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        this@HomeActivity,
                                        "Error: Invalid user ID. Please log out and log in again.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                return@launch
                            }
                            
                            Log.d("HomeActivity", "Updating profile picture for user ID: $userId")
                            Log.d("HomeActivity", "Compressed image size: ${outputStream.size()} bytes")
                            
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
                                    // Save the compressed image to cache for immediate display
                                    val cachePath = File(cacheDir, "images")
                                    cachePath.mkdirs()
                                    val file = File(cachePath, "profile_$userId.jpg")
                                    
                                    if (file.exists()) {
                                        file.delete()
                                    }
                                    
                                    file.createNewFile()
                                    val fileOutputStream = FileOutputStream(file)
                                    fileOutputStream.write(outputStream.toByteArray())
                                    fileOutputStream.close()
                                    
                                    val newProfileUri = Uri.fromFile(file)
                                    
                                    // Update UI on main thread
                                    withContext(Dispatchers.Main) {
                                        // Update the profile image in the UI
                                        setContent {
                                            AstroglowTheme {
                                                // Get user data from SharedPreferences
                                                val userName = sharedPreferences.getString("user_name", "") ?: ""
                                                
                                                // Show the HomeScreen with the updated profile image
                                                HomeScreen(
                                                    userName = userName,
                                                    initialProfileImage = newProfileUri,
                                                    onProfileImageChanged = { uri ->
                                                        // This will be called by the ProfileTab to update its image
                                                        Log.d("HomeActivity", "Profile image updated in ProfileTab: $uri")
                                                    }
                                                )
                                            }
                                        }
                                        
                                        Toast.makeText(
                                            this@HomeActivity,
                                            "Profile picture updated successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } else {
                                    val errorBody = response.body?.string()
                                    Log.e("HomeActivity", "Failed to update profile picture. Status: ${response.code}, Error: $errorBody")
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(
                                            this@HomeActivity,
                                            "Failed to update profile picture: ${response.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            } catch (e: SocketTimeoutException) {
                                Log.e("HomeActivity", "Socket timeout when updating profile picture", e)
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        this@HomeActivity,
                                        "Connection timeout. Please check if the backend server is running.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            } catch (e: IOException) {
                                Log.e("HomeActivity", "IO exception when updating profile picture", e)
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        this@HomeActivity,
                                        "Cannot connect to the server. Please ensure the backend is running.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("HomeActivity", "Error updating profile picture", e)
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

    private fun showFingerprintLogin() {
        val biometricManager = BiometricManager.from(this)
        val canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)

        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Login with Fingerprint")
                .setSubtitle("Use your fingerprint to login")
                .setNegativeButtonText("Cancel")
                .build()

            val biometricPrompt = BiometricPrompt(
                this,
                ContextCompat.getMainExecutor(this),
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        // Get user data from SharedPreferences
                        val sharedPreferences = getSharedPreferences("auth", MODE_PRIVATE)
                        val userId = sharedPreferences.getLong("user_id", -1)
                        val userEmail = sharedPreferences.getString("user_email", "") ?: ""
                        val userName = sharedPreferences.getString("user_name", "") ?: ""

                        // Verify biometrics with backend
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val url = "${Constants.BASE_URL}/api/auth/verify-biometrics"
                                val json = JSONObject().apply {
                                    put("userId", userId)
                                    put("email", userEmail)
                                }

                                val requestBody = json.toString().toRequestBody("application/json".toMediaType())
                                val request = Request.Builder()
                                    .url(url)
                                    .post(requestBody)
                                    .build()

                                val response = client.newCall(request).execute()
                                val responseBody = response.body?.string()
                                val jsonResponse = JSONObject(responseBody)

                                withContext(Dispatchers.Main) {
                                    if (response.isSuccessful && jsonResponse.getString("status") == "success") {
                                        // Update login status
                                        sharedPreferences.edit().putBoolean("is_logged_in", true).apply()
                                        
                                        // Show success message
                                        Toast.makeText(
                                            this@HomeActivity,
                                            "Login successful",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        
                                        // Refresh the UI
                                        recreate()
                                    } else {
                                        Toast.makeText(
                                            this@HomeActivity,
                                            "Login failed: ${jsonResponse.optString("message", "Unknown error")}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        this@HomeActivity,
                                        "Error: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        Toast.makeText(
                            this@HomeActivity,
                            "Authentication error: $errString",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        Toast.makeText(
                            this@HomeActivity,
                            "Authentication failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )

            biometricPrompt.authenticate(promptInfo)
        } else {
            Toast.makeText(
                this,
                "Fingerprint authentication is not available",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(userName: String, initialProfileImage: Uri? = null, onProfileImageChanged: (Uri?) -> Unit = {}) {
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

    // Update profile image when initialProfileImage changes
    LaunchedEffect(initialProfileImage) {
        if (initialProfileImage != null) {
            profileImage = initialProfileImage
            Log.d("HomeScreen", "Profile image updated from initialProfileImage: $initialProfileImage")
        }
    }

    // Fetch profile picture when component is created
    LaunchedEffect(userId) {
        if (userId <= 0) {
            Log.e("HomeScreen", "Invalid user ID: $userId")
            return@LaunchedEffect
        }
        
        Log.d("HomeScreen", "Fetching profile picture for user ID: $userId")
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // First try to get profile picture from server (database)
                Log.d("HomeScreen", "Fetching profile picture from server")
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
                                        val newUri = Uri.fromFile(file)
                                        profileImage = newUri
                                        onProfileImageChanged(newUri)
                                        Log.d("HomeScreen", "Profile image updated from server: $newUri")
                                    }
                                    return@launch
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Log.e("HomeScreen", "Error processing server profile picture: ${e.message}")
                            }
                        }
                    }
                }
                
                // If server profile picture fails or is not available, try Google profile picture
                val profilePictureUrl = sharedPreferences.getString("profile_picture_url", null)
                if (profilePictureUrl != null) {
                    Log.d("HomeScreen", "Falling back to Google profile picture from URL: $profilePictureUrl")
                    try {
                        val client = OkHttpClient.Builder()
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(30, TimeUnit.SECONDS)
                            .writeTimeout(30, TimeUnit.SECONDS)
                            .build()
                        
                        val request = Request.Builder()
                            .url(profilePictureUrl)
                            .get()
                            .build()
                        
                        val response = client.newCall(request).execute()
                        Log.d("HomeScreen", "Google profile picture response code: ${response.code}")
                        
                        if (response.isSuccessful) {
                            val responseBody = response.body
                            if (responseBody != null) {
                                val imageBytes = responseBody.bytes()
                                Log.d("HomeScreen", "Google profile picture bytes size: ${imageBytes.size}")
                                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                Log.d("HomeScreen", "Google profile picture bitmap created: ${bitmap != null}")
                                
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
                                    
                                    val uri = Uri.fromFile(file)
                                    Log.d("HomeScreen", "Google profile picture saved to: $uri")
                                    
                                    withContext(Dispatchers.Main) {
                                        profileImage = uri
                                        onProfileImageChanged(uri)
                                        Log.d("HomeScreen", "Profile image updated from Google: $uri")
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("HomeScreen", "Error loading Google profile picture", e)
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
                            Log.d("HomeActivity", "Rendering profile image: $profileImage")
                            Image(
                                painter = rememberAsyncImagePainter(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(profileImage)
                                        .crossfade(true)
                                        .error(R.drawable.ic_profile_placeholder)
                                        .build(),
                                    onError = {
                                        Log.e("HomeActivity", "Error loading profile image")
                                        profileImage = null
                                    }
                                ),
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Log.d("HomeActivity", "No profile image, showing initial: ${currentUserName.take(1).uppercase()}")
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
                    },
                    showUploadTab = userId == 1L
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
                        onProfileImageChanged(newImage)
                        Log.d("HomeScreen", "Profile image updated from ProfileTab: $newImage")
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
                        4 -> if (userId == 1L) UploadTab()
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
    val profilePictureUrl = sharedPreferences.getString("profile_picture_url", null)
    
    var showImagePicker by remember { mutableStateOf(false) }
    var profileImage by remember { mutableStateOf<Uri?>(null) }
    var isEditingUsername by remember { mutableStateOf(false) }
    var isEditingEmail by remember { mutableStateOf(false) }
    
    // Fetch profile picture when component is created
    LaunchedEffect(userId) {
        if (userId <= 0) {
            Log.e("ProfileTab", "Invalid user ID: $userId")
            return@LaunchedEffect
        }
        
        Log.d("ProfileTab", "Fetching profile picture for user ID: $userId")
        Log.d("ProfileTab", "Google profile picture URL: $profilePictureUrl")
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // First try to get profile picture from server (database)
                Log.d("ProfileTab", "Fetching profile picture from server")
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
                                        android.util.Log.d("ProfileTab", "Profile image URI set from server: $newUri")
                                        android.util.Log.d("ProfileTab", "File exists: ${file.exists()}")
                                        android.util.Log.d("ProfileTab", "File size: ${file.length()}")
                                    }
                                    return@launch
                                } else {
                                    android.util.Log.e("ProfileTab", "Failed to decode bitmap from base64")
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("ProfileTab", "Error processing server profile picture: ${e.message}")
                                e.printStackTrace()
                            }
                        } else {
                            android.util.Log.d("ProfileTab", "No profile picture found in server response")
                        }
                    } else {
                        val message = jsonObject.optString("message", "Failed to load profile picture")
                        android.util.Log.e("ProfileTab", "Error status: $message")
                    }
                } else {
                    android.util.Log.e("ProfileTab", "Unsuccessful response: ${response.code}")
                }
                
                // If server profile picture fails or is not available, try Google profile picture
                if (profilePictureUrl != null) {
                    Log.d("ProfileTab", "Falling back to Google profile picture from URL: $profilePictureUrl")
                    try {
                        val client = OkHttpClient.Builder()
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(30, TimeUnit.SECONDS)
                            .writeTimeout(30, TimeUnit.SECONDS)
                            .build()
                        
                        val request = Request.Builder()
                            .url(profilePictureUrl)
                            .get()
                            .build()
                        
                        val response = client.newCall(request).execute()
                        Log.d("ProfileTab", "Google profile picture response code: ${response.code}")
                        
                        if (response.isSuccessful) {
                            val responseBody = response.body
                            if (responseBody != null) {
                                val imageBytes = responseBody.bytes()
                                Log.d("ProfileTab", "Google profile picture bytes size: ${imageBytes.size}")
                                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                Log.d("ProfileTab", "Google profile picture bitmap created: ${bitmap != null}")
                                
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
                                    
                                    val uri = Uri.fromFile(file)
                                    Log.d("ProfileTab", "Google profile picture saved to: $uri")
                                    
                                    withContext(Dispatchers.Main) {
                                        profileImage = uri
                                        onProfileImageChanged(uri)
                                        Log.d("ProfileTab", "Profile image URI set from Google: $uri")
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("ProfileTab", "Error loading Google profile picture", e)
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
                            showImagePicker = false
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
                        painter = rememberAsyncImagePainter(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(profileImage)
                                .crossfade(true)
                                .build()
                        ),
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
                                            // Validate user ID
                                            if (userId <= 0) {
                                                Log.e("ProfileTab", "Invalid user ID for username update: $userId")
                                                withContext(Dispatchers.Main) {
                                                    Toast.makeText(
                                                        context,
                                                        "Error: Invalid user ID. Please log out and log in again.",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                                return@launch
                                            }
                                            
                                            Log.d("ProfileTab", "Updating username for user ID: $userId")
                                            
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
                                            } else {
                                                val errorBody = response.body?.string()
                                                Log.e("ProfileTab", "Failed to update username. Status: ${response.code}, Error: $errorBody")
                                                withContext(Dispatchers.Main) {
                                                    Toast.makeText(
                                                        context,
                                                        "Failed to update username: ${response.message}",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        } catch (e: Exception) {
                                            Log.e("ProfileTab", "Error updating username", e)
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
                                    // Validate user ID
                                    if (userId <= 0) {
                                        Log.e("ProfileTab", "Invalid user ID for password change: $userId")
                                        withContext(Dispatchers.Main) {
                                            passwordError = "Error: Invalid user ID. Please log out and log in again."
                                        }
                                        return@launch
                                    }
                                    
                                    Log.d("ProfileTab", "Changing password for user ID: $userId")
                                    
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
                                    
                                    // Execute the request on IO thread
                                    val response = withContext(Dispatchers.IO) {
                                        client.newCall(request).execute()
                                    }
                                    
                                    // Read response body on IO thread
                                    val responseBody = withContext(Dispatchers.IO) {
                                        response.body?.string() ?: "{}"
                                    }
                                    
                                    withContext(Dispatchers.Main) {
                                        if (response.isSuccessful) {
                                            // Clear fields and show success message
                                            oldPassword = ""
                                            newPassword = ""
                                            confirmPassword = ""
                                            showPasswordFields = false
                                            
                                            val jsonResponse = JSONObject(responseBody)
                                            
                                            // Update the stored password in SharedPreferences
                                            sharedPreferences.edit()
                                                .putString("user_password", newPassword)
                                                .apply()
                                            
                                            Toast.makeText(
                                                context,
                                                jsonResponse.optString("message", "Password changed successfully"),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            val jsonError = JSONObject(responseBody)
                                            Log.e("ProfileTab", "Failed to change password. Status: ${response.code}, Error: $responseBody")
                                            passwordError = jsonError.optString("message", "Failed to change password")
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.e("ProfileTab", "Error changing password", e)
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

        // Use the new RecommendationsSection component
        RecommendationsSection()

        Spacer(modifier = Modifier.height(24.dp))

        // Use the new FavoritesSection component
        FavoritesSection()
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
    val sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    val userId = sharedPreferences.getLong("user_id", -1)
    var hasBiometrics by remember { mutableStateOf(false) }
    var showBiometricDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var isTogglingBiometrics by remember { mutableStateOf(false) }
    
    // Check if user has biometrics enabled
    LaunchedEffect(userId) {
        if (userId > 0) {
            try {
                withContext(Dispatchers.IO) {
                    val client = OkHttpClient.Builder()
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .build()
                    
                    val request = Request.Builder()
                        .url("${Constants.BASE_URL}/api/authentication/checkBiometrics/$userId")
                        .get()
                        .build()
                    
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        val jsonObject = JSONObject(responseBody)
                        withContext(Dispatchers.Main) {
                            hasBiometrics = jsonObject.getBoolean("hasBiometrics")
                            isLoading = false
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            isLoading = false
                            Toast.makeText(
                                context,
                                "Failed to check biometrics status: ${response.code}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("SettingsTab", "Error checking biometrics status", e)
                withContext(Dispatchers.Main) {
                    isLoading = false
                    Toast.makeText(
                        context,
                        "Error checking biometrics status: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            isLoading = false
        }
    }
    
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

        // Biometrics Settings Item
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
                .clickable { showBiometricDialog = true },
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
                    imageVector = Icons.Outlined.Fingerprint,
                    contentDescription = "Biometrics",
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
                        text = "BIOMETRICS",
                        color = Color.White,
                        fontFamily = interFontFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (hasBiometrics) "Biometrics is enabled" else "Enable fingerprint for account security",
                            color = Color.White.copy(alpha = 0.8f),
                            fontFamily = interLightFontFamily,
                            fontSize = 14.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Our Team Settings Item
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

    // Biometrics Dialog
    if (showBiometricDialog) {
        AlertDialog(
            onDismissRequest = { 
                if (!isTogglingBiometrics) {
                    showBiometricDialog = false 
                }
            },
            title = {
                Text(
                    text = if (hasBiometrics) "Disable Biometrics" else "Enable Biometrics",
                    fontFamily = interFontFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    if (isTogglingBiometrics) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFF9C27B0),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    } else {
                        Text(
                            text = if (hasBiometrics) 
                                "Are you sure you want to disable biometric authentication?" 
                            else 
                                "Enable fingerprint authentication for quick and secure access to your account.",
                            fontFamily = interFontFamily,
                            fontSize = 16.sp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (isTogglingBiometrics) return@TextButton
                        
                        isTogglingBiometrics = true
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val client = OkHttpClient.Builder()
                                    .connectTimeout(30, TimeUnit.SECONDS)
                                    .readTimeout(30, TimeUnit.SECONDS)
                                    .writeTimeout(30, TimeUnit.SECONDS)
                                    .build()
                                
                                val requestBody = JSONObject().apply {
                                    put("enable", !hasBiometrics)
                                }.toString()
                                
                                Log.d("Biometrics", "Sending request to toggle biometrics: $requestBody")
                                
                                val request = Request.Builder()
                                    .url("${Constants.BASE_URL}/api/authentication/toggleBiometrics/$userId")
                                    .post(requestBody.toRequestBody("application/json".toMediaType()))
                                    .build()
                                
                                val response = client.newCall(request).execute()
                                val responseBody = response.body?.string()
                                Log.d("Biometrics", "Response code: ${response.code}, Body: $responseBody")
                                
                                withContext(Dispatchers.Main) {
                                    if (response.isSuccessful) {
                                        try {
                                            val jsonResponse = JSONObject(responseBody ?: "{}")
                                            val success = jsonResponse.optBoolean("success", true)
                                            
                                            if (success) {
                                                hasBiometrics = !hasBiometrics
                                                Toast.makeText(
                                                    context,
                                                    if (hasBiometrics) "Biometrics enabled successfully" else "Biometrics disabled successfully",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                
                                                // If biometrics was just enabled, prompt for registration
                                                if (hasBiometrics) {
                                                    showBiometricDialog = false
                                                    // Show fingerprint registration prompt
                                                    val biometricManager = BiometricManager.from(context)
                                                    when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
                                                        BiometricManager.BIOMETRIC_SUCCESS -> {
                                                            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                                                                .setTitle("Register Fingerprint")
                                                                .setSubtitle("Place your finger on the sensor to register")
                                                                .setNegativeButtonText("Cancel")
                                                                .build()

                                                            val biometricPrompt = BiometricPrompt(
                                                                context as FragmentActivity,
                                                                ContextCompat.getMainExecutor(context),
                                                                object : BiometricPrompt.AuthenticationCallback() {
                                                                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                                                        super.onAuthenticationSucceeded(result)
                                                                        Toast.makeText(
                                                                            context,
                                                                            "Fingerprint registered successfully",
                                                                            Toast.LENGTH_SHORT
                                                                        ).show()
                                                                    }

                                                                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                                                                        super.onAuthenticationError(errorCode, errString)
                                                                        Toast.makeText(
                                                                            context,
                                                                            "Registration error: $errString",
                                                                            Toast.LENGTH_SHORT
                                                                        ).show()
                                                                    }

                                                                    override fun onAuthenticationFailed() {
                                                                        super.onAuthenticationFailed()
                                                                        Toast.makeText(
                                                                            context,
                                                                            "Registration failed",
                                                                            Toast.LENGTH_SHORT
                                                                        ).show()
                                                                    }
                                                                }
                                                            )

                                                            biometricPrompt.authenticate(promptInfo)
                                                        }
                                                        else -> {
                                                            Toast.makeText(
                                                                context,
                                                                "Fingerprint authentication is not available",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                    }
                                                }
                                            } else {
                                                val message = jsonResponse.optString("message", "Unknown error")
                                                Toast.makeText(
                                                    context,
                                                    "Failed: $message",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        } catch (e: Exception) {
                                            Log.e("Biometrics", "Error parsing response", e)
                                            hasBiometrics = !hasBiometrics
                                            Toast.makeText(
                                                context,
                                                if (hasBiometrics) "Biometrics enabled successfully" else "Biometrics disabled successfully",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    } else {
                                        val errorMessage = try {
                                            val jsonError = JSONObject(responseBody ?: "{}")
                                            jsonError.optString("message", "Unknown error")
                                        } catch (e: Exception) {
                                            "Failed to ${if (hasBiometrics) "disable" else "enable"} biometrics (${response.code})"
                                        }
                                        
                                        Toast.makeText(
                                            context,
                                            errorMessage,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    isTogglingBiometrics = false
                                    showBiometricDialog = false
                                }
                            } catch (e: Exception) {
                                Log.e("Biometrics", "Error toggling biometrics", e)
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        context,
                                        "Error: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    isTogglingBiometrics = false
                                    showBiometricDialog = false
                                }
                            }
                        }
                    },
                    enabled = !isTogglingBiometrics
                ) {
                    Text(
                        text = if (hasBiometrics) "Disable" else "Enable",
                        fontFamily = interFontFamily,
                        color = Color(0xFF9C27B0)
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        if (!isTogglingBiometrics) {
                            showBiometricDialog = false 
                        }
                    },
                    enabled = !isTogglingBiometrics
                ) {
                    Text(
                        text = "Cancel",
                        fontFamily = interFontFamily,
                        color = Color.Gray
                    )
                }
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
                        colors = listOf(Color(0xFF8E24AA), Color(0xFF42A5F5))
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

