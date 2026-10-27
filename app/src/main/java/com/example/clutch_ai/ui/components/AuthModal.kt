package com.example.clutch_ai.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.clutch_ai.ui.theme.*

@Composable
fun AuthModal(
    isLoading: Boolean,
    errorMessage: String?,
    onSignUp: (fullName: String, email: String, password: String) -> Unit,
    onSignIn: (email: String, password: String) -> Unit,
    onDismiss: () -> Unit
) {
    var isSignUpMode by remember { mutableStateOf(false) } // Default to Sign In for clean look
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismiss() }
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color(0xFFFAFCFF))
                    .border(
                        width = 1.5.dp,
                        brush = Brush.verticalGradient(
                            listOf(Color.White, Color(0xFFD1D5DB).copy(alpha = 0.7f))
                        ),
                        shape = RoundedCornerShape(26.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* Consume inside clicks */ }
                    .padding(horizontal = 24.dp, vertical = 22.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Header with Icon & Close
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ClutchReactorLogo(size = 32.dp, showText = false)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (isSignUpMode) "Create Account" else "Welcome Back",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MidnightChassis
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEEF2F6))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isSignUpMode) "Sync your private chat history & documents" else "Sign in to access your saved sessions & knowledge",
                        fontSize = 12.sp,
                        color = TextMuted,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Tactile Clay Mode Switcher
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(11.dp))
                                .background(if (!isSignUpMode) MidnightChassis else Color.Transparent)
                                .clickable { isSignUpMode = false }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Sign In",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (!isSignUpMode) ElectricCyan else TextMuted
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(11.dp))
                                .background(if (isSignUpMode) MidnightChassis else Color.Transparent)
                                .clickable { isSignUpMode = true }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Create Account",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSignUpMode) ElectricCyan else TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Error Banner
                    AnimatedVisibility(visible = !errorMessage.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(StatusRose.copy(alpha = 0.12f))
                                .border(1.dp, StatusRose.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = errorMessage ?: "",
                                color = StatusRose,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Full Name (Only for Sign Up)
                    if (isSignUpMode) {
                        Text(
                            text = "FULL NAME",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MidnightChassis,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            placeholder = { Text("e.g. Ritej Mule", fontSize = 13.sp, color = TextLightSlate) },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricCyanDark,
                                unfocusedBorderColor = Color(0xFFCBD5E1),
                                focusedContainerColor = Color(0xFFF8FAFC),
                                unfocusedContainerColor = Color(0xFFF8FAFC)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Email Field
                    Text(
                        text = "EMAIL ADDRESS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MidnightChassis,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("you@domain.com", fontSize = 13.sp, color = TextLightSlate) },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyanDark,
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Password Field
                    Text(
                        text = "PASSWORD",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MidnightChassis,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("••••••••", fontSize = 13.sp, color = TextLightSlate) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (isSignUpMode) {
                                if (fullName.isNotBlank() && email.isNotBlank() && password.isNotBlank()) {
                                    onSignUp(fullName.trim(), email.trim(), password)
                                }
                            } else {
                                if (email.isNotBlank() && password.isNotBlank()) {
                                    onSignIn(email.trim(), password)
                                }
                            }
                        }),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyanDark,
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Primary Action Button
                    Button(
                        onClick = {
                            if (isSignUpMode) {
                                onSignUp(fullName.trim(), email.trim(), password)
                            } else {
                                onSignIn(email.trim(), password)
                            }
                        },
                        enabled = !isLoading && email.isNotBlank() && password.isNotBlank() && (!isSignUpMode || fullName.isNotBlank()),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MidnightChassis,
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFFCBD5E1)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = ElectricCyan,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (isSignUpMode) "Create Account" else "Sign In",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (email.isNotBlank() && password.isNotBlank() && (!isSignUpMode || fullName.isNotBlank())) ElectricCyan else Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Dismiss / Cancel Button
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextMuted
                        )
                    }
                }
            }
        }
    }
}
