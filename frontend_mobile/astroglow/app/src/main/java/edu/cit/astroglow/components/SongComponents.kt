package edu.cit.astroglow.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import edu.cit.astroglow.interLightFontFamily

@Composable
fun SongGrid() {
    Column {
        // Row 1
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            SongItem(title = "Song 1", modifier = Modifier.weight(1f))
            SongItem(title = "Song 2", modifier = Modifier.weight(1f))
        }

        // Row 2
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            SongItem(title = "Song 3", modifier = Modifier.weight(1f))
            SongItem(title = "Song 4", modifier = Modifier.weight(1f))
        }

        // Row 3
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            SongItem(title = "Song 5", modifier = Modifier.weight(1f))
            SongItem(title = "Song 6", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun SongItem(title: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .height(48.dp)
            .padding(end = 8.dp)
    ) {
        // Gray thumbnail
        Box(
            modifier = Modifier
                .width(48.dp)
                .height(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.LightGray)
        )

        // Song info with black background
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .clip(RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
                .background(Color.Black),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = title,
                color = Color.White,
                fontFamily = interLightFontFamily,
                modifier = Modifier.padding(start = 12.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
} 