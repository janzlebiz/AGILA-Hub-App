package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.AuditEvent
import com.example.data.models.Invitation
import com.example.data.models.RoleCode
import com.example.ui.components.GlassCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.EagleBlueDark
import com.example.ui.theme.EagleBluePrimary
import com.example.ui.theme.ForestGreenContainer
import com.example.ui.theme.ForestGreenSecondary

@Composable
fun AdminDashboardScreen(
    invitations: List<Invitation>,
    auditLogs: List<AuditEvent>,
    onCreateInvitation: (email: String, role: RoleCode, targetOrg: String) -> Invitation,
    modifier: Modifier = Modifier
) {
    var showCreateInviteDialog by remember { mutableStateOf(false) }
    var emailInput by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(RoleCode.MEMBER) }
    var orgScopeInput by remember { mutableStateOf("Bantayog Elite Eagles Club") }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .testTag("admin_dashboard_scroll"),
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
                            text = "Admin Portal & Governance",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = EagleBluePrimary
                        )
                        Text(
                            text = "Scoped Role Assignment & Audit Management",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = { showCreateInviteDialog = true },
                        modifier = Modifier.testTag("create_invitation_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Invite")
                    }
                }
            }

            // Active Pending Invitations List
            item {
                Text(
                    text = "Single-Use Member Invitations",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            items(invitations) { inv ->
                GlassCard(testTag = "invitation_item_${inv.id}") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = inv.email,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        StatusBadge(
                            text = inv.assignedRole.displayName,
                            backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                            textColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Target Scope: ${inv.targetOrgName}",
                        fontSize = 12.sp,
                        color = EagleBluePrimary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Expires: ${inv.expiresAt}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        StatusBadge(
                            text = if (inv.isRedeemed) "REDEEMED" else "ACTIVE",
                            backgroundColor = ForestGreenContainer,
                            textColor = ForestGreenSecondary
                        )
                    }
                }
            }

            // System Audit Log Stream
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Real-Time System Audit Event Stream",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Icon(
                        imageVector = Icons.Default.Assessment,
                        contentDescription = null,
                        tint = EagleBluePrimary
                    )
                }
            }

            items(auditLogs) { log ->
                GlassCard(testTag = "audit_log_${log.id}") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = log.action,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = EagleBlueDark
                        )
                        Text(
                            text = log.timestamp,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = "Actor: ${log.actorName} • Resource: ${log.resourceType} • Scope: ${log.orgScope}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        if (showCreateInviteDialog) {
            AlertDialog(
                onDismissRequest = { showCreateInviteDialog = false },
                title = { Text("Generate Member Invitation", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("Applicant Email Address") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("invite_email_input")
                        )

                        Text("Select Assigned Role:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

                        RoleCode.values().forEach { role ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                RadioButton(
                                    selected = (selectedRole == role),
                                    onClick = { selectedRole = role },
                                    modifier = Modifier.testTag("role_radio_${role.name}")
                                )
                                Text(
                                    text = role.displayName,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        OutlinedTextField(
                            value = orgScopeInput,
                            onValueChange = { orgScopeInput = it },
                            label = { Text("Organization Scope") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("invite_scope_input")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (emailInput.isNotBlank()) {
                                onCreateInvitation(emailInput, selectedRole, orgScopeInput)
                                emailInput = ""
                                showCreateInviteDialog = false
                            }
                        },
                        modifier = Modifier.testTag("submit_invitation_button")
                    ) {
                        Text("Create Invitation Token")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateInviteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
