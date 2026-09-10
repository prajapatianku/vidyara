package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.RegistrationRequest
import com.example.ui.theme.PrimaryViolet

@Composable
fun ApproveRegistrationDialog(
    request: RegistrationRequest,
    onApprove: (customSeat: String, customFee: Int) -> Unit,
    onReject: () -> Unit,
    onDismiss: () -> Unit
) {
    var seatInput by remember {
        mutableStateOf(
            if (request.preferredSeat.startsWith("Seat ")) request.preferredSeat.removePrefix("Seat ").trim()
            else if (request.preferredSeat != "Any Available") request.preferredSeat
            else ""
        )
    }
    var feeInput by remember { mutableStateOf("1000") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                // Title & Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Approve Admission",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1B1F)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Request Details Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF3EDF7),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = request.studentName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryViolet
                        )
                        Text(text = "📞 ${request.mobile}", fontSize = 13.sp, color = Color(0xFF49454F))
                        if (request.email.isNotBlank()) {
                            Text(text = "✉️ ${request.email}", fontSize = 13.sp, color = Color(0xFF49454F))
                        }
                        Text(text = "📚 Target Exam: ${request.course}", fontSize = 13.sp, color = Color(0xFF49454F))
                        Text(text = "⏰ Shift: ${request.requestedShift}", fontSize = 13.sp, color = Color(0xFF49454F))
                        Text(text = "🪑 Preferred Seat: ${request.preferredSeat}", fontSize = 13.sp, color = Color(0xFF49454F))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Seat Input
                OutlinedTextField(
                    value = seatInput,
                    onValueChange = { seatInput = it },
                    label = { Text("Assign Seat Number") },
                    placeholder = { Text("e.g. 15 or A-10") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Fee Input
                OutlinedTextField(
                    value = feeInput,
                    onValueChange = { feeInput = it },
                    label = { Text("Monthly Fee (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFB3261E))
                    ) {
                        Text("Reject", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            val feeVal = feeInput.toIntOrNull() ?: 1000
                            onApprove(seatInput, feeVal)
                        },
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Approve & Pass", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
