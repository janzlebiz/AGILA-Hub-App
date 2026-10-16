package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.AttendanceRecord
import com.example.data.models.AttendanceState
import com.example.data.models.EventItem
import com.example.ui.components.GlassCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.EagleBluePrimary
import com.example.ui.theme.ForestGreenContainer
import com.example.ui.theme.ForestGreenSecondary

@Composable
fun AttendanceScreen(
    events: List<EventItem>,
    records: List<AttendanceRecord>,
    isOfficer: Boolean,
    onRecordAttendance: (eventId: String, memberName: String, state: AttendanceState) -> Boolean,
    modifier: Modifier = Modifier
) {
    var selectedEvent by remember { mutableStateOf(events.firstOrNull()) }
    var memberNameInput by remember { mutableStateOf("Kuya Janzle Santos") }
    var recordMessage by remember { mutableStateOf<Pair<String, Boolean>?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("attendance_screen_column"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Fraternal Attendance Registry",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = EagleBluePrimary
            )
            Text(
                text = "Official Event Attendance & Roll Call Logs",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Attendance Recording Card for Officers
        if (isOfficer && events.isNotEmpty()) {
            item {
                GlassCard(testTag = "attendance_recording_card") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.HowToReg,
                            contentDescription = null,
                            tint = EagleBluePrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Record Member Attendance",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = EagleBluePrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Select Scheduled Event:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    events.forEach { evt ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RadioButton(
                                selected = (selectedEvent?.id == evt.id),
                                onClick = { selectedEvent = evt },
                                modifier = Modifier.testTag("event_radio_${evt.id}")
                            )
                            Column {
                                Text(
                                    text = evt.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "${evt.eventDate} • ${evt.orgScopeName}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = memberNameInput,
                        onValueChange = { memberNameInput = it; recordMessage = null },
                        label = { Text("Member Full Name") },
                        leadingIcon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("attendance_member_input"),
                        singleLine = true
                    )

                    if (recordMessage != null) {
                        Text(
                            text = recordMessage!!.first,
                            color = if (recordMessage!!.second) ForestGreenSecondary else MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (selectedEvent != null && memberNameInput.isNotBlank()) {
                                val success = onRecordAttendance(selectedEvent!!.id, memberNameInput, AttendanceState.PRESENT)
                                recordMessage = if (success) {
                                    Pair("Attendance recorded successfully for $memberNameInput!", true)
                                } else {
                                    Pair("Duplicate Entry: Member already recorded for this event.", false)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_attendance_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EagleBluePrimary)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Record Attendance", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Recorded Logs List
        item {
            Text(
                text = "Recent Verified Attendance History",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        items(records) { rec ->
            GlassCard(testTag = "attendance_record_${rec.id}") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = rec.memberName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    StatusBadge(
                        text = rec.status.name,
                        backgroundColor = ForestGreenContainer,
                        textColor = ForestGreenSecondary
                    )
                }

                Text(
                    text = rec.eventTitle,
                    fontSize = 13.sp,
                    color = EagleBluePrimary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Recorded by ${rec.recordedBy}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = rec.recordedAt,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
