package com.example.telegram.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.telegram.ui.theme.TelegramBlue
import com.example.telegram.ui.theme.TelegramLightBlue
import kotlinx.coroutines.delay

data class OnboardingSlide(
    val title: String,
    val description: @Composable () -> Unit
)

@Composable
fun LandingScreen(
    isDarkMode: Boolean,
    onToggleNightMode: () -> Unit,
    onStartMessaging: () -> Unit,
    onSkipToChats: () -> Unit
) {
    var currentSlideIndex by remember { mutableIntStateOf(0) }

    val slides = listOf(
        OnboardingSlide(
            title = "Bluemoon",
            description = {
                Text(
                    text = buildAnnotatedString {
                        append("The world's ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("fastest")
                        }
                        append(" messaging app.\nIt is ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("free")
                        }
                        append(" and ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("secure")
                        }
                        append(".")
                    },
                    textAlign = TextAlign.Center,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        ),
        OnboardingSlide(
            title = "Fast",
            description = {
                Text(
                    text = buildAnnotatedString {
                        append("Bluemoon delivers messages ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("faster")
                        }
                        append("\nthan any other application.")
                    },
                    textAlign = TextAlign.Center,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        ),
        OnboardingSlide(
            title = "Free",
            description = {
                Text(
                    text = buildAnnotatedString {
                        append("Bluemoon is ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("free forever")
                        }
                        append(". No ads.\nNo subscription fees.")
                    },
                    textAlign = TextAlign.Center,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        ),
        OnboardingSlide(
            title = "Powerful",
            description = {
                Text(
                    text = buildAnnotatedString {
                        append("Bluemoon has ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("no limits")
                        }
                        append("\non the size of your media and chats.")
                    },
                    textAlign = TextAlign.Center,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        ),
        OnboardingSlide(
            title = "Secure",
            description = {
                Text(
                    text = buildAnnotatedString {
                        append("Bluemoon keeps your messages ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("safe")
                        }
                        append("\nfrom hacker attacks.")
                    },
                    textAlign = TextAlign.Center,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        ),
        OnboardingSlide(
            title = "Cloud-Based",
            description = {
                Text(
                    text = buildAnnotatedString {
                        append("Bluemoon lets you access your\nmessages from ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("multiple devices")
                        }
                        append(".")
                    },
                    textAlign = TextAlign.Center,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        )
    )

    // Optional auto-slide advancement
    LaunchedEffect(currentSlideIndex) {
        delay(4000)
        currentSlideIndex = (currentSlideIndex + 1) % slides.size
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .navigationBarsPadding()
            .pointerInput(Unit) {
                var totalDrag = 0f
                detectHorizontalDragGestures(
                    onDragStart = { totalDrag = 0f },
                    onHorizontalDrag = { _, dragAmount -> totalDrag += dragAmount },
                    onDragEnd = {
                        if (totalDrag < -50) {
                            // Swipe Left -> Next
                            currentSlideIndex = (currentSlideIndex + 1) % slides.size
                        } else if (totalDrag > 50) {
                            // Swipe Right -> Prev
                            currentSlideIndex = if (currentSlideIndex > 0) currentSlideIndex - 1 else slides.size - 1
                        }
                    }
                )
            }
            .testTag("landing_screen")
    ) {
        // Night Mode Moon Icon in top-right corner
        IconButton(
            onClick = onToggleNightMode,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .testTag("landing_night_mode_btn")
        ) {
            Icon(
                imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                contentDescription = "Toggle Night Mode",
                tint = if (isDarkMode) Color(0xFFFFD700) else TelegramBlue,
                modifier = Modifier.size(26.dp)
            )
        }

        // Center Content: Telegram Logo, Title, Subtitle, Dots
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Large Telegram Circle Badge
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF29B6F6),
                                TelegramBlue
                            )
                        )
                    )
                    .testTag("telegram_landing_logo"),
                contentAlignment = Alignment.Center
            ) {
                TelegramPaperPlaneIcon(
                    modifier = Modifier.size(88.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Slide Title
            Text(
                text = slides[currentSlideIndex].title,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Slide Subtitle
            Box(
                modifier = Modifier.height(52.dp),
                contentAlignment = Alignment.Center
            ) {
                slides[currentSlideIndex].description()
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Indicator Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.testTag("landing_indicator_dots")
            ) {
                slides.forEachIndexed { index, _ ->
                    val isSelected = index == currentSlideIndex
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 7.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) TelegramBlue
                                else TelegramBlue.copy(alpha = 0.25f)
                            )
                            .clickable { currentSlideIndex = index }
                    )
                }
            }
        }

        // Bottom Actions: "Start Messaging" and "Skip to Chats"
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onStartMessaging,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("start_messaging_button"),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TelegramBlue,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Start Messaging",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onSkipToChats,
                modifier = Modifier.testTag("skip_to_chats_button")
            ) {
                Text(
                    text = "Enter Existing Account",
                    color = TelegramBlue,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun TelegramPaperPlaneIcon(
    modifier: Modifier = Modifier,
    planeColor: Color = Color.White
) {
    Canvas(modifier = modifier) {
        drawTelegramPaperPlane(planeColor)
    }
}

fun DrawScope.drawTelegramPaperPlane(planeColor: Color = Color.White) {
    val w = size.width
    val h = size.height

    // Exact geometrical paths for the Telegram paper plane
    val mainBody = Path().apply {
        moveTo(w * 0.20f, h * 0.52f)
        lineTo(w * 0.82f, h * 0.22f)
        lineTo(w * 0.65f, h * 0.76f)
        lineTo(w * 0.44f, h * 0.62f)
        close()
    }
    drawPath(mainBody, planeColor)

    // Folded underside wing
    val underside = Path().apply {
        moveTo(w * 0.44f, h * 0.62f)
        lineTo(w * 0.50f, h * 0.74f)
        lineTo(w * 0.54f, h * 0.65f)
        close()
    }
    drawPath(underside, planeColor.copy(alpha = 0.78f))

    // Top wing fold shadow
    val shadowWing = Path().apply {
        moveTo(w * 0.44f, h * 0.62f)
        lineTo(w * 0.72f, h * 0.36f)
        lineTo(w * 0.54f, h * 0.65f)
        close()
    }
    drawPath(shadowWing, planeColor.copy(alpha = 0.88f))
}
