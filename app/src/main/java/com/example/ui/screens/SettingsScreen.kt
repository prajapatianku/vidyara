package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.SaaSPlanType
import com.example.data.model.SaaSPurchaseRecord
import com.example.ui.components.PlanBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.LibraryViewModel
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext

@Composable
fun SettingsScreen(
    viewModel: LibraryViewModel,
    modifier: Modifier = Modifier,
    onOpenSuperAdmin: () -> Unit = {}
) {
    val context = LocalContext.current
    val owner by viewModel.ownerProfile.collectAsState()
    val library by viewModel.library.collectAsState()
    val branches by viewModel.branches.collectAsState()
    val activeBranchId by viewModel.activeBranchId.collectAsState()
    val saasPlan by viewModel.saasSubscription.collectAsState()
    val saasPurchases by viewModel.saasPurchaseHistory.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()
    val showBranchDialog by viewModel.showBranchManagerDialog.collectAsState()
    val isHindi by viewModel.isHindi.collectAsState()

    var showPaymentHistoryDialog by remember { mutableStateOf(false) }
    var selectedInvoiceForPreview by remember { mutableStateOf<SaaSPurchaseRecord?>(null) }
    var showAuditLogsDialog by remember { mutableStateOf(false) }
    var showLibraryProfileDialog by remember { mutableStateOf(false) }
    var showShiftManagerDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }
    var showSupportDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SlateBackground),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Owner Profile Card
        item {
            Surface(
                color = PureWhite,
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(OrangePrimaryContainer)
                                    .border(1.dp, OrangePrimary.copy(alpha = 0.3f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = owner.fullName.take(2).uppercase(),
                                    fontWeight = FontWeight.Black,
                                    color = OrangePrimaryDark,
                                    fontSize = 20.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = owner.fullName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = WarmTextDark
                                )
                                Text(
                                    text = owner.phone,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = WarmTextMuted
                                )
                                Text(
                                    text = owner.email,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = WarmTextMuted
                                )
                            }
                        }

                        PlanBadge(
                            planType = saasPlan.planType,
                            onClick = { viewModel.requestUpgrade("settings") }
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(14.dp)) }

        // Section: SaaS Subscription & Plan Upgrade + Payment History
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyPrimary)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val daysRemaining = viewModel.getSubscriptionDaysRemaining()
                    val isPaidPlan = saasPlan.planType != SaaSPlanType.FREE
                    val canRenew = daysRemaining in 0..7

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Vidyara Subscription",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PureWhite
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = if (saasPlan.planType == SaaSPlanType.FREE) Color(0xFF64748B) else Color(0xFF16A34A),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = saasPlan.planType.displayName.uppercase(),
                                        color = PureWhite,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            val validityText = if (saasPlan.planType == SaaSPlanType.FREE) {
                                "Free Tier • Up to 20 Students"
                            } else if (daysRemaining >= 0) {
                                "Valid until ${saasPlan.endDate} ($daysRemaining days remaining)"
                            } else {
                                "Valid until ${saasPlan.endDate}"
                            }
                            Text(
                                text = validityText,
                                style = MaterialTheme.typography.bodySmall,
                                color = PureWhite.copy(alpha = 0.85f),
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!isPaidPlan) {
                            Button(
                                onClick = { viewModel.requestUpgrade("settings") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                            ) {
                                Icon(imageVector = Icons.Default.Star, contentDescription = null, modifier = Modifier.size(14.dp), tint = PureWhite)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Upgrade Plan", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                            }
                        } else if (canRenew) {
                            Button(
                                onClick = { viewModel.requestUpgrade("settings") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                            ) {
                                Icon(imageVector = Icons.Default.Autorenew, contentDescription = null, modifier = Modifier.size(14.dp), tint = PureWhite)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Renew Plan", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                            }
                        }

                        OutlinedButton(
                            onClick = { showPaymentHistoryDialog = true },
                            modifier = if (isPaidPlan && !canRenew) Modifier.fillMaxWidth() else Modifier.weight(1.15f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.6f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PureWhite)
                        ) {
                            Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(14.dp), tint = PureWhite)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Payment History", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Section: Management Options
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = translate("Library Operations", isHindi),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = WarmTextDark
                )
                Spacer(modifier = Modifier.height(8.dp))

                SettingsItemCard(
                    title = translate("Library Details & Timings", isHindi),
                    subtitle = "${library.name} • ${library.openingTime} - ${library.closingTime}",
                    icon = Icons.Default.Storefront,
                    onClick = { showLibraryProfileDialog = true }
                )

                SettingsItemCard(
                    title = translate("Manage Library Shifts", isHindi),
                    subtitle = "Configure shifts, operating timings & fee rates",
                    icon = Icons.Default.AccessTime,
                    onClick = { showShiftManagerDialog = true }
                )

                SettingsItemCard(
                    title = translate("WhatsApp Fee Due Reminders", isHindi),
                    subtitle = if (viewModel.hasFeature("whatsapp_fee_reminders")) "Active • Automated alerts to owner & students" else "Notify owner on due dates (Plan 2 & 3)",
                    icon = Icons.Default.Chat,
                    isLocked = !viewModel.hasFeature("whatsapp_fee_reminders"),
                    onClick = {
                        if (!viewModel.hasFeature("whatsapp_fee_reminders")) {
                            viewModel.requestUpgrade("whatsapp_fee_reminders")
                        } else {
                            viewModel.showWhatsAppReminderDialog(true)
                        }
                    }
                )

                SettingsItemCard(
                    title = translate("Multi-Branch Management", isHindi),
                    subtitle = "${branches.size} branch registered (Requires Business Plan)",
                    icon = Icons.Default.AccountTree,
                    isLocked = !viewModel.hasFeature("multi_branch"),
                    onClick = { viewModel.showBranchManagerDialog(true) }
                )

                SettingsItemCard(
                    title = translate("Audit & Security Logs", isHindi),
                    subtitle = "Track financial & seat changes (${auditLogs.size} logs)",
                    icon = Icons.Default.History,
                    onClick = { showAuditLogsDialog = true }
                )

                SettingsItemCard(
                    title = translate("Support & Helpdesk", isHindi),
                    subtitle = "Get in touch with call, WhatsApp, or email support",
                    icon = Icons.Default.HelpOutline,
                    onClick = { showSupportDialog = true }
                )

                SettingsItemCard(
                    title = translate("Language", isHindi),
                    subtitle = if (isHindi) "हिन्दी (Hindi)" else "English",
                    icon = Icons.Default.Language,
                    onClick = { showLanguageDialog = true }
                )

                SettingsItemCard(
                    title = translate("Terms & Conditions", isHindi),
                    subtitle = "Terms of Service, Privacy Policy & Usage Rules",
                    icon = Icons.Default.Description,
                    onClick = { showTermsDialog = true }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Section: Account & Session Management (Logout Feature)
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "Account & Session",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = WarmTextDark
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Platform Owner (Super Admin) Portal Item - STRICTLY HIDDEN UNLESS AUTHENTICATED
                val isSuperAdminAuth by viewModel.isSuperAdminAuthenticated.collectAsState()
                if (isSuperAdminAuth) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onOpenSuperAdmin() },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NavyPrimary.copy(alpha = 0.25f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(NavyPrimary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AdminPanelSettings,
                                        contentDescription = "Super Admin",
                                        tint = NavyPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Return to Platform Owner Panel",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = NavyPrimary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(color = AmberTertiary, shape = RoundedCornerShape(4.dp)) {
                                            Text("ADMIN", fontSize = 9.sp, fontWeight = FontWeight.Black, color = NavyPrimary, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                        }
                                    }
                                    Text(
                                        text = "All libraries, pricing, coupons, broadcasts & revenue",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = WarmTextMuted
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = NavyPrimary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { showLogoutConfirmDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFEE2E2)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFFEE2E2)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Logout,
                                    contentDescription = "Log Out",
                                    tint = DangerRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Log Out",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = DangerRed
                                )
                                Text(
                                    text = "Sign out from ${owner.fullName}'s admin session",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = WarmTextMuted
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = DangerRed.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }

    // Logout Confirmation Dialog
    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = null,
                        tint = DangerRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Confirm Logout", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = "Are you sure you want to log out of ${library.name}? You can sign back in anytime using your registered email or phone number.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = WarmTextDark
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmDialog = false
                        viewModel.logout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Log Out", color = PureWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showLogoutConfirmDialog = false }) {
                    Text("Cancel", color = WarmTextDark)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = PureWhite
        )
    }

    // Audit Logs Dialog
    if (showAuditLogsDialog) {
        AuditLogsDialog(
            auditLogs = auditLogs,
            onDismiss = { showAuditLogsDialog = false }
        )
    }

    // Library Profile Dialog
    if (showLibraryProfileDialog) {
        LibraryProfileDialog(
            library = library,
            viewModel = viewModel,
            onDismiss = { showLibraryProfileDialog = false }
        )
    }

    // Shift Manager Dialog
    if (showShiftManagerDialog) {
        ShiftManagerDialog(
            viewModel = viewModel,
            onDismiss = { showShiftManagerDialog = false }
        )
    }

    // Subscription Payment History Dialog
    if (showPaymentHistoryDialog) {
        SaaSPaymentHistoryDialog(
            viewModel = viewModel,
            purchases = saasPurchases,
            onDismiss = { showPaymentHistoryDialog = false },
            onPreviewInvoice = { record ->
                selectedInvoiceForPreview = record
            }
        )
    }

    // Invoice Preview Dialog
    selectedInvoiceForPreview?.let { record ->
        SaaSInvoicePreviewDialog(
            record = record,
            viewModel = viewModel,
            onDismiss = { selectedInvoiceForPreview = null }
        )
    }

    // Support & Helpdesk Dialog
    if (showSupportDialog) {
        SupportDialog(onDismiss = { showSupportDialog = false })
    }

    // Language Selection Dialog
    if (showLanguageDialog) {
        LanguageDialog(
            viewModel = viewModel,
            onDismiss = { showLanguageDialog = false }
        )
    }

    // Terms & Conditions Dialog
    if (showTermsDialog) {
        TermsDialog(
            isHindi = isHindi,
            onDismiss = { showTermsDialog = false }
        )
    }
}

@Composable
fun SupportDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Help & Support",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = WarmTextDark
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = WarmTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "If you face any issues or need help, connect with our support desk:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = WarmTextMuted,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // Call Action Row
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable {
                            try {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:8709489716")
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Fallback
                            }
                        },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Call",
                            tint = NavyPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(text = "Call Us", fontWeight = FontWeight.Bold, color = WarmTextDark)
                            Text(text = "8709489716", style = MaterialTheme.typography.bodySmall, color = WarmTextMuted)
                        }
                    }
                }

                // WhatsApp Action Row
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable {
                            try {
                                val url = "https://api.whatsapp.com/send?phone=918709489716&text=Hello%20Support"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Fallback
                            }
                        },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "WhatsApp",
                            tint = Color(0xFF25D366),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(text = "WhatsApp Support", fontWeight = FontWeight.Bold, color = WarmTextDark)
                            Text(text = "8709489716", style = MaterialTheme.typography.bodySmall, color = WarmTextMuted)
                        }
                    }
                }

                // Email Action Row
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable {
                            try {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:mylibrary@gmail.com")
                                    putExtra(Intent.EXTRA_SUBJECT, "My Library App Support Query")
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Fallback
                            }
                        },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email",
                            tint = OrangePrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(text = "Email Us", fontWeight = FontWeight.Bold, color = WarmTextDark)
                            Text(text = "mylibrary@gmail.com", style = MaterialTheme.typography.bodySmall, color = WarmTextMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LanguageDialog(
    viewModel: LibraryViewModel,
    onDismiss: () -> Unit
) {
    val isHindi by viewModel.isHindi.collectAsState()
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = PureWhite,
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = translate("Select Language", isHindi),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = WarmTextDark
                )
                Spacer(modifier = Modifier.height(16.dp))

                // English Option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            viewModel.setHindiLanguage(false)
                            onDismiss()
                        }
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "English",
                        fontWeight = if (!isHindi) FontWeight.Bold else FontWeight.Normal,
                        color = if (!isHindi) OrangePrimaryDark else WarmTextDark
                    )
                    if (!isHindi) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = OrangePrimary)
                    }
                }

                HorizontalDivider(color = Color(0xFFF1E9E0))

                // Hindi Option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            viewModel.setHindiLanguage(true)
                            onDismiss()
                        }
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "हिन्दी (Hindi)",
                        fontWeight = if (isHindi) FontWeight.Bold else FontWeight.Normal,
                        color = if (isHindi) OrangePrimaryDark else WarmTextDark
                    )
                    if (isHindi) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = OrangePrimary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = if (isHindi) "बंद करें" else "Close", color = WarmTextDark)
                }
            }
        }
    }
}

@Composable
fun TermsDialog(
    isHindi: Boolean,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = PureWhite,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = translate("Terms & Conditions", isHindi),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = WarmTextDark
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = WarmTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (isHindi) {
                        Text(
                            text = """
                                विद्यारा प्लेटफॉर्म में स्वागत है!
                                
                                1. सेवा की शर्तें
                                विद्यारा एक डिजिटल लाइब्रेरी और सीट प्रबंधन सेवा है। इस एप्लिकेशन का उपयोग करके, आप इन शर्तों से सहमत होते हैं।
                                
                                2. उपयोगकर्ता पंजीकरण और सुरक्षा
                                लाइब्रेरी मालिकों को अपना सही विवरण प्रदान करना होगा। अपने खाते की गोपनीयता बनाए रखना आपकी ज़िम्मेदारी है।
                                
                                3. सदस्यता और भुगतान
                                - सभी भुगतान रेज़रपे (Razorpay) के माध्यम से संसाधित किए जाते हैं।
                                - एक महीने के प्लान की गणना 28 दिनों के आधार पर की जाती है।
                                - प्लान समाप्त होने से 1 सप्ताह पहले आप इसे रिन्यू कर सकते हैं।
                                - भुगतान गैर-वापसी योग्य हैं।
                                
                                4. छात्रों का डेटा
                                लाइब्रेरी मालिक अपने छात्रों के डेटा के उपयोग और सुरक्षा के लिए पूर्ण रूप से उत्तरदायी हैं। विद्यारा किसी भी नुकसान के लिए ज़िम्मेदार नहीं है।
                                
                                5. नियमों में बदलाव
                                हम किसी भी समय इन नियमों को संशोधित कर सकते हैं।
                                
                                धन्यवाद,
                                विद्यारा टीम
                            """.trimIndent(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = WarmTextDark,
                            lineHeight = 20.sp
                        )
                    } else {
                        Text(
                            text = """
                                Welcome to the Vidyara Platform!
                                
                                1. Terms of Service
                                Vidyara provides digital library administration and seat-mapping SaaS tools. By accessing or using this app, you agree to comply with these terms.
                                
                                2. User Registration & Security
                                Library owners must provide accurate credentials and contact details during registration. You are solely responsible for maintaining the privacy of your account.
                                
                                3. Subscriptions & Payments
                                - All subscription fees are processed securely via Razorpay.
                                - Plan durations are calculated on a base of 28 days per month.
                                - Plan renewals can be initialized up to 1 week before expiration.
                                - All subscription payments are final and non-refundable.
                                
                                4. Student Data & Privacy
                                Library owners retain sole responsibility for the student data, attendance profiles, and transaction receipts logged onto the workspace database.
                                
                                5. Amendments
                                We reserve the right to modify these terms at any time. Continued use of the app implies consent to updated guidelines.
                                
                                Thank you,
                                The Vidyara Team
                            """.trimIndent(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = WarmTextDark,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text(text = if (isHindi) "सहमत हैं / Close" else "Agree & Close", color = PureWhite)
                }
            }
        }
    }
}

@Composable
fun SettingsItemCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isLocked: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isLocked) AmberTertiaryContainer else NavyPrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isLocked) Icons.Default.Lock else icon,
                        contentDescription = null,
                        tint = if (isLocked) AmberTertiary else NavyPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
fun RegistrationQrDialog(
    library: com.example.data.model.Library,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Registration QR Code", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // QR Graphic Placeholder / Container
                Surface(
                    modifier = Modifier
                        .size(180.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    color = Color.White,
                    shadowElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode2,
                            contentDescription = "Registration QR",
                            modifier = Modifier.size(130.dp),
                            tint = NavyPrimary
                        )
                        Text(text = "Scan to Register", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(text = library.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = "https://my-library.app/register/${library.registrationToken}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share / Print QR Poster")
                }
            }
        }
    }
}

@Composable
fun BranchManagerDialog(
    branches: List<com.example.data.model.Branch>,
    activeBranchId: String,
    viewModel: LibraryViewModel,
    onDismiss: () -> Unit
) {
    var newBranchName by remember { mutableStateOf("") }
    var newBranchAddress by remember { mutableStateOf("") }
    var newBranchPhone by remember { mutableStateOf("") }
    var newBranchCity by remember { mutableStateOf("Greater Noida") }
    var newBranchState by remember { mutableStateOf("Uttar Pradesh") }
    var newBranchPincode by remember { mutableStateOf("201310") }
    var newBranchTotalSeats by remember { mutableStateOf("60") }
    var newBranchOpeningTime by remember { mutableStateOf("06:00 AM") }
    var newBranchClosingTime by remember { mutableStateOf("11:00 PM") }
    var newBranchUpiId by remember { mutableStateOf("saraswati.lib@okhdfcbank") }

    LaunchedEffect(newBranchPincode) {
        val cleanPin = newBranchPincode.trim().filter { it.isDigit() }
        if (cleanPin.length == 6) {
            viewModel.lookupPincode(cleanPin) { detectedCity, detectedState ->
                newBranchCity = detectedCity
                newBranchState = detectedState
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Branch Management", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = WarmTextDark)
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = WarmTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = "Active Branches", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = WarmTextDark)
                Spacer(modifier = Modifier.height(6.dp))

                branches.forEach { br ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clickable {
                                viewModel.switchBranch(br.id)
                                onDismiss()
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (br.id == activeBranchId) NavyPrimaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = br.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = WarmTextDark)
                                Text(text = "${br.code} • ${br.address}", style = MaterialTheme.typography.bodySmall, color = WarmTextMuted)
                                Text(text = "Seats: ${br.totalSeats} | UPI: ${br.upiId}", fontSize = 10.sp, color = OrangePrimaryDark)
                            }
                            if (br.id == activeBranchId) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Active", tint = NavyPrimary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(text = "Create New Branch (Premium Plan)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = WarmTextDark)
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = newBranchName,
                    onValueChange = { newBranchName = it },
                    label = { Text("Library / Branch Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = newBranchAddress,
                    onValueChange = { newBranchAddress = it },
                    label = { Text("Address *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = newBranchCity,
                        onValueChange = { newBranchCity = it },
                        label = { Text("City") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = newBranchPincode,
                        onValueChange = { newBranchPincode = it },
                        label = { Text("Pincode") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = newBranchPhone,
                        onValueChange = { newBranchPhone = it },
                        label = { Text("Phone / Mobile") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = newBranchTotalSeats,
                        onValueChange = { newBranchTotalSeats = it },
                        label = { Text("Seats Capacity") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = newBranchOpeningTime,
                        onValueChange = { newBranchOpeningTime = it },
                        label = { Text("Opens At") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = newBranchClosingTime,
                        onValueChange = { newBranchClosingTime = it },
                        label = { Text("Closes At") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = newBranchUpiId,
                    onValueChange = { newBranchUpiId = it },
                    label = { Text("UPI payment ID") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        if (newBranchName.isNotBlank()) {
                            viewModel.createBranch(
                                name = newBranchName,
                                code = "BR-0" + (branches.size + 1),
                                address = newBranchAddress,
                                phone = newBranchPhone.ifBlank { "+91 9876543210" },
                                city = newBranchCity,
                                state = newBranchState,
                                pincode = newBranchPincode,
                                totalSeats = newBranchTotalSeats.toIntOrNull() ?: 60,
                                openingTime = newBranchOpeningTime,
                                closingTime = newBranchClosingTime,
                                upiId = newBranchUpiId
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    enabled = newBranchName.isNotBlank()
                ) {
                    Text("Create Branch")
                }
            }
        }
    }
}

@Composable
fun AuditLogsDialog(
    auditLogs: List<com.example.data.model.AuditLog>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.8f)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Audit & Security Logs", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(auditLogs) { log ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = log.action, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = NavyPrimary)
                                    Text(text = log.timestamp, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(text = log.details, style = MaterialTheme.typography.bodySmall)
                                Text(text = "By: ${log.user}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LibraryProfileDialog(
    library: com.example.data.model.Library,
    viewModel: LibraryViewModel,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(library.name) }
    var phone by remember { mutableStateOf(library.phone) }
    var email by remember { mutableStateOf(library.email) }
    var address by remember { mutableStateOf(library.address) }
    var location by remember { mutableStateOf(library.location) }
    var city by remember { mutableStateOf(library.city) }
    var state by remember { mutableStateOf(library.state) }
    var pincode by remember { mutableStateOf(library.pincode) }
    var upiId by remember { mutableStateOf(library.upiId) }
    var totalSeats by remember { mutableStateOf(library.totalSeats.toString()) }
    var openingTime by remember { mutableStateOf(library.openingTime) }
    var closingTime by remember { mutableStateOf(library.closingTime) }

    LaunchedEffect(pincode) {
        val cleanPin = pincode.trim().filter { it.isDigit() }
        if (cleanPin.length == 6) {
            viewModel.lookupPincode(cleanPin) { detectedCity, detectedState ->
                city = detectedCity
                state = detectedState
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Edit Library Profile", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = WarmTextDark)
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = WarmTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Library Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = pincode,
                        onValueChange = { pincode = it },
                        label = { Text("Pincode (6 digits)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("City") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = state,
                        onValueChange = { state = it },
                        label = { Text("State") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = totalSeats,
                        onValueChange = { totalSeats = it },
                        label = { Text("Seats Capacity") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = openingTime,
                        onValueChange = { openingTime = it },
                        label = { Text("Opens At") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = closingTime,
                        onValueChange = { closingTime = it },
                        label = { Text("Closes At") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = upiId,
                    onValueChange = { upiId = it },
                    label = { Text("UPI Payment ID") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            viewModel.updateLibraryDetails(
                                name = name,
                                phone = phone,
                                address = address,
                                city = city,
                                state = state,
                                pincode = pincode,
                                totalSeats = totalSeats.toIntOrNull() ?: 60,
                                openingTime = openingTime,
                                closingTime = closingTime,
                                upiId = upiId
                            )
                            onDismiss()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    enabled = name.isNotBlank()
                ) {
                    Text("Save Changes")
                }
            }
        }
    }
}

@Composable
fun ShiftManagerDialog(
    viewModel: LibraryViewModel,
    onDismiss: () -> Unit
) {
    val shifts by viewModel.shifts.collectAsState()
    
    var showAddForm by remember { mutableStateOf(false) }
    
    // Add shift form state
    var newName by remember { mutableStateOf("") }
    var newStart by remember { mutableStateOf("") }
    var newEnd by remember { mutableStateOf("") }
    var newPrice by remember { mutableStateOf("") }

    // Edit shift state
    var editingShiftId by remember { mutableStateOf<String?>(null) }
    var editName by remember { mutableStateOf("") }
    var editStart by remember { mutableStateOf("") }
    var editEnd by remember { mutableStateOf("") }
    var editPrice by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = PureWhite,
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.85f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = OrangePrimaryDark,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Shift Configuration",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = WarmTextDark
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = WarmTextDark)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (showAddForm) {
                        // Add Shift Form Card
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = SlateBackground.copy(alpha = 0.5f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, OrangePrimary.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Add Custom Shift",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = WarmTextDark
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    
                                    OutlinedTextField(
                                        value = newName,
                                        onValueChange = { newName = it },
                                        label = { Text("Shift Name (e.g. Night Shift)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = WarmTextDark,
                                            unfocusedTextColor = WarmTextDark,
                                            focusedContainerColor = PureWhite,
                                            unfocusedContainerColor = PureWhite
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = newStart,
                                            onValueChange = { newStart = it },
                                            label = { Text("Start (e.g. 09:00 PM)") },
                                            singleLine = true,
                                            modifier = Modifier.weight(1f),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = WarmTextDark,
                                                unfocusedTextColor = WarmTextDark,
                                                focusedContainerColor = PureWhite,
                                                unfocusedContainerColor = PureWhite
                                            )
                                        )
                                        OutlinedTextField(
                                            value = newEnd,
                                            onValueChange = { newEnd = it },
                                            label = { Text("End (e.g. 06:00 AM)") },
                                            singleLine = true,
                                            modifier = Modifier.weight(1f),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = WarmTextDark,
                                                unfocusedTextColor = WarmTextDark,
                                                focusedContainerColor = PureWhite,
                                                unfocusedContainerColor = PureWhite
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = newPrice,
                                        onValueChange = { newPrice = it },
                                        label = { Text("Monthly Fee (₹)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = WarmTextDark,
                                            unfocusedTextColor = WarmTextDark,
                                            focusedContainerColor = PureWhite,
                                            unfocusedContainerColor = PureWhite
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { showAddForm = false },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Cancel", color = WarmTextDark)
                                        }
                                        Button(
                                            onClick = {
                                                val fee = newPrice.toIntOrNull() ?: 0
                                                if (newName.isNotBlank() && newStart.isNotBlank() && newEnd.isNotBlank() && fee > 0) {
                                                    viewModel.createShift(newName, newStart, newEnd, fee)
                                                    showAddForm = false
                                                    newName = ""
                                                    newStart = ""
                                                    newEnd = ""
                                                    newPrice = ""
                                                }
                                            },
                                            modifier = Modifier.weight(1.2f),
                                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                                        ) {
                                            Text("Add Shift")
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        item {
                            Button(
                                onClick = { showAddForm = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Create Custom Shift")
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Existing Timings & Fees",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = WarmTextDark,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    items(shifts) { shift ->
                        val isEditing = editingShiftId == shift.id
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF3ECE4))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                if (isEditing) {
                                    Text(
                                        text = "Editing Shift: ${shift.name}",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = OrangePrimaryDark
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    
                                    OutlinedTextField(
                                        value = editName,
                                        onValueChange = { editName = it },
                                        label = { Text("Shift Name") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = WarmTextDark,
                                            unfocusedTextColor = WarmTextDark,
                                            focusedContainerColor = PureWhite,
                                            unfocusedContainerColor = PureWhite
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = editStart,
                                            onValueChange = { editStart = it },
                                            label = { Text("Start Time") },
                                            singleLine = true,
                                            modifier = Modifier.weight(1f),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = WarmTextDark,
                                                unfocusedTextColor = WarmTextDark,
                                                focusedContainerColor = PureWhite,
                                                unfocusedContainerColor = PureWhite
                                            )
                                        )
                                        OutlinedTextField(
                                            value = editEnd,
                                            onValueChange = { editEnd = it },
                                            label = { Text("End Time") },
                                            singleLine = true,
                                            modifier = Modifier.weight(1f),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = WarmTextDark,
                                                unfocusedTextColor = WarmTextDark,
                                                focusedContainerColor = PureWhite,
                                                unfocusedContainerColor = PureWhite
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = editPrice,
                                        onValueChange = { editPrice = it },
                                        label = { Text("Default Fee (₹)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = WarmTextDark,
                                            unfocusedTextColor = WarmTextDark,
                                            focusedContainerColor = PureWhite,
                                            unfocusedContainerColor = PureWhite
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(
                                            onClick = { editingShiftId = null },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Cancel", color = WarmTextDark)
                                        }
                                        Button(
                                            onClick = {
                                                val priceVal = editPrice.toIntOrNull() ?: shift.defaultPrice
                                                val nameVal = editName.ifBlank { shift.name }
                                                viewModel.updateShift(
                                                    shift.copy(
                                                        name = nameVal,
                                                        startTime = editStart,
                                                        endTime = editEnd,
                                                        defaultPrice = priceVal
                                                    )
                                                )
                                                editingShiftId = null
                                            },
                                            modifier = Modifier.weight(1.2f),
                                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                                        ) {
                                            Text("Save Changes")
                                        }
                                    }
                                } else {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = shift.name,
                                                fontWeight = FontWeight.Bold,
                                                color = WarmTextDark,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(top = 2.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Schedule,
                                                    contentDescription = null,
                                                    tint = WarmTextMuted,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "${shift.startTime} - ${shift.endTime}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = WarmTextMuted
                                                )
                                            }
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "₹${shift.defaultPrice}/mo",
                                                fontWeight = FontWeight.ExtraBold,
                                                color = OrangePrimaryDark,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                OutlinedButton(
                                                    onClick = {
                                                        editingShiftId = shift.id
                                                        editName = shift.name
                                                        editStart = shift.startTime
                                                        editEnd = shift.endTime
                                                        editPrice = shift.defaultPrice.toString()
                                                    },
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                                ) {
                                                    Text("Edit", fontSize = 11.sp, color = WarmTextDark)
                                                }

                                                IconButton(
                                                    onClick = {
                                                        viewModel.deleteShift(shift.id)
                                                    },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Delete Shift",
                                                        tint = DangerRed,
                                                        modifier = Modifier.size(18.dp)
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
            }
        }
    }
}

@Composable
fun SaaSPaymentHistoryDialog(
    viewModel: LibraryViewModel,
    purchases: List<SaaSPurchaseRecord>,
    onDismiss: () -> Unit,
    onPreviewInvoice: (SaaSPurchaseRecord) -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = PureWhite,
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.85f),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = NavyPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Subscription Payments",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = WarmTextDark
                            )
                        }
                        Text(
                            text = "Verified Razorpay transactions & tax invoices",
                            style = MaterialTheme.typography.bodySmall,
                            color = WarmTextMuted
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = WarmTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (purchases.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Receipt,
                                contentDescription = null,
                                tint = Color(0xFFCBD5E1),
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No Online Subscription Purchases Yet",
                                fontWeight = FontWeight.Bold,
                                color = WarmTextDark,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "When you upgrade or renew via Razorpay, your payment history and official GST invoices will appear here.",
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = WarmTextMuted,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(purchases) { p ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFBF9F5)),
                                border = BorderStroke(1.dp, Color(0xFFE5DECE))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    // Row 1: Product Name & Status Badge
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = p.productName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = WarmTextDark,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Surface(
                                            color = Color(0xFFDCFCE7),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = p.status.uppercase(),
                                                color = Color(0xFF16A34A),
                                                fontWeight = FontWeight.Black,
                                                fontSize = 10.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Row 2: Amount & Timestamp
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(text = "Amount Paid", fontSize = 11.sp, color = WarmTextMuted)
                                            Text(
                                                text = "₹${p.amount}",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 16.sp,
                                                color = NavyPrimary
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(text = "Timestamp", fontSize = 11.sp, color = WarmTextMuted)
                                            Text(
                                                text = p.timestamp,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 12.sp,
                                                color = WarmTextDark
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    HorizontalDivider(color = Color(0xFFE5DECE).copy(alpha = 0.6f))
                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Row 3: Razorpay Ref No
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(text = "Razorpay Ref. No.", fontSize = 10.sp, color = WarmTextMuted)
                                            Text(
                                                text = p.razorpayPaymentId,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF475569)
                                            )
                                        }
                                        Text(
                                            text = p.invoiceNumber,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = OrangePrimaryDark
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Action Buttons
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { onPreviewInvoice(p) },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(vertical = 4.dp),
                                            border = BorderStroke(1.dp, NavyPrimary),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyPrimary)
                                        ) {
                                            Icon(imageVector = Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("View Invoice", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = { viewModel.downloadOrShareSaaSInvoice(context, p) },
                                            modifier = Modifier.weight(1.1f),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(vertical = 4.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                                        ) {
                                            Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp), tint = PureWhite)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Download / Share", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                                        }
                                    }
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
fun SaaSInvoicePreviewDialog(
    record: SaaSPurchaseRecord,
    viewModel: LibraryViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val owner by viewModel.ownerProfile.collectAsState()
    val lib by viewModel.library.collectAsState()

    val subtotal = (record.amount * 100) / 118
    val gst = record.amount - subtotal

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = PureWhite,
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.85f),
            shadowElevation = 10.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "VIDYARA TAX INVOICE",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = NavyPrimary
                        )
                        Text(
                            text = "Invoice: ${record.invoiceNumber}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = OrangePrimaryDark
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = WarmTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Invoice Meta Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Date & Time:", fontSize = 11.sp, color = WarmTextMuted)
                                Text(text = record.timestamp, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WarmTextDark)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Status:", fontSize = 11.sp, color = WarmTextMuted)
                                Text(text = "PAID (Razorpay Verified)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Razorpay Ref ID:", fontSize = 11.sp, color = WarmTextMuted)
                                Text(text = record.razorpayPaymentId, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Billed To / Library Details
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "BILLED TO", fontSize = 11.sp, fontWeight = FontWeight.Black, color = WarmTextMuted)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = lib.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = WarmTextDark)
                            Text(text = "Owner: ${owner.fullName} (${owner.phone})", fontSize = 11.sp, color = WarmTextDark)
                            Text(text = "${lib.address}, ${lib.city}", fontSize = 11.sp, color = WarmTextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Item Description & Pricing
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "SUBSCRIPTION PARTICULARS", fontSize = 11.sp, fontWeight = FontWeight.Black, color = WarmTextMuted)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = record.productName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = WarmTextDark)
                                    Text(text = "Period: ${record.billingPeriod} • ${record.branchCount} Branch(es)", fontSize = 10.sp, color = WarmTextMuted)
                                }
                                Text(text = "₹${record.amount}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyPrimary)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = Color(0xFFE2E8F0))
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Taxable Value:", fontSize = 11.sp, color = WarmTextMuted)
                                Text(text = "₹$subtotal", fontSize = 11.sp, color = WarmTextDark)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "GST (18% inclusive):", fontSize = 11.sp, color = WarmTextMuted)
                                Text(text = "₹$gst", fontSize = 11.sp, color = WarmTextDark)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Total Paid:", fontSize = 13.sp, fontWeight = FontWeight.Black, color = WarmTextDark)
                                Text(text = "₹${record.amount}", fontSize = 15.sp, fontWeight = FontWeight.Black, color = Color(0xFF16A34A))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = { viewModel.downloadOrShareSaaSInvoice(context, record) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = PureWhite, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Download / Share Invoice (PDF/Text)", fontWeight = FontWeight.Bold, color = PureWhite)
                }
            }
        }
    }
}
