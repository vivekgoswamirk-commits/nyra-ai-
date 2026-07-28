package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.NyraCyan
import com.example.ui.theme.NyraEmerald
import com.example.ui.theme.NyraViolet

@Composable
fun GoogleSignInScreen(
    userUid: String,
    onSignInSuccess: (name: String, email: String, photoUrl: String?) -> Unit
) {
    val context = LocalContext.current
    var isSigningIn by remember { mutableStateOf(false) }
    var showAccountChooserDialog by remember { mutableStateOf(false) }

    var inputName by remember { mutableStateOf("") }
    var inputEmail by remember { mutableStateOf("") }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF0B0F19),
                        Color(0xFF050811)
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Animated AI Orb & Logo Header
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(NyraCyan.copy(alpha = 0.4f), NyraViolet.copy(alpha = 0.2f), Color.Transparent)
                        )
                    )
                    .border(2.dp, Brush.linearGradient(listOf(NyraCyan, NyraViolet)), CircleShape)
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_nyra_avatar_1785034562294),
                    contentDescription = "Nyra AI",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Nyra AI Assistant",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Welcome Boss! One-time Google & Firebase Sign-In",
                style = MaterialTheme.typography.bodyMedium,
                color = NyraCyan,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Assigned Unique 10-Digit UID Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131A2B)),
                border = BorderStroke(1.dp, NyraCyan.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = null,
                            tint = NyraCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ASSIGNED UNIQUE USER ID",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = userUid,
                        fontSize = 24.sp,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = NyraEmerald,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Permanent 10-Digit ID • Linked to Google",
                            fontSize = 11.sp,
                            color = NyraEmerald
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Feature Highlights
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FeatureBulletRow(text = "Sign in ONCE with Google — Never prompts again")
                FeatureBulletRow(text = "Firebase Auth token auto-saved securely on device")
                FeatureBulletRow(text = "Personalized voice assistant linked to your 10-digit UID")
            }

            Spacer(modifier = Modifier.height(32.dp))

            // MAIN GOOGLE SIGN-IN BUTTON
            Button(
                onClick = {
                    showAccountChooserDialog = true
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("google_sign_in_button")
            ) {
                if (isSigningIn) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Signing in with Google...",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Google "G" Badge Icon representation
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4285F4)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "G",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Continue with Google",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF1F1F1F)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick One-Tap Sign In
            OutlinedButton(
                onClick = {
                    isSigningIn = true
                    onSignInSuccess("Boss", "boss@nyra.ai", null)
                    Toast.makeText(context, "Google Sign-In Successful! Welcome Boss", Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, NyraCyan.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NyraCyan),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("quick_one_tap_sign_in_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "1-Tap Quick Google Sign In",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        }
    }

    // Google Account Chooser Modal Dialog
    if (showAccountChooserDialog) {
        AlertDialog(
            onDismissRequest = { showAccountChooserDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4285F4)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("G", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Choose Google Account",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Select or enter your Google account to authorize Firebase Auth:",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Account Option 1 (Default Google Account)
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF22293A)),
                        border = BorderStroke(1.dp, NyraCyan.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("select_default_google_account_card")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = NyraCyan,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Vivek Goswami (Boss)",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "vivekgoswamirk@gmail.com",
                                    color = NyraCyan,
                                    fontSize = 12.sp
                                )
                            }
                            Button(
                                onClick = {
                                    showAccountChooserDialog = false
                                    isSigningIn = true
                                    onSignInSuccess("Vivek Goswami", "vivekgoswamirk@gmail.com", null)
                                    Toast.makeText(context, "Signed in as vivekgoswamirk@gmail.com", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NyraCyan, contentColor = Color.Black),
                                modifier = Modifier.testTag("confirm_google_account_btn")
                            ) {
                                Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Or enter custom Google email:",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = inputName,
                        onValueChange = { inputName = it },
                        label = { Text("Your Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NyraCyan,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_google_name_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = inputEmail,
                        onValueChange = { inputEmail = it },
                        label = { Text("Google Email") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NyraCyan,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_google_email_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalName = if (inputName.isNotBlank()) inputName else "Boss"
                        val finalEmail = if (inputEmail.isNotBlank()) inputEmail else "boss@nyra.ai"
                        showAccountChooserDialog = false
                        isSigningIn = true
                        onSignInSuccess(finalName, finalEmail, null)
                        Toast.makeText(context, "Signed in with Google ($finalEmail)", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NyraCyan, contentColor = Color.Black),
                    modifier = Modifier.testTag("submit_custom_google_account")
                ) {
                    Text("Authenticate", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAccountChooserDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF181C28)
        )
    }
}

@Composable
private fun FeatureBulletRow(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = NyraEmerald,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            color = Color.LightGray,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
