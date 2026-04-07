package io.github.letsee.ui.components

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import io.github.letsee.ui.LetSeeTheme
import io.github.letsee.ui.testutils.TestFixtures
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class LetSeeFloatingButtonTest {

    @Test
    fun floatingButton_rendersSeeLabel() = runComposeUiTest {
        setContent {
            LetSeeTheme(darkTheme = false) {
                LetSeeFloatingButton(
                    pendingCount = 0,
                    isMockEnabled = false,
                    isDarkTheme = false,
                    onClick = {},
                )
            }
        }

        onNodeWithText("See").fetchSemanticsNode()
    }

    @Test
    fun floatingButton_usesActiveShellTag_whenMocksEnabled() = runComposeUiTest {
        setContent {
            LetSeeTheme(darkTheme = false) {
                LetSeeFloatingButton(
                    pendingCount = 1,
                    isMockEnabled = true,
                    isDarkTheme = false,
                    onClick = {},
                )
            }
        }

        onNodeWithTag("letsee_floating_shell_active", useUnmergedTree = true).fetchSemanticsNode()
    }

    @Test
    fun floatingButton_usesInactiveShellTag_whenMocksDisabled() = runComposeUiTest {
        setContent {
            LetSeeTheme(darkTheme = false) {
                LetSeeFloatingButton(
                    pendingCount = 1,
                    isMockEnabled = false,
                    isDarkTheme = false,
                    onClick = {},
                )
            }
        }

        onNodeWithTag("letsee_floating_shell_inactive", useUnmergedTree = true).fetchSemanticsNode()
    }

    @Test
    fun floatingButton_quickAccessShowsPathTail_andActiveCardTag() = runComposeUiTest {
        val request = TestFixtures.requestUiModelWithSpecificMocks(
            displayName = "${TestFixtures.LongBaseUrlPath}?withLatestBalances=true&size=10",
        )

        setContent {
            LetSeeTheme(darkTheme = true) {
                LetSeeFloatingButton(
                    pendingCount = 1,
                    quickAccessRequest = request,
                    isMockEnabled = true,
                    isDarkTheme = true,
                    onClick = {},
                )
            }
        }

        onNodeWithTag("letsee_quick_access_card_active", useUnmergedTree = true).fetchSemanticsNode()
        onNodeWithText("productsummary/context/arrangements", substring = true).fetchSemanticsNode()
    }
}
