package edu.cit.astroglow.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.cit.astroglow.interFontFamily
import edu.cit.astroglow.interLightFontFamily

/**
 * Shared bottom navigation bar used across the app
 */
@Composable
fun BottomNavBar(selectedTab: Int, onTabSelected: (Int) -> Unit, showUploadTab: Boolean = false) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        color = Color.Black
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // First tab is Home
            NavBarItem(
                icon = Icons.Filled.Home,
                label = "Home",
                isSelected = selectedTab == 0,
                onClick = { onTabSelected(0) }
            )
            
            // Second tab is Favorites
            NavBarItem(
                icon = Icons.Outlined.Favorite,
                label = "Favorites",
                isSelected = selectedTab == 1,
                onClick = { onTabSelected(1) }
            )
            
            // Third tab is Playlist
            NavBarItem(
                icon = Icons.Outlined.List,
                label = "Playlist",
                isSelected = selectedTab == 2,
                onClick = { onTabSelected(2) }
            )
            
            // Fourth tab is Settings
            NavBarItem(
                icon = Icons.Outlined.Settings,
                label = "Settings",
                isSelected = selectedTab == 3,
                onClick = { onTabSelected(3) }
            )

            // Fifth tab is Upload (only shown if showUploadTab is true)
            if (showUploadTab) {
                NavBarItem(
                    icon = Icons.Filled.Upload,
                    label = "Upload",
                    isSelected = selectedTab == 4,
                    onClick = { onTabSelected(4) }
                )
            }
        }
    }
}

/**
 * Individual navigation item used in the bottom navigation bar
 */
@Composable
fun NavBarItem(icon: ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .padding(4.dp)
            .fillMaxHeight()
            .width(80.dp)
            .clickable(onClick = onClick)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) Color(0xFF0050D0) else Color.White,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            color = if (isSelected) Color(0xFF0050D0) else Color.White,
            fontFamily = interLightFontFamily,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
} 