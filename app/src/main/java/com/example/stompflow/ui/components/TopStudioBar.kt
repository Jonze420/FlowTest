package com.example.stompflow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stompflow.ui.theme.AccentGreen
import com.example.stompflow.ui.theme.AccentRed
import com.example.stompflow.ui.theme.DarkBorder
import com.example.stompflow.ui.theme.DarkSurface
import com.example.stompflow.ui.theme.OrangePrimary

@Composable
fun TopStudioBar(
    statusText: String,
    isActiveAudio: Boolean,
    onPanicStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .border(1.dp, DarkBorder)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Identity
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(OrangePrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = "StompFlow",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = Color.White
            )
        }

        // Status pill and panic stop
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Status Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
                    .testTag("status_pill")
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isActiveAudio) AccentGreen else Color.Gray)
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            }

            // Panic Stop Button
            IconButton(
                onClick = onPanicStop,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AccentRed.copy(alpha = 0.2f))
                    .border(1.dp, AccentRed.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .testTag("panic_stop_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "Stop All Audio",
                    tint = AccentRed,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
