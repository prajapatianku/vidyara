package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Student
import com.example.data.model.StudentStatus
import com.example.data.model.Seat
import com.example.ui.components.ApproveRegistrationDialog
import com.example.ui.components.StudentRegistrationQrDialog
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.LibraryViewModel
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.text.style.TextAlign

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentsScreen(
    viewModel: LibraryViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val owner by viewModel.ownerProfile.collectAsState()
    val library by viewModel.library.collectAsState()
    val students by viewModel.filteredStudents.collectAsState()
    val allStudents by viewModel.students.collectAsState()
    val requests by viewModel.registrationRequests.collectAsState()
    val pendingRequests = remember(requests) { requests.filter { it.status == "pending" } }
    val searchQuery by viewModel.studentSearchQuery.collectAsState()
    val statusFilter by viewModel.studentStatusFilter.collectAsState()
    val shiftFilter by viewModel.studentShiftFilter.collectAsState()
    val selectedStudent by viewModel.selectedStudentForDetail.collectAsState()
    val showAddDialog by viewModel.showAddStudentDialog.collectAsState()
    val isHindi by viewModel.isHindi.collectAsState()

    var showRegistrationQrModal by remember { mutableStateOf(false) }
    var selectedApprovalRequest by remember { mutableStateOf<com.example.data.model.RegistrationRequest?>(null) }

    val shiftOptions = listOf("All", "Morning", "Afternoon", "Evening", "Full Day")
    val statusOptions = listOf("All", "Expiring in 3 Days", "Active", "Has Due", "Expired")

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (!viewModel.canAddStudent()) {
                        viewModel.requestUpgrade("student_limit_20")
                    } else {
                        viewModel.showAddStudentDialog(true)
                    }
                },
                icon = { Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null) },
                text = { Text(translate("Add Student", isHindi), fontWeight = FontWeight.Bold) },
                containerColor = OrangePrimary,
                contentColor = Color.White
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(SlateBackground)
        ) {
            // Top Search Bar
            Surface(
                color = PureWhite,
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setStudentSearchQuery(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(translate("Search Students...", isHindi)) },
                        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = OrangePrimary) },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { viewModel.setStudentSearchQuery("") }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = Color(0xFFE5DECE),
                            focusedContainerColor = PureWhite,
                            unfocusedContainerColor = PureWhite
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Status Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(statusOptions) { status ->
                            FilterChip(
                                selected = statusFilter == status,
                                onClick = { viewModel.setStudentStatusFilter(status) },
                                label = { Text(translate(status, isHindi), fontWeight = if (statusFilter == status) FontWeight.Bold else FontWeight.Normal) },
                                shape = RoundedCornerShape(20.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = OrangePrimaryContainer,
                                    selectedLabelColor = OrangePrimaryDark,
                                    containerColor = PureWhite
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = statusFilter == status,
                                    borderColor = if (statusFilter == status) OrangePrimary else Color(0xFFE5DECE)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Shift Filter Row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(shiftOptions) { shift ->
                            FilterChip(
                                selected = shiftFilter == shift,
                                onClick = { viewModel.setStudentShiftFilter(shift) },
                                label = { Text(translate(shift, isHindi), fontWeight = if (shiftFilter == shift) FontWeight.Bold else FontWeight.Normal) },
                                shape = RoundedCornerShape(20.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = WarmPeachSecondaryContainer,
                                    selectedLabelColor = OrangePrimaryDark,
                                    containerColor = PureWhite
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = shiftFilter == shift,
                                    borderColor = if (shiftFilter == shift) OrangePrimaryDark else Color(0xFFE5DECE)
                                )
                            )
                        }
                    }
                }
            }

            // Student Count Summary, QR Button & WhatsApp Alerts Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Showing ${students.size} of ${allStudents.size} Students",
                    style = MaterialTheme.typography.labelMedium,
                    color = WarmTextMuted,
                    fontWeight = FontWeight.SemiBold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFF3EDF7),
                        border = BorderStroke(1.dp, PrimaryViolet.copy(alpha = 0.5f)),
                        modifier = Modifier.clickable { showRegistrationQrModal = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = "QR Code",
                                tint = PrimaryViolet,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "QR Form",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryViolet
                            )
                        }
                    }

                    val dueCount = remember(allStudents) { allStudents.count { it.dueAmount > 0 } }
                    if (dueCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFFE8F8EE),
                            border = BorderStroke(1.dp, Color(0xFF25D366).copy(alpha = 0.5f)),
                            modifier = Modifier.clickable { viewModel.showWhatsAppReminderDialog(true) }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Chat,
                                    contentDescription = null,
                                    tint = Color(0xFF128C7E),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$dueCount Dues",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF128C7E)
                                )
                            }
                        }
                    }
                }
            }

            // Pending Online Registrations Banner
            if (pendingRequests.isNotEmpty()) {
                Surface(
                    color = Color(0xFFFEF2F2),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "📋 Pending Online Registrations (${pendingRequests.size})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF991B1B)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        pendingRequests.take(2).forEach { req ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = req.studentName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F172A))
                                    Text(text = "📞 ${req.mobile} • ${req.requestedShift}", fontSize = 11.sp, color = Color(0xFF475569))
                                }
                                Button(
                                    onClick = { selectedApprovalRequest = req },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                                ) {
                                    Text("Approve", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Student List
            if (students.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PersonSearch,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = OrangePrimary.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No students found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = WarmTextDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try adjusting your search or filters, or add a new student.",
                            style = MaterialTheme.typography.bodySmall,
                            color = WarmTextMuted
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp, top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(students) { student ->
                        StudentCard(
                            student = student,
                            onClick = { viewModel.selectStudentForDetail(student) },
                            onCollectFee = { viewModel.showCollectFeeDialog(student) },
                            onWhatsAppReminder = { viewModel.sendStudentWhatsAppReminder(context, student) },
                            onReleaseSeat = { viewModel.releaseStudentSeat(student.id) },
                            onCall = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${student.mobile}"))
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }
        }

        // Student Registration QR Dialog
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

@Composable
fun StudentCard(
    student: Student,
    onClick: () -> Unit,
    onCollectFee: () -> Unit,
    onWhatsAppReminder: () -> Unit,
    onReleaseSeat: () -> Unit,
    onCall: () -> Unit
) {
    val daysUntilDue = remember(student.feeDueDate) {
        if (student.feeDueDate.isBlank()) 999
        else {
            try {
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                val dueDate = sdf.parse(student.feeDueDate) ?: return@remember 999
                val todayCal = java.util.Calendar.getInstance()
                todayCal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                todayCal.set(java.util.Calendar.MINUTE, 0)
                todayCal.set(java.util.Calendar.SECOND, 0)
                todayCal.set(java.util.Calendar.MILLISECOND, 0)
                ((dueDate.time - todayCal.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
            } catch (e: Exception) {
                999
            }
        }
    }
    val isExpiringSoon = daysUntilDue in 0..3 && student.dueAmount > 0
    val isInGracePeriod = daysUntilDue in -2..-1 && student.dueAmount > 0
    val isOverdueExpired = daysUntilDue < -2 && student.dueAmount > 0
    val canReleaseSeat = student.assignedSeatNumber.isNotBlank() && (isExpiringSoon || isInGracePeriod || isOverdueExpired || student.status == com.example.data.model.StudentStatus.EXPIRED)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF3ECE4)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(OrangePrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = student.fullName.take(2).uppercase(),
                            fontWeight = FontWeight.Black,
                            color = OrangePrimary,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = student.fullName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = WarmTextDark
                        )
                        Text(
                            text = "${student.studentCode} • ${student.course}",
                            style = MaterialTheme.typography.bodySmall,
                            color = WarmTextMuted
                        )
                    }
                }

                StatusBadge(status = if (student.dueAmount > 0) "Has Due" else "Active")
            }

            // Expiring Soon / Grace Period Banner
            if (isExpiringSoon || isInGracePeriod || isOverdueExpired) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = if (isInGracePeriod || isOverdueExpired) Color(0xFFFEE2E2) else Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isInGracePeriod || isOverdueExpired) Icons.Default.Warning else Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = if (isInGracePeriod || isOverdueExpired) DangerRed else WarningAmber,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when {
                                    daysUntilDue == 0 -> "🚨 Due Today (${student.feeDueDate})"
                                    daysUntilDue > 0 -> "⚠️ Due in $daysUntilDue days (${student.feeDueDate})"
                                    daysUntilDue in -2..-1 -> "⏳ Grace Period (${-daysUntilDue}d overdue)"
                                    else -> "❌ Expired (${-daysUntilDue}d overdue)"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isInGracePeriod || isOverdueExpired) DangerRed else Color(0xFF92400E)
                            )
                        }
                        if (canReleaseSeat) {
                            Text(
                                text = "Release Seat ✕",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = DangerRed,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable { onReleaseSeat() }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFF3ECE4))
            Spacer(modifier = Modifier.height(10.dp))

            // Details row: Shift, Seat, Fee
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Shift", fontSize = 10.sp, color = WarmTextMuted)
                    Text(text = student.assignedShiftName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = WarmTextDark)
                }

                Column {
                    Text(text = "Seat", fontSize = 10.sp, color = WarmTextMuted)
                    Text(
                        text = if (student.assignedSeatNumber.isNotBlank()) "Seat ${student.assignedSeatNumber}" else "Floating",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (student.assignedSeatNumber.isNotBlank()) OrangePrimaryDark else WarmTextDark
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Fee / Due", fontSize = 10.sp, color = WarmTextMuted)
                    Row {
                        Text(text = "₹${student.monthlyFee}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = WarmTextDark)
                        if (student.dueAmount > 0) {
                            Text(text = " (Due ₹${student.dueAmount})", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.ExtraBold, color = DangerRed)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📅 Joined: ${student.joiningDate}",
                    fontSize = 11.sp,
                    color = WarmTextMuted,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Next Due: ${student.feeDueDate}",
                    fontSize = 11.sp,
                    color = if (student.dueAmount > 0) DangerRed else Color(0xFF16A34A),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCall,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 6.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5DECE))
                ) {
                    Icon(imageVector = Icons.Default.Phone, contentDescription = "Call", modifier = Modifier.size(16.dp), tint = WarmTextDark)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Call", fontSize = 12.sp, color = WarmTextDark, fontWeight = FontWeight.Bold)
                }

                if (student.dueAmount > 0) {
                    Button(
                        onClick = onWhatsAppReminder,
                        modifier = Modifier.weight(1.3f),
                        contentPadding = PaddingValues(vertical = 6.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF128C7E))
                    ) {
                        Icon(imageVector = Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = onCollectFee,
                    modifier = Modifier.weight(if (student.dueAmount > 0) 1.2f else 1.3f),
                    contentPadding = PaddingValues(vertical = 6.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (student.dueAmount > 0) OrangePrimary else Color(0xFF16A34A)
                    )
                ) {
                    Icon(imageVector = Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (student.dueAmount > 0) "Collect Due" else "Collect Fee", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun AddStudentDialog(
    viewModel: LibraryViewModel,
    onDismiss: () -> Unit
) {
    val shifts by viewModel.shifts.collectAsState()
    val seats by viewModel.seats.collectAsState()

    var fullName by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var course by remember { mutableStateOf("UPSC / Govt Exam") }
    var selectedShift by remember { mutableStateOf(shifts.firstOrNull()?.name ?: "Full Day") }
    var selectedSeat by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }
    var studentAddress by remember { mutableStateOf("") }
    var sendWelcomeWhatsApp by remember { mutableStateOf(true) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val isHindi by viewModel.isHindi.collectAsState()
    val vacantSeats = remember(seats) {
        seats.filter { it.status == com.example.data.model.SeatStatus.AVAILABLE }
            .sortedBy { it.seatNumber.toIntOrNull() ?: Int.MAX_VALUE }
    }
    var showSeatSelectionDialog by remember { mutableStateOf(false) }
    
    // Auto-fill initial rate based on selected shift
    val defaultShiftPrice = remember(selectedShift, shifts) {
        shifts.find { it.name == selectedShift }?.defaultPrice ?: 1000
    }
    var monthlyFee by remember(defaultShiftPrice) { mutableStateOf(defaultShiftPrice.toString()) }

    val todayDateStr = remember { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()) }
    var admissionDate by remember { mutableStateOf(todayDateStr) }
    var isExistingStudent by remember { mutableStateOf(false) }
    var feeDueDate by remember(admissionDate) {
        mutableStateOf(viewModel.calculateNextDueDate(admissionDate))
    }
    var isFeePaidUpfront by remember { mutableStateOf(false) }

    if (showSeatSelectionDialog) {
        SeatSelectionDialog(
            availableSeats = vacantSeats,
            onSeatSelected = { selectedSeat = it },
            onDismiss = { showSeatSelectionDialog = false }
        )
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
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Add New Student", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = WarmTextDark)
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = WarmTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name *") },
                    textStyle = AppInputTextStyle,
                    colors = appOutlinedTextFieldColors(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = mobile,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }
                        if (digits.length <= 10) {
                            mobile = digits
                        }
                    },
                    label = { Text("Mobile Number * (10 Digits)") },
                    textStyle = AppInputTextStyle,
                    colors = appOutlinedTextFieldColors(),
                    singleLine = true,
                    isError = mobile.isNotEmpty() && mobile.length != 10,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = course,
                    onValueChange = { course = it },
                    label = { Text("Exam / Course (e.g. UPSC, SSC)") },
                    textStyle = AppInputTextStyle,
                    colors = appOutlinedTextFieldColors(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Shift Selection Row
                Text(
                    text = "Select Shift *",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = WarmTextDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(shifts) { sh ->
                        FilterChip(
                            selected = selectedShift == sh.name,
                            onClick = { selectedShift = sh.name },
                            label = { Text(sh.name, fontSize = 11.sp, fontWeight = if (selectedShift == sh.name) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = OrangePrimaryContainer,
                                selectedLabelColor = OrangePrimaryDark,
                                containerColor = PureWhite
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedShift == sh.name,
                                borderColor = if (selectedShift == sh.name) OrangePrimary else Color(0xFFE5DECE)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showSeatSelectionDialog = true }
                    ) {
                        OutlinedTextField(
                            value = selectedSeat,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select Seat *") },
                            placeholder = { Text("Tap to select") },
                            textStyle = AppInputTextStyle,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = WarmTextDark,
                                disabledBorderColor = Color(0xFFE5DECE),
                                disabledLabelColor = WarmTextMuted,
                                disabledContainerColor = PureWhite
                            ),
                            singleLine = true,
                            enabled = false,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    OutlinedTextField(
                        value = monthlyFee,
                        onValueChange = { monthlyFee = it },
                        label = { Text("Shift Price (₹)") },
                        textStyle = AppInputTextStyle,
                        colors = appOutlinedTextFieldColors(),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Section: Admission & Billing Schedule
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Admission & Billing Schedule",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { isExistingStudent = !isExistingStudent }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Checkbox(
                                    checked = isExistingStudent,
                                    onCheckedChange = { isExistingStudent = it },
                                    modifier = Modifier.size(18.dp),
                                    colors = CheckboxDefaults.colors(checkedColor = OrangePrimary)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Existing Student",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = WarmTextDark
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Admission Date Input
                        OutlinedTextField(
                            value = admissionDate,
                            onValueChange = { input ->
                                admissionDate = input
                                if (input.length == 10) {
                                    val calc = viewModel.calculateNextDueDate(input)
                                    if (calc.isNotBlank()) {
                                        feeDueDate = calc
                                    }
                                }
                            },
                            label = { Text("Admission Date (YYYY-MM-DD) *") },
                            placeholder = { Text("YYYY-MM-DD") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(18.dp))
                            },
                            textStyle = AppInputTextStyle,
                            colors = appOutlinedTextFieldColors(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Quick Presets Chips
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            FilterChip(
                                selected = admissionDate == todayDateStr,
                                onClick = {
                                    admissionDate = todayDateStr
                                    feeDueDate = viewModel.calculateNextDueDate(todayDateStr)
                                },
                                label = { Text("Today", fontSize = 10.sp) },
                                shape = RoundedCornerShape(8.dp)
                            )

                            val firstOfMonth = remember {
                                val cal = java.util.Calendar.getInstance()
                                cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
                                java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(cal.time)
                            }
                            FilterChip(
                                selected = admissionDate == firstOfMonth,
                                onClick = {
                                    admissionDate = firstOfMonth
                                    feeDueDate = viewModel.calculateNextDueDate(firstOfMonth)
                                    isExistingStudent = true
                                },
                                label = { Text("1st of Month", fontSize = 10.sp) },
                                shape = RoundedCornerShape(8.dp)
                            )

                            val oneMonthAgo = remember {
                                val cal = java.util.Calendar.getInstance()
                                cal.add(java.util.Calendar.DAY_OF_YEAR, -28)
                                java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(cal.time)
                            }
                            FilterChip(
                                selected = admissionDate == oneMonthAgo,
                                onClick = {
                                    admissionDate = oneMonthAgo
                                    feeDueDate = viewModel.calculateNextDueDate(oneMonthAgo)
                                    isExistingStudent = true
                                },
                                label = { Text("28 Days Ago", fontSize = 10.sp) },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Next Due Date Input
                        OutlinedTextField(
                            value = feeDueDate,
                            onValueChange = { feeDueDate = it },
                            label = { Text("Next Fee Due Date (YYYY-MM-DD) *") },
                            placeholder = { Text("YYYY-MM-DD") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Event, contentDescription = null, tint = OrangePrimaryDark, modifier = Modifier.size(18.dp))
                            },
                            supportingText = {
                                Text("Auto-calculated in 28-day cycles from admission date", fontSize = 10.sp, color = WarmTextMuted)
                            },
                            textStyle = AppInputTextStyle,
                            colors = appOutlinedTextFieldColors(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Current Payment Status for this student
                        Text(
                            text = "Current Payment Status:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = WarmTextDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val feeVal = monthlyFee.toIntOrNull() ?: 1000
                            FilterChip(
                                selected = !isFeePaidUpfront,
                                onClick = { isFeePaidUpfront = false },
                                label = { Text("Fee Due Now (₹$feeVal)", fontSize = 11.sp, fontWeight = if (!isFeePaidUpfront) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFFEE2E2),
                                    selectedLabelColor = DangerRed,
                                    containerColor = PureWhite
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = !isFeePaidUpfront,
                                    borderColor = if (!isFeePaidUpfront) DangerRed else Color(0xFFE2E8F0)
                                )
                            )

                            FilterChip(
                                selected = isFeePaidUpfront,
                                onClick = { isFeePaidUpfront = true },
                                label = { Text("Already Paid (₹0 Due)", fontSize = 11.sp, fontWeight = if (isFeePaidUpfront) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFDCFCE7),
                                    selectedLabelColor = Color(0xFF16A34A),
                                    containerColor = PureWhite
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isFeePaidUpfront,
                                    borderColor = if (isFeePaidUpfront) Color(0xFF16A34A) else Color(0xFFE2E8F0)
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Gender Selection
                Text(
                    text = "Gender *",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = WarmTextDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("Male", "Female", "Other").forEach { gen ->
                        val isSel = gender == gen
                        FilterChip(
                            selected = isSel,
                            onClick = { gender = gen },
                            label = { Text(gen, fontSize = 11.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = OrangePrimaryContainer,
                                selectedLabelColor = OrangePrimaryDark,
                                containerColor = PureWhite
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSel,
                                borderColor = if (isSel) OrangePrimary else Color(0xFFE5DECE)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Address Input
                OutlinedTextField(
                    value = studentAddress,
                    onValueChange = { studentAddress = it },
                    label = { Text("Address") },
                    textStyle = AppInputTextStyle,
                    colors = appOutlinedTextFieldColors(),
                    singleLine = false,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // WhatsApp Welcome Pass Checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { sendWelcomeWhatsApp = !sendWelcomeWhatsApp }
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = sendWelcomeWhatsApp,
                        onCheckedChange = { sendWelcomeWhatsApp = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF128C7E))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = null,
                        tint = Color(0xFF128C7E),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = translate("Send Welcome Pass via WhatsApp", isHindi),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = WarmTextDark
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        if (fullName.isNotBlank() && mobile.isNotBlank()) {
                            val feeVal = monthlyFee.toIntOrNull() ?: 1000
                            val actualDue = if (isFeePaidUpfront) 0 else feeVal
                            val newStudent = viewModel.createStudentAndReturn(
                                fullName = fullName,
                                mobile = mobile,
                                whatsapp = mobile,
                                email = email,
                                course = course,
                                assignedSeat = selectedSeat.trim(),
                                assignedShift = selectedShift,
                                monthlyFee = feeVal,
                                initialDue = actualDue,
                                gender = gender,
                                address = studentAddress,
                                joiningDate = admissionDate.trim(),
                                feeDueDate = feeDueDate.trim()
                            )
                            if (newStudent != null && sendWelcomeWhatsApp) {
                                viewModel.sendStudentWelcomeWhatsApp(context, newStudent)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    enabled = fullName.isNotBlank() && mobile.length == 10
                ) {
                    Text(translate("Enroll Student", isHindi))
                }
            }
        }
    }
}

@Composable
fun StudentDetailDialog(
    student: Student,
    viewModel: LibraryViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val payments by viewModel.payments.collectAsState()
    val studentPayments = payments.filter { it.studentId == student.id || it.studentName == student.fullName }
    val isWhatsAppUnlocked = viewModel.hasFeature("whatsapp_fee_reminders")
    val isHindi by viewModel.isHindi.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.95f)
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
                    Text(text = "Student Profile", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Digital ID Card Preview
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyPrimary)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "MY LIBRARY STUDENT PASS", color = AmberTertiary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Surface(color = Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                                Text(text = student.studentCode, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(4.dp, 2.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = student.fullName.take(2).uppercase(), fontWeight = FontWeight.Bold, color = NavyPrimary, fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = student.fullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(text = student.course, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
                                Text(text = "Shift: ${student.assignedShiftName} | Seat: ${student.assignedSeatNumber.ifBlank { "Floating" }}", fontSize = 11.sp, color = AmberTertiary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Profile Info: Gender & Address
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFBF8F3)),
                    border = BorderStroke(1.dp, Color(0xFFE5DECE))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Gender", style = MaterialTheme.typography.bodySmall, color = WarmTextMuted)
                            Text(text = student.gender, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = WarmTextDark)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Contact", style = MaterialTheme.typography.bodySmall, color = WarmTextMuted)
                            Text(text = student.mobile, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = WarmTextDark)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Admission Date", style = MaterialTheme.typography.bodySmall, color = WarmTextMuted)
                            Text(text = student.joiningDate, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = WarmTextDark)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Next Fee Due Date", style = MaterialTheme.typography.bodySmall, color = WarmTextMuted)
                            Text(text = student.feeDueDate, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = if (student.dueAmount > 0) DangerRed else Color(0xFF16A34A))
                        }
                        if (student.address.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(text = "Address", style = MaterialTheme.typography.bodySmall, color = WarmTextMuted)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = student.address, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = WarmTextDark)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // WhatsApp Fee Due Alert & Action
                if (student.dueAmount > 0) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F8EE)),
                        border = BorderStroke(1.dp, Color(0xFF25D366).copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Chat,
                                        contentDescription = "WhatsApp",
                                        tint = Color(0xFF128C7E),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Fee Due Reminder (${student.feeDueDate})",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF128C7E)
                                    )
                                }
                                Text(
                                    text = "Due: ₹${student.dueAmount}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp,
                                    color = DangerRed
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    if (!isWhatsAppUnlocked) {
                                        onDismiss()
                                        viewModel.requestUpgrade("whatsapp_fee_reminders")
                                    } else {
                                        viewModel.sendStudentWhatsAppReminder(context, student)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF128C7E))
                            ) {
                                Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isWhatsAppUnlocked) "Send Fee Due Alert on WhatsApp" else "Unlock WhatsApp Alerts (2nd/3rd Plan)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.showCollectFeeDialog(student)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                    ) {
                        Icon(imageVector = Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Collect Fee")
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.deleteStudent(student.id)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Remove")
                    }
                }

                if (student.assignedSeatNumber.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = {
                            viewModel.releaseStudentSeat(student.id)
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, DangerRed),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed)
                    ) {
                        Icon(imageVector = Icons.Default.Chair, contentDescription = null, modifier = Modifier.size(16.dp), tint = DangerRed)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "${translate("Release Seat", isHindi)} (${student.assignedSeatNumber})", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Payment History
                Text(text = "Payment History", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                if (studentPayments.isEmpty()) {
                    Text(text = "No payment records found.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(studentPayments) { p ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clickable { viewModel.showActiveReceipt(p) },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = p.receiptNumber, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                        Text(text = "${p.paymentDate} • via ${p.paymentMethod}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text(text = "₹${p.amount}", fontWeight = FontWeight.Bold, color = SuccessGreen)
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
fun SeatSelectionDialog(
    availableSeats: List<Seat>,
    onSeatSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.7f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Vacant Seat",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = WarmTextDark
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = WarmTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (availableSeats.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No vacant seats available in this branch!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DangerRed,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(availableSeats) { seat ->
                            Surface(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(1.dp, Color(0xFF22C55E), RoundedCornerShape(10.dp))
                                    .clickable {
                                        onSeatSelected(seat.seatNumber)
                                        onDismiss()
                                    },
                                color = Color(0xFFF0FDF4),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text(
                                        text = seat.seatNumber,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF15803D)
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
