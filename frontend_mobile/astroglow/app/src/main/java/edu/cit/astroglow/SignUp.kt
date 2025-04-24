package edu.cit.astroglow

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import edu.cit.astroglow.CreateAccountActivity
import edu.cit.astroglow.ui.theme.AstroglowTheme
import edu.cit.astroglow.R
import edu.cit.astroglow.interFontFamily
import edu.cit.astroglow.interLightFontFamily
import edu.cit.astroglow.data.api.RetrofitClient
import edu.cit.astroglow.data.model.User
import edu.cit.astroglow.data.model.UserEntity
import edu.cit.astroglow.data.repository.AstroGlowRepository
import kotlinx.coroutines.launch
import retrofit2.Response
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.platform.LocalConfiguration


class SignUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AstroglowTheme {
                SignUpScreen(
                    onNavigateToHome = {
                        val intent = Intent(this@SignUpActivity, HomeActivity::class.java)
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

// Add validation functions
private fun validateUsername(username: String): String {
    if (username.isEmpty()) return "Username is required"
    if (username.length < 3 || username.length > 30) return "Username must be between 3 and 30 characters"
    if (!username.matches(Regex("^[a-zA-Z0-9_]+$"))) return "Username can only contain letters, numbers, and underscores"
    if (username.lowercase().contains("admin") || username.lowercase().contains("moderator")) 
        return "Username cannot contain prohibited terms"
    return ""
}

private fun validateEmail(email: String): String {
    if (email.isEmpty()) return "Email is required"
    if (email.length > 255) return "Email is too long"
    if (!email.matches(Regex("^[A-Za-z0-9+_.-]+@(.+)$"))) return "Invalid email format"
    return ""
}

private fun validatePassword(password: String): String {
    if (password.isEmpty()) return "Password is required"
    if (password.length < 8) return "Password must be at least 8 characters long"
    if (!password.contains(Regex("[A-Z]"))) return "Password must contain at least one uppercase letter"
    if (!password.contains(Regex("[a-z]"))) return "Password must contain at least one lowercase letter"
    if (!password.contains(Regex("[0-9]"))) return "Password must contain at least one number"
    if (!password.contains(Regex("[!@#\$%^&*(),.?\":{}|<>]"))) return "Password must contain at least one special character"
    if (password.length > 128) return "Password is too long"
    return ""
}

@Composable
fun SignUpScreen(
    onNavigateToHome: () -> Unit = {}
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rePassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rePasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    
    // Add error states
    var nameError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var rePasswordError by remember { mutableStateOf("") }
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { AstroGlowRepository(RetrofitClient.api) }
    
    // Get screen configuration
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val bottomMargin = if (isLandscape) 100.dp else 42.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFE81EDE), Color(0xFF251468))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Spacer(modifier = Modifier.height(40.dp)) // Add space at the top
                Text(
                    text = "Create Account",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = interFontFamily,
                    color = Color.White,
                    modifier = Modifier.padding(top = 32.dp)
                )
                Text(
                    text = "Start listening to AstroGlow!",
                    fontSize = 16.sp,
                    fontFamily = interLightFontFamily,
                    color = Color.LightGray, // Slightly lighter color for subtitle
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                // Input Fields
                Text("Name", fontFamily = interFontFamily, color = Color.White, modifier = Modifier.padding(bottom = 4.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { 
                        name = it
                        nameError = validateUsername(it)
                    },
                    isError = nameError.isNotEmpty(),
                    placeholder = { Text("Enter your desired name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Name Icon", tint = Color.Black) },
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
                if (nameError.isNotEmpty()) {
                    Text(
                        text = nameError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                Text("Email", fontFamily = interFontFamily, color = Color.White, modifier = Modifier.padding(bottom = 4.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { 
                        email = it
                        emailError = validateEmail(it)
                    },
                    isError = emailError.isNotEmpty(),
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
                if (emailError.isNotEmpty()) {
                    Text(
                        text = emailError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                Text("Password", fontFamily = interFontFamily, color = Color.White, modifier = Modifier.padding(bottom = 4.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { 
                        password = it
                        passwordError = validatePassword(it)
                        if (rePassword.isNotEmpty()) {
                            rePasswordError = if (it != rePassword) "Passwords do not match" else ""
                        }
                    },
                    isError = passwordError.isNotEmpty(),
                    placeholder = { Text("Create your password") },
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
                if (passwordError.isNotEmpty()) {
                    Text(
                        text = passwordError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                Text("Re-enter Password", fontFamily = interFontFamily, color = Color.White, modifier = Modifier.padding(bottom = 4.dp))
                OutlinedTextField(
                    value = rePassword,
                    onValueChange = { 
                        rePassword = it
                        rePasswordError = if (it != password) "Passwords do not match" else ""
                    },
                    isError = rePasswordError.isNotEmpty(),
                    placeholder = { Text("Re enter your password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password Icon", tint = Color.Black) },
                    trailingIcon = {
                        IconButton(onClick = { rePasswordVisible = !rePasswordVisible }) {
                            Icon(
                                painter = painterResource(id = if (rePasswordVisible) R.drawable.hide_password else R.drawable.show_password),
                                contentDescription = "Toggle password visibility",
                                tint = Color.Black
                            )
                        }
                    },
                    visualTransformation = if (rePasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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
                if (rePasswordError.isNotEmpty()) {
                    Text(
                        text = rePasswordError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Create Account Button at the bottom
            Button(
                onClick = { 
                    // Validate all fields
                    nameError = validateUsername(name)
                    emailError = validateEmail(email)
                    passwordError = validatePassword(password)
                    rePasswordError = if (password != rePassword) "Passwords do not match" else ""
                    
                    if (nameError.isEmpty() && emailError.isEmpty() && 
                        passwordError.isEmpty() && rePasswordError.isEmpty()) {
                        isLoading = true
                        scope.launch {
                            try {
                                val user = User(
                                    userName = name,
                                    userEmail = email,
                                    userPassword = password
                                )
                                
                                Log.d("SignUpActivity", "Attempting to register user: ${user.userName}, ${user.userEmail}")
                                val response = repository.register(user)
                                Log.d("SignUpActivity", "Registration response: ${response.code()}, ${response.message()}")
                                
                                if (response.isSuccessful) {
                                    // Store user information in SharedPreferences
                                    val userData = response.body()
                                    Log.d("SignUpActivity", "Registration successful, user data: $userData")
                                    
                                    if (userData != null) {
                                        context.getSharedPreferences("auth", android.content.Context.MODE_PRIVATE)
                                            .edit()
                                            .putLong("user_id", userData.id ?: -1)
                                            .putString("user_email", userData.userEmail)
                                            .putString("user_name", userData.userName)
                                            .putString("user_password", password)
                                            .putBoolean("is_logged_in", true)
                                            .apply()
                                        
                                        Log.d("SignUpActivity", "Stored user data - ID: ${userData.id}, Email: ${userData.userEmail}, Name: ${userData.userName}")
                                        
                                        // Show success toast and navigate to home
                                        Toast.makeText(context, "Account created successfully", Toast.LENGTH_SHORT).show()
                                        onNavigateToHome()
                                    } else {
                                        Log.e("SignUpActivity", "Registration response body is null")
                                        Toast.makeText(context, "Registration failed: No user data returned", Toast.LENGTH_LONG).show()
                                    }
                                } else {
                                    val errorBody = response.errorBody()?.string()
                                    Log.e("SignUpActivity", "Registration failed: $errorBody")
                                    
                                    val errorMessage = try {
                                        // Try to parse JSON error message
                                        val messageStart = errorBody?.indexOf("\"message\":\"")
                                        val messageEnd = errorBody?.indexOf("\"}", messageStart ?: 0)
                                        if (messageStart != null && messageEnd != null && messageStart > 0) {
                                            errorBody.substring(messageStart + 11, messageEnd)
                                        } else {
                                            errorBody ?: "Registration failed"
                                        }
                                    } catch (e: Exception) {
                                        "Registration failed: ${response.message()}"
                                    }
                                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                }
                            } catch (e: Exception) {
                                Log.e("SignUpActivity", "Error during registration", e)
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0050D0)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = bottomMargin)
                    .height(56.dp),
                enabled = !isLoading && name.isNotEmpty() && email.isNotEmpty() && 
                         password.isNotEmpty() && rePassword.isNotEmpty()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White)
                } else {
                    Text(text = "Create Account", color = Color.White, fontFamily = interFontFamily, fontSize = 16.sp)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    AstroglowTheme {
        SignUpScreen(
            onNavigateToHome = {}
        )
    }
}

