package io.github.letsee.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.letsee.models.Category
import io.github.letsee.models.Mock
import io.github.letsee.models.Request
import io.github.letsee.ui.RequestUIModel
import kotlin.math.roundToInt

private const val BUTTON_SIZE_DP = 68
private const val INNER_BUTTON_SIZE_DP = 52
private val OVERLAY_MARGIN = 24.dp
private val BUTTON_TO_CARD_SPACING = 8.dp
private val ACTIVE_CONTAINER_COLOR = Color(0xFF97B34A)
private val INACTIVE_CONTAINER_COLOR = Color(0xFFD2D5DB)

/**
 * Floating LetSee debug button with an optional quick-access mock panel.
 *
 * When [quickAccessRequest] is non-null and its SPECIFIC-category mocks are non-empty the
 * container expands horizontally to show an animated card with scrollable mock pill buttons
 * alongside the main FAB.  Tapping a pill immediately calls [onMockSelected] without opening
 * the full debug panel.
 *
 * The entire widget is draggable.  After each layout pass [onInteractiveBoundsChanged] is
 * invoked with the current bounding box (in root-coordinate dp / UIKit-point units) so that
 * the iOS host window can narrow its touch-passthrough area to exactly this region.
 *
 * @param pendingCount             count of intercepted pending requests (drives the badge)
 * @param quickAccessRequest       the latest pending request whose mocks should be surfaced
 *                                 inline; null when no quick access should be shown
 * @param onClick                  called when the main FAB circle is tapped
 * @param onMockSelected           called when a mock pill is tapped
 * @param onInteractiveBoundsChanged  platform callback: (x, y, width, height) in logical units
 * @param initialOffsetX           initial horizontal drag offset in pixels
 * @param initialOffsetY           initial vertical drag offset in pixels
 */
@Composable
fun LetSeeFloatingButton(
    pendingCount: Int,
    quickAccessRequest: RequestUIModel? = null,
    isMockEnabled: Boolean = false,
    isDarkTheme: Boolean = false,
    onClick: () -> Unit,
    onMockSelected: (Request, Mock) -> Unit = { _, _ -> },
    onInteractiveBoundsChanged: ((x: Float, y: Float, width: Float, height: Float) -> Unit)? = null,
    viewportWidth: Dp? = null,
    modifier: Modifier = Modifier,
    initialOffsetX: Float = 0f,
    initialOffsetY: Float = 0f,
) {
    var offsetX by remember { mutableStateOf(initialOffsetX) }
    var offsetY by remember { mutableStateOf(initialOffsetY) }
    val density = LocalDensity.current

    val specificMocks = remember(quickAccessRequest) {
        quickAccessRequest
            ?.categorisedMocks
            ?.firstOrNull { it.category == Category.SPECIFIC }
            ?.mocks
            ?.sorted()
            ?: emptyList()
    }

    val showQuickAccess = quickAccessRequest != null && specificMocks.isNotEmpty()
    val quickAccessWidth = remember(viewportWidth) {
        viewportWidth?.let {
            (it - (OVERLAY_MARGIN * 2) - BUTTON_SIZE_DP.dp - BUTTON_TO_CARD_SPACING)
                .coerceAtLeast(160.dp)
        }
    }

    val accessibilityLabel = if (pendingCount > 0) {
        "LetSee debug button, $pendingCount pending requests"
    } else {
        "LetSee debug button"
    }

    val shellColor = if (isMockEnabled) ACTIVE_CONTAINER_COLOR else INACTIVE_CONTAINER_COLOR
    val innerButtonContainerColor = if (isDarkTheme) Color.White else Color.Black
    val innerButtonTextColor = if (isDarkTheme) Color.Black else Color.White

    Row(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
            .onGloballyPositioned { coords ->
                if (onInteractiveBoundsChanged != null) {
                    val pos = coords.positionInRoot()
                    val size = coords.size
                    // Convert from composition pixels to logical units (dp / UIKit points).
                    val scale = density.density
                    onInteractiveBoundsChanged(
                        pos.x / scale,
                        pos.y / scale,
                        size.width / scale,
                        size.height / scale,
                    )
                }
            }
            .semantics(mergeDescendants = true) {
                contentDescription = accessibilityLabel
            }
            .testTag("letsee_floating_button"),
        horizontalArrangement = Arrangement.spacedBy(BUTTON_TO_CARD_SPACING),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Main floating shell with pending-count badge.
        Box {
            Surface(
                modifier = Modifier
                    .size(BUTTON_SIZE_DP.dp)
                    .testTag(
                        if (isMockEnabled) {
                            "letsee_floating_shell_active"
                        } else {
                            "letsee_floating_shell_inactive"
                        },
                    ),
                shape = RoundedCornerShape(16.dp),
                color = shellColor,
                shadowElevation = 8.dp,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Surface(
                        modifier = Modifier
                            .size(INNER_BUTTON_SIZE_DP.dp)
                            .clickable(onClick = onClick),
                        shape = RoundedCornerShape(12.dp),
                        color = innerButtonContainerColor,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "See",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = letSeeButtonFontFamily(),
                                color = innerButtonTextColor,
                            )
                        }
                    }
                }
            }

            if (pendingCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(20.dp)
                        .background(
                            color = MaterialTheme.colorScheme.error,
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (pendingCount > 99) "99+" else pendingCount.toString(),
                        color = MaterialTheme.colorScheme.onError,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                }
            }
        }

        // Quick-access mock panel — appears on the RIGHT of the button.
        AnimatedVisibility(
            visible = showQuickAccess,
            enter = fadeIn() + expandHorizontally(expandFrom = Alignment.Start),
            exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.Start),
        ) {
            if (quickAccessRequest != null && specificMocks.isNotEmpty()) {
                QuickAccessCard(
                    request = quickAccessRequest,
                    mocks = specificMocks,
                    isMockEnabled = isMockEnabled,
                    onMockSelected = onMockSelected,
                    modifier = quickAccessWidth?.let { Modifier.width(it) } ?: Modifier,
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Internal composables
// ---------------------------------------------------------------------------

@Composable
private fun QuickAccessCard(
    request: RequestUIModel,
    mocks: List<Mock>,
    isMockEnabled: Boolean,
    onMockSelected: (Request, Mock) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.testTag(
            if (isMockEnabled) {
                "letsee_quick_access_card_active"
            } else {
                "letsee_quick_access_card_inactive"
            },
        ),
    ) {
        Card(
            modifier = Modifier.testTag("letsee_quick_access_card"),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isMockEnabled) ACTIVE_CONTAINER_COLOR else INACTIVE_CONTAINER_COLOR,
            ),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                val quickAccessPath = remember(request.displayName) {
                    truncateFromStartKeepingPathTail(request.displayName.substringBefore("?"))
                }
                Text(
                    text = quickAccessPath,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.testTag("letsee_quick_access_title"),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.testTag("letsee_quick_access_mocks"),
                ) {
                    items(mocks, key = { it.name }) { mock ->
                        MockPill(
                            mock = mock,
                            onClick = { onMockSelected(request.request, mock) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MockPill(mock: Mock, onClick: () -> Unit) {
    val bgColor = when (mock) {
        is Mock.SUCCESS -> MaterialTheme.colorScheme.primary
        is Mock.FAILURE -> MaterialTheme.colorScheme.error
        is Mock.ERROR -> MaterialTheme.colorScheme.error
        is Mock.LIVE -> MaterialTheme.colorScheme.tertiary
        is Mock.CANCEL -> MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = when (mock) {
        is Mock.SUCCESS -> MaterialTheme.colorScheme.onPrimary
        is Mock.FAILURE -> MaterialTheme.colorScheme.onError
        is Mock.ERROR -> MaterialTheme.colorScheme.onError
        is Mock.LIVE -> MaterialTheme.colorScheme.onTertiary
        is Mock.CANCEL -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        modifier = Modifier
            .height(30.dp)
            .testTag("quick_access_mock_${mock.name}")
            .semantics { contentDescription = "Quick select ${mock.displayName}" },
    ) {
        Text(
            text = mock.displayName,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor,
            maxLines = 1,
        )
    }
}

private fun truncateFromStartKeepingPathTail(path: String, maxChars: Int = 72): String {
    if (path.length <= maxChars) return path

    val segments = path.split('/').filter { it.isNotBlank() }
    if (segments.isEmpty()) return "...${path.takeLast(maxChars - 3)}"

    var tail = ""
    for (index in segments.indices.reversed()) {
        val candidate = if (tail.isEmpty()) {
            "/${segments[index]}"
        } else {
            "/${segments[index]}$tail"
        }

        if (candidate.length + 3 > maxChars) break
        tail = candidate
    }

    return if (tail.isNotEmpty()) "...$tail" else "...${path.takeLast(maxChars - 3)}"
}
