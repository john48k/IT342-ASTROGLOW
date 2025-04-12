package edu.cit.astroglow

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import edu.cit.astroglow.R
import edu.cit.astroglow.ui.theme.AstroglowTheme
import androidx.compose.ui.geometry.Offset
import edu.cit.astroglow.CreateAccountActivity
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState

class AboutActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AstroglowTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // Animation for image only
                    val infiniteTransition = rememberInfiniteTransition()
                    
                    // Image animation
                    val scale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.05f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 2000, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        )
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFFE81EDE), Color(0xFF251468)),
                                    start = Offset(0f, 0f),
                                    end = Offset(1f, 1f)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 24.dp)
                        ) {
                            Text(
                                text = "About Us",
                                fontSize = 46.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontFamily = interFontFamily,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            
                            Text(
                                text = "Our team from AstroGlow, creates music and beats that you can listen to for free. We will develop a secure music library with integrated biometrics and face id system.",
                                fontSize = 16.sp,
                                color = Color.White,
                                fontFamily = interLightFontFamily,
                                modifier = Modifier.padding(bottom = 24.dp),
                                textAlign = TextAlign.Start
                            )
                            
                            AsyncImage(
                                model = R.drawable.about,
                                contentDescription = "About Image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(24.dp))
                                    .graphicsLayer(
                                        scaleX = scale,
                                        scaleY = scale
                                    )
                                    .padding(vertical = 16.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(32.dp))
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
                                    val intent = Intent(this@AboutActivity, CreateAccountActivity::class.java)
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
