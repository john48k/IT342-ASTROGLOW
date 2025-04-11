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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import edu.cit.astroglow.R
import edu.cit.astroglow.ui.theme.AstroglowTheme
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateFloatAsState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AstroglowTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
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
                        targetValue = -10f, // Move up by 10dp
                        animationSpec = infiniteRepeatable(
                            animation = tween(
                                durationMillis = 1500, 
                                easing = LinearEasing,
                                delayMillis = 500 // Delay to create sequence
                            ),
                            repeatMode = RepeatMode.Reverse
                        )
                    )
                    
                    // Subtitle animation - vertical hover with same timing as title
                    val subtitleOffset by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = -8f, // Move up by 8dp
                        animationSpec = infiniteRepeatable(
                            animation = tween(
                                durationMillis = 1500, // Same duration as title
                                easing = LinearEasing,
                                delayMillis = 500 // Same delay as title
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
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Image(
                                painter = painterResource(id = R.drawable.moon_with_flag),
                                contentDescription = "Moon with Flag",
                                modifier = Modifier
                                    .padding(16.dp)
                                    .size(450.dp)
                                    .graphicsLayer(
                                        scaleX = logoScale,
                                        scaleY = logoScale
                                    ),
                                contentScale = ContentScale.Crop
                            )
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
                            Text(
                                text = "Welcome to AstroGlow, your trusted music provider. Listen to our latest beats from the coolest artist!",
                                fontSize = 16.sp,
                                fontFamily = interLightFontFamily,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth()
                                    .graphicsLayer(
                                        translationY = subtitleOffset
                                    )
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(end = 16.dp, bottom = 32.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            IconButton(
                                onClick = {
                                    val intent = Intent(this@MainActivity, AboutActivity::class.java)
                                    startActivity(intent)
                                },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.White)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.baseline_navigate_next_24),
                                    contentDescription = "Navigate Next",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
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

