package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Seat
import com.example.data.model.SeatStatus
import com.example.ui.theme.*
import com.example.ui.viewmodel.LibraryViewModel

@Composable
fun SeatMapScreen(
    viewModel: LibraryViewModel,
    modifier: Modifier = Modifier
) {
    val seats by viewModel.seats.collectAsState()
    val students by viewModel.students.collectAsState()
    val shifts by viewModel.shifts.collectAsState()
    val selectedSeat by viewModel.selectedSeatForAction.collectAsState()

    var selectedFloor by remember { mutableStateOf("Ground Floor") }

    val totalCount = seats.size
    val occupiedCount = seats.count { it.status == SeatStatus.OCCUPIED }
    val availableCount = seats.count { it.status == SeatStatus.AVAILABLE }
    val maintenanceCount = seats.count { it.status == SeatStatus.MAINTENANCE }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SlateBackground)
    ) {
        // Floor Selector & Header
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
                    Column {
                        Text(
                            text = "Interactive Seat Map",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = WarmTextDark
                        )
                        Text(
                            text = "Live occupancy layout & desk assignment",
                            style = MaterialTheme.typography.bodySmall,
                            color = WarmTextMuted
                        )
                    }

                    Surface(
                        color = OrangePrimaryContainer,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, OrangePrimary.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "$occupiedCount / $totalCount Occupied",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = OrangePrimaryDark,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Status Legend Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    LegendItem(color = Color(0xFF16A34A), label = "Available ($availableCount)")
                    LegendItem(color = OrangePrimary, label = "Occupied ($occupiedCount)")
                    LegendItem(color = DangerRed, label = "Maintenance ($maintenanceCount)")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Seat Grid Map
        val sortedSeats = remember(seats) {
            seats.sortedBy { it.seatNumber.toIntOrNull() ?: Int.MAX_VALUE }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sortedSeats) { seat ->
                SeatGridItem(
                    seat = seat,
                    onClick = { viewModel.selectSeatForAction(seat) }
                )
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 11.sp, color = WarmTextMuted, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun SeatGridItem(
    seat: Seat,
    onClick: () -> Unit
) {
    val (bgColor, borderColor, textColor, icon) = when (seat.status) {
        SeatStatus.AVAILABLE -> Quadruple(
            Color(0xFFF0FDF4),
            Color(0xFF22C55E),
            Color(0xFF15803D),
            Icons.Default.Chair
        )
        SeatStatus.OCCUPIED -> Quadruple(
            OrangePrimaryContainer,
            OrangePrimary,
            OrangePrimaryDark,
            Icons.Default.Person
        )
        SeatStatus.MAINTENANCE -> Quadruple(
            Color(0xFFFEF2F2),
            DangerRed,
            DangerRed,
            Icons.Default.Build
        )
        SeatStatus.RESERVED -> Quadruple(
            WarmPeachSecondaryContainer,
            OrangeSecondary,
            OrangeSecondary,
            Icons.Default.Bookmark
        )
    }

    Surface(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        color = bgColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = seat.seatNumber,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SeatActionDialog(
    seat: Seat,
    students: List<com.example.data.model.Student>,
    shifts: List<String>,
    viewModel: LibraryViewModel,
    onDismiss: () -> Unit
) {
    var selectedStudentName by remember { mutableStateOf(students.firstOrNull()?.fullName ?: "") }
    var selectedShiftName by remember { mutableStateOf(shifts.firstOrNull() ?: "Full Day") }

    var isEditingLabel by remember { mutableStateOf(false) }
    var customSeatLabel by remember { mutableStateOf(seat.seatNumber) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Seat ${seat.seatNumber}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(text = "Status: ${seat.status.name}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { isEditingLabel = !isEditingLabel }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Seat Label", tint = OrangePrimaryDark)
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                if (isEditingLabel) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customSeatLabel,
                            onValueChange = { customSeatLabel = it },
                            label = { Text("Custom Seat Name (e.g. A-12, 101, VIP)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                if (customSeatLabel.isNotBlank() && customSeatLabel != seat.seatNumber) {
                                    viewModel.renameSeat(seat.seatNumber, customSeatLabel)
                                    isEditingLabel = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                        ) {
                            Text("Save")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (seat.status == SeatStatus.OCCUPIED) {
                    val occupantStudent = students.find { it.id == seat.assignedStudentId || it.fullName.equals(seat.assignedStudentName, ignoreCase = true) }
                    val occupantPhone = occupantStudent?.mobile ?: "Not Available"

                    // Occupant Info
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = NavyPrimaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "CURRENT OCCUPANT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Name: ${seat.assignedStudentName ?: "Student"}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(text = "Mobile Phone: $occupantPhone", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text(text = "Seat Number: Desk ${seat.seatNumber}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Text(text = "Shift: ${seat.assignedShiftName ?: "Full Day"}", style = MaterialTheme.typography.bodySmall)
                            Text(text = "Access valid till: ${seat.expiryDate ?: "Active"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            viewModel.releaseSeat(seat.seatNumber)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                    ) {
                        Icon(imageVector = Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Vacate / Release Seat")
                    }
                } else if (seat.status == SeatStatus.AVAILABLE) {
                    Text(text = "Assign to Student", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = selectedStudentName,
                        onValueChange = { selectedStudentName = it },
                        label = { Text("Student Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val student = students.find { it.fullName.equals(selectedStudentName, ignoreCase = true) }
                            val stuId = student?.id ?: "stu_quick"
                            viewModel.assignSeatToStudent(seat.seatNumber, stuId, selectedStudentName, selectedShiftName)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        enabled = selectedStudentName.isNotBlank()
                    ) {
                        Text("Confirm Seat Assignment")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { viewModel.toggleSeatMaintenance(seat.seatNumber) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Mark for Maintenance")
                    }
                } else {
                    Text(text = "Seat is currently under maintenance.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.toggleSeatMaintenance(seat.seatNumber) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                    ) {
                        Text("Mark Available")
                    }
                }
            }
        }
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
