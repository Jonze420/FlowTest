package com.example.stompflow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.stompflow.data.model.EffectNode
import com.example.stompflow.ui.theme.AccentRed
import com.example.stompflow.ui.theme.DarkBorder
import com.example.stompflow.ui.theme.DarkSurface
import com.example.stompflow.ui.theme.DarkSurfaceElevated
import com.example.stompflow.ui.theme.OrangePrimary
import com.example.stompflow.ui.theme.TextSecondary

@Composable
fun EffectCardView(
    effect: EffectNode,
    index: Int,
    totalCount: Int,
    onToggleEnabled: (Boolean) -> Unit,
    onParamChange: (Int) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val effectType = effect.effectType

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, if (effect.enabled) effectType.color.copy(alpha = 0.5f) else DarkBorder, RoundedCornerShape(12.dp))
            .testTag("effect_card_${effect.uid}")
    ) {
        Column {
            // Top Accent Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(if (effect.enabled) effectType.color else Color.Gray.copy(alpha = 0.3f))
            )

            Column(modifier = Modifier.padding(14.dp)) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Glowing LED
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (effect.enabled) effectType.color else Color.DarkGray)
                                .border(1.dp, Color.Black, CircleShape)
                        )

                        Text(
                            text = effectType.label.uppercase(),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    // On/Off Switch
                    Switch(
                        checked = effect.enabled,
                        onCheckedChange = onToggleEnabled,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = effectType.color,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = DarkSurface
                        ),
                        modifier = Modifier.testTag("switch_${effect.uid}")
                    )
                }

                // Parameter Slider Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = effectType.paramName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Text(
                        text = "${effect.param}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (effect.enabled) OrangePrimary else TextSecondary
                    )
                }

                Slider(
                    value = effect.param.toFloat(),
                    onValueChange = { onParamChange(it.toInt()) },
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(
                        thumbColor = if (effect.enabled) effectType.color else Color.Gray,
                        activeTrackColor = if (effect.enabled) effectType.color else Color.Gray,
                        inactiveTrackColor = DarkBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("slider_${effect.uid}")
                )

                // Footer Controls (Reorder & Delete)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = onMoveUp,
                            enabled = index > 0,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "Move Up",
                                tint = if (index > 0) TextSecondary else Color.DarkGray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = onMoveDown,
                            enabled = index < totalCount - 1,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = "Move Down",
                                tint = if (index < totalCount - 1) TextSecondary else Color.DarkGray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Remove Effect",
                            tint = AccentRed.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
