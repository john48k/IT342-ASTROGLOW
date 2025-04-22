package edu.cit.astroglow

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import edu.cit.astroglow.ui.theme.AstroglowTheme
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

class BiometricSetupActivity : FragmentActivity() {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AstroglowTheme {
                BiometricSetupScreen(
                    onBackPressed = { finish() },
                    onSetupBiometrics = { showFingerprintSetup() }
                )
            }
        }
    }

    private fun showFingerprintSetup() {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
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
                                this@BiometricSetupActivity,
                                "Authentication error: $errString",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                            Toast.makeText(
                                this@BiometricSetupActivity,
                                "Authentication failed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )

                biometricPrompt.authenticate(promptInfo)
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Toast.makeText(
                    this,
                    "No biometric hardware available",
                    Toast.LENGTH_SHORT
                ).show()
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Toast.makeText(
                    this,
                    "Biometric hardware is currently unavailable",
                    Toast.LENGTH_SHORT
                ).show()
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Toast.makeText(
                    this,
                    "No biometrics enrolled. Please set up fingerprint in device settings",
                    Toast.LENGTH_LONG
                ).show()
            }
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
                            this@BiometricSetupActivity,
                            "Fingerprint authentication enabled successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(
                            this@BiometricSetupActivity,
                            "Failed to enable fingerprint authentication",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("BiometricSetupActivity", "Error enabling biometrics", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@BiometricSetupActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}

@Composable
fun BiometricSetupScreen(
    onBackPressed: () -> Unit,
    onSetupBiometrics: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF9C27B0), Color(0xFF2196F3))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar with back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackPressed) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Biometric Setup",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Fingerprint icon
            Icon(
                imageVector = Icons.Outlined.Fingerprint,
                contentDescription = "Fingerprint",
                tint = Color.White,
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Instructions
            Text(
                text = "Enable fingerprint authentication for quick and secure access to your account",
                color = Color.White,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Setup button
            Button(
                onClick = onSetupBiometrics,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 32.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Fingerprint,
                        contentDescription = "Setup Fingerprint",
                        tint = Color(0xFF9C27B0)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Setup Fingerprint",
                        color = Color(0xFF9C27B0),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
} 