package com.example.clutch_ai

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.clutch_ai.ui.components.ChatHistoryBottomSheet
import com.example.clutch_ai.ui.components.ChatHistoryDrawerContent
import com.example.clutch_ai.ui.components.CinematicIntroScreen
import com.example.clutch_ai.ui.components.CitationBottomSheet
import com.example.clutch_ai.ui.components.ClutchBottomNavBar
import com.example.clutch_ai.ui.components.ProfileModal
import com.example.clutch_ai.ui.components.ClutchTopAppBar
import com.example.clutch_ai.ui.components.NavigationTab
import com.example.clutch_ai.ui.components.SpotlightSearchModal
import com.example.clutch_ai.ui.screens.ChatScreen
import com.example.clutch_ai.ui.screens.DashboardScreen
import com.example.clutch_ai.ui.screens.TrainScreen
import com.example.clutch_ai.ui.screens.VaultScreen
import com.example.clutch_ai.ui.screens.AuthScreen
import com.example.clutch_ai.ui.theme.CanvasBackground
import com.example.clutch_ai.ui.theme.ClutchaiTheme
import com.example.clutch_ai.viewmodel.ClutchViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            splashScreen.setOnExitAnimationListener { splashScreenView ->
                splashScreenView.remove()
            }
        }
        enableEdgeToEdge()
        setContent {
            ClutchaiTheme {
                ClutchAppRoot()
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ClutchAppRoot(
    viewModel: ClutchViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val tabs = remember {
        listOf(NavigationTab.DASHBOARD, NavigationTab.CHAT, NavigationTab.TRAIN, NavigationTab.VAULT)
    }
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()
    var isSidebarOpen by remember { mutableStateOf(false) }
    var isProfileOpen by remember { mutableStateOf(false) }

    var showIntro by rememberSaveable { mutableStateOf(true) }
    val isImeVisible = WindowInsets.isImeVisible

    if (!uiState.isLoggedIn) {
        AuthScreen(
            isLoading = uiState.isAuthLoading,
            errorMessage = uiState.authErrorMessage,
            onSignUp = { fullName, email, password ->
                viewModel.signUp(fullName, email, password)
            },
            onSignIn = { email, password ->
                viewModel.signIn(email, password)
            }
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CanvasBackground)
        ) {
        // Main App Scaffold (Pre-composed underneath so Dashboard is instantly ready)
        Scaffold(
            topBar = {
                ClutchTopAppBar(
                    onOpenProfile = { isProfileOpen = true },
                    showLogo = !showIntro
                )
            },
            bottomBar = {
                if (!isImeVisible) {
                    ClutchBottomNavBar(
                        selectedTab = tabs[pagerState.currentPage],
                        onTabSelected = { tab ->
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(tab.ordinal)
                            }
                        }
                    )
                }
            },
            containerColor = CanvasBackground,
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { innerPadding ->
            // Smooth Horizontal Pager for Swiping Between Tabs (Dashboard -> Chat -> Train -> Vault)
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .imePadding()
            ) { page ->
                when (tabs[page]) {
                    NavigationTab.DASHBOARD -> {
                        DashboardScreen(
                            userName = uiState.userFullName,
                            documents = uiState.documents,
                            activeFilter = uiState.activeFilter,
                            uploadState = uiState.uploadState,
                            totalFiles = uiState.telemetry.totalFiles,
                            totalChunks = uiState.telemetry.totalChunks,
                            storageKb = uiState.telemetry.storageEstimateKb,
                            scanLatencyMs = uiState.telemetry.scanLatencyMs,
                            engineName = uiState.telemetry.engineName,
                            onFilterChanged = { viewModel.setFilter(it) },
                            onUploadDocument = { uri ->
                                viewModel.uploadDocumentFromUri(context, uri)
                            },
                            onNavigateToChatWithPrompt = { prompt ->
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(NavigationTab.CHAT.ordinal)
                                }
                                viewModel.sendMessage(prompt)
                            },
                            onChatWithDoc = { doc ->
                                viewModel.attachDocumentToChat(doc.title)
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(NavigationTab.CHAT.ordinal)
                                }
                            },
                            onInspectDoc = { _ ->
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(NavigationTab.VAULT.ordinal)
                                }
                            }
                        )
                    }

                    NavigationTab.CHAT -> {
                        val activeSession = uiState.chatSessions.find {
                            it.id == uiState.activeSessionId
                        } ?: uiState.chatSessions.firstOrNull()

                        ChatScreen(
                            messages = activeSession?.messages ?: emptyList(),
                            activeSessionTitle = activeSession?.title ?: "Private Chat",
                            attachedDocTitle = uiState.attachedDocTitle,
                            isGenerating = uiState.isGeneratingResponse,
                            onSendMessage = { viewModel.sendMessage(it) },
                            onOpenHistory = { isSidebarOpen = true },
                            onNewChat = { viewModel.createNewChatSession() },
                            onRemoveAttachedDoc = { viewModel.removeAttachedDoc() },
                            onSelectCitation = { viewModel.setSelectedCitation(it) },
                            onAttachDocument = { uri: android.net.Uri -> viewModel.attachDocumentFromUri(context, uri) }
                        )
                    }

                    NavigationTab.TRAIN -> {
                        TrainScreen(
                            memories = uiState.memories,
                            trainingState = uiState.trainingState,
                            onTrainMemory = { title, content, category ->
                                viewModel.trainNewMemory(title, content, category)
                            },
                            onToggleMemory = { viewModel.toggleMemory(it) },
                            onDeleteMemory = { viewModel.deleteMemory(it) }
                        )
                    }

                    NavigationTab.VAULT -> {
                        VaultScreen(
                            documents = uiState.documents,
                            totalVectors = uiState.telemetry.totalVectors,
                            storageKb = uiState.telemetry.storageEstimateKb,
                            onPurgeVault = { viewModel.purgeVault() }
                        )
                    }
                }
            }
        }

        // Chat History Slide-Over Sidebar Drawer
        AnimatedVisibility(
            visible = isSidebarOpen,
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(150))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { isSidebarOpen = false }
            ) {
                AnimatedVisibility(
                    visible = isSidebarOpen,
                    enter = slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(220)),
                    exit = slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(180))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(320.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { /* Consume clicks inside drawer */ }
                    ) {
                        ChatHistoryDrawerContent(
                            sessions = uiState.chatSessions,
                            activeSessionId = uiState.activeSessionId,
                            onSelectSession = { sessionId ->
                                viewModel.selectSession(sessionId)
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(NavigationTab.CHAT.ordinal)
                                }
                                isSidebarOpen = false
                            },
                            onNewChat = {
                                viewModel.createNewChatSession()
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(NavigationTab.CHAT.ordinal)
                                }
                                isSidebarOpen = false
                            },
                            onRenameSession = { id, title -> viewModel.renameSession(id, title) },
                            onDeleteSession = { viewModel.deleteSession(it) },
                            onClose = { isSidebarOpen = false }
                        )
                    }
                }
            }
        }

        // User Profile Modal
        if (isProfileOpen) {
            ProfileModal(
                fullName = uiState.userFullName,
                email = uiState.userEmail,
                totalFiles = uiState.telemetry.totalFiles,
                storageKb = uiState.telemetry.storageEstimateKb,
                activeMemories = uiState.memories.size,
                onSignOut = {
                    viewModel.signOut()
                    isProfileOpen = false
                },
                onDismiss = { isProfileOpen = false }
            )
        }

        // Spotlight Cmd+K Search Modal
        if (uiState.isSpotlightOpen) {
            SpotlightSearchModal(
                documents = uiState.documents,
                onDismiss = { viewModel.toggleSpotlight(false) },
                onSelectDocument = { doc ->
                    viewModel.attachDocumentToChat(doc.title)
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(NavigationTab.CHAT.ordinal)
                    }
                }
            )
        }

        // Citation Bottom Sheet Modal
        uiState.selectedCitation?.let { citation ->
            CitationBottomSheet(
                citation = citation,
                onDismiss = { viewModel.setSelectedCitation(null) },
                onInspectInVault = { _ ->
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(NavigationTab.VAULT.ordinal)
                    }
                }
            )
        }

        // Chat History Bottom Sheet Modal (Kept for secondary triggers)
        if (uiState.isHistoryOpen) {
            ChatHistoryBottomSheet(
                sessions = uiState.chatSessions,
                activeSessionId = uiState.activeSessionId,
                onSelectSession = { viewModel.selectSession(it) },
                onNewChat = { viewModel.createNewChatSession() },
                onRenameSession = { id, title -> viewModel.renameSession(id, title) },
                onDeleteSession = { viewModel.deleteSession(it) },
                onDismiss = { viewModel.toggleHistory(false) }
            )
        }

        // Cinematic 3-Gear Intro Overlay (Smooth handover to instant dashboard)
        AnimatedVisibility(
            visible = showIntro,
            enter = androidx.compose.animation.EnterTransition.None,
            exit = fadeOut(animationSpec = tween(durationMillis = 180))
        ) {
            CinematicIntroScreen(
                onComplete = { showIntro = false }
            )
        }
    }
    }
}
