package edu.cit.astroglow

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import edu.cit.astroglow.ui.theme.AstroglowTheme
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateColor
import androidx.compose.material3.Icon
import androidx.activity.ComponentActivity
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState

class OurTeamActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AstroglowTheme {
                AboutUsScreen()
            }
        }
    }
}

@Composable
fun AboutUsScreen() {
    // Animation for gradient colors
    val infiniteTransition = rememberInfiniteTransition()
    
    // Animate first color
    val firstColor = infiniteTransition.animateColor(
        initialValue = Color(0xFFE81EDE), // Pink
        targetValue = Color(0xFF9C27B0),  // Purple
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    ).value
    
    // Animate second color
    val secondColor = infiniteTransition.animateColor(
        initialValue = Color(0xFF0050D0), // Blue
        targetValue = Color(0xFF2196F3),  // Light Blue
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    ).value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(firstColor, secondColor)
                )
            )
            .padding(16.dp)
    ) {
        Text(
            text = "Our Team",
            fontFamily = interFontFamily,
            color = Color.White,
            fontSize = 46.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = 24.dp)
                .padding(top = 48.dp)
        )
        
        // Team Member Profiles
        TeamMemberProfile(
            imageRes = R.drawable.john,
            email = "Johngabriel.canal@cit.edu",
            googleAccount = "Johncanal300@gmail.com",
            facebookAccount = "John Gabriel",
            role = "Frontend Developer"
        )
        TeamMemberProfile(
            imageRes = R.drawable.cg,
            email = "cg.fernandez@cit.edu",
            googleAccount = "fcg8963@gmail.com",
            facebookAccount = "Cg M. Fernandez",
            role = "Mobile Developer"
        )
        TeamMemberProfile(
            imageRes = R.drawable.allen,
            email = "Allenluis.mangoroban@cit.edu",
            googleAccount = "Allenmangoroban@gmail.com",
            facebookAccount = "Allen Luis S. Mangoroban",
            role = "Backend Developer"
        )
    }
}

@Composable
fun TeamMemberProfile(
    imageRes: Int,
    email: String,
    googleAccount: String,
    facebookAccount: String,
    role: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(Color.White)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(100.dp))
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        TeamInfo(
            role = role,
            email = email,
            googleAccount = googleAccount,
            facebookAccount = facebookAccount
        )
    }
}

@Composable
fun TeamInfo(
    role: String,
    email: String,
    googleAccount: String,
    facebookAccount: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF6A1B9A), Color(0xFF283593))
                )
            )
            .padding(16.dp)
    ) {
        // Role Section with gradient background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFE81EDE), Color(0xFF0050D0))
                    )
                )
                .padding(vertical = 8.dp, horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = role,
                fontFamily = interFontFamily,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Email Section
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_outlook),
                    contentDescription = "Outlook Account",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Email",
                    fontFamily = interFontFamily,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = email,
                fontFamily = interLightFontFamily,
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.padding(start = 28.dp, top = 4.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Google Account Section
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_google),
                    contentDescription = "Google Account",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Google",
                    fontFamily = interFontFamily,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = googleAccount,
                fontFamily = interLightFontFamily,
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.padding(start = 28.dp, top = 4.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Facebook Account Section
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_facebook),
                    contentDescription = "Facebook Account",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Facebook",
                    fontFamily = interFontFamily,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = facebookAccount,
                fontFamily = interLightFontFamily,
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.padding(start = 28.dp, top = 4.dp)
            )
        }
    }
}

