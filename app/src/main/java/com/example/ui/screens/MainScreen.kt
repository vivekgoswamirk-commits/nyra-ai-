package com.example.ui.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.MainViewModel
import com.example.ui.theme.NyraAmber
import com.example.ui.theme.NyraCyan
import com.example.ui.theme.NyraEmerald
import com.example.ui.theme.NyraViolet

import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Person

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val isMuted by viewModel.tts.isMuted.collectAsState()
    val permissions by viewModel.permissions.collectAsState()
    val showApiKeyDialog by viewModel.showApiKeyDialog.collectAsState()
    val showProfileDialog by viewModel.showProfileDialog.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    val missingPermCount = permissions.count { !it.isGranted }

    if (showApiKeyDialog) {
        ApiKeySetupDialog(
            currentKey = viewModel.apiKeyManager.getApiKey(),
            onSaveKey = { key -> viewModel.saveApiKey(key) },
            onDismiss = { viewModel.dismissApiKeyDialog() }
        )
    }

    if (showProfileDialog) {
        UserProfileDialog(
            profile = userProfile,
            onSaveProfile = { name, email -> viewModel.updateUserProfile(name, email) },
            onDismiss = { viewModel.dismissProfileDialog() }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { viewModel.openProfileDialog() }
                            .testTag("topbar_user_profile_header")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(NyraViolet.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_nyra_avatar_1785034562294),
                                contentDescription = "Nyra",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Nyra AI",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = NyraEmerald.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "UID: ${userProfile.uid}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NyraEmerald,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "${userProfile.userName} • Tap for Profile",
                                style = MaterialTheme.typography.labelSmall,
                                color = NyraCyan
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.openProfileDialog() },
                        modifier = Modifier.testTag("open_user_profile_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "User Profile",
                            tint = NyraCyan
                        )
                    }

                    if (missingPermCount > 0) {
                        IconButton(
                            onClick = { viewModel.selectTab(1) },
                            modifier = Modifier.testTag("perm_alert_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    Badge(containerColor = NyraAmber) {
                                        Text(text = "$missingPermCount", color = Color.Black)
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Permissions missing",
                                    tint = NyraAmber
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = { viewModel.openApiKeyDialog() },
                        modifier = Modifier.testTag("open_api_key_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = "Gemini API Key Settings",
                            tint = if (viewModel.apiKeyManager.hasApiKey()) NyraCyan else NyraAmber
                        )
                    }

                    IconButton(
                        onClick = { viewModel.toggleMuteTts() },
                        modifier = Modifier.testTag("mute_tts_button")
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = if (isMuted) "Unmute Speech" else "Mute Speech",
                            tint = if (isMuted) Color.Gray else NyraCyan
                        )
                    }

                    IconButton(
                        onClick = { viewModel.clearHistory() },
                        modifier = Modifier.testTag("clear_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear History",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    icon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = "Assistant Chat"
                        )
                    },
                    label = { Text("Chat") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = NyraCyan,
                        indicatorColor = NyraCyan
                    ),
                    modifier = Modifier.testTag("nav_tab_chat")
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (missingPermCount > 0) {
                                    Badge(containerColor = NyraAmber) {
                                        Text(text = "!")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Permissions Setup"
                            )
                        }
                    },
                    label = { Text("Setup") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = NyraCyan,
                        indicatorColor = NyraCyan
                    ),
                    modifier = Modifier.testTag("nav_tab_setup")
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { viewModel.selectTab(2) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Apps,
                            contentDescription = "Installed Apps"
                        )
                    },
                    label = { Text("Apps") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = NyraCyan,
                        indicatorColor = NyraCyan
                    ),
                    modifier = Modifier.testTag("nav_tab_apps")
                )

                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { viewModel.selectTab(3) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Safety Policy"
                        )
                    },
                    label = { Text("Safety") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = NyraCyan,
                        indicatorColor = NyraCyan
                    ),
                    modifier = Modifier.testTag("nav_tab_safety")
                )

                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { viewModel.selectTab(4) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Admin & Pro"
                        )
                    },
                    label = { Text("Admin") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = NyraCyan,
                        indicatorColor = NyraCyan
                    ),
                    modifier = Modifier.testTag("nav_tab_admin")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> ChatScreen(viewModel = viewModel)
                1 -> PermissionsScreen(viewModel = viewModel)
                2 -> InstalledAppsScreen(viewModel = viewModel)
                3 -> SafetyPolicyScreen()
                4 -> {
                    val isBanned by viewModel.isUserBanned.collectAsStateWithLifecycle()
                    AdminAndSubscriptionScreen(
                        isUserBanned = isBanned,
                        onUnblockActiveUser = { viewModel.unblockUserByAdmin() }
                    )
                }
            }
        }
    }
}

@Composable
fun ApiKeySetupDialog(
    currentKey: String,
    onSaveKey: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var keyText by remember { mutableStateOf(currentKey) }
    var passwordVisible by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.VpnKey,
                    contentDescription = null,
                    tint = NyraCyan
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Gemini API Key Setup", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column {
                Text(
                    text = "Boss, enter your Gemini API key below to activate AI features. It will be saved securely on your device and will NOT be asked again!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = keyText,
                    onValueChange = { keyText = it },
                    label = { Text("Gemini API Key (AIzaSy...)") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle key visibility",
                                tint = NyraCyan
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("api_key_input_field")
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = {
                        try {
                            uriHandler.openUri("https://aistudio.google.com/app/apikey")
                        } catch (e: Exception) {
                            // ignore
                        }
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("🔑 Get Free Key from Google AI Studio", color = NyraCyan, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (keyText.isNotBlank()) {
                        onSaveKey(keyText.trim())
                    }
                },
                enabled = keyText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = NyraCyan, contentColor = Color.Black)
            ) {
                Text("Save Key", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Later", color = Color.Gray)
            }
        },
        containerColor = Color(0xFF1E1E2E)
    )
}
