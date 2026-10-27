package com.example.clutch_ai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.clutch_ai.ui.theme.ElectricCyan
import com.example.clutch_ai.ui.theme.MidnightChassis
import com.example.clutch_ai.ui.theme.StatusEmerald

@Composable
fun ClutchTopAppBar(
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier,
    isLoggedIn: Boolean = false,
    userInitials: String = "",
    showLogo: Boolean = true
) {
    Surface(
        color = Color(0xFFFFFFFF).copy(alpha = 0.95f),
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Clean Logo in top left corner (smooth handover after intro)
            Box(
                modifier = Modifier.alpha(if (showLogo) 1f else 0f)
            ) {
                ClutchReactorLogo(
                    size = 36.dp,
                    showText = true
                )
            }

            // User Profile Action Button
            if (isLoggedIn && userInitials.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MidnightChassis)
                        .border(1.dp, ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onOpenProfile
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userInitials,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan
                    )
                    // Active Green Dot in corner
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(2.dp)
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(StatusEmerald)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFEEF3F9))
                        .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onOpenProfile
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "User Profile / Login",
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
