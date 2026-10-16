package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Announcement
import com.example.data.models.AnnouncementStatus
import com.example.ui.components.GlassCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.EagleBluePrimary

@Composable
fun AnnouncementsScreen(
    announcements: List<Announcement>,
    isOfficer: Boolean,
    onCreateAnnouncement: (title: String, body: String, scope: String) -> Unit,
    onArchiveAnnouncement: (id: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var titleInput by remember { mutableStateOf("") }
    var bodyInput by remember { mutableStateOf("") }
    var scopeInput by remember { mutableStateOf("Bantayog Elite Eagles Club") }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .testTag("announcements_list"),
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
                            text = "Fraternal Bulletins & Orders",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = EagleBluePrimary
                        )
                        Text(
                            text = "Official National, Regional & Club Communications",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (isOfficer) {
                        Button(
                            onClick = { showCreateDialog = true },
                            modifier = Modifier.testTag("create_announcement_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Post")
                        }
                    }
                }
            }

            items(announcements.filter { it.status == AnnouncementStatus.PUBLISHED }) { ann ->
                GlassCard(testTag = "bulletin_card_${ann.id}") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusBadge(
                            text = ann.orgScopeName,
                            backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                            textColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = ann.publishedAt,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (isOfficer) {
                                IconButton(
                                    onClick = { onArchiveAnnouncement(ann.id) },
                                    modifier = Modifier.testTag("archive_button_${ann.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Archive,
                                        contentDescription = "Archive Bulletin",
                                        tint = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = ann.title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = ann.body,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Authorized Author: ${ann.authorName}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = EagleBluePrimary
                    )
                }
            }
        }

        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                title = { Text("Post Official Bulletin", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = titleInput,
                            onValueChange = { titleInput = it },
                            label = { Text("Bulletin Subject / Title") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("bulletin_title_input")
                        )
                        OutlinedTextField(
                            value = bodyInput,
                            onValueChange = { bodyInput = it },
                            label = { Text("Official Content / Body") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("bulletin_body_input"),
                            minLines = 3
                        )
                        OutlinedTextField(
                            value = scopeInput,
                            onValueChange = { scopeInput = it },
                            label = { Text("Organization Scope") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("bulletin_scope_input")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (titleInput.isNotBlank() && bodyInput.isNotBlank()) {
                                onCreateAnnouncement(titleInput, bodyInput, scopeInput)
                                titleInput = ""
                                bodyInput = ""
                                showCreateDialog = false
                            }
                        },
                        modifier = Modifier.testTag("submit_bulletin_button")
                    ) {
                        Text("Publish Bulletin")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
