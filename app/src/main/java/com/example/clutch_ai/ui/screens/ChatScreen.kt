package com.example.clutch_ai.ui.screens
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.material.icons.filled.Close

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.clutch_ai.model.ChatMessage
import com.example.clutch_ai.model.Citation
import com.example.clutch_ai.model.CodeSnippet
import com.example.clutch_ai.model.MessageSender
import com.example.clutch_ai.ui.components.ClayButton
import com.example.clutch_ai.ui.components.ClayCard
import com.example.clutch_ai.ui.components.ClayPill
import com.example.clutch_ai.ui.components.ClayTextField
import com.example.clutch_ai.ui.components.ClutchReactorLogo
import com.example.clutch_ai.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatScreen(
    messages: List<ChatMessage>,
    activeSessionTitle: String,
    attachedDocTitle: String?,
    isGenerating: Boolean,
    onSendMessage: (String) -> Unit,
    onOpenHistory: () -> Unit,
    onNewChat: () -> Unit,
    onRemoveAttachedDoc: () -> Unit,
    onSelectCitation: (Citation) -> Unit,
    onAttachDocument: ((android.net.Uri) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var promptInput by remember { mutableStateOf("") }
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: android.net.Uri? ->
        uri?.let { onAttachDocument?.invoke(it) }
    }
    val listState = rememberLazyListState()
    val isImeVisible = WindowInsets.isImeVisible

    // Auto-scroll on new messages or keyboard open
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }
    LaunchedEffect(isImeVisible) {
        if (isImeVisible && messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val userMessageCount = messages.count { it.sender == MessageSender.USER }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Top Action Bar: Hamburger Menu on left under the top-bar logo, New Chat on right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onOpenHistory,
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEEF3F9))
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Chat History Menu",
                    tint = Color(0xFF0F172A),
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(
                onClick = onNewChat,
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEEF3F9))
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Start New Chat",
                    tint = Color(0xFF0F172A),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 2. CONVERSATION STREAM OR WELCOME HERO
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Ultra-Clean Welcome Hero when chat is fresh
            if (userMessageCount == 0) {
                item {
                    ChatWelcomeHero()
                }
            }

            items(messages) { message ->
                if (message.sender == MessageSender.AI) {
                    AiMessageItem(
                        message = message,
                        onCitationClick = onSelectCitation,
                        onCopyText = { text ->
                            copyToClipboard(context, "AI Response", text)
                        },
                        onRegenerate = {
                            val lastUserMsg = messages.lastOrNull { it.sender == MessageSender.USER }
                            if (lastUserMsg != null) {
                                onSendMessage(lastUserMsg.text)
                            }
                        }
                    )
                } else {
                    UserMessageItem(
                        message = message,
                        onCopyText = { text ->
                            copyToClipboard(context, "Prompt", text)
                        }
                    )
                }
            }

            if (isGenerating) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 8.dp, top = 8.dp, bottom = 8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = ElectricCyanDark,
                            strokeWidth = 2.dp
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(6.dp)) }
        }

        // 3. ATTACHED DOCUMENT PILL (IF ACTIVE)
        if (attachedDocTitle != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MidnightChassis)
                        .border(1.dp, MidnightChassisBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Referencing: $attachedDocTitle",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier
                                .size(14.dp)
                                .clickable { onRemoveAttachedDoc() }
                        )
                    }
                }
            }
        }

        // 4. BOTTOM PROMPT CAPSULE
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            ClayTextField(
                value = promptInput,
                onValueChange = { promptInput = it },
                placeholder = "Message Clutch AI...",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.AttachFile,
                        contentDescription = "Attach Document from Device",
                        tint = ElectricCyanDark,
                        modifier = Modifier
                            .size(22.dp)
                            .clickable {
                                filePickerLauncher.launch(
                                    arrayOf(
                                        "*/*",
                                        "application/pdf",
                                        "text/*",
                                        "application/json",
                                        "image/*"
                                    )
                                )
                            }
                    )
                },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (promptInput.isEmpty() && clipboardManager.hasText()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFE0F2FE))
                                    .clickable {
                                        clipboardManager.getText()?.text?.let { promptInput = it }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Paste",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricCyanDark,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        ClayButton(
                            onClick = {
                                if (promptInput.isNotBlank()) {
                                    onSendMessage(promptInput)
                                    promptInput = ""
                                }
                            },
                            backgroundColor = ElectricCyan,
                            contentColor = MidnightChassis,
                            cornerRadius = 14.dp
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = MidnightChassis,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                cornerRadius = 24.dp
            )
        }
    }
}

/**
 * Ultra-Clean Welcome Hero: Logo at Center (No Container) + "How can I help you today?"
 */
@Composable
fun ChatWelcomeHero() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 90.dp, bottom = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo at center WITHOUT container
        ClutchReactorLogo(size = 54.dp, showText = false)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "How can I help you today?",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MidnightChassis
        )
    }
}

@Composable
fun AiMessageItem(
    message: ChatMessage,
    onCitationClick: (Citation) -> Unit,
    onCopyText: (String) -> Unit,
    onRegenerate: () -> Unit
) {
    var feedbackState by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // AI Header Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ClutchReactorLogo(size = 26.dp, showText = false)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Clutch AI",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = MidnightChassis
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF1F5F9))
                        .border(1.dp, ClayBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Assistant • On-Device",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Thinking indicator loader under AI header
        if (message.isThinking || (message.text.isBlank() && message.sender == MessageSender.AI)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF1F5F9))
                    .border(1.dp, ClayBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(12.dp),
                    strokeWidth = 2.dp,
                    color = ElectricCyan
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Thinking...",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Markdown Free-Style Text Column
        if (message.text.isNotBlank()) {
            MarkdownTextColumn(text = message.text, codeSnippet = message.codeSnippet)
        }

        // Recalled Personal Memory Tags
        if (message.memoryHits.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = null,
                    tint = ElectricCyanDark,
                    modifier = Modifier.size(14.dp)
                )
                message.memoryHits.forEach { tag ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF1F5F9))
                            .border(1.dp, ClayBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = tag,
                            color = MidnightChassis,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Interactive Citation Footnotes
        if (message.citations.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Sources:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace
                )
                message.citations.forEach { citation ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFECFEFF))
                            .border(1.dp, Color(0xFFA5F3FC), RoundedCornerShape(8.dp))
                            .clickable { onCitationClick(citation) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "[${citation.number}] ${citation.docTitle.take(18)}...",
                            color = Color(0xFF0E7490),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Action Toolbar (Copy, Regenerate, Thumbs)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onCopyText(message.text) },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = TextMuted,
                    modifier = Modifier.size(15.dp)
                )
            }

            IconButton(
                onClick = onRegenerate,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Regenerate",
                    tint = TextMuted,
                    modifier = Modifier.size(15.dp)
                )
            }

            IconButton(
                onClick = {
                    feedbackState = if (feedbackState == "up") null else "up"
                },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ThumbUp,
                    contentDescription = "Good response",
                    tint = if (feedbackState == "up") StatusEmerald else TextMuted,
                    modifier = Modifier.size(15.dp)
                )
            }

            IconButton(
                onClick = {
                    feedbackState = if (feedbackState == "down") null else "down"
                },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ThumbDown,
                    contentDescription = "Bad response",
                    tint = if (feedbackState == "down") Color(0xFFEF4444) else TextMuted,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

@Composable
fun UserMessageItem(
    message: ChatMessage,
    onCopyText: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.End
    ) {
        val bubbleShape = RoundedCornerShape(
            topStart = 20.dp,
            topEnd = 4.dp,
            bottomStart = 20.dp,
            bottomEnd = 20.dp
        )

        Box(
            modifier = Modifier
                .clip(bubbleShape)
                .background(MidnightChassis)
                .border(1.dp, MidnightChassisBorder, bubbleShape)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = message.text,
                color = Color.White,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = message.timestamp,
                fontSize = 10.sp,
                color = TextMuted,
                fontFamily = FontFamily.Monospace
            )
            IconButton(
                onClick = { onCopyText(message.text) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = TextMuted,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
fun MarkdownTextColumn(
    text: String,
    codeSnippet: CodeSnippet? = null
) {
    val context = LocalContext.current
    val lines = text.split("\n")

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        lines.forEach { line ->
            when {
                line.startsWith("### ") -> {
                    Text(
                        text = line.removePrefix("### "),
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MidnightChassis,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                line.startsWith("## ") -> {
                    Text(
                        text = line.removePrefix("## "),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 19.sp,
                        color = MidnightChassis,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }

                line.startsWith("#### ") -> {
                    Text(
                        text = line.removePrefix("#### "),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MidnightChassis,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                line.startsWith("- ") || line.startsWith("• ") || line.startsWith("1. ") || line.startsWith("2. ") || line.startsWith("3. ") || line.startsWith("4. ") -> {
                    val formatted = buildAnnotatedString {
                        val clean = if (line.startsWith("- ") || line.startsWith("• ")) line.substring(2) else line.substringAfter(" ")
                        val parts = clean.split("**")
                        parts.forEachIndexed { index, part ->
                            if (index % 2 == 1) {
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MidnightChassis)) {
                                    append(part)
                                }
                            } else {
                                append(part)
                            }
                        }
                    }
                    Row(modifier = Modifier.padding(start = 6.dp)) {
                        Text(
                            text = "• ",
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyanDark
                        )
                        Text(
                            text = formatted,
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                            color = TextPrimary
                        )
                    }
                }

                line.startsWith("```") -> {
                    // Handled below via codeSnippet
                }

                line.isNotBlank() -> {
                    val formatted = buildAnnotatedString {
                        val parts = line.split("**")
                        parts.forEachIndexed { index, part ->
                            if (index % 2 == 1) {
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MidnightChassis)) {
                                    append(part)
                                }
                            } else {
                                append(part)
                            }
                        }
                    }
                    Text(
                        text = formatted,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        color = TextPrimary
                    )
                }
            }
        }

        // Code Capsule Block
        codeSnippet?.let { snippet ->
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MidnightChassis)
                    .border(1.dp, MidnightChassisBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(ElectricCyan.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = snippet.language,
                                color = ElectricCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        ClayPill(
                            text = "Copy Code",
                            isSelected = false,
                            activeColor = MidnightChassis,
                            activeTextColor = ElectricCyan,
                            onClick = {
                                copyToClipboard(context, "Code Snippet", snippet.code)
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = snippet.code,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
}
