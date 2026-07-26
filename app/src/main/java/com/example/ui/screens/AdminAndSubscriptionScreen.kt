package com.example.ui.screens

import java.util.Locale
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NyraAmber
import com.example.ui.theme.NyraCardBorder
import com.example.ui.theme.NyraCyan
import com.example.ui.theme.NyraEmerald
import com.example.ui.theme.NyraGlassBorder
import com.example.ui.theme.NyraPurple
import com.example.ui.theme.NyraRose

data class SubscriptionPlan(
    val id: String,
    val name: String,
    val price: String,
    val duration: String,
    val features: List<String>,
    val isPopular: Boolean = false
)

data class PaymentRequest(
    val id: String,
    val userName: String,
    val userEmail: String,
    val planName: String,
    val amount: String,
    val transactionId: String,
    val date: String,
    var status: String // "Pending", "Approved", "Rejected"
)

data class ManagedUser(
    val id: String,
    val name: String,
    val email: String,
    val plan: String,
    val deviceModel: String,
    val appVersion: String,
    var isBlocked: Boolean
)

@Composable
fun AdminAndSubscriptionScreen(
    modifier: Modifier = Modifier,
    isUserBanned: Boolean = false,
    onUnblockActiveUser: (() -> Unit)? = null
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: User Subscription & QR, 1: Admin Dashboard & Controls
    var isAdminAuthenticated by remember { mutableStateOf(false) }
    var adminEmailInput by remember { mutableStateOf("vivekgoswamirk@gmail.com") }
    var adminPinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }

    // Mock Mutable State for Subscription Plans
    var subscriptionPlans by remember {
        mutableStateOf(
            listOf(
                SubscriptionPlan(
                    id = "free",
                    name = "Free Plan",
                    price = "₹0 / mo",
                    duration = "Lifetime",
                    features = listOf("Basic Voice & Text Chat", "Open System Apps", "Standard Speed Engine", "Community Support"),
                    isPopular = false
                ),
                SubscriptionPlan(
                    id = "premium_pro",
                    name = "Nyra Pro Boss",
                    price = "₹299 / mo",
                    duration = "30 Days",
                    features = listOf("Unlimited Ultra-Fast AI Voice", "Custom Cute Female Voice Pitch", "Unlimited App Launching", "Financial Safety Shield", "Priority VIP Engine", "No Ads or Delays"),
                    isPopular = true
                )
            )
        )
    }

    // Mock Mutable QR Payment Info
    var upiId by remember { mutableStateOf("nyra.ai@upi") }
    var bankName by remember { mutableStateOf("HDFC Bank AI Business") }

    // Mock Payment Requests
    var paymentRequests by remember {
        mutableStateOf(
            listOf(
                PaymentRequest("REQ-101", "Rahul Sharma", "rahul@gmail.com", "Nyra Pro Boss", "₹299", "UTR9832145521", "Just now", "Pending"),
                PaymentRequest("REQ-102", "Priya Patel", "priya@gmail.com", "Nyra Pro Boss", "₹299", "UTR8821943012", "10 mins ago", "Pending"),
                PaymentRequest("REQ-103", "Amit Kumar", "amit@gmail.com", "Nyra Pro Boss", "₹299", "UTR7710293811", "1 hour ago", "Approved")
            )
        )
    }

    // Mock Managed Users
    var usersList by remember {
        mutableStateOf(
            listOf(
                ManagedUser("U101", "Rahul Sharma", "rahul@gmail.com", "Pro Boss", "Pixel 8 Pro", "v2.4.0", false),
                ManagedUser("U102", "Priya Patel", "priya@gmail.com", "Pro Boss", "Samsung S24 Ultra", "v2.4.0", false),
                ManagedUser("U103", "Vikram Singh", "vikram@gmail.com", "Free Plan", "OnePlus 12", "v2.3.9", false),
                ManagedUser("U104", "Spam User", "spammer@bot.com", "Free Plan", "Emulator", "v1.0.0", true)
            )
        )
    }

    var showEditPlanDialog by remember { mutableStateOf<SubscriptionPlan?>(null) }
    var showQrEditDialog by remember { mutableStateOf(false) }
    var showAnnouncementDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF05050D))
    ) {
        // Tab Header
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFF0C0C18),
            contentColor = NyraCyan,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = NyraCyan
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Plans & Payment", fontWeight = FontWeight.Bold)
                    }
                },
                selectedContentColor = NyraCyan,
                unselectedContentColor = Color.Gray,
                modifier = Modifier.testTag("tab_user_subscription")
            )

            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Admin System", fontWeight = FontWeight.Bold)
                    }
                },
                selectedContentColor = NyraPurple,
                unselectedContentColor = Color.Gray,
                modifier = Modifier.testTag("tab_admin_panel")
            )
        }

        if (selectedTab == 0) {
            // User View: Plans & QR Payment Submission
            UserSubscriptionView(
                plans = subscriptionPlans,
                upiId = upiId,
                bankName = bankName,
                onSubmitPayment = { txnId, planName ->
                    paymentRequests = listOf(
                        PaymentRequest("REQ-${System.currentTimeMillis() % 10000}", "Current User (You)", "user@nyra.app", planName, "₹299", txnId, "Just now", "Pending")
                    ) + paymentRequests
                }
            )
        } else {
            // Admin View: Check Auth first
            if (!isAdminAuthenticated) {
                AdminAuthCard(
                    adminEmailInput = adminEmailInput,
                    adminPinInput = adminPinInput,
                    pinError = pinError,
                    emailError = emailError,
                    onEmailChange = {
                        adminEmailInput = it
                        emailError = false
                    },
                    onPinChange = {
                        adminPinInput = it
                        pinError = false
                    },
                    onLoginClick = {
                        val inputEmail = adminEmailInput.trim().lowercase(Locale.getDefault())
                        if (inputEmail == "vivekgoswamirk@gmail.com") {
                            if (adminPinInput == "1234" || adminPinInput == "admin") {
                                isAdminAuthenticated = true
                            } else {
                                pinError = true
                            }
                        } else {
                            emailError = true
                        }
                    }
                )
            } else {
                AdminDashboardView(
                    usersList = usersList,
                    paymentRequests = paymentRequests,
                    subscriptionPlans = subscriptionPlans,
                    upiId = upiId,
                    bankName = bankName,
                    isUserBanned = isUserBanned,
                    onUnblockActiveUser = onUnblockActiveUser,
                    onApproveRequest = { reqId ->
                        paymentRequests = paymentRequests.map {
                            if (it.id == reqId) it.copy(status = "Approved") else it
                        }
                    },
                    onRejectRequest = { reqId ->
                        paymentRequests = paymentRequests.map {
                            if (it.id == reqId) it.copy(status = "Rejected") else it
                        }
                    },
                    onToggleBlockUser = { userId ->
                        usersList = usersList.map {
                            if (it.id == userId) it.copy(isBlocked = !it.isBlocked) else it
                        }
                    },
                    onEditPlan = { plan -> showEditPlanDialog = plan },
                    onEditQr = { showQrEditDialog = true },
                    onSendAnnouncement = { showAnnouncementDialog = true }
                )
            }
        }
    }

    // Dialogs
    showEditPlanDialog?.let { plan ->
        var planName by remember { mutableStateOf(plan.name) }
        var planPrice by remember { mutableStateOf(plan.price) }
        var planDuration by remember { mutableStateOf(plan.duration) }

        AlertDialog(
            onDismissRequest = { showEditPlanDialog = null },
            title = { Text("Edit Plan: ${plan.name}", color = Color.White) },
            containerColor = Color(0xFF101020),
            text = {
                Column {
                    OutlinedTextField(
                        value = planName,
                        onValueChange = { planName = it },
                        label = { Text("Plan Name") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NyraCyan, unfocusedTextColor = Color.White, focusedTextColor = Color.White)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = planPrice,
                        onValueChange = { planPrice = it },
                        label = { Text("Price") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NyraCyan, unfocusedTextColor = Color.White, focusedTextColor = Color.White)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = planDuration,
                        onValueChange = { planDuration = it },
                        label = { Text("Duration") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NyraCyan, unfocusedTextColor = Color.White, focusedTextColor = Color.White)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        subscriptionPlans = subscriptionPlans.map {
                            if (it.id == plan.id) it.copy(name = planName, price = planPrice, duration = planDuration) else it
                        }
                        showEditPlanDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NyraCyan, contentColor = Color.Black)
                ) {
                    Text("Save Plan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditPlanDialog = null }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    if (showQrEditDialog) {
        var newUpi by remember { mutableStateOf(upiId) }
        var newBank by remember { mutableStateOf(bankName) }

        AlertDialog(
            onDismissRequest = { showQrEditDialog = false },
            title = { Text("Update QR Payment Info", color = Color.White) },
            containerColor = Color(0xFF101020),
            text = {
                Column {
                    OutlinedTextField(
                        value = newUpi,
                        onValueChange = { newUpi = it },
                        label = { Text("Admin UPI ID") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NyraCyan, unfocusedTextColor = Color.White, focusedTextColor = Color.White)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newBank,
                        onValueChange = { newBank = it },
                        label = { Text("Bank / Business Name") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NyraCyan, unfocusedTextColor = Color.White, focusedTextColor = Color.White)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        upiId = newUpi
                        bankName = newBank
                        showQrEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NyraPurple, contentColor = Color.White)
                ) {
                    Text("Update QR Details")
                }
            },
            dismissButton = {
                TextButton(onClick = { showQrEditDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun UserSubscriptionView(
    plans: List<SubscriptionPlan>,
    upiId: String,
    bankName: String,
    onSubmitPayment: (String, String) -> Unit
) {
    var userTxnId by remember { mutableStateOf("") }
    var selectedPlanForPay by remember { mutableStateOf(plans.firstOrNull { it.isPopular }?.name ?: "Nyra Pro Boss") }
    var paymentSubmittedMsg by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF101026)),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(NyraCyan, NyraPurple))),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Upgrade to Nyra Pro Boss 👑",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Unlock cute Indian female AI voice, unlimited app launches & zero limits.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFCBD5E1)
                    )
                }
            }
        }

        // Plans Row
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                plans.forEach { plan ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = if (plan.isPopular) Color(0xFF1A183B) else Color(0xFF10101E)),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(
                                if (plan.isPopular) listOf(NyraCyan, NyraPurple) else listOf(NyraCardBorder, NyraCardBorder)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = plan.name, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                                if (plan.isPopular) {
                                    Surface(shape = RoundedCornerShape(12.dp), color = NyraPurple) {
                                        Text("MOST POPULAR", style = MaterialTheme.typography.labelSmall, color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "${plan.price} (${plan.duration})", style = MaterialTheme.typography.titleLarge, color = NyraCyan, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(10.dp))
                            plan.features.forEach { feat ->
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NyraEmerald, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(feat, style = MaterialTheme.typography.bodyMedium, color = Color(0xFFE2E8F0))
                                }
                            }
                        }
                    }
                }
            }
        }

        // QR Payment Section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0D1A)),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(NyraGlassBorder, NyraGlassBorder))),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.QrCode, contentDescription = null, tint = NyraCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Active UPI QR Payment", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // QR Visual Mock Card
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = "QR Code", tint = Color.Black, modifier = Modifier.size(110.dp))
                            Text("Scan to Pay $upiId", style = MaterialTheme.typography.labelSmall, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("UPI ID: $upiId", style = MaterialTheme.typography.bodyMedium, color = NyraCyan, fontWeight = FontWeight.Bold)
                    Text("Payee: $bankName", style = MaterialTheme.typography.labelMedium, color = Color.Gray)

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = userTxnId,
                        onValueChange = { userTxnId = it },
                        placeholder = { Text("Enter UTR / Transaction Ref ID (e.g. UTR982310)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NyraCyan,
                            unfocusedBorderColor = NyraCardBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (userTxnId.isNotBlank()) {
                                onSubmitPayment(userTxnId, selectedPlanForPay)
                                paymentSubmittedMsg = true
                                userTxnId = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = NyraCyan, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Submit Payment for Verification", fontWeight = FontWeight.Bold)
                    }

                    if (paymentSubmittedMsg) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Payment Request Submitted! Admin will approve shortly.", color = NyraEmerald, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminAuthCard(
    adminEmailInput: String,
    adminPinInput: String,
    pinError: Boolean,
    emailError: Boolean,
    onEmailChange: (String) -> Unit,
    onPinChange: (String) -> Unit,
    onLoginClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF101026)),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(NyraPurple, NyraCyan))),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = NyraPurple, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("Admin System Access", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                Text("🔒 Exclusively restricted to vivekgoswamirk@gmail.com", style = MaterialTheme.typography.bodySmall, color = NyraCyan, fontWeight = FontWeight.SemiBold)

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = adminEmailInput,
                    onValueChange = onEmailChange,
                    label = { Text("Admin Gmail Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NyraCyan,
                        unfocusedBorderColor = NyraCardBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                if (emailError) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("❌ Access Denied! Admin Panel is strictly locked to vivekgoswamirk@gmail.com", color = NyraRose, style = MaterialTheme.typography.labelSmall)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = adminPinInput,
                    onValueChange = onPinChange,
                    label = { Text("Admin PIN (1234)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NyraPurple,
                        unfocusedBorderColor = NyraCardBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                if (pinError) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Invalid Admin PIN. Use '1234'", color = NyraRose, style = MaterialTheme.typography.labelSmall)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onLoginClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NyraPurple, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Authenticate Admin", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AdminDashboardView(
    usersList: List<ManagedUser>,
    paymentRequests: List<PaymentRequest>,
    subscriptionPlans: List<SubscriptionPlan>,
    upiId: String,
    bankName: String,
    isUserBanned: Boolean = false,
    onUnblockActiveUser: (() -> Unit)? = null,
    onApproveRequest: (String) -> Unit,
    onRejectRequest: (String) -> Unit,
    onToggleBlockUser: (String) -> Unit,
    onEditPlan: (SubscriptionPlan) -> Unit,
    onEditQr: () -> Unit,
    onSendAnnouncement: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Moderation & Unblock System Control
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = if (isUserBanned) Color(0xFF320E15) else Color(0xFF0F2218)),
                border = BorderStroke(1.dp, if (isUserBanned) NyraRose else NyraEmerald),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("🛡️ Anti-Abuse System Control", style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                if (isUserBanned) "🚫 Current App User: BANNED for Abusive Language!" else "✅ Current App User: CLEAN / UNBLOCKED",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isUserBanned) NyraRose else NyraEmerald,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (isUserBanned && onUnblockActiveUser != null) {
                            Button(
                                onClick = onUnblockActiveUser,
                                colors = ButtonDefaults.buttonColors(containerColor = NyraEmerald, contentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("🔓 Unblock User", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
        // Admin Stats Overview Grid
        item {
            Text("Admin Analytics & Revenue", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AdminStatTile("Total Users", "${usersList.size + 14250}", NyraCyan, Modifier.weight(1f))
                AdminStatTile("Active Today", "9,840", NyraEmerald, Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AdminStatTile("Premium Subs", "1,240", NyraPurple, Modifier.weight(1f))
                AdminStatTile("Monthly Revenue", "₹3,70,760", NyraAmber, Modifier.weight(1f))
            }
        }

        // Action Buttons Row
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onEditQr,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1C38)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.QrCode, contentDescription = null, tint = NyraCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Manage QR Code", color = Color.White, fontSize = 12.sp)
                }

                Button(
                    onClick = onSendAnnouncement,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1C38)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = NyraPurple, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Announcement", color = Color.White, fontSize = 12.sp)
                }
            }
        }

        // Subscription Plans Control
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF101020)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Edit Subscription Plans", style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    subscriptionPlans.forEach { plan ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(plan.name, style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Bold)
                                Text("${plan.price} • ${plan.duration}", style = MaterialTheme.typography.labelSmall, color = NyraCyan)
                            }
                            IconButton(onClick = { onEditPlan(plan) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = NyraCyan)
                            }
                        }
                    }
                }
            }
        }

        // Pending Payment Approval System
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF101020)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("QR Subscription Requests", style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    paymentRequests.forEach { req ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF181830)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(req.userName, style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Bold)
                                    Text(req.status, style = MaterialTheme.typography.labelSmall, color = if (req.status == "Approved") NyraEmerald else if (req.status == "Rejected") NyraRose else NyraAmber)
                                }
                                Text("Plan: ${req.planName} (${req.amount}) • UTR: ${req.transactionId}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)

                                if (req.status == "Pending") {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = { onApproveRequest(req.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = NyraEmerald, contentColor = Color.Black),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Approve Sub")
                                        }
                                        Button(
                                            onClick = { onRejectRequest(req.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = NyraRose, contentColor = Color.White),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Reject")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // User Management System (Block / Unblock)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF101020)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("User Management & Device Control", style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    usersList.forEach { user ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(user.name, style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Bold)
                                Text("${user.email} • ${user.deviceModel} (${user.appVersion})", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                            Button(
                                onClick = { onToggleBlockUser(user.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = if (user.isBlocked) NyraEmerald else NyraRose),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(if (user.isBlocked) "Unblock" else "Block", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminStatTile(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121226)),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(color.copy(alpha = 0.5f), color.copy(alpha = 0.2f)))),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.Bold)
        }
    }
}
