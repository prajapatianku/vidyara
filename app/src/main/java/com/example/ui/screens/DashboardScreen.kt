package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.model.PlatformBroadcast
import com.example.data.model.SaaSPlanType
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.LibraryViewModel

@Composable
fun DashboardScreen(
    viewModel: LibraryViewModel,
    onNavigateToStudents: () -> Unit,
    onNavigateToSeats: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToExpenses: () -> Unit,
    modifier: Modifier = Modifier
) {
    val owner by viewModel.ownerProfile.collectAsState()
    val library by viewModel.library.collectAsState()
    val branches by viewModel.branches.collectAsState()
    val activeBranchId by viewModel.activeBranchId.collectAsState()
    val saasPlan by viewModel.saasSubscription.collectAsState()
    val metrics by viewModel.dashboardMetrics.collectAsState()
    val students by viewModel.students.collectAsState()
    val requests by viewModel.registrationRequests.collectAsState()
    val isHindi by viewModel.isHindi.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    val activeBranch = branches.find { it.id == activeBranchId } ?: branches.firstOrNull()
    val daysRemaining = remember(saasPlan) {
        viewModel.getSubscriptionDaysRemaining()
    }

    val appControl by viewModel.platformAppControl.collectAsState()
    val broadcasts by viewModel.platformBroadcasts.collectAsState()
    val dismissedBroadcasts by viewModel.platformRepository.dismissedBroadcastIds.collectAsState()
    val isSubExpired = daysRemaining == 0 && saasPlan.planType != SaaSPlanType.FREE
    val activeBroadcast = remember(broadcasts, saasPlan, isSubExpired, dismissedBroadcasts) {
        viewModel.platformRepository.getActiveBroadcastForOwner(saasPlan.planType, isSubExpired)
    }

    var showNotificationCenter by remember { mutableStateOf(false) }
    var showRegistrationQrModal by remember { mutableStateOf(false) }
    var selectedApprovalRequest by remember { mutableStateOf<com.example.data.model.RegistrationRequest?>(null) }
    val pendingDuesStudents = remember(students) { students.filter { it.dueAmount > 0 } }
    val pendingRegistrationRequests = remember(requests) { requests.filter { it.status == "pending" } }
    val unreadNotificationsCount = (if (activeBroadcast != null) 1 else 0) +
            pendingDuesStudents.size +
            (if (daysRemaining in 0..7) 1 else 0) +
            pendingRegistrationRequests.size

    Box(modifier = modifier.fillMaxSize().background(Color(0xFFF5F7FB))) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Top App Bar with Centered Logo & Notification Bell (Seamlessly on background canvas)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left spacer matching bell size to ensure perfectly centered logo
                    Spacer(modifier = Modifier.size(36.dp))

                    // Center: Official Vidyara Header Logo Lockup
                    Image(
                        painter = painterResource(id = R.drawable.ic_vidyara_header_logo),
                        contentDescription = "Vidyara",
                        modifier = Modifier
                            .height(32.dp)
                            .width(140.dp)
                    )

                    // Right: Notification Bell in Navy with royal blue notification dot
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clickable { showNotificationCenter = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            tint = Color(0xFF021B57),
                            modifier = Modifier.size(24.dp)
                        )
                        if (unreadNotificationsCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-4).dp, y = 4.dp)
                                    .size(9.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0066FF))
                            )
                        }
                    }
                }
            }

            // Platform Maintenance Banner (if active)
            if (appControl.maintenanceMode) {
                item {
                    Surface(
                        color = Color(0xFFFEF3C7),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Engineering, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SYSTEM NOTICE: ${appControl.maintenanceMessage}",
                                color = Color(0xFF92400E),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Active Platform Broadcast Announcement Banner (Vibrant Royal Blue matching mockup)
            activeBroadcast?.let { bc ->
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0747A6)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Campaign,
                                contentDescription = "Broadcast",
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = bc.title,
                                    color = Color(0xFFFBBF24),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = bc.message,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                            IconButton(
                                onClick = {
                                    viewModel.platformRepository.dismissBroadcast(bc.id + "_" + bc.timestamp)
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Subscription Expiry Alert
            if (daysRemaining in 0..7) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = DangerRed,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clickable { viewModel.requestUpgrade("general") }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Warning",
                                tint = PureWhite,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Your subscription expires in $daysRemaining days! Tap here to renew.",
                                color = PureWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Owner Profile & Branch Dropdown Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEEF2F6)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFF021B57),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Welcome, ${owner.fullName.split(" ").firstOrNull() ?: "Owner"}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0A1931)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { viewModel.showBranchManagerDialog(true) }
                                    .padding(vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Color(0xFF0747A6),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = activeBranch?.name ?: library.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF4B5563),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = Color(0xFF0747A6),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Plan Badge (★ Premium)
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFD0D7E2)),
                        modifier = Modifier
                            .clickable { viewModel.requestUpgrade("view_plans") }
                            .padding(start = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "★",
                                color = Color(0xFF0747A6),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when (saasPlan.planType) {
                                    SaaSPlanType.FREE -> "Free Plan"
                                    SaaSPlanType.PREMIUM -> "Premium"
                                    SaaSPlanType.BUSINESS -> "Business"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0A1931)
                            )
                        }
                    }
                }
            }

            // Hero Collection Card (Royal Blue Gradient with LIVE pill badge)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(22.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF004CB8), // Royal Sapphire Blue
                                        Color(0xFF052B75)  // Navy Depth
                                    )
                                )
                            )
                            .padding(horizontal = 18.dp, vertical = 16.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = translate("Today's Collection", isHindi),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White.copy(alpha = 0.95f)
                                    )
                                }
                                Surface(
                                    color = Color(0xFF00388A).copy(alpha = 0.7f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF10B981)) // Glowing Green
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "LIVE",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "₹${metrics.todayCollection}",
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = translate("This Month", isHindi),
                                        fontSize = 11.sp,
                                        color = Color(0xFFC2D6F5),
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "₹${metrics.monthlyCollection}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.White.copy(alpha = 0.2f)))

                                Column(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onNavigateToExpenses() }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = translate("Expenses >", isHindi),
                                        fontSize = 11.sp,
                                        color = Color(0xFFC2D6F5),
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "₹${metrics.totalExpenses}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.White.copy(alpha = 0.2f)))

                                Column(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onNavigateToStudents() }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = translate("Pending Dues >", isHindi),
                                        fontSize = 11.sp,
                                        color = Color(0xFFC2D6F5),
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "₹${metrics.pendingDues}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFBBF24) // Yellow/Gold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Library Overview (2x2 Grid with two-tone section indicator)
            item {
                Spacer(modifier = Modifier.height(18.dp))
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(
                            modifier = Modifier
                                .width(4.dp)
                                .height(14.dp)
                                .clip(RoundedCornerShape(2.dp))
                        ) {
                            Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color(0xFF0747A6)))
                            Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color(0xFFF59E0B)))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = translate("Library Overview", isHindi),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF06193E)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val isFreePlan = saasPlan.planType == SaaSPlanType.FREE
                        KpiMetricCard(
                            title = translate("Active Students", isHindi),
                            value = if (isFreePlan) "${metrics.activeStudentsCount}/20" else "${metrics.activeStudentsCount}",
                            subtitle = if (isFreePlan) {
                                if (metrics.activeStudentsCount >= 20) "⚠️ Limit Reached" else "${20 - metrics.activeStudentsCount} slots left"
                            } else "Total enrolled",
                            icon = Icons.Default.People,
                            iconBgColor = Color(0xFFDCEBFF),
                            iconTintColor = Color(0xFF0747A6),
                            modifier = Modifier.weight(1f),
                            onClick = {
                                if (isFreePlan && metrics.activeStudentsCount >= 20) {
                                    viewModel.requestUpgrade("student_limit_20")
                                } else {
                                    onNavigateToStudents()
                                }
                            }
                        )

                        KpiMetricCard(
                            title = translate("Present Today", isHindi),
                            value = "${metrics.presentTodayCount}",
                            subtitle = "${metrics.absentTodayCount} Absent",
                            icon = Icons.Default.CheckCircle,
                            iconBgColor = Color(0xFFDEF7EC),
                            iconTintColor = Color(0xFF0E9F6E),
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToAttendance
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        KpiMetricCard(
                            title = translate("Seats Occupancy", isHindi),
                            value = "${metrics.occupiedSeatsCount}/${metrics.totalSeatsCount}",
                            subtitle = "${metrics.availableSeatsCount} seats available",
                            icon = Icons.Default.Chair,
                            iconBgColor = Color(0xFFDCEBFF),
                            iconTintColor = Color(0xFF0747A6),
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToSeats
                        )

                        KpiMetricCard(
                            title = translate("Fee Dues", isHindi),
                            value = "₹${metrics.pendingDues}",
                            subtitle = "${metrics.expiringCount} students pending",
                            icon = Icons.Default.AttachMoney,
                            iconBgColor = Color(0xFFFCE8E6),
                            iconTintColor = Color(0xFFE02424),
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToStudents
                        )
                    }
                }
            }

            // Quick Actions Section
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(
                            modifier = Modifier
                                .width(4.dp)
                                .height(14.dp)
                                .clip(RoundedCornerShape(2.dp))
                        ) {
                            Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color(0xFF0747A6)))
                            Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color(0xFFF59E0B)))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = translate("Quick Actions", isHindi),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF06193E)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickActionCard(
                            title = "Students",
                            icon = Icons.Default.People,
                            color = Color(0xFF0747A6),
                            onClick = onNavigateToStudents,
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionCard(
                            title = "Seats",
                            icon = Icons.Default.Chair,
                            color = Color(0xFFF59E0B),
                            onClick = onNavigateToSeats,
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionCard(
                            title = "Register QR",
                            icon = Icons.Default.QrCode,
                            color = PrimaryViolet,
                            onClick = { showRegistrationQrModal = true },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Recent Students Section
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(14.dp)
                                    .clip(RoundedCornerShape(2.dp))
                            ) {
                                Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color(0xFF0747A6)))
                                Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color(0xFFF59E0B)))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Recent Students",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF06193E)
                            )
                        }
                        Text(
                            text = "View All (${students.size}) >",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0747A6),
                            modifier = Modifier.clickable { onNavigateToStudents() }
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            items(students.take(5)) { student ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clickable { viewModel.selectStudentForDetail(student) },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = BorderStroke(1.dp, Color(0xFFE8EEF5)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE1EFFF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = student.fullName.take(2).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF0747A6)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = student.fullName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0A1931)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${student.studentCode} • ${student.assignedShiftName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF6B7280)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            if (student.assignedSeatNumber.isNotBlank()) {
                                Surface(
                                    color = Color(0xFFE8F4FD),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Color(0xFFD0E8FC))
                                ) {
                                    Text(
                                        text = "Seat ${student.assignedSeatNumber}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0747A6),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            if (student.dueAmount > 0) {
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "Due: ₹${student.dueAmount}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DangerRed
                                )
                            }
                        }
                    }
                }
            }
        }

        // =====================================================================
        // NOTIFICATION CENTER MODAL DIALOG
        // =====================================================================
        if (showNotificationCenter) {
            Dialog(
                onDismissRequest = { showNotificationCenter = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .fillMaxHeight(0.85f),
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFFF8FAFC),
                    shadowElevation = 8.dp
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Modal Header
                        Surface(
                            color = PureWhite,
                            modifier = Modifier.fillMaxWidth(),
                            shadowElevation = 1.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 18.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFEFF6FF)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Notifications,
                                            contentDescription = null,
                                            tint = Color(0xFF0066FF),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Notification Center",
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF0F172A)
                                        )
                                        Text(
                                            text = "$unreadNotificationsCount active item${if (unreadNotificationsCount != 1) "s" else ""}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }

                                IconButton(onClick = { showNotificationCenter = false }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = Color(0xFF64748B)
                                    )
                                }
                            }
                        }

                        // Notification Items List
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(vertical = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Section 1: Super Admin Broadcast Announcements
                            activeBroadcast?.let { bc ->
                                item {
                                    Text(
                                        text = "📢 Platform Broadcast",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A2D6E))
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = bc.title,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color(0xFFFBBF24)
                                                )
                                                Surface(
                                                    color = Color.White.copy(alpha = 0.15f),
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text(
                                                        text = bc.timestamp.take(10),
                                                        fontSize = 10.sp,
                                                        color = Color.White,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = bc.message,
                                                fontSize = 12.sp,
                                                color = Color.White,
                                                lineHeight = 17.sp
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.End
                                            ) {
                                                TextButton(
                                                    onClick = {
                                                        viewModel.platformRepository.dismissBroadcast(bc.id + "_" + bc.timestamp)
                                                    }
                                                ) {
                                                    Text("Dismiss Announcement", color = Color(0xFFFBBF24), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Section 2: Student Fee Due Reminders
                            if (pendingDuesStudents.isNotEmpty()) {
                                item {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "💰 Student Fee Due Reminders (${pendingDuesStudents.size})",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                }

                                items(pendingDuesStudents) { student ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                                        border = BorderStroke(1.dp, Color(0xFFFEE2E2))
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = student.fullName,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp,
                                                        color = Color(0xFF0F172A)
                                                    )
                                                    Text(
                                                        text = "Seat ${if (student.assignedSeatNumber.isNotBlank()) student.assignedSeatNumber else "N/A"} • ${student.assignedShiftName}",
                                                        fontSize = 11.sp,
                                                        color = Color(0xFF64748B)
                                                    )
                                                    if (student.feeDueDate.isNotBlank()) {
                                                        Text(
                                                            text = "Due Date: ${student.feeDueDate}",
                                                            fontSize = 11.sp,
                                                            color = DangerRed,
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    }
                                                }
                                                Column(horizontalAlignment = Alignment.End) {
                                                    Text(
                                                        text = "₹${student.dueAmount}",
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = DangerRed
                                                    )
                                                    Text(
                                                        text = "PENDING DUE",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = DangerRed
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(10.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                OutlinedButton(
                                                    onClick = {
                                                        showNotificationCenter = false
                                                        viewModel.selectStudentForDetail(student)
                                                        onNavigateToStudents()
                                                    },
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(vertical = 4.dp),
                                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                                ) {
                                                    Text("View Profile", fontSize = 11.sp, color = Color(0xFF0F172A))
                                                }

                                                Button(
                                                    onClick = {
                                                        viewModel.sendStudentWhatsAppReminder(context, student)
                                                    },
                                                    modifier = Modifier.weight(1.3f),
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(vertical = 4.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                                                ) {
                                                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(12.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("WhatsApp Alert", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Section 3: Subscription & Platform Notices
                            if (daysRemaining in 0..7) {
                                item {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "⚡ Subscription Reminder",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                                        border = BorderStroke(1.dp, Color(0xFFFECACA))
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Warning, contentDescription = null, tint = DangerRed, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Subscription expires in $daysRemaining days",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = DangerRed
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Renew your subscription to avoid any interruption in student bookings, daily attendance, and cloud sync.",
                                                fontSize = 11.sp,
                                                color = Color(0xFF7F1D1D)
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Button(
                                                onClick = {
                                                    showNotificationCenter = false
                                                    viewModel.requestUpgrade("general")
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                            ) {
                                                Text("Renew Plan Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }

                            // Section 4: Public QR Registration Requests
                            if (pendingRegistrationRequests.isNotEmpty()) {
                                item {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "📋 Pending Registration Requests (${pendingRegistrationRequests.size})",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                }

                                items(pendingRegistrationRequests) { req ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(text = req.studentName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F172A))
                                                Text(text = "${req.course} • ${req.requestedShift}", fontSize = 11.sp, color = Color(0xFF64748B))
                                                Text(text = "Mob: ${req.mobile}", fontSize = 11.sp, color = Color(0xFF64748B))
                                            }
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                FilledTonalButton(
                                                    onClick = { viewModel.rejectRequest(req.id) },
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                    shape = RoundedCornerShape(8.dp),
                                                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFFFEE2E2), contentColor = DangerRed)
                                                ) {
                                                    Text("Reject", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                                Button(
                                                    onClick = {
                                                        showNotificationCenter = false
                                                        selectedApprovalRequest = req
                                                    },
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                    shape = RoundedCornerShape(8.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0066FF))
                                                ) {
                                                    Text("Approve", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Empty State
                            if (activeBroadcast == null && pendingDuesStudents.isEmpty() && daysRemaining > 7 && pendingRegistrationRequests.isEmpty()) {
                                item {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 40.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(60.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFEFF6FF)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.NotificationsNone,
                                                contentDescription = null,
                                                tint = Color(0xFF0066FF),
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "All Caught Up!",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "No pending fee dues or unread broadcast announcements.",
                                            fontSize = 12.sp,
                                            color = Color(0xFF64748B),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }

                        // Bottom Close Action
                        Surface(
                            color = PureWhite,
                            modifier = Modifier.fillMaxWidth(),
                            shadowElevation = 4.dp
                        ) {
                            Box(modifier = Modifier.padding(14.dp)) {
                                OutlinedButton(
                                    onClick = { showNotificationCenter = false },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Close Notification Center", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Student Registration QR Code Dialog
        if (showRegistrationQrModal) {
            StudentRegistrationQrDialog(
                accountId = owner.id.ifBlank { "acc_default" },
                libraryName = library.name,
                onDismiss = { showRegistrationQrModal = false }
            )
        }

        // Student Registration Approval Dialog
        selectedApprovalRequest?.let { req ->
            ApproveRegistrationDialog(
                request = req,
                onApprove = { customSeat, customFee ->
                    viewModel.approveRequest(context, req.id, customSeat, customFee)
                    selectedApprovalRequest = null
                },
                onReject = {
                    viewModel.rejectRequest(req.id)
                    selectedApprovalRequest = null
                },
                onDismiss = { selectedApprovalRequest = null }
            )
        }
    }
}

