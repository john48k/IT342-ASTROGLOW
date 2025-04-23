package edu.cit.astroglow.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Favorite
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
fun FavoritesSection() {
    Column {
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