package com.example.clutch_ai.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.clutch_ai.R
import com.example.clutch_ai.ui.theme.*

@Composable
fun AuthScreen(
    isLoading: Boolean,
    errorMessage: String?,
    onSignUp: (fullName: String, email: String, password: String) -> Unit,
    onSignIn: (email: String, password: String) -> Unit,
    onContinueAsGuest: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isSignUpMode by remember { mutableStateOf(true) }
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CanvasBackground)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Reactor Logo Icon
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(MidnightChassis)
                    .border(2.dp, ElectricCyan.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.clutch_gear_icon),
                    contentDescription = "Clutch Reactor",
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Brand Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "CLUTCH",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = MidnightChassis,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = ".AI",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = ElectricCyanDark,
                    fontFamily = FontFamily.Monospace
                )
            }

            Text(
                text = "Personal Intelligence & Knowledge Vault",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Main Auth Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(22.dp))
                    .padding(20.dp)
            ) {
                Column {
                    // Mode Toggle: Sign Up vs Sign In
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSignUpMode) MidnightChassis else Color.Transparent)
                                .clickable { isSignUpMode = true }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Create Account",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSignUpMode) Color.White else TextMuted
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (!isSignUpMode) MidnightChassis else Color.Transparent)
                                .clickable { isSignUpMode = false }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Sign In",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (!isSignUpMode) Color.White else TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Error Banner
                    AnimatedVisibility(visible = errorMessage != null, enter = fadeIn(), exit = fadeOut()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(StatusRose.copy(alpha = 0.12f))
                                .border(1.dp, StatusRose.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = errorMessage ?: "",
                                color = StatusRose,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Full Name (Only in Sign Up Mode)
                    if (isSignUpMode) {
                        Text(
                            text = "FULL NAME",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MidnightChassis,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
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
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Email Field
                    Text(
                        text = "EMAIL ADDRESS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MidnightChassis,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
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

                    Spacer(modifier = Modifier.height(14.dp))

                    // Password Field
                    Text(
                        text = "PASSWORD",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MidnightChassis,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
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

                    Spacer(modifier = Modifier.height(22.dp))

                    // Submit Button
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
                            .height(48.dp),
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

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick bypass for returning/exploring users
                    androidx.compose.material3.TextButton(
                        onClick = onContinueAsGuest,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Continue to Dashboard →",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ElectricCyanDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Privacy Assurance Note
            Text(
                text = "Your conversations and knowledge base remain strictly private to your authenticated account.",
                fontSize = 11.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }
}
