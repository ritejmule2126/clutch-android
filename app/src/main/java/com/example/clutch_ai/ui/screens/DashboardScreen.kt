package com.example.clutch_ai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.clutch_ai.model.FileType
import com.example.clutch_ai.model.StashDocument
import com.example.clutch_ai.ui.theme.*
import com.example.clutch_ai.viewmodel.UploadProgressState
import com.example.clutch_ai.viewmodel.UploadStep

@Composable
fun DashboardScreen(
    documents: List<StashDocument>,
    activeFilter: String,
    uploadState: UploadProgressState,
    totalFiles: Int,
    totalChunks: Int,
    storageKb: Long,
    scanLatencyMs: Float,
    engineName: String,
    onFilterChanged: (String) -> Unit,
    onUploadDocument: (android.net.Uri) -> Unit = {},
    onNavigateToChatWithPrompt: (String) -> Unit,
    onChatWithDoc: (StashDocument) -> Unit,
    onInspectDoc: (StashDocument) -> Unit,
    modifier: Modifier = Modifier
) {
        var omnibarQuery by remember { mutableStateOf("") }
    val context = LocalContext.current
    val docPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: android.net.Uri? ->
        uri?.let { onUploadDocument(it) }
    }

    val filteredDocs = remember(activeFilter, documents) {
        if (activeFilter == "All") documents
        else documents.filter {
            when (activeFilter) {
                "PDFs" -> it.fileType == FileType.PDF
                "Notes" -> it.fileType == FileType.MARKDOWN || it.fileType == FileType.TEXT
                "Images" -> it.fileType == FileType.IMAGE
                else -> true
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(2.dp)) }

        // 1. CLEAN GREETING & PROMPT INPUT
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                Text(
                    text = "Good day, Ritej",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MidnightChassis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "What would you like to explore or work on?",
                    fontSize = 13.sp,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Search & Prompt Input
                OutlinedTextField(
                    value = omnibarQuery,
                    onValueChange = { omnibarQuery = it },
                    placeholder = { Text("Ask anything or plan your day...", color = TextMuted, fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (omnibarQuery.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(ElectricCyan)
                                    .clickable {
                                        onNavigateToChatWithPrompt(omnibarQuery)
                                        omnibarQuery = ""
                                    }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = "Send",
                                    tint = MidnightChassis,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedBorderColor = ElectricCyanDark,
                        unfocusedContainerColor = Color(0xFFF8FAFC),
                        focusedContainerColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Suggestion Chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val quickIdeas = listOf(
                        "⚡ Plan my day" to "Plan my day with high-leverage time blocks",
                        "💡 Brainstorm" to "Brainstorm innovative solutions and product concepts",
                        "✉️ Draft email" to "Draft a professional follow-up email",
                        "🏛️ Timeless wisdom" to "Explain the four Yugas and the Bhagavad Gita"
                    )
                    items(quickIdeas) { (label, prompt) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF1F5F9))
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                                .clickable { onNavigateToChatWithPrompt(prompt) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MidnightChassis
                            )
                        }
                    }
                }
            }
        }

        // 2. CLEAN 3-METRIC STATS OVERVIEW
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Metric 1: Documents
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "DOCS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$totalFiles Files",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MidnightChassis
                        )
                        Text(
                            text = "Knowledge base",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                // Metric 2: Segments
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Segment,
                                contentDescription = null,
                                tint = Color(0xFF059669),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CHUNKS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$totalChunks Segments",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MidnightChassis
                        )
                        Text(
                            text = "Indexed & active",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                // Metric 3: Storage
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = null,
                                tint = Color(0xFF6366F1),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "STORAGE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$storageKb KB",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MidnightChassis
                        )
                        Text(
                            text = "Local footprint",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }

        // 3. FILE INTAKE & ACTION CARD
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ADD DOCUMENTS & NOTES",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MidnightChassis,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Expand your assistant knowledge with reference files",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (uploadState.step != UploadStep.IDLE) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF1F5F9))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = when (uploadState.step) {
                                        UploadStep.SCANNING -> "Reading ${uploadState.fileName}..."
                                        UploadStep.PARSING -> "Extracting key insights..."
                                        UploadStep.GENERATING_VECTORS -> "Indexing for fast search..."
                                        UploadStep.STASHED -> "Saved successfully!"
                                        else -> ""
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MidnightChassis
                                )
                                Text(
                                    text = "${(uploadState.progress * 100).toInt()}%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF059669)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { uploadState.progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = ElectricCyanDark,
                                trackColor = Color(0xFFE2E8F0)
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    docPickerLauncher.launch(arrayOf("text/*", "application/json", "text/markdown", "*/*"))
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFF1F5F9),
                                    contentColor = MidnightChassis
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EditNote,
                                    contentDescription = null,
                                    tint = MidnightChassis,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Import Note", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    docPickerLauncher.launch(arrayOf("application/pdf", "*/*"))
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MidnightChassis,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Import File", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // 4. SAVED KNOWLEDGE & DOCUMENTS LIST
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SAVED DOCUMENTS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MidnightChassis,
                        letterSpacing = 0.5.sp
                    )

                    Text(
                        text = "${filteredDocs.size} Files",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Filter Chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val filters = listOf("All", "PDFs", "Notes", "Images")
                    items(filters) { filter ->
                        val isSelected = filter == activeFilter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) MidnightChassis else Color(0xFFF1F5F9))
                                .clickable { onFilterChanged(filter) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = filter,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) Color.White else MidnightChassis
                            )
                        }
                    }
                }
            }
        }

        // Clean Document List Items
        if (filteredDocs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                        .padding(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F5F9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Documents Indexed",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MidnightChassis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap 'Import File' above to index PDFs or notes directly into your knowledge base.",
                            fontSize = 12.sp,
                            color = TextMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(filteredDocs) { doc ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when (doc.fileType) {
                                            FileType.PDF -> Color(0xFFEF4444).copy(alpha = 0.10f)
                                            FileType.MARKDOWN -> Color(0xFF6366F1).copy(alpha = 0.10f)
                                            else -> Color(0xFF0284C7).copy(alpha = 0.10f)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (doc.fileType) {
                                        FileType.PDF -> Icons.Default.Description
                                        FileType.MARKDOWN -> Icons.Default.EditNote
                                        else -> Icons.Default.Folder
                                    },
                                    contentDescription = null,
                                    tint = when (doc.fileType) {
                                        FileType.PDF -> Color(0xFFEF4444)
                                        FileType.MARKDOWN -> Color(0xFF6366F1)
                                        else -> Color(0xFF0284C7)
                                    },
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = doc.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MidnightChassis
                                )
                                Text(
                                    text = "${doc.fileSizeFormat} • ${doc.addedDate}",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = doc.summary,
                        fontSize = 12.sp,
                        color = TextPrimary.copy(alpha = 0.85f),
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF1F5F9))
                                .clickable { onInspectDoc(doc) }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "View Summary",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MidnightChassis
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MidnightChassis)
                                .clickable { onChatWithDoc(doc) }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "Reference in Chat",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ElectricCyan
                            )
                        }
                    }
                }
            }
        }

        }

        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
}
