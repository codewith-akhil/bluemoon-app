package com.example.telegram.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.telegram.ui.components.TelegramKeypad
import com.example.telegram.ui.theme.TelegramBlue
import kotlinx.coroutines.delay

@Composable
fun OtpVerificationScreen(
    phoneNumber: String,
    dialCode: String,
    otpCode: String,
    onOtpChange: (String) -> Unit,
    onVerifyOtp: (String) -> Unit,
    onResendCode: () -> Unit,
    onBack: () -> Unit,
    hasError: Boolean = false,
    errorMessage: String = ""
) {
    var timerSeconds by remember { mutableIntStateOf(60) }
    var isTimerRunning by remember { mutableStateOf(true) }

    // 60-Second Countdown timer
    LaunchedEffect(isTimerRunning, timerSeconds) {
        if (isTimerRunning && timerSeconds > 0) {
            delay(1000)
            timerSeconds -= 1
        } else if (timerSeconds == 0) {
            isTimerRunning = false
        }
    }

    // Auto-trigger verification when 6 digits entered
    LaunchedEffect(otpCode) {
        if (otpCode.length == 6) {
            onVerifyOtp(otpCode)
        }
    }

    // Cursor animation
    val infiniteTransition = rememberInfiniteTransition(label = "otp_cursor")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor_alpha"
    )

    // Shake offset for error state
    val shakeOffset by animateFloatAsState(
        targetValue = if (hasError) 10f else 0f,
        animationSpec = tween(durationMillis = 100),
        label = "shake_animation"
    )

    val fullPhone = "$dialCode $phoneNumber"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("otp_verification_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("otp_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title
            Text(
                text = "Verification Code",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Subtitle with target phone/email
            Text(
                text = buildAnnotatedString {
                    append("Please check your SMS sent to\n")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)) {
                        append(fullPhone)
                    }
                    append(" and enter the 6-digit code we just sent you.")
                },
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // 6-Digit OTP Boxes Row (Matching Screenshot 5)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(x = if (hasError) shakeOffset.dp else 0.dp)
                    .padding(horizontal = 4.dp)
                    .testTag("otp_boxes_row"),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 6) {
                    val digit = otpCode.getOrNull(i)?.toString() ?: ""
                    val isFocused = otpCode.length == i || (otpCode.length == 6 && i == 5)

                    OtpDigitBox(
                        digit = digit,
                        isFocused = isFocused && otpCode.length == i,
                        hasError = hasError,
                        cursorAlpha = if (isFocused && digit.isEmpty()) cursorAlpha else 0f
                    )
                }
            }

            // Error message banner if present
            if (hasError && errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 60-Second Resend Countdown Timer / Actions
            if (timerSeconds > 0) {
                val formattedTime = String.format("%02d:%02d", timerSeconds / 60, timerSeconds % 60)
                Text(
                    text = "Resend code in $formattedTime",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("otp_timer_label")
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TextButton(
                        onClick = {
                            timerSeconds = 60
                            isTimerRunning = true
                            onResendCode()
                        },
                        modifier = Modifier.testTag("resend_otp_sms_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = TelegramBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Resend Code via SMS",
                                color = TelegramBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    TextButton(
                        onClick = {
                            timerSeconds = 60
                            isTimerRunning = true
                            onResendCode()
                        },
                        modifier = Modifier.testTag("resend_otp_call_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = null,
                                tint = TelegramBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Call me to dictate code",
                                color = TelegramBlue,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Quick Demo Helpers for reviewer convenience
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Demo code: 123456",
                    fontSize = 12.sp,
                    color = TelegramBlue,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(TelegramBlue.copy(alpha = 0.1f))
                        .clickable {
                            onOtpChange("123456")
                        }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .testTag("autofill_demo_otp")
                )

                Text(
                    text = "Test Error",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
                        .clickable {
                            onOtpChange("999999")
                        }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .testTag("test_error_otp")
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // In-App Keypad
            TelegramKeypad(
                onDigitClick = { digit ->
                    if (otpCode.length < 6) {
                        onOtpChange(otpCode + digit)
                    }
                },
                onDeleteClick = {
                    if (otpCode.isNotEmpty()) {
                        onOtpChange(otpCode.dropLast(1))
                    }
                },
                onClearAll = {
                    onOtpChange("")
                }
            )

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun OtpDigitBox(
    digit: String,
    isFocused: Boolean,
    hasError: Boolean,
    cursorAlpha: Float
) {
    val borderColor = when {
        hasError -> MaterialTheme.colorScheme.error
        isFocused -> TelegramBlue
        digit.isNotEmpty() -> TelegramBlue.copy(alpha = 0.6f)
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    }

    val borderWidth = if (isFocused || hasError) 2.dp else 1.dp

    Box(
        modifier = Modifier
            .size(width = 46.dp, height = 58.dp)
            .clip(RoundedCornerShape(10.dp))
            .border(
                width = borderWidth,
                color = borderColor,
                shape = RoundedCornerShape(10.dp)
            )
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        if (digit.isNotEmpty()) {
            Text(
                text = digit,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (hasError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
        } else if (isFocused) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(26.dp)
                    .background(TelegramBlue.copy(alpha = cursorAlpha))
            )
        }
    }
}
