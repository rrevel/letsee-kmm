package io.github.letsee.ui.screens

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import io.github.letsee.ui.LetSeeTheme
import io.github.letsee.ui.testutils.TestFixtures
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class RequestListScreenTest {

    @Test
    fun requestRow_showsFullLongUrl_withoutEllipsisTruncation() = runComposeUiTest {
        val requestModel = TestFixtures.requestUiModel(
            displayName = TestFixtures.LongFullUrl,
        )

        setContent {
            LetSeeTheme(darkTheme = false) {
                RequestRow(
                    requestModel = requestModel,
                    onClick = {},
                )
            }
        }

        onNodeWithTag("request_row_${requestModel.requestId}", useUnmergedTree = true).fetchSemanticsNode()
        onNodeWithText("arrangement-manager/client-api/v2", substring = true).fetchSemanticsNode()
        onNodeWithText("GET").fetchSemanticsNode()
        onNodeWithTag("status_chip_idle", useUnmergedTree = true).fetchSemanticsNode()
    }

    @Test
    fun requestRow_showsBaseUrlPlaceholderPath_whenShortenedPathIsUsed() = runComposeUiTest {
        val requestModel = TestFixtures.requestUiModel(
            displayName = TestFixtures.LongBaseUrlPath,
        )

        setContent {
            LetSeeTheme(darkTheme = false) {
                RequestRow(
                    requestModel = requestModel,
                    onClick = {},
                )
            }
        }

        onNodeWithText("[BaseURL]/arrangement-manager", substring = true).fetchSemanticsNode()
        onNodeWithTag("status_chip_idle", useUnmergedTree = true).fetchSemanticsNode()
    }
}
