package io.github.letsee.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

import io.github.letsee.interfaces.LetSee
import io.github.letsee.ui.components.JsonViewer
import io.github.letsee.ui.navigation.DebugScreen
import io.github.letsee.ui.platform.copyToClipboard
import io.github.letsee.ui.screens.MockPickerScreen
import io.github.letsee.ui.screens.RequestListScreen
import io.github.letsee.ui.screens.ScenariosScreen
import io.github.letsee.ui.screens.SettingsScreen

private val tabScreens = listOf(
    DebugScreen.RequestList,
    DebugScreen.Scenarios,
    DebugScreen.Settings,
)

private val tabLabels = listOf("Requests", "Scenarios", "Settings")
private val tabTestTags = listOf("letsee_tab_requests", "letsee_tab_scenarios", "letsee_tab_settings")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LetSeeDebugPanel(letSee: LetSee, onClose: () -> Unit = {}) {
    val scope = rememberCoroutineScope()
    val components = remember { LetSeeUIFactory.create(letSee, scope) }

    var selectedTab by remember { mutableStateOf(0) }
    val backStack = remember { mutableStateListOf<DebugScreen>() }
    var currentScreen by remember { mutableStateOf<DebugScreen>(DebugScreen.RequestList) }

    fun navigateTo(screen: DebugScreen) {
        backStack.add(currentScreen)
        currentScreen = screen
    }

    fun navigateBack() {
        if (backStack.isNotEmpty()) {
            currentScreen = backStack.removeLast()
        }
    }

    val isOnTabRoot = currentScreen is DebugScreen.RequestList ||
        currentScreen is DebugScreen.Scenarios ||
        currentScreen is DebugScreen.Settings

    LetSeeTheme {
        Scaffold(
            modifier = Modifier.testTag("letsee_debug_panel"),
            topBar = {
                // Single top bar that adapts per screen — no nested Scaffolds.
                Column {
                    when (val screen = currentScreen) {
                        is DebugScreen.RequestList,
                        is DebugScreen.Scenarios,
                        is DebugScreen.Settings -> {
                            TopAppBar(
                                title = {
                                    Text(
                                        text = "LetSee Debug",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                    )
                                },
                                actions = {
                                    IconButton(
                                        onClick = onClose,
                                        modifier = Modifier
                                            .semantics { contentDescription = "Close debug panel" }
                                            .testTag("letsee_close_button"),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = "Close",
                                            tint = MaterialTheme.colorScheme.onSurface,
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                                ),
                            )
                            TabRow(
                                selectedTabIndex = selectedTab,
                                modifier = Modifier.fillMaxWidth(),
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.primary,
                            ) {
                                tabLabels.forEachIndexed { index, label ->
                                    Tab(
                                        selected = selectedTab == index,
                                        onClick = {
                                            selectedTab = index
                                            backStack.clear()
                                            currentScreen = tabScreens[index]
                                        },
                                        text = {
                                            Text(
                                                text = label,
                                                fontWeight = if (selectedTab == index) {
                                                    FontWeight.Bold
                                                } else {
                                                    FontWeight.Normal
                                                },
                                            )
                                        },
                                        modifier = Modifier
                                            .semantics { contentDescription = "$label tab" }
                                            .testTag(tabTestTags[index]),
                                    )
                                }
                            }
                        }

                        is DebugScreen.MockPicker -> {
                            MockPickerTopBar(
                                title = screen.requestUIModel.displayName,
                                fullUrl = screen.requestUIModel.request.uri,
                                onBack = { navigateBack() },
                            )
                        }

                        is DebugScreen.JsonDetail -> {
                            TopAppBar(
                                title = {
                                    Text(
                                        text = "JSON Detail",
                                        style = MaterialTheme.typography.titleMedium,
                                    )
                                },
                                navigationIcon = {
                                    IconButton(
                                        onClick = { navigateBack() },
                                        modifier = Modifier
                                            .semantics { contentDescription = "Navigate back" }
                                            .testTag("letsee_back_button"),
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Back",
                                            tint = MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                                ),
                            )
                        }
                    }
                }
            },
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                when (val screen = currentScreen) {
                    is DebugScreen.RequestList -> {
                        RequestListScreen(
                            requestListStateHolder = components.requestListStateHolder,
                            onRequestSelected = { request ->
                                navigateTo(DebugScreen.MockPicker(request))
                            },
                        )
                    }

                    is DebugScreen.Scenarios -> {
                        ScenariosScreen(
                            scenarioListStateHolder = components.scenarioListStateHolder,
                        )
                    }

                    is DebugScreen.Settings -> {
                        SettingsScreen(
                            settingsStateHolder = components.settingsStateHolder,
                        )
                    }

                    is DebugScreen.MockPicker -> {
                        MockPickerScreen(
                            requestUIModel = screen.requestUIModel,
                            requestListStateHolder = components.requestListStateHolder,
                            onMockSelected = { navigateBack() },
                            onViewJson = { mock ->
                                mock.formatted?.let { json ->
                                    navigateTo(DebugScreen.JsonDetail(json))
                                }
                            },
                        )
                    }

                    is DebugScreen.JsonDetail -> {
                        JsonViewer(
                            json = screen.json,
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("letsee_json_viewer"),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MockPickerTopBar(
    title: String,
    fullUrl: String,
    onBack: () -> Unit,
) {
    val pathOnlyTitle = remember(title) { title.substringBefore("?") }
    var expanded by remember(pathOnlyTitle) { mutableStateOf(false) }
    var hasOverflow by remember(pathOnlyTitle) { mutableStateOf(false) }
    val lineLimit = if (expanded) Int.MAX_VALUE else 3

    Column {
        TopAppBar(
            title = {},
            navigationIcon = {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .testTag("mock_picker_back")
                        .semantics { contentDescription = "Navigate back" },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
            ),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = pathOnlyTitle,
                modifier = Modifier
                    .weight(1f)
                    .testTag("mock_picker_path_label"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = lineLimit,
                overflow = TextOverflow.Ellipsis,
                onTextLayout = { layoutResult ->
                    if (!expanded) {
                        hasOverflow = layoutResult.hasVisualOverflow
                    }
                },
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (hasOverflow || expanded) {
                    IconButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.testTag("mock_picker_more"),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreHoriz,
                            contentDescription = if (expanded) "Show less URL" else "Show more URL",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                IconButton(
                    onClick = { copyToClipboard(fullUrl) },
                    modifier = Modifier.testTag("mock_picker_copy"),
                ) {
                    Icon(
                        imageVector = Icons.Filled.ContentCopy,
                        contentDescription = "Copy URL",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}
