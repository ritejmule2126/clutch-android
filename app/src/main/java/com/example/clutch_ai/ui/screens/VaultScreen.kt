package com.example.clutch_ai.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.clutch_ai.model.DocumentChunk
import com.example.clutch_ai.model.StashDocument
import com.example.clutch_ai.ui.theme.MidnightChassis
import com.example.clutch_ai.ui.theme.StatusRose
import com.example.clutch_ai.ui.theme.TextMuted
import com.example.clutch_ai.ui.theme.TextPrimary

@Composable
fun VaultScreen(
    documents: List<StashDocument>,
    totalVectors: Int,
    storageKb: Long,
    onPurgeVault: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedDocId by remember { mutableStateOf<String?>(null) }
    var showPurgeConfirmation by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val allChunks = remember(documents) {
        documents.flatMap { doc ->
            if (doc.chunks.isEmpty()) {
                listOf(
                    DocumentChunk(
                        id = "default_${doc.id}",
                        docId = doc.id,
                        docTitle = doc.title,
                        chunkIndex = 1,
                        text = doc.summary,
                        vectorPreview = emptyList(),
                        cosineScore = 0.95f,
                        byteSize = 256
                    )
                )
            } else doc.chunks
        }
    }

    val filteredChunks = remember(searchQuery, selectedDocId, allChunks) {
        allChunks.filter { chunk ->
            (selectedDocId == null || chunk.docId == selectedDocId) &&
                    (searchQuery.isBlank() || chunk.docTitle.contains(searchQuery, ignoreCase = true) ||
                            chunk.text.contains(searchQuery, ignoreCase = true))
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // 1. CLEAN VAULT HEADER
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(18.dp))
                .padding(18.dp)
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
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFEEF2F6))
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderSpecial,
                                contentDescription = null,
                                tint = MidnightChassis,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Knowledge Vault",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MidnightChassis
                            )
                            Text(
                                text = "Indexed documents & notes",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(StatusRose.copy(alpha = 0.10f))
                            .clickable { showPurgeConfirmation = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Clear Vault",
                                tint = StatusRose,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Clear Vault",
                                color = StatusRose,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "SAVED FILES",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextMuted
                        )
                        Text(
                            text = "${documents.size} Files",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MidnightChassis
                        )
                    }

                    Column {
                        Text(
                            text = "INDEXED SEGMENTS",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextMuted
                        )
                        Text(
                            text = "${allChunks.size} Chunks",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MidnightChassis
                        )
                    }

                    Column {
                        Text(
                            text = "STORAGE SIZE",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextMuted
                        )
                        Text(
                            text = "$storageKb KB",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MidnightChassis
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. SEARCH BAR
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search indexed knowledge...", color = TextMuted, fontSize = 13.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFE2E8F0),
                focusedBorderColor = MidnightChassis,
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Document Filter Chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                val isSelected = selectedDocId == null
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) MidnightChassis else Color(0xFFF1F5F9))
                        .clickable { selectedDocId = null }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "All Chunks (${allChunks.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) Color.White else MidnightChassis
                    )
                }
            }

            items(documents) { doc ->
                val isSelected = selectedDocId == doc.id
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) MidnightChassis else Color(0xFFF1F5F9))
                        .clickable { selectedDocId = doc.id }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = doc.title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) Color.White else MidnightChassis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3. CLEAN CHUNKS / KNOWLEDGE SEGMENTS LIST
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(filteredChunks) { chunk ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = Color(0xFF0284C7),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = chunk.docTitle,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MidnightChassis
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFF1F5F9))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Segment ${chunk.chunkIndex}",
                                    color = TextMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = chunk.text,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            fontWeight = FontWeight.Normal,
                            color = TextPrimary.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        // Confirmation Dialog
        if (showPurgeConfirmation) {
            AlertDialog(
                onDismissRequest = { showPurgeConfirmation = false },
                title = { Text("Clear Knowledge Vault?", fontWeight = FontWeight.Bold) },
                text = { Text("This will remove all indexed document chunks from your knowledge base.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onPurgeVault()
                            showPurgeConfirmation = false
                            Toast.makeText(context, "Knowledge vault cleared", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("Clear All", color = StatusRose, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPurgeConfirmation = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
