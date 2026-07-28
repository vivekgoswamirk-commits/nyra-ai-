package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.UserProfile
import com.example.ui.theme.NyraCyan
import com.example.ui.theme.NyraEmerald
import com.example.ui.theme.NyraViolet

@Composable
fun UserProfileDialog(
    profile: UserProfile,
    onSaveProfile: (String, String) -> Unit,
    onSignOut: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isEditing by remember { mutableStateOf(false) }
    var nameText by remember { mutableStateOf(profile.userName) }
    var emailText by remember { mutableStateOf(profile.userEmail) }
    var copiedUid by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = NyraCyan
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "User Profile",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = NyraEmerald.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, NyraEmerald.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = NyraEmerald,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "ACTIVE",
                            color = NyraEmerald,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar Header
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(NyraViolet.copy(alpha = 0.3f))
                        .padding(3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_nyra_avatar_1785034562294),
                        contentDescription = "User Profile Avatar",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // User Title & Role
                Text(
                    text = profile.userName,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = profile.accountTier,
                    style = MaterialTheme.typography.bodySmall,
                    color = NyraCyan
                )

                Spacer(modifier = Modifier.height(16.dp))

                // UNIQUE 10-DIGIT USER UID CARD
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131722)),
                    border = BorderStroke(1.dp, NyraCyan.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = null,
                                tint = NyraCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "UNIQUE USER ID (10-DIGIT UID)",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = profile.uid,
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                modifier = Modifier.testTag("user_uid_display_text")
                            )

                            Button(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("User UID", profile.uid)
                                    clipboard.setPrimaryClip(clip)
                                    copiedUid = true
                                    Toast.makeText(context, "UID Copied: ${profile.uid}", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (copiedUid) NyraEmerald else NyraCyan.copy(alpha = 0.2f),
                                    contentColor = if (copiedUid) Color.Black else NyraCyan
                                ),
                                modifier = Modifier.testTag("copy_uid_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (copiedUid) Icons.Default.Check else Icons.Default.ContentCopy,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (copiedUid) "Copied!" else "Copy",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Editable Fields / Details
                if (isEditing) {
                    OutlinedTextField(
                        value = nameText,
                        onValueChange = { nameText = it },
                        label = { Text("Display Name") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Badge, contentDescription = null, tint = NyraCyan)
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NyraCyan,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_profile_name_field")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = emailText,
                        onValueChange = { emailText = it },
                        label = { Text("Email / Contact") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = NyraCyan)
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NyraCyan,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_profile_email_field")
                    )
                } else {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2130)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color.LightGray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Name: ", color = Color.Gray, fontSize = 13.sp)
                                    Text(profile.userName, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                }

                                IconButton(
                                    onClick = { isEditing = true },
                                    modifier = Modifier
                                        .size(28.dp)
                                        .testTag("edit_profile_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Profile",
                                        tint = NyraCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = Color.LightGray,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Email: ", color = Color.Gray, fontSize = 13.sp)
                                Text(profile.userEmail, color = Color.White, fontSize = 14.sp)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = Color.LightGray,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Member Since: ", color = Color.Gray, fontSize = 13.sp)
                                Text(profile.joinedDate, color = Color.White, fontSize = 14.sp)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Auth: ${profile.authProvider}",
                                    color = NyraEmerald,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                TextButton(
                                    onClick = onSignOut,
                                    modifier = Modifier.testTag("user_profile_sign_out_button")
                                ) {
                                    Text(
                                        text = "Sign Out",
                                        color = Color(0xFFEF4444),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (isEditing) {
                Button(
                    onClick = {
                        onSaveProfile(nameText, emailText)
                        isEditing = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NyraCyan, contentColor = Color.Black),
                    modifier = Modifier.testTag("save_profile_button")
                ) {
                    Text("Save Changes", fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = NyraCyan, contentColor = Color.Black),
                    modifier = Modifier.testTag("close_profile_button")
                ) {
                    Text("Done", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            if (isEditing) {
                TextButton(onClick = { isEditing = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        },
        containerColor = Color(0xFF181C28)
    )
}
