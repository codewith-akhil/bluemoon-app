package com.example.telegram.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.telegram.data.models.Country
import com.example.telegram.ui.components.TelegramKeypad
import com.example.telegram.ui.theme.SecretChatGreen
import com.example.telegram.ui.theme.TelegramBlue
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun PhoneAuthScreen(
    selectedCountry: Country,
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    onOpenCountryPicker: () -> Unit,
    onSubmitPhoneNumber: () -> Unit,
    onBackToLanding: () -> Unit,
    isDarkMode: Boolean,
    onToggleNightMode: () -> Unit
) {
    var showPermissionDialog by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }
    var hasAttemptedSubmit by remember { mutableStateOf(false) }
    var isInputFocused by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val shakeOffset = remember { Animatable(0f) }

    // Synchronize TextFieldValue with external phoneNumber
    var textFieldValue by remember(phoneNumber) {
        mutableStateOf(
            TextFieldValue(
                text = phoneNumber,
                selection = androidx.compose.ui.text.TextRange(phoneNumber.length)
            )
        )
    }

    // Auto-focus the input field on first display
    LaunchedEffect(Unit) {
        try {
            focusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    // Phone validation helper
    fun validateNumber(rawNumber: String): String? {
        val trimmed = rawNumber.filter { it.isDigit() }
        return when {
            trimmed.isEmpty() -> "Please enter your mobile phone number"
            trimmed.length < 7 -> "Phone number is too short (min. 7 digits, ${trimmed.length} entered)"
            trimmed.length > 15 -> "Phone number is too long (max. 15 digits)"
            trimmed.all { it == trimmed[0] } && trimmed.length >= 6 -> "Please enter a valid phone number"
            else -> null
        }
    }

    // Validate on input change if user already attempted to submit
    val isNumberValid = remember(phoneNumber) {
        validateNumber(phoneNumber) == null
    }

    fun triggerShake() {
        coroutineScope.launch {
            shakeOffset.snapTo(0f)
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 350)
            ) {
                // simple oscillation
            }
            // Sequence shake
            for (i in 0..2) {
                shakeOffset.animateTo(12f, tween(40))
                shakeOffset.animateTo(-12f, tween(40))
            }
            shakeOffset.animateTo(0f, tween(40))
        }
    }

    fun handleAttemptSubmit() {
        hasAttemptedSubmit = true
        val error = validateNumber(phoneNumber)
        if (error != null) {
            validationError = error
            triggerShake()
        } else {
            validationError = null
            focusManager.clearFocus()
            showPermissionDialog = true
        }
    }

    // Update phone number handler with digit sanitation
    fun handleTextUpdate(newText: String) {
        val sanitized = newText.filter { it.isDigit() }.take(15)
        onPhoneNumberChange(sanitized)
        if (hasAttemptedSubmit) {
            validationError = validateNumber(sanitized)
        } else if (validationError != null) {
            validationError = null
        }
    }

    // Format phone number with space groups (e.g. 98765 43210)
    val formattedDisplayPhone = remember(phoneNumber) {
        if (phoneNumber.length > 5) {
            "${phoneNumber.take(5)} ${phoneNumber.drop(5)}"
        } else {
            phoneNumber
        }
    }

    // Cursor animation for custom cursor if needed
    val infiniteTransition = rememberInfiniteTransition(label = "cursor")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("phone_auth_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackToLanding,
                    modifier = Modifier.testTag("phone_auth_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = onToggleNightMode,
                    modifier = Modifier.testTag("phone_night_mode_btn")
                ) {
                    Icon(
                        imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle Night Mode",
                        tint = if (isDarkMode) Color(0xFFFFD700) else TelegramBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title
            Text(
                text = "Your phone number",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = "Please confirm your country code\nand enter your phone number.",
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(26.dp))

            // 1. Country Selection Card Field
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable(onClick = onOpenCountryPicker)
                    .padding(horizontal = 16.dp, vertical = 13.dp)
                    .testTag("country_picker_selector_card")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedCountry.flagEmoji,
                        fontSize = 20.sp
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = selectedCountry.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Select Country",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Phone Number Input Box (with Full Keyboard & Click-to-Focus support)
            val boxBorderColor = when {
                validationError != null -> MaterialTheme.colorScheme.error
                isNumberValid -> SecretChatGreen
                isInputFocused -> TelegramBlue
                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(x = shakeOffset.value.roundToInt(), y = 0) }
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = if (isInputFocused || validationError != null || isNumberValid) 2.dp else 1.dp,
                        color = boxBorderColor,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .background(
                        if (validationError != null) MaterialTheme.colorScheme.error.copy(alpha = 0.04f)
                        else MaterialTheme.colorScheme.surface
                    )
                    .clickable { focusRequester.requestFocus() }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("phone_number_display_box")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Country Code Prefix (Clickable to switch country)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(onClick = onOpenCountryPicker)
                    ) {
                        Text(
                            text = selectedCountry.dialCode,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Vertical Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(24.dp)
                            .background(boxBorderColor.copy(alpha = 0.4f))
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Real BasicTextField for direct physical, software keyboard & clipboard input
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (phoneNumber.isEmpty()) {
                            Text(
                                text = "Mobile number",
                                fontSize = 17.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                            )
                        }

                        BasicTextField(
                            value = textFieldValue,
                            onValueChange = { newValue ->
                                val digitsOnly = newValue.text.filter { it.isDigit() }.take(15)
                                textFieldValue = newValue.copy(text = digitsOnly)
                                handleTextUpdate(digitsOnly)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester)
                                .onFocusChanged { isInputFocused = it.isFocused }
                                .testTag("phone_number_text_field"),
                            textStyle = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    handleAttemptSubmit()
                                }
                            ),
                            cursorBrush = SolidColor(TelegramBlue)
                        )
                    }

                    // Trailing Status / Clear Button
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isNumberValid && validationError == null) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Valid Number",
                                tint = SecretChatGreen,
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(end = 4.dp)
                            )
                        }

                        if (phoneNumber.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    handleTextUpdate("")
                                    focusRequester.requestFocus()
                                },
                                modifier = Modifier
                                    .size(28.dp)
                                    .testTag("clear_phone_number_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Validation Error Banner with smooth animation
            AnimatedVisibility(
                visible = validationError != null,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp, start = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = validationError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Digit counter & guidance when typing
            if (validationError == null && phoneNumber.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, end = 4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "${phoneNumber.length} digits",
                        fontSize = 12.sp,
                        color = if (isNumberValid) SecretChatGreen else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Arrow Action Button (Floating Blue Circle with Right Arrow)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                FloatingActionButton(
                    onClick = { handleAttemptSubmit() },
                    containerColor = if (isNumberValid) TelegramBlue else TelegramBlue.copy(alpha = 0.7f),
                    contentColor = Color.White,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(4.dp),
                    modifier = Modifier
                        .size(56.dp)
                        .testTag("submit_phone_fab")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Submit Phone Number",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            // On-screen numeric keypad (fully linked to state)
            TelegramKeypad(
                onDigitClick = { digit ->
                    if (phoneNumber.length < 15) {
                        handleTextUpdate(phoneNumber + digit)
                    }
                },
                onDeleteClick = {
                    if (phoneNumber.isNotEmpty()) {
                        handleTextUpdate(phoneNumber.dropLast(1))
                    }
                },
                onClearAll = {
                    handleTextUpdate("")
                }
            )

            Spacer(modifier = Modifier.height(10.dp))
        }

        // Call Log / SMS Automatic Verification Permission Dialog
        if (showPermissionDialog) {
            Dialog(onDismissRequest = { showPermissionDialog = false }) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("permission_dialog")
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Blue Header with Telegram verification icon
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .background(TelegramBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            TelegramCallVerificationIcon(modifier = Modifier.size(72.dp))
                        }

                        // Dialog Content
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Text(
                                text = "Please allow Bluemoon to send and read the verification SMS code to automatically sign you in securely.",
                                fontSize = 15.sp,
                                lineHeight = 22.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = {
                                        showPermissionDialog = false
                                        onSubmitPhoneNumber()
                                    },
                                    modifier = Modifier.testTag("permission_continue_btn")
                                ) {
                                    Text(
                                        text = "Continue",
                                        color = TelegramBlue,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TelegramCallVerificationIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Circle icon with plane on left
        drawCircle(
            color = Color.White,
            radius = w * 0.22f,
            center = Offset(w * 0.32f, h * 0.5f)
        )
        // Draw tiny paper plane inside circle
        val planeBody = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.22f, h * 0.50f)
            lineTo(w * 0.40f, h * 0.40f)
            lineTo(w * 0.35f, h * 0.60f)
            lineTo(w * 0.28f, h * 0.54f)
            close()
        }
        drawPath(planeBody, TelegramBlue)

        // Dotted verification code pill on right
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(w * 0.48f, h * 0.42f),
            size = Size(w * 0.44f, h * 0.16f),
            cornerRadius = CornerRadius(16f, 16f)
        )

        // 4 dots inside the pill
        for (i in 0..3) {
            drawCircle(
                color = TelegramBlue,
                radius = 4f,
                center = Offset(w * (0.54f + i * 0.09f), h * 0.50f)
            )
        }

        // Horizontal lines above and below
        drawLine(
            color = Color.White.copy(alpha = 0.7f),
            start = Offset(w * 0.40f, h * 0.30f),
            end = Offset(w * 0.72f, h * 0.30f),
            strokeWidth = 5f
        )
        drawLine(
            color = Color.White.copy(alpha = 0.7f),
            start = Offset(w * 0.40f, h * 0.70f),
            end = Offset(w * 0.65f, h * 0.70f),
            strokeWidth = 5f
        )
    }
}
