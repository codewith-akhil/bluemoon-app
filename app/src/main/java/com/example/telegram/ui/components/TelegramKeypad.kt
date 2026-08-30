package com.example.telegram.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class KeypadButton(
    val digit: String,
    val letters: String = "",
    val isBackspace: Boolean = false,
    val isEmpty: Boolean = false
)

@Composable
fun TelegramKeypad(
    onDigitClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onClearAll: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val keyRows = listOf(
        listOf(
            KeypadButton("1", ""),
            KeypadButton("2", "ABC"),
            KeypadButton("3", "DEF")
        ),
        listOf(
            KeypadButton("4", "GHI"),
            KeypadButton("5", "JKL"),
            KeypadButton("6", "MNO")
        ),
        listOf(
            KeypadButton("7", "PQRS"),
            KeypadButton("8", "TUV"),
            KeypadButton("9", "WXYZ")
        ),
        listOf(
            KeypadButton("", "", isEmpty = true),
            KeypadButton("0", "+"),
            KeypadButton("", "", isBackspace = true)
        )
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("telegram_keypad"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        keyRows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { button ->
                    Box(modifier = Modifier.weight(1f)) {
                        when {
                            button.isEmpty -> {
                                Spacer(modifier = Modifier.height(52.dp))
                            }
                            button.isBackspace -> {
                                BackspaceKey(
                                    onClick = onDeleteClick,
                                    onLongClick = onClearAll
                                )
                            }
                            else -> {
                                NumberKey(
                                    digit = button.digit,
                                    letters = button.letters,
                                    onClick = { onDigitClick(button.digit) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NumberKey(
    digit: String,
    letters: String,
    onClick: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.2f
    val buttonBg = if (isDark) Color(0xFF242F3D).copy(alpha = 0.85f) else Color(0xFFF1F3F5)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.94f else 1f, label = "key_scale")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(buttonBg)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                onClick = onClick
            )
            .testTag("keypad_digit_$digit"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = digit,
                fontSize = 24.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (letters.isNotEmpty()) {
                Spacer(modifier = Modifier.size(6.dp))
                Text(
                    text = letters,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun BackspaceKey(
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.2f
    val buttonBg = if (isDark) Color(0xFF242F3D).copy(alpha = 0.85f) else Color(0xFFF1F3F5)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(buttonBg)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = {
                        if (onLongClick != null) onLongClick() else onClick()
                    }
                )
            }
            .testTag("keypad_backspace"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Backspace,
            contentDescription = "Backspace",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
    }
}
