package com.example.clutch_ai.model

enum class FileType(val label: String, val extension: String) {
    PDF("PDF Document", ".pdf"),
    IMAGE("OCR Image", ".png"),
    MARKDOWN("Markdown Note", ".md"),
    TEXT("Raw Text", ".txt")
}

data class DocumentChunk(
    val id: String,
    val docId: String,
    val docTitle: String,
    val chunkIndex: Int,
    val text: String,
    val vectorPreview: List<Float>,
    val cosineScore: Float = 0.92f,
    val byteSize: Int = 512,
    val sectionRef: String = "Section 1.2"
)

data class StashDocument(
    val id: String,
    val title: String,
    val fileType: FileType,
    val fileSizeFormat: String,
    val chunkCount: Int,
    val addedDate: String,
    val vectorDimension: String = "384d",
    val summary: String,
    val chunks: List<DocumentChunk> = emptyList()
)

data class Citation(
    val number: Int,
    val docTitle: String,
    val excerpt: String,
    val score: Float,
    val chunkIndex: Int
)

enum class MessageSender {
    USER, AI
}

data class CodeSnippet(
    val language: String,
    val code: String
)

data class ChatMessage(
    val id: String,
    val sender: MessageSender,
    val text: String,
    val timestamp: String,
    val latencyMs: Long? = null,
    val modelName: String? = null,
    val codeSnippet: CodeSnippet? = null,
    val citations: List<Citation> = emptyList(),
    val memoryHits: List<String> = emptyList(),
    val attachedDocTitle: String? = null,
    val thoughtText: String? = null,
    val isThinking: Boolean = false
)

data class ChatSession(
    val id: String,
    val title: String,
    val lastUpdated: String,
    val messages: List<ChatMessage>
)

data class TelemetryData(
    val totalFiles: Int,
    val totalVectors: Int,
    val totalChunks: Int,
    val storageEstimateKb: Long,
    val scanLatencyMs: Float,
    val engineName: String
)

enum class MemoryCategory(val label: String, val badgeColorHex: Long) {
    PERSONAL_FACT("About Me", 0xFF00D2EE),
    CORE_RULE("Rules & Tone", 0xFF6366F1),
    WORK_HABIT("Work & Tech", 0xFF10B981),
    UNIVERSAL_WISDOM("Mindset & Values", 0xFFF59E0B),
    SYSTEM_DIRECTIVE("Preferences", 0xFF8B5CF6)
}

data class TrainedMemory(
    val id: String,
    val category: MemoryCategory,
    val title: String,
    val content: String,
    val confidenceScore: Float = 0.96f,
    val vectorDimension: Int = 384,
    val learnedAt: String,
    val isActive: Boolean = true
)

data class TrainingProgressState(
    val isTraining: Boolean = false,
    val currentEpoch: Int = 0,
    val totalEpochs: Int = 3,
    val currentLoss: Float = 0.0f,
    val progress: Float = 0.0f,
    val statusMessage: String = ""
)

data class KnowledgeDomain(
    val title: String,
    val era: String,
    val description: String,
    val isPreloaded: Boolean = true
)
