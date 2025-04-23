package edu.cit.astroglow.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.cit.astroglow.interFontFamily

@Composable
fun PlaylistTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Playlists",
            fontFamily = interFontFamily,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { /* Create playlist */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0050D0)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Create Playlist")
            }

            Button(
                onClick = { /* Import playlist */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0050D0)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Import")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Your playlists will appear here",
            fontFamily = interFontFamily,
            color = Color.White,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
    }
} 