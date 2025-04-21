package edu.cit.astroglow

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.MaterialTheme
import edu.cit.astroglow.R
import edu.cit.astroglow.data.api.RetrofitClient
import edu.cit.astroglow.data.model.LoginRequest
import edu.cit.astroglow.data.repository.AstroGlowRepository
import edu.cit.astroglow.interFontFamily
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import edu.cit.astroglow.ui.theme.AstroglowTheme
import kotlinx.coroutines.launch
import retrofit2.Response
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.withContext
import com.google.gson.Gson
import com.google.gson.JsonObject
import edu.cit.astroglow.data.model.UserEntity
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.concurrent.TimeUnit
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import okhttp3.ResponseBody
import edu.cit.astroglow.data.model.AuthenticationEntity
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import android.os.Build

class LoginActivity : ComponentActivity() {
    companion object {
        private const val TAG = "LoginActivity"
    }

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .requestId()
            .build()
        
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        
        // Initialize the Google Sign In launcher
        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    Log.d(TAG, "Attempting to get Google Sign In result")
                    val account = task.getResult(ApiException::class.java)
                    
                    // Get account details
                    val email = account.email
                    val displayName = account.displayName
                    val photoUrl = account.photoUrl?.toString()
                    
                    Log.d(TAG, "Google Sign In successful. Email: $email, Name: $displayName, Photo URL: $photoUrl")
                    
                    if (email != null && displayName != null) {
                        handleGoogleSignIn(email, displayName, photoUrl)
                    } else {
                        Log.e(TAG, "Google Sign In missing email or display name")
                        Toast.makeText(
                            this,
                            "Failed to get email or name from Google account",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } catch (e: ApiException) {
                    Log.e(TAG, "Google sign in failed. Error code: ${e.statusCode}", e)
                    val errorMessage = when (e.statusCode) {
                        GoogleSignInStatusCodes.SIGN_IN_FAILED -> 
                            "Sign in failed. Please check your internet connection."
                        GoogleSignInStatusCodes.SIGN_IN_CANCELLED -> 
                            "Sign in was cancelled."
                        GoogleSignInStatusCodes.SIGN_IN_CURRENTLY_IN_PROGRESS -> 
                            "Sign in is already in progress."
                        GoogleSignInStatusCodes.INVALID_ACCOUNT -> 
                            "Invalid account selected."
                        GoogleSignInStatusCodes.SIGN_IN_REQUIRED -> 
                            "Please sign in to continue."
                        else -> "Google sign in failed: ${e.statusCode}"
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            } else {
                Log.e(TAG, "Google Sign In failed: Result code ${result.resultCode}")
                Toast.makeText(
                    this,
                    "Google Sign In failed: Cancelled by user",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        
        setContent {
            AstroglowTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFFE81EDE), Color(0xFF251468))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp)
                        ) {
                            val infiniteTransition = rememberInfiniteTransition()
                            val scale by infiniteTransition.animateFloat(
                                initialValue = 1f,
                                targetValue = 1.05f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(durationMillis = 2000, easing = LinearEasing),
                                    repeatMode = RepeatMode.Reverse
                                )
                            )

                            Image(
                                painter = painterResource(id = R.drawable.login),
                                contentDescription = "Moon with Flag",
                                modifier = Modifier
                                    .padding(16.dp)
                                    .size(400.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .graphicsLayer(
                                        scaleX = scale,
                                        scaleY = scale
                                    )
                            )

                            Text(
                                text = "Welcome Back",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = interFontFamily,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Log in to continue",
                                fontSize = 16.sp,
                                fontFamily = interFontFamily,
                                color = Color.White,
                                modifier = Modifier.padding(16.dp),
                                textAlign = TextAlign.Center
                            )

                            var email by remember { mutableStateOf("") }
                            var password by remember { mutableStateOf("") }
                            var passwordVisible by remember { mutableStateOf(false) }
                            var isLoading by remember { mutableStateOf(false) }
                            
                            val context = LocalContext.current
                            val scope = rememberCoroutineScope()
                            val repository = remember { AstroGlowRepository(RetrofitClient.api) }

                            Column(
                                horizontalAlignment = Alignment.Start,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Email", fontFamily = interFontFamily, color = Color.White, modifier = Modifier.padding(bottom = 4.dp))
                                OutlinedTextField(
                                    value = email,
                                    onValueChange = { email = it },
                                    placeholder = { Text("Enter your Email Address") },
                                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email Icon", tint = Color.Black) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.DarkGray,
                                        unfocusedTextColor = Color.DarkGray,
                                        focusedContainerColor = Color.White.copy(alpha = 0.9f),
                                        unfocusedContainerColor = Color.White.copy(alpha = 0.9f),
                                        focusedBorderColor = Color.Transparent,
                                        unfocusedBorderColor = Color.Transparent
                                    )
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                Text("Password", fontFamily = interFontFamily, color = Color.White, modifier = Modifier.padding(bottom = 4.dp))
                                OutlinedTextField(
                                    value = password,
                                    onValueChange = { password = it },
                                    placeholder = { Text("Enter your password") },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password Icon", tint = Color.Black) },
                                    trailingIcon = {
                                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                            Icon(
                                                painter = painterResource(id = if (passwordVisible) R.drawable.hide_password else R.drawable.show_password),
                                                contentDescription = "Toggle password visibility",
                                                tint = Color.Black
                                            )
                                        }
                                    },
                                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.DarkGray,
                                        unfocusedTextColor = Color.DarkGray,
                                        focusedContainerColor = Color.White.copy(alpha = 0.9f),
                                        unfocusedContainerColor = Color.White.copy(alpha = 0.9f),
                                        focusedBorderColor = Color.Transparent,
                                        unfocusedBorderColor = Color.Transparent
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { 
                                    if (email.isEmpty() || password.isEmpty()) {
                                        Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    
                                    isLoading = true
                                    scope.launch {
                                        try {
                                            val loginRequest = LoginRequest(
                                                userEmail = email,
                                                userPassword = password,
                                                rememberMe = true
                                            )
                                            val response = repository.login(loginRequest)
                                            
                                            if (response.isSuccessful) {
                                                val user = response.body()
                                                if (user != null) {
                                                    // Store the user information
                                                    context.getSharedPreferences("auth", android.content.Context.MODE_PRIVATE)
                                                        .edit()
                                                        .putLong("user_id", user.id ?: -1)
                                                        .putString("user_email", user.userEmail)
                                                        .putString("user_name", user.userName)
                                                        .putString("user_password", user.userPassword)
                                                        .putBoolean("is_logged_in", true)
                                                        .apply()
                                                    
                                                    Log.d(TAG, "Stored user data - ID: ${user.id}, Email: ${user.userEmail}, Name: ${user.userName}")
                                                    
                                                    Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()
                                                    val intent = Intent(context, HomeActivity::class.java)
                                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                    Log.d(TAG, "Starting HomeActivity with flags: ${intent.flags}")
                                                    context.startActivity(intent)
                                                }
                                            } else {
                                                val errorBody = response.errorBody()?.string()
                                                val errorMessage = try {
                                                    // Try to parse JSON error message
                                                    val messageStart = errorBody?.indexOf("\"message\":\"")
                                                    val messageEnd = errorBody?.indexOf("\"}", messageStart ?: 0)
                                                    if (messageStart != null && messageEnd != null && messageStart > 0) {
                                                        errorBody.substring(messageStart + 11, messageEnd)
                                                    } else {
                                                        when (response.code()) {
                                                            401 -> "Invalid email or password"
                                                            else -> errorBody ?: "Login failed"
                                                        }
                                                    }
                                                } catch (e: Exception) {
                                                    "Login failed: ${response.message()}"
                                                }
                                                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                            }
                                        } catch (e: Exception) {
                                            val errorMessage = when {
                                                e.message?.contains("timeout") == true -> 
                                                    "Request timed out. Please check if the server is running."
                                                e.message?.contains("Failed to connect") == true -> 
                                                    "Cannot connect to the server. Please ensure the backend is running."
                                                else -> "Error: ${e.message}"
                                            }
                                            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0050D0)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start= 16.dp, top = 32.dp, bottom = 8.dp, end = 16.dp)
                                    .height(56.dp),
                                enabled = !isLoading
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(color = Color.White)
                                } else {
                                    Text(text = "Log In", color = Color.White, fontFamily = interFontFamily)
                                }
                            }

                            // Add divider with "OR" text
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 16.dp)
                            ) {
                                Divider(
                                    color = Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "OR",
                                    color = Color.White,
                                    fontFamily = interFontFamily,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                Divider(
                                    color = Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Google login button
                            OutlinedButton(
                                onClick = { 
                                    val signInIntent = googleSignInClient.signInIntent
                                    googleSignInLauncher.launch(signInIntent)
                                },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color.White,
                                    contentColor = Color.Black
                                ),
                                border = BorderStroke(1.dp, Color.Black),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .height(48.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_google),
                                        contentDescription = "Google",
                                        tint = Color.Unspecified,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Continue with Google",
                                        color = Color.Black,
                                        fontFamily = interFontFamily
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    private fun handleGoogleSignIn(email: String, name: String, photoUrl: String? = null) {
        Log.d(TAG, "Starting Google Sign In process for email: $email")
        val repository = AstroGlowRepository(RetrofitClient.api)
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val existingUser = withContext(Dispatchers.IO) {
                    try {
                        // First check if user exists by getting all users and checking email
                        val usersResponse = RetrofitClient.api.getAllUsers()
                        
                        if (usersResponse.isSuccessful) {
                            val allUsers = usersResponse.body() ?: emptyList()
                            allUsers.find { it.userEmail == email }
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error checking existing user", e)
                        null
                    }
                }

                if (existingUser != null) {
                    // User exists, proceed with login
                    Log.d(TAG, "Existing user found, logging in: ${existingUser.userName}")
                    
                    // Store user information including the user ID
                    var userId = existingUser.id ?: 0L
                    Log.d(TAG, "User ID from existingUser: ${existingUser.id}, using: $userId")
                    
                    // If user ID is null or 0, try to fetch it directly from the server
                    if (userId <= 0) {
                        Log.d(TAG, "User ID is null or 0, attempting to fetch it directly from server")
                        try {
                            // Try to get the user by email directly
                            val userResponse = withContext(Dispatchers.IO) {
                                RetrofitClient.api.getUserByEmail(email)
                            }
                            
                            if (userResponse.isSuccessful && userResponse.body() != null) {
                                val user = userResponse.body()
                                userId = user?.id ?: 0L
                                Log.d(TAG, "Fetched user ID from server: $userId")
                            } else {
                                Log.e(TAG, "Failed to fetch user by email. Status: ${userResponse.code()}")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error fetching user by email", e)
                        }
                    }
                    
                    // Make sure we have a valid user ID
                    if (userId <= 0) {
                        Log.e(TAG, "Invalid user ID: $userId")
                        Toast.makeText(
                            this@LoginActivity,
                            "Error: Invalid user ID. Please try again.",
                            Toast.LENGTH_LONG
                        ).show()
                        return@launch
                    }
                    
                    getSharedPreferences("auth", MODE_PRIVATE)
                        .edit()
                        .putLong("user_id", userId)
                        .putString("user_email", existingUser.userEmail)
                        .putString("user_name", existingUser.userName)
                        .putString("display_name", name)
                        .putString("user_password", existingUser.userPassword)
                        .putString("profile_picture_url", photoUrl)
                        .putBoolean("is_logged_in", true)
                        .apply()
                    
                    Log.d(TAG, "Stored user data - ID: $userId, Email: ${existingUser.userEmail}, Name: ${existingUser.userName}")
                    
                    Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_SHORT).show()
                    
                    // Navigate to HomeActivity and finish this activity
                    val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    Log.d(TAG, "Starting HomeActivity with flags: ${intent.flags}")
                    startActivity(intent)
                    finish()
                    return@launch
                }
                
                // If we get here, user doesn't exist, create new account
                Log.d(TAG, "User not found, creating new account")
                
                withContext(Dispatchers.IO) {
                    try {
                        // Format username to comply with backend validation while preserving readability
                        var formattedUsername = name.replace(" ", "_")
                            .replace(Regex("[^a-zA-Z0-9_]"), "") // Remove any other special characters
                        
                        // Check if username exists and append number if needed
                        var counter = 1
                        var originalUsername = formattedUsername
                        while (true) {
                            val checkResponse = RetrofitClient.api.getAllUsers()
                            if (!checkResponse.isSuccessful) break
                            
                            val users = checkResponse.body() ?: emptyList()
                            if (!users.any { it.userName == formattedUsername }) break
                            
                            formattedUsername = "${originalUsername}_${counter++}"
                        }
                        
                        // Generate a secure password that meets the requirements
                        val securePassword = generateSecurePassword()
                        
                        // Create a user entity with Google credentials
                        val user = UserEntity(
                            userName = formattedUsername,
                            userEmail = email,
                            userPassword = securePassword,
                            authentication = AuthenticationEntity(type = "GOOGLE"),
                            playlists = emptyList(),
                            offlineLibraries = emptyList(),
                            favorites = emptyList()
                        )
                        
                        Log.d(TAG, "Attempting to create new user: $user")
                        
                        // Post the user to the backend using the /api/user/postUser endpoint
                        val response = RetrofitClient.api.postUser(user)
                        
                        withContext(Dispatchers.Main) {
                            if (response.isSuccessful) {
                                val userResponse = response.body()
                                if (userResponse != null) {
                                    Log.d(TAG, "User successfully created: ${userResponse.userName}")
                                    
                                    // Store both formatted and display names, including user ID
                                    var userId = userResponse.id ?: 0L
                                    Log.d(TAG, "User ID from userResponse: ${userResponse.id}, using: $userId")
                                    
                                    // If user ID is null or 0, try to fetch it directly from the server
                                    if (userId <= 0) {
                                        Log.d(TAG, "User ID is null or 0, attempting to fetch it directly from server")
                                        try {
                                            // Try to get the user by email directly
                                            val directUserResponse = withContext(Dispatchers.IO) {
                                                RetrofitClient.api.getUserByEmail(email)
                                            }
                                            
                                            if (directUserResponse.isSuccessful && directUserResponse.body() != null) {
                                                val directUser = directUserResponse.body()
                                                userId = directUser?.id ?: 0L
                                                Log.d(TAG, "Fetched user ID from server: $userId")
                                            } else {
                                                Log.e(TAG, "Failed to fetch user by email. Status: ${directUserResponse.code()}")
                                            }
                                        } catch (e: Exception) {
                                            Log.e(TAG, "Error fetching user by email", e)
                                        }
                                    }
                                    
                                    // Make sure we have a valid user ID
                                    if (userId <= 0) {
                                        Log.e(TAG, "Invalid user ID from server: $userId")
                                        Toast.makeText(
                                            this@LoginActivity,
                                            "Error: Invalid user ID received from server. Please try again.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        return@withContext
                                    }
                                    
                                    getSharedPreferences("auth", MODE_PRIVATE)
                                        .edit()
                                        .putLong("user_id", userId)
                                        .putString("user_email", userResponse.userEmail)
                                        .putString("user_name", userResponse.userName)
                                        .putString("display_name", name)
                                        .putString("user_password", userResponse.userPassword)
                                        .putString("profile_picture_url", photoUrl)
                                        .putBoolean("is_logged_in", true)
                                        .apply()
                                    
                                    Log.d(TAG, "Stored user data - ID: $userId, Email: ${userResponse.userEmail}, Name: ${userResponse.userName}")
                                    
                                    Toast.makeText(this@LoginActivity, "Account created successfully", Toast.LENGTH_SHORT).show()
                                    
                                    // Navigate to HomeActivity and finish this activity
                                    val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                    finish()
                                } else {
                                    Log.e(TAG, "Response successful but body is null")
                                    Toast.makeText(
                                        this@LoginActivity,
                                        "Failed to create user: Empty response",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            } else {
                                val errorBody = response.errorBody()?.string()
                                Log.e(TAG, "Failed to create user. Status: ${response.code()}, Error: $errorBody")
                                
                                // If user already exists by email, try to log in directly
                                if (errorBody?.contains("Duplicate entry") == true && errorBody.contains("uk_user_email")) {
                                    // Retry getting the user one last time
                                    val finalCheckResponse = RetrofitClient.api.getAllUsers()
                                    if (finalCheckResponse.isSuccessful) {
                                        val finalUser = finalCheckResponse.body()?.find { it.userEmail == email }
                                        if (finalUser != null) {
                                            // Store user information including the user ID
                                            var userId = finalUser.id ?: 0L
                                            Log.d(TAG, "User ID from finalUser: ${finalUser.id}, using: $userId")
                                            
                                            // If user ID is null or 0, try to fetch it directly from the server
                                            if (userId <= 0) {
                                                Log.d(TAG, "User ID is null or 0, attempting to fetch it directly from server")
                                                try {
                                                    // Try to get the user by email directly
                                                    val directUserResponse = withContext(Dispatchers.IO) {
                                                        RetrofitClient.api.getUserByEmail(email)
                                                    }
                                                    
                                                    if (directUserResponse.isSuccessful && directUserResponse.body() != null) {
                                                        val directUser = directUserResponse.body()
                                                        userId = directUser?.id ?: 0L
                                                        Log.d(TAG, "Fetched user ID from server: $userId")
                                                    } else {
                                                        Log.e(TAG, "Failed to fetch user by email. Status: ${directUserResponse.code()}")
                                                    }
                                                } catch (e: Exception) {
                                                    Log.e(TAG, "Error fetching user by email", e)
                                                }
                                            }
                                            
                                            // Make sure we have a valid user ID
                                            if (userId <= 0) {
                                                Log.e(TAG, "Invalid user ID from final check: $userId")
                                                Toast.makeText(
                                                    this@LoginActivity,
                                                    "Error: Invalid user ID. Please try again.",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                                return@withContext
                                            }
                                            
                                            getSharedPreferences("auth", MODE_PRIVATE)
                                                .edit()
                                                .putLong("user_id", userId)
                                                .putString("user_email", finalUser.userEmail)
                                                .putString("user_name", finalUser.userName)
                                                .putString("display_name", name)
                                                .putString("user_password", finalUser.userPassword)
                                                .putString("profile_picture_url", photoUrl)
                                                .putBoolean("is_logged_in", true)
                                                .apply()
                                            
                                            Log.d(TAG, "Stored user data - ID: $userId, Email: ${finalUser.userEmail}, Name: ${finalUser.userName}")
                                            
                                            Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_SHORT).show()
                                            
                                            // Navigate to HomeActivity and finish this activity
                                            val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            startActivity(intent)
                                            finish()
                                            return@withContext
                                        }
                                    }
                                }
                                
                                // If we get here, show the error message
                                val errorMessage = when {
                                    errorBody?.contains("Username can only contain") == true -> 
                                        "Username can only contain letters, numbers, and underscores"
                                    errorBody?.contains("Username must be between") == true -> 
                                        "Username must be between 3 and 30 characters"
                                    errorBody?.contains("Duplicate entry") == true ->
                                        "Username already exists. Please try again."
                                    errorBody?.contains("Password must be at least") == true ->
                                        "Password must be at least 8 characters long and contain uppercase, lowercase, number, and special character"
                                    else -> "Failed to create user: ${errorBody ?: "Unknown error"}"
                                }
                                
                                Toast.makeText(
                                    this@LoginActivity,
                                    errorMessage,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Log.e(TAG, "Error during user creation/login", e)
                            val errorMessage = when {
                                e.message?.contains("timeout") == true -> 
                                    "Request timed out. Please check if the server is running."
                                e.message?.contains("Failed to connect") == true -> 
                                    "Cannot connect to the server. Please ensure the backend is running."
                                else -> "Error processing request: ${e.message}"
                            }
                            Toast.makeText(
                                this@LoginActivity,
                                errorMessage,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error preparing user data", e)
                Toast.makeText(
                    this@LoginActivity,
                    "Error preparing user data: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    
    private fun generateSecurePassword(): String {
        val upperCase = ('A'..'Z').random().toString()
        val lowerCase = ('a'..'z').random().toString()
        val number = ('0'..'9').random().toString()
        val specialChars = listOf('!', '@', '#', '$', '%', '^', '&', '*')
        val special = specialChars.random().toString()
        
        // Generate remaining characters (ensuring at least 8 characters total)
        val remainingLength = 8
        val remainingChars = (('A'..'Z') + ('a'..'z') + ('0'..'9') + specialChars)
            .shuffled()
            .take(remainingLength - 4) // -4 because we already have 4 required characters
            .joinToString("")
        
        // Combine all parts and shuffle
        return (upperCase + lowerCase + number + special + remainingChars)
            .toList()
            .shuffled()
            .joinToString("")
    }
    
    private fun isEmulator(): Boolean {
        return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.PRODUCT.contains("sdk_gphone")
                || Build.PRODUCT.contains("google_sdk")
                || Build.PRODUCT.contains("sdk")
                || Build.PRODUCT.contains("sdk_x86")
                || Build.PRODUCT.contains("vbox86p"))
    }
}

