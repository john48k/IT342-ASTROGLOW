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
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.graphicsLayer
import edu.cit.astroglow.R
import edu.cit.astroglow.ui.theme.AstroglowTheme
import edu.cit.astroglow.SignUpActivity
import edu.cit.astroglow.LoginActivity
import edu.cit.astroglow.interFontFamily
import edu.cit.astroglow.interLightFontFamily


class CreateAccountActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                        ) {

                            // Add animation setup
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
                                painter = painterResource(id = R.drawable.moon_with_flag),
                                contentDescription = "Moon with Flag",
                                modifier = Modifier
                                    .padding(16.dp)
                                    .size(400.dp)
                                    .graphicsLayer( // Apply animation
                                        scaleX = scale,
                                        scaleY = scale
                                    )
                            )

                            Text(
                                text = "AstroGlow",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = interFontFamily,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Your trusted music and beats player. Browse through our library to see our latest creation.",
                                fontSize = 16.sp,
                                fontFamily = interLightFontFamily,
                                color = Color.White,
                                modifier = Modifier.padding(16.dp),
                                textAlign = TextAlign.Center
                            )

                            Button(
                                onClick = {
                                    // Navigate to SignUpActivity
                                    val intent = Intent(this@CreateAccountActivity, SignUpActivity::class.java)
                                    startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0050D0)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start= 16.dp, top = 32.dp, bottom = 8.dp, end =  16.dp)
                                    .height(56.dp)
                            ) {
                                Text(text = "Create Account", color = Color.White, fontFamily = interFontFamily)
                            }
                            TextButton(
                                onClick = {
                                    // Navigate to LoginActivity
                                    val intent = Intent(this@CreateAccountActivity, LoginActivity::class.java)
                                    startActivity(intent)
                                },
                                modifier = Modifier.padding(top = 0.dp, bottom = 8.dp)
                            ) {
                                Text(text = "Already have Account?", color = Color.White, fontFamily = interFontFamily)
                            }
                        }
                    }
                }
            }
        }
    }
}
