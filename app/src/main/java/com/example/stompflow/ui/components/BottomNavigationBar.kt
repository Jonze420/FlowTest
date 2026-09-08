package com.example.stompflow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stompflow.ui.theme.DarkBorder
import com.example.stompflow.ui.theme.DarkSurface
import com.example.stompflow.ui.theme.OrangePrimary
import com.example.stompflow.ui.theme.TextSecondary

enum class StudioTab(val label: String, val icon: ImageVector, val tag: String) {
    PATCHES("Patches", Icons.Default.GraphicEq, "tab_patches"),
    RHYTHM("Rhythm", Icons.Default.Album, "tab_rhythm"),
    LOOPER("Looper", Icons.Default.Repeat, "tab_looper"),
    SESSIONS("Sessions", Icons.Default.Bookmark, "tab_sessions"),
    OPTIONS("Options", Icons.Default.Settings, "tab_options")
}

@Composable
fun BottomNavigationBar(
    selectedTab: StudioTab,
    onSelectTab: (StudioTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(DarkSurface)
            .border(1.dp, DarkBorder)
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StudioTab.entries.forEach { tab ->
            val isSelected = tab == selectedTab
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelectTab(tab) }
                    .padding(vertical = 6.dp)
                    .testTag(tab.tag),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = tab.icon,
                    contentDescription = tab.label,
                    tint = if (isSelected) OrangePrimary else TextSecondary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = tab.label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (isSelected) OrangePrimary else TextSecondary
                )
            }
        }
    }
}
