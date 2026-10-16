package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.ConstitutionQueryResult
import com.example.data.models.DocItem
import com.example.ui.components.GlassCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.BumblebeeYellowContainer
import com.example.ui.theme.EagleBlueDark
import com.example.ui.theme.EagleBluePrimary
import com.example.ui.theme.ForestGreenContainer
import com.example.ui.theme.ForestGreenSecondary

@Composable
fun ConstitutionRagScreen(
    documents: List<DocItem>,
    onQueryConstitution: (prompt: String) -> ConstitutionQueryResult,
    modifier: Modifier = Modifier
) {
    var queryInput by remember { mutableStateOf("What is the quorum requirement for regular club meetings?") }
    var currentResult by remember { mutableStateOf<ConstitutionQueryResult?>(null) }

    LaunchedEffect(Unit) {
        currentResult = onQueryConstitution(queryInput)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("rag_screen_feed"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Constitution & By-Laws RAG Q&A",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = EagleBluePrimary
            )
            Text(
                text = "Source-Grounded Fraternal Rules & Authoritative Retrieval",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Search Prompt Input Box
        item {
            GlassCard(testTag = "rag_search_card") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = EagleBluePrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Query Approved Constitution",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = EagleBluePrimary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = queryInput,
                    onValueChange = { queryInput = it },
                    label = { Text("Ask a Constitutional or Parliamentary Question") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rag_query_input"),
                    singleLine = false,
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (queryInput.isNotBlank()) {
                            currentResult = onQueryConstitution(queryInput)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("submit_rag_query_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EagleBluePrimary)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Search & Retrieve Answer", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Search Result & Citation Output
        if (currentResult != null) {
            val result = currentResult!!
            item {
                GlassCard(testTag = "rag_answer_card") {
                    if (result.isAbstained) {
                        // Abstention State
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.errorContainer,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "System Abstention Notice",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Text(
                                        text = result.abstentionReason ?: "Query could not be grounded in ingested Constitution sources.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        // Grounded Answer State
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StatusBadge(
                                text = "GROUNDED ANSWER",
                                backgroundColor = ForestGreenContainer,
                                textColor = ForestGreenSecondary
                            )
                            Text(
                                text = "Citations: ${result.citations.size}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = EagleBluePrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = result.answer,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Verified Document Citations:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = EagleBlueDark
                        )

                        result.citations.forEach { cite ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                                    .background(
                                        color = BumblebeeYellowContainer,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(10.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Bookmark,
                                            contentDescription = null,
                                            tint = EagleBlueDark,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${cite.documentTitle} • ${cite.articleRef}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EagleBlueDark
                                        )
                                    }
                                    Text(
                                        text = "${cite.sectionRef}: \"${cite.excerpt}\"",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Ingested Source Documents Library
        item {
            Text(
                text = "Ingested & Approved Document Ingest Library",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        items(documents) { doc ->
            GlassCard(testTag = "ingested_doc_${doc.id}") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = EagleBluePrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = doc.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Edition: ${doc.version} • Category: ${doc.category}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    StatusBadge(
                        text = "APPROVED",
                        backgroundColor = ForestGreenContainer,
                        textColor = ForestGreenSecondary
                    )
                }
            }
        }
    }
}
