package edu.cit.astroglow

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.cit.astroglow.ui.theme.AstroglowTheme
import edu.cit.astroglow.R
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment
import edu.cit.astroglow.components.BottomNavBar
import edu.cit.astroglow.interFontFamily
import edu.cit.astroglow.interLightFontFamily

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AstroglowTheme {
                HomeScreen(userName = "Cg") // This would come from user data
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(userName: String) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Home, 1 = Favorites, 2 = Playlist, 3 = Settings
    var showHomeTab by remember { mutableStateOf(true) }
    var showProfileTab by remember { mutableStateOf(false) }
    
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
    
    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { 
                    // Add AstroGlow text here
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { 
                            showHomeTab = true
                            showProfileTab = false
                            selectedTab = 0 // Reset to Home tab
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.mipmap.logo_foreground),
                            contentDescription = "AstroGlow Logo",
                            modifier = Modifier.size(47.dp),
                            tint = Color.Unspecified,
                        )
                        Text(
                            text = "AstroGlow",
                            color = Color.White,
                            fontFamily = interFontFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                },
                navigationIcon = { 
                    // Empty now that logo is in title
                },
                actions = {
                    // Account icon on the right - now navigates to Profile
                    IconButton(onClick = { 
                        showHomeTab = false
                        showProfileTab = true
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.account_icon),
                            contentDescription = "Account",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            // Only show bottom nav if not on Profile screen
            if (!showProfileTab) {
                BottomNavBar(
                    selectedTab = selectedTab,
                    onTabSelected = {
                        selectedTab = it
                        showHomeTab = true
                    }
                )
            }
        }
    ) { paddingValues ->
        // The main content based on selected tab or special screens
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(firstColor, secondColor)
                    )
                )
        ) {
            when {
                showProfileTab -> ProfileTab() // Show Profile when profile icon clicked
                else -> {
                    when (selectedTab) {
                        0 -> HomeTabWithSearch(userName) // Home
                        1 -> FavoritesTab()             // Favorites
                        2 -> PlaylistTab()              // Playlist
                        3 -> SettingsTab()              // Settings
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Profile",
            fontFamily = interFontFamily,
            color = Color.White,
            fontSize = 46.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            contentAlignment = Alignment.Center // Center the profile icon
        ) {
            Box(
                modifier = Modifier
                    .size(150.dp) // Larger profile icon
                    .clip(RoundedCornerShape(75.dp))
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile Picture",
                    tint = Color.White,
                    modifier = Modifier.size(140.dp)
                )
            }
        }
        
        // Profile Details with design
        ProfileDetailItem(icon = R.drawable.ic_google, label = "Google Account", value = "fcg8963@gmail.com")
        ProfileDetailItem(icon = R.drawable.ic_github, label = "GitHub Account", value = "Cg-Del")
        ProfileDetailItem(icon = null, label = "Name", value = "Cg M. Fernandez")
        ProfileDetailItem(icon = null, label = "Email", value = "cg.fernandez@cit.edu")
        ProfileDetailItem(icon = null, label = "Password", value = "************")

        Spacer(modifier = Modifier.weight(1f))

        // Edit Button
        Button(
            onClick = { /* Edit Profile */ },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Text("Edit Profile", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun ProfileDetailItem(icon: Int?, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = label,
            fontFamily = interLightFontFamily,
            color = Color.White,
            fontSize = 16.sp
        )
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp) // Increased gradient height
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF6A1B9A), Color(0xFF283593)) // Darker gradient colors
                )
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = value,
            fontFamily = interLightFontFamily,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTabWithSearch(userName: String) {
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Welcome, $userName",
            fontFamily = interFontFamily,
            color = Color.White,
            fontSize = 46.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Listen to our music for free and browse through our library to see our latest creation.",
            fontFamily = interLightFontFamily,
            color = Color.White,
            fontSize = 18.sp,
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
        )

        // Search bar now in home tab with navigation
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Enter a song", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray) },
            trailingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_forward),
                    contentDescription = "Search",
                    tint = Color.Gray,
                    modifier = Modifier.clickable {
                        val intent = Intent(context, SearchActivity::class.java).apply {
                            putExtra("search_query", searchQuery)
                        }
                        context.startActivity(intent)
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .clickable {
                    val intent = Intent(context, SearchActivity::class.java).apply {
                        putExtra("search_query", searchQuery)
                    }
                    context.startActivity(intent)
                },
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = Color.DarkGray,
                unfocusedTextColor = Color.DarkGray
            ),
            singleLine = true
        )

        // Sample song list
        SongGrid()

        Spacer(modifier = Modifier.height(24.dp))

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
                .padding(vertical = 12.dp)
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
            // Title and arrow at the top
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
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
                    modifier = Modifier.size(24.dp)
                )
            }
            
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 48.dp, bottom = 16.dp, end = 0.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(3) {
                    // Item with subtle shadow
                    Box(
                        modifier = Modifier
                            .width(160.dp)
                            .height(140.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

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
                .padding(vertical = 12.dp)
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
            // Title and badge at the top
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Favorites",
                    fontFamily = interFontFamily,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                // Badge with number
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(Color.White, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "4",
                        color = Color(0xFF0050D0),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 48.dp, bottom = 16.dp, end = 0.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(3) {
                    // Item with subtle shadow
                    Box(
                        modifier = Modifier
                            .width(160.dp)
                            .height(140.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                    )
                }
            }
        }
    }
}

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

@Composable
fun FavoritesTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Favorites",
            fontFamily = interFontFamily,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Icon(
            imageVector = Icons.Outlined.Favorite,
            contentDescription = "Favorites",
            tint = Color.White,
            modifier = Modifier.size(80.dp)
        )

        Text(
            text = "Your favorite songs will appear here",
            fontFamily = interFontFamily,
            color = Color.White,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
fun PlaylistTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
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

@Composable
fun SettingsTab() {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Large "Settings" title
        Text(
            text = "Settings",
            fontFamily = interFontFamily,
            color = Color.White,
            fontSize = 46.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Settings Items
        SettingsListItem(
            icon = Icons.Outlined.Fingerprint,
            title = "BIOMETRICS",
            description = "Enable fingerprint for account security and protect your library for a secure experience."
        )
        Spacer(modifier = Modifier.height(16.dp))
        SettingsListItem(
            icon = Icons.Outlined.Face,
            title = "FACE ID",
            description = "Register face id to unlock application faster and provide secure access to all your music."
        )
        Spacer(modifier = Modifier.height(16.dp))
        SettingsListItem(
            icon = Icons.Outlined.Info,
            title = "Our Team",
            description = "Get to know the team of developers who developed AstroGlow.",
            onClick = { 
                val intent = Intent(context, OurTeamActivity::class.java)
                context.startActivity(intent)
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        // Optional: Sign Out button (can be kept or removed based on final design)
        Button(
            onClick = { /* Sign out */ },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0)), // Purple from gradient
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .height(50.dp)
        ) {
            Text("Sign Out", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

/**
 * Composable for individual settings items, matching the design image.
 */
@Composable
fun SettingsListItem(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: (() -> Unit)? = null // Make onClick optional
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF8E24AA), Color(622790)) // Gradient colors
                )
            )
            .clickable { onClick?.invoke() }, // Make pressable
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left side: Icon Box with gradient
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(100.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF42A5F5), Color(0xFF8E24AA)) // Reversed gradient
                    ),
                    shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White, // White icon tint
                modifier = Modifier.size(40.dp)
            )
        }
        
        // Right side: Text Box with gradient
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF8E24AA), Color(0xFF42A5F5)) // Gradient colors
                    ),
                    shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
                )
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontFamily = interFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = description,
                    color = Color.White.copy(alpha = 0.8f),
                    fontFamily = interLightFontFamily, // Use light font for description
                    fontSize = 14.sp,
                    lineHeight = 18.sp // Adjust line height for better readability
                )
            }
        }
    }
}

