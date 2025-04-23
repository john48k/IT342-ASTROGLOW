package edu.cit.astroglow

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import edu.cit.astroglow.R
import edu.cit.astroglow.ui.theme.AstroglowTheme
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.width
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class MainActivity : FragmentActivity() {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AstroglowTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // Get screen configuration
                    val configuration = LocalConfiguration.current
                    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
                    val bottomMargin = if (isLandscape) 100.dp else 32.dp
                    
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
                    
                    // Sequential animations with different delays
                    val logoScale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.05f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 2200, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        )
                    )
                    
                    // Title animation - vertical hover
                    val titleOffset by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = -10f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(
                                durationMillis = 1500, 
                                easing = LinearEasing,
                                delayMillis = 500
                            ),
                            repeatMode = RepeatMode.Reverse
                        )
                    )
                    
                    // Subtitle animation - vertical hover with same timing as title
                    val subtitleOffset by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = -8f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(
                                durationMillis = 1500,
                                easing = LinearEasing,
                                delayMillis = 500
                            ),
                            repeatMode = RepeatMode.Reverse
                        )
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(firstColor, secondColor)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp)
                        ) {
                            Spacer(modifier = Modifier.weight(1f))
                            
                            Image(
                                painter = painterResource(id = R.drawable.moon_with_flag),
                                contentDescription = "Moon with Flag",
                                modifier = Modifier
                                    .size(450.dp)
                                    .graphicsLayer(
                                        scaleX = logoScale,
                                        scaleY = logoScale
                                    ),
                                contentScale = ContentScale.Crop
                            )
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            Text(
                                text = "AstroGlow",
                                fontSize = 46.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = interFontFamily,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .graphicsLayer(
                                        translationY = titleOffset
                                    )
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "Welcome to AstroGlow, your trusted music provider. Listen to our latest beats from the coolest artist!",
                                fontSize = 16.sp,
                                fontFamily = interLightFontFamily,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .graphicsLayer(
                                        translationY = subtitleOffset
                                    )
                            )
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            Spacer(modifier = Modifier.height(bottomMargin))
                            
                            Spacer(modifier = Modifier.weight(1f))
                        }
                        
                        // Navigation button at bottom right
                        IconButton(
                            onClick = {
                                val intent = Intent(this@MainActivity, AboutActivity::class.java)
                                startActivity(intent)
                            },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                                .padding(bottom = 32.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.baseline_navigate_next_24),
                                contentDescription = "Navigate Next",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        // Biometric login button at bottom left
                        IconButton(
                            onClick = { showFingerprintLogin() },
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                                .padding(bottom = 32.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Fingerprint,
                                contentDescription = "Login with Fingerprint",
                                tint = Color(0xFF9C27B0),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    private fun showFingerprintSetup() {
        val biometricManager = BiometricManager.from(this)
        val canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)

        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Setup Fingerprint")
                .setSubtitle("Place your finger on the sensor to register")
                .setNegativeButtonText("Cancel")
                .build()

            val biometricPrompt = BiometricPrompt(
                this,
                ContextCompat.getMainExecutor(this),
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        // Enable biometrics in the backend
                        enableBiometrics()
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        Toast.makeText(
                            this@MainActivity,
                            "Authentication error: $errString",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        Toast.makeText(
                            this@MainActivity,
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

    private fun enableBiometrics() {
        val sharedPreferences = getSharedPreferences("auth", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getLong("user_id", -1)

        if (userId <= 0) {
            Toast.makeText(
                this,
                "Please log in first to enable biometrics",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val requestBody = JSONObject().apply {
                    put("enable", true)
                }.toString()

                val request = Request.Builder()
                    .url("${Constants.BASE_URL}/api/authentication/toggleBiometrics/$userId")
                    .post(requestBody.toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@MainActivity,
                            "Fingerprint authentication enabled successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "Failed to enable fingerprint authentication",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error enabling biometrics", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
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

                        if (userId <= 0 || userEmail.isEmpty() || userName.isEmpty()) {
                            Toast.makeText(
                                this@MainActivity,
                                "Please log in first to use fingerprint login",
                                Toast.LENGTH_SHORT
                            ).show()
                            return
                        }

                        // Verify biometrics with backend
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val url = "${Constants.BASE_URL}/api/authentication/checkBiometrics/$userId"
                                val request = Request.Builder()
                                    .url(url)
                                    .get()
                                    .build()

                                val response = client.newCall(request).execute()
                                val responseBody = response.body?.string()
                                val jsonResponse = JSONObject(responseBody)

                                withContext(Dispatchers.Main) {
                                    if (response.isSuccessful) {
                                        val hasBiometrics = jsonResponse.optBoolean("hasBiometrics", false)
                                        
                                        if (hasBiometrics) {
                                            // Update login status
                                            sharedPreferences.edit().putBoolean("is_logged_in", true).apply()
                                            
                                            // Navigate to HomeActivity
                                            val intent = Intent(this@MainActivity, HomeActivity::class.java)
                                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            startActivity(intent)
                                            finish()
                                        } else {
                                            Toast.makeText(
                                                this@MainActivity,
                                                "Fingerprint not registered. Please enable it in Settings.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    } else {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Login failed: ${jsonResponse.optString("message", "Unknown error")}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        this@MainActivity,
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
                            this@MainActivity,
                            "Authentication error: $errString",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        Toast.makeText(
                            this@MainActivity,
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

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AstroglowTheme {
        Greeting("Android")
    }
}

// Define the font family using the local XML
val interFontFamily = FontFamily(
    Font(R.font.interdisplay_black),
    Font(R.font.inter_blackitalic)
)

val interLightFontFamily = FontFamily(
    Font(R.font.interdisplay_light)
)

