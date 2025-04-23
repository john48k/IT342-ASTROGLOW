package edu.cit.astroglow.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.cit.astroglow.interFontFamily

@Composable
fun RecommendationsSection() {
    Column {
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
    }
} 