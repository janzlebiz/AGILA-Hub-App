package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.ContributionRecord
import com.example.ui.components.GlassCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.BumblebeeYellowTertiary
import com.example.ui.theme.EagleBlueDark
import com.example.ui.theme.EagleBluePrimary
import com.example.ui.theme.ForestGreenContainer
import com.example.ui.theme.ForestGreenSecondary

@Composable
fun FinanceScreen(
    contributions: List<ContributionRecord>,
    isTreasurer: Boolean,
    onRecordContribution: (memberName: String, amount: Double, purpose: String) -> Boolean,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var memberNameInput by remember { mutableStateOf("Kuya Janzle Santos") }
    var amountInput by remember { mutableStateOf("1000") }
    var purposeInput by remember { mutableStateOf("Special Building Assessment 2026") }

    val totalAmount = contributions.sumOf { it.amount }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .testTag("finance_screen_feed"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Fraternal Treasury & Dues",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = EagleBluePrimary
                        )
                        Text(
                            text = "Audited Fixed-Precision Contributions Ledger",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (isTreasurer) {
                        Button(
                            onClick = { showAddDialog = true },
                            modifier = Modifier.testTag("record_contribution_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Record")
                        }
                    }
                }
            }

            // Summary Balance Card
            item {
                GlassCard(testTag = "finance_summary_card") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = EagleBlueDark,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = BumblebeeYellowTertiary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Total Verifiable Contributions",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 13.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "₱ %,.2f".format(totalAmount),
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "Audited Ledger • Fixed Decimal Precision • Currency: PHP",
                                color = BumblebeeYellowTertiary,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            // Contributions List Header
            item {
                Text(
                    text = "Personal & Club Verified Receipts",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            items(contributions) { con ->
                GlassCard(testTag = "contribution_item_${con.id}") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = con.purpose,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "₱ %,.2f".format(con.amount),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreenSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Member: ${con.memberName}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        StatusBadge(
                            text = con.status.name,
                            backgroundColor = ForestGreenContainer,
                            textColor = ForestGreenSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Audited by ${con.recordedBy}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = con.effectiveDate,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Record Contribution Receipt", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = memberNameInput,
                            onValueChange = { memberNameInput = it },
                            label = { Text("Member Name") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("contribution_member_input")
                        )
                        OutlinedTextField(
                            value = amountInput,
                            onValueChange = { amountInput = it },
                            label = { Text("Amount (PHP)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("contribution_amount_input")
                        )
                        OutlinedTextField(
                            value = purposeInput,
                            onValueChange = { purposeInput = it },
                            label = { Text("Purpose / Fee Category") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("contribution_purpose_input")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val amt = amountInput.toDoubleOrNull() ?: 0.0
                            if (amt > 0 && memberNameInput.isNotBlank() && purposeInput.isNotBlank()) {
                                onRecordContribution(memberNameInput, amt, purposeInput)
                                showAddDialog = false
                            }
                        },
                        modifier = Modifier.testTag("submit_contribution_button")
                    ) {
                        Text("Record Entry")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
